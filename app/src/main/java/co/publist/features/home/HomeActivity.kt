package co.publist.features.home


import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.Utils.Constants.EDIT_WISH_INTENT
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.databinding.ActivityHomeBinding
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.profile.ProfileActivity
import co.publist.features.wishes.WishesFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.edit_wish_bottom_sheet.*
import javax.inject.Inject


class HomeActivity : BaseActivity<HomeViewModel>() {

    @Inject
    lateinit var viewModel: HomeViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var wishesFragment: WishesFragment
    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityHomeBinding>(
            this,
            R.layout.activity_home
        ).executePendingBindings()
        wishesFragment =
            supportFragmentManager.findFragmentById(R.id.wishesFragment) as WishesFragment
        sheetBehavior = BottomSheetBehavior.from(editWishBottomSheet)

        viewModel.onCreated()
        setObservers()
        setListeners()
    }

    override fun onStart() {
        wishesFragment.viewModel.loadData(PUBLIC)  // To reload data when coming back from another activity
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        super.onStart()
    }
    override fun onDestroy() {
        viewModel.clearGuestSelectedCategories()
        super.onDestroy()
    }

    private fun setObservers() {
        viewModel.userLiveData.observe(this, Observer { user ->
            if (user != null)
                loadProfilePicture(profilePictureImageView, user.profilePictureUrl)
            else
                profilePictureImageView.setImageResource(R.drawable.ic_guest)

        })

        viewModel.profilePictureClickLiveData.observe(this, Observer { isGuest ->
            if (isGuest)
                finish()
            else
                startActivity(Intent(this, ProfileActivity::class.java))
        })

        viewModel.addWishClickLiveData.observe(this, Observer { isGuest ->
            if (isGuest)
                finish()
            else
                startActivity(Intent(this, CreateWishActivity::class.java))
        })

        wishesFragment.viewModel.isFavoriteAdded.observe(this, Observer {isFavoriteAdded ->
            if(isFavoriteAdded)
                Toast.makeText(this,getString(R.string.add_favorite), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this,getString(R.string.remove_favorite), Toast.LENGTH_SHORT).show()

        })

        wishesFragment.viewModel.wishDeletedLiveData.observe(this, Observer {
            wishesFragment.viewModel.loadData(PUBLIC)
            Toast.makeText(this, getString(R.string.delete_wish), Toast.LENGTH_SHORT).show()
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })

        wishesFragment.viewModel.editWishLiveData.observe(this, Observer {wish ->
            val intent = Intent(this,CreateWishActivity::class.java)
            intent.putExtra(EDIT_WISH_INTENT,wish)
            startActivity(intent)

        })

    }

    private fun setListeners() {
        profilePictureImageView.setOnClickListener {
            viewModel.handleEditProfile()
        }

        addWishTextView.setOnClickListener {
            viewModel.handleAddWish()
        }

        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                blurredBgView.visibility = View.VISIBLE
                //Change alpha on sliding
                blurredBgView.alpha = slideOffset
//                window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    blurredBgView.visibility = View.GONE
//                    window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
        })

        blurredBgView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        editWishTextView.setOnClickListener {
            wishesFragment.viewModel.editSelectedWish()
        }

        deleteWishTextView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showDeleteDialog()
        }

        editWishBottomSheet.setOnClickListener {
            //Do nothing when clicking on empty space to avoid triggering blurredBg thus collapsing bottomsheet
        }

        searchView.apply {
            //Quick hack to set threshold of SearchView's AutoCompleteTextView (minimum number of characters to display suggestions)
            val autoCompleteTextViewID =
                resources.getIdentifier("android:id/search_src_text", null, null)
            val autoCompleteTextView = findViewById<AutoCompleteTextView>(autoCompleteTextViewID)
            autoCompleteTextView.threshold = 1

            setOnSearchClickListener {
                profilePictureImageView.visibility = View.GONE
                val params = searchView.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                searchView.layoutParams = params
            }

            setOnCloseListener {
                profilePictureImageView.visibility = View.VISIBLE
                val params = searchView.layoutParams
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                searchView.layoutParams = params
                false
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText?.length!! >= 1) {
                        val queryResultCursor = viewModel.getSuggestedCategoriesFromQuery(newText)
                        searchView.suggestionsAdapter.changeCursor(queryResultCursor)
                    } else
                        searchView.suggestionsAdapter.changeCursor(null)

                    return true
                }

            })

//            autoCompleteTextView.setOnItemClickListener { adapterView,_, position,_ ->
//                    searchView.setQuery(adapterView.getItemAtPosition(position) as String,true)
//            }

           /* val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))*/
            suggestionsAdapter =
                SimpleCursorAdapter(
                this@HomeActivity,
                android.R.layout.simple_list_item_1,
                    null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
                intArrayOf(android.R.id.text1), Activity.DEFAULT_KEYS_SEARCH_LOCAL
            )
        }


    }

    private fun showDeleteDialog() {
        val deleteDialog =
            AlertDialog.Builder(this)
        deleteDialog.setTitle(getString(R.string.delete_dialog_title))
        deleteDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
            wishesFragment.viewModel.deleteSelectedWish()
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        deleteDialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
        }
        deleteDialog.show()
    }

    fun showEditWishDialog(wish: WishAdapterItem) {
        wishesFragment.viewModel.selectedWish = wish
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

}

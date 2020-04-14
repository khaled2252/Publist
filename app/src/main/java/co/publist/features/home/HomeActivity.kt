package co.publist.features.home


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.Utils.Constants.AUTO_COMPLETE_TEXT_VIEW_ID
import co.publist.core.utils.Utils.Constants.EDIT_WISH_INTENT
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.core.utils.Utils.Constants.SEARCH
import co.publist.databinding.ActivityHomeBinding
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.profile.ProfileActivity
import co.publist.features.wishes.WishesFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.snackbar.Snackbar
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
        if (wishesFragment.wishesType == -1)
            wishesFragment.viewModel.loadWishes(PUBLIC)
        else
            wishesFragment.viewModel.loadWishes(wishesFragment.wishesType)  // To reload same data when coming back from another activity , recent apps , lock screen etc..

        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        super.onStart()
    }

    override fun onDestroy() {
        viewModel.clearGuestSelectedCategories()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!searchView.isIconified || searchView.query.isNotEmpty()) {
            searchView.setQuery("", false)
            searchView.isIconified = true
        } else
            super.onBackPressed()
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

        wishesFragment.viewModel.isFavoriteAdded.observe(this, Observer { isFavoriteAdded ->
            if (isFavoriteAdded)
                Snackbar.make(
                    homeActivityContainer,
                    getString(R.string.add_favorite),
                    Snackbar.LENGTH_SHORT
                ).show()
            else
                Snackbar.make(
                    homeActivityContainer,
                    getString(R.string.remove_favorite),
                    Snackbar.LENGTH_SHORT
                ).show()
        })

        wishesFragment.viewModel.wishDeletedLiveData.observe(this, Observer {
            wishesFragment.viewModel.loadWishes(PUBLIC)
            Toast.makeText(this, getString(R.string.delete_wish), Toast.LENGTH_SHORT).show()
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })

        wishesFragment.viewModel.editWishLiveData.observe(this, Observer { wish ->
            val intent = Intent(this, CreateWishActivity::class.java)
            intent.putExtra(EDIT_WISH_INTENT, wish)
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
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    blurredBgView.visibility = View.GONE
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
            //Getting reference of AutoCompleteTextView inside the SearchView
            val autoCompleteTextViewID =
                resources.getIdentifier(AUTO_COMPLETE_TEXT_VIEW_ID, null, null)
            val autoCompleteTextView = findViewById<AutoCompleteTextView>(autoCompleteTextViewID)
            autoCompleteTextView.threshold = 1 //Start suggestions after typing 1 char

            autoCompleteTextView.setOnItemClickListener { adapterView, _, position, _ ->
                //User selected a category from AutoComplete , Make a query for wishes in that category
                val selectedCategory = adapterView.getItemAtPosition(position) as String
                searchView.setQuery(selectedCategory, true)

                //Ui Changes when a category is selected
                autoCompleteTextView.visibility = View.INVISIBLE
                searchView.setQuery("", false)
                //Create categoryChip
                val categoryChip = Chip(this@HomeActivity, null)
                val chipDrawable = ChipDrawable.createFromAttributes(
                    this@HomeActivity,
                    null,
                    0,
                    R.style.CategoryChip
                )
                categoryChip.setChipDrawable(chipDrawable)
                categoryChip.isCheckedIconVisible = false
                categoryChip.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)
                categoryChip.text = selectedCategory
                categoryChip.setOnCloseIconClickListener {
                    searchCategoryChipGroup.removeView(it)
                    autoCompleteTextView.visibility = View.VISIBLE
                    searchView.isIconified = true
                }

                searchCategoryChipGroup.addView(categoryChip)
            }

            setOnSearchClickListener {
                toggleSearchViewUi(true)
            }

            setOnCloseListener {
                if (wishesFragment.wishesType == SEARCH) //Ensure that it was coming from a search (doesn't reload public if user didn't make a query)
                    wishesFragment.viewModel.loadWishes(PUBLIC)
                if (searchCategoryChipGroup.childCount > 0)// Remove category chip (was selected from autocomplete)
                {
                    searchCategoryChipGroup.removeAllViews()
                    autoCompleteTextView.visibility = View.VISIBLE
                }

                toggleSearchViewUi(false)
                false
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    wishesFragment.viewModel.searchQuery = query!!
                    wishesFragment.viewModel.loadWishes(SEARCH)
                    this@HomeActivity.hideKeyboard()
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText?.length!! >= 1) {
                        val queryResultArray = viewModel.getSuggestedCategoriesFromQuery(newText)
                        autoCompleteTextView.setAdapter(
                            ArrayAdapter(
                                this@HomeActivity,
                                R.layout.item_search_suggestion,
                                R.id.suggestion_text_view,
                                queryResultArray
                            )
                        )
                    } else
                        autoCompleteTextView.setAdapter(null)
                    return true
                }

            })

        }

    }

    private fun toggleSearchViewUi(isSearching: Boolean) {
        val toolBarParams = toolBar.layoutParams as AppBarLayout.LayoutParams
        val params = searchView.layoutParams
        if (isSearching) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            searchView.layoutParams = params

            toolBarParams.scrollFlags = 0
            profilePictureImageView.visibility = View.GONE
            addWishTextView.visibility = View.GONE
        } else {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            searchView.layoutParams = params

            toolBarParams.scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
            profilePictureImageView.visibility = View.VISIBLE
            addWishTextView.visibility = View.VISIBLE
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

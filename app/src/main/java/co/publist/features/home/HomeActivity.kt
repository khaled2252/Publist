package co.publist.features.home


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.databinding.ActivityHomeBinding
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.login.LoginActivity
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
            else {
                profilePictureImageView.setImageResource(R.drawable.ic_guest)
                logoutTextView.visibility = View.GONE
            }
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
            Toast.makeText(this, getString(R.string.delete_wish), Toast.LENGTH_SHORT).show()
        })
    }

    private fun setListeners() {
        logoutTextView.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setMessage(getString(R.string.logout_dialog_title))
            builder.setNeutralButton("YES") { _, _ ->
                viewModel.handleLogout()
                finish()
                startActivity(Intent(this, LoginActivity::class.java))
            }
            builder.setPositiveButton("No") { _, _ ->
            }
            builder.create().show()
        }

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
                window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    blurredBgView.visibility = View.GONE
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
        })

        blurredBgView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        editWishTextView.setOnClickListener {

        }

        deleteWishTextView.setOnClickListener {
            showDeleteDialog()
        }

    }

    private fun showDeleteDialog() {
        val deleteDialog =
            AlertDialog.Builder(this)
        deleteDialog.setTitle("Are you sure you want to delete this wish?")
        deleteDialog.setPositiveButton("YES") { _, _ ->
            wishesFragment.viewModel.deleteSelectedWish()
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        deleteDialog.setNegativeButton("No") { _, _ ->
        }
        deleteDialog.show()
    }

    fun showEditWishDialog(wish: Wish) {
        wishesFragment.viewModel.selectedWish = wish
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

}

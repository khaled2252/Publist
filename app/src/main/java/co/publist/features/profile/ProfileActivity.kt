package co.publist.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import co.publist.R
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.Utils.Constants.COMING_FROM_PROFILE_INTENT
import co.publist.features.editprofile.EditProfileActivity
import co.publist.features.login.LoginActivity
import co.publist.features.mylists.MyListsFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.back_button_layout.*
import kotlinx.android.synthetic.main.edit_wish_bottom_sheet.*
import javax.inject.Inject


class ProfileActivity : BaseActivity<ProfileViewModel>() {

    @Inject
    lateinit var viewModel: ProfileViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    private lateinit var profilePagerAdapter: ProfilePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        onCreated()
        setObservers()
        setListeners()
    }

    private fun onCreated() {
        setPagerAdapter()
        sheetBehavior = BottomSheetBehavior.from(editWishBottomSheet)
    }

    private fun setPagerAdapter() {
        profilePagerAdapter = ProfilePagerAdapter(supportFragmentManager, lifecycle)
        profile_pager!!.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        profile_pager!!.adapter = profilePagerAdapter
        profile_pager!!.isUserInputEnabled = true // for swiping
        profile_pager!!.offscreenPageLimit =
            2 // to pre-load pages? to avoid loading pages when swiped (slow animation)
        TabLayoutMediator(tab_layout, profile_pager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = getText(R.string.my_favorites)
                    }
                    1 -> {
                        tab.text = getText(R.string.lists)
                    }
                }
            }).attach()
    }


    private fun setObservers() {
        viewModel.userLiveData.observe(this, Observer { user ->
            nameTextView.text = user.name
            loadProfilePicture(profilePictureImageView, user.profilePictureUrl)
        })

        viewModel.logoutLiveData.observe(this, Observer {
            finishAffinity() //To clear all past activities
            startActivity(Intent(this, LoginActivity::class.java))
        })
    }

    private fun setListeners() {
        backArrowImageViewLayout.setOnClickListener {
            onBackPressed()
        }

        editProfileImageViewLayout.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra(COMING_FROM_PROFILE_INTENT, true)
            startActivity(intent)
        }

        logoutImageViewLayout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.logout_dialog_title))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.handleLogout()
            }
            builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            }
            builder.create().show()
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
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val myListsWishesFragmentViewModel =
                (profilePagerAdapter.getFragmentReference(1) as MyListsFragment).wishesFragment.viewModel
            myListsWishesFragmentViewModel.editSelectedWish()
        }

        deleteWishTextView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val deleteDialog =
            AlertDialog.Builder(this)
        deleteDialog.setTitle(getString(R.string.delete_dialog_title))
        deleteDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
            val myListsWishesFragmentViewModel =
                (profilePagerAdapter.getFragmentReference(1) as MyListsFragment).wishesFragment.viewModel
            myListsWishesFragmentViewModel.deleteSelectedWish()
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        deleteDialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
        }
        deleteDialog.show()
    }

    fun showEditWishDialog(wish: WishAdapterItem) {
        val myListsWishesFragmentViewModel =
            (profilePagerAdapter.getFragmentReference(1) as MyListsFragment).wishesFragment.viewModel
        myListsWishesFragmentViewModel.selectedWish = wish
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}

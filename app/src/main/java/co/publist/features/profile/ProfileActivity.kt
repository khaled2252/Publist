package co.publist.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import co.publist.R
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.editprofile.EditProfileActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_profile.*
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
        val profilePagerAdapter = ProfilePagerAdapter(supportFragmentManager, lifecycle)
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

        viewModel.wishDeletedLiveData.observe(this, Observer {
            Toast.makeText(this, getString(R.string.delete_wish), Toast.LENGTH_SHORT).show()
        })

        viewModel.editWishLiveData.observe(this, Observer {wish ->
            val intent = Intent(this, CreateWishActivity::class.java)
            intent.putExtra("editedWish",wish)
            startActivity(intent)
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })
    }

    private fun setListeners() {
        backArrowImageViewLayout.setOnClickListener {
            onBackPressed()
        }

        editProfileImageViewLayout.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("isComingFromProfile", true)
            startActivity(intent)
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
            viewModel.editSelectedWish()
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
            viewModel.deleteSelectedWish()
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        deleteDialog.setNegativeButton("No") { _, _ ->
        }
        deleteDialog.show()
    }

    fun showEditWishDialog(wish: Wish) {
        viewModel.selectedWish = wish
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}

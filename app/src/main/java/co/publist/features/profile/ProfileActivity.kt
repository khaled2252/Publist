package co.publist.features.profile

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.features.editprofile.EditProfileActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_profile.*
import javax.inject.Inject


class ProfileActivity : BaseActivity<ProfileViewModel>() {

    @Inject
    lateinit var viewModel: ProfileViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        onCreated()
        setObservers()
        setListeners()
    }

    private fun onCreated() {
        setPagerAdapter()
    }

    private fun setPagerAdapter() {
        val profilePagerAdapter = ProfilePagerAdapter(supportFragmentManager, lifecycle)
        profile_pager!!.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        profile_pager!!.adapter = profilePagerAdapter
        profile_pager!!.isUserInputEnabled = true // for swiping
        profile_pager!!.offscreenPageLimit = 2 // to pre-load pages? to avoid loading pages when swiped (slow animation)
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
    }

    private fun setListeners() {
        editProfileImageView.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("isComingFromProfile",true)
            startActivity(intent)
        }
    }

}

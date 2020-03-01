package co.publist.features.home


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.databinding.ActivityHomeBinding
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.login.LoginActivity
import co.publist.features.profile.ProfileActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar.*
import javax.inject.Inject


class HomeActivity : BaseActivity<HomeViewModel>() {

    @Inject
    lateinit var viewModel: HomeViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityHomeBinding>(
            this,
            R.layout.activity_home
        ).executePendingBindings()
        viewModel.onCreated()
        setObservers()
        setListeners()
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
    }


}

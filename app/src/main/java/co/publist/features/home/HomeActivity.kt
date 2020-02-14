package co.publist.features.home


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils
import co.publist.databinding.ActivityHomeBinding
import co.publist.features.editprofile.EditProfileActivity
import co.publist.features.login.LoginActivity
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
                Utils.loadProfilePicture(profilePictureImageView, user.profilePictureUrl)
            else {
                profilePictureImageView.setImageResource(R.drawable.ic_user_2x)
                logoutTextView.visibility = View.GONE
            }
        })

        viewModel.isGuest.observe(this, Observer { isGuest ->
            if (isGuest)
                finish()
            else
                startActivity(Intent(this, EditProfileActivity::class.java))
        })
    }

    private fun setListeners() {
        logoutTextView.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setMessage("Are you sure you want to logout?")
            builder.setNeutralButton("YES") { _, _ ->
                viewModel.handleLogout()
                Toast.makeText(applicationContext, "Logged out successfully!", Toast.LENGTH_SHORT)
                    .show()
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
    }


}

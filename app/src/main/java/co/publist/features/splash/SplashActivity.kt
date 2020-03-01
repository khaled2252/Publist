package co.publist.features.splash


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils
import co.publist.features.editprofile.EditProfileActivity
import co.publist.features.home.HomeActivity
import co.publist.features.login.LoginActivity
import javax.inject.Inject


class SplashActivity : BaseActivity<SplashViewModel>() {

    @Inject
    lateinit var viewModel: SplashViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel.onCreated()
        viewModel.userLoggedIn.observe(this, Observer { pair ->
            Handler().postDelayed({
                val isNewUser = pair.first
                val isMyCategoriesEmpty = pair.second
                if (!isNewUser) {
                    if (isMyCategoriesEmpty)
                        navigateEditProfile()
                    else
                        navigateToHome()
                } else
                    navigateToLogin()
            }, Utils.Constants.SPLASH_DELAY)
        })
    }

    private fun navigateEditProfile() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, EditProfileActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToHome() {
        Toast.makeText(this, R.string.welcome_back, Toast.LENGTH_SHORT).show()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

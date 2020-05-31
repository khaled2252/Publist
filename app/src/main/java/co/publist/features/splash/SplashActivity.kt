package co.publist.features.splash


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils
import co.publist.features.editprofile.EditProfileActivity
import co.publist.features.home.HomeActivity
import co.publist.features.login.LoginActivity
import co.publist.features.onboarding.OnBoardingActivity
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        viewModel.onCreated()
        viewModel.userLoggedIn.observe(this, Observer { data ->
            Handler().postDelayed({
                val isNewUser = data.first
                val isMyCategoriesEmpty = data.second
                val isOnBoardingFinished = data.third
                if (!isNewUser) {
                    if (isMyCategoriesEmpty)
                        navigateEditProfile()
                    else
                        navigateToHome()
                } else if (!isOnBoardingFinished)
                    navigateToOnBoarding()
                else
                    navigateToLogin()
            }, Utils.Constants.SPLASH_DELAY)
        })
    }

    private fun navigateToOnBoarding() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()
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

package com.publist.features.splash


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.publist.R
import com.publist.core.platform.BaseActivity
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.Utils
import com.publist.features.editprofile.EditProfileActivity
import com.publist.features.home.HomeActivity
import com.publist.features.login.LoginActivity
import com.publist.features.onboarding.OnBoardingActivity
import com.publist.features.terms.TermsActivity
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
        viewModel.userLoggedIn.observe(this, Observer { userStatus ->
            Handler(Looper.myLooper()!!).postDelayed({
                if (userStatus.isTermsAndConditionsAccepted) {
                    if (!userStatus.isNew) {
                        if (userStatus.isMyCategoriesEmpty)
                            navigateEditProfile()
                        else
                            navigateToHome()
                    } else if (!userStatus.isOnBoardingFinished)
                        navigateToOnBoarding()
                    else
                        navigateToLogin()
                } else
                    navigateToTermsAndConditions()
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

    private fun navigateToTermsAndConditions() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, TermsActivity::class.java))
        finish()
    }
}

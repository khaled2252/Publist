package co.publist.features.splash


import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
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
        viewModel.onScreenCreate()
        viewModel.loaded.observe(this, Observer {
            if (it)
                navigateToLogin()
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent)
        finish()
    }
}

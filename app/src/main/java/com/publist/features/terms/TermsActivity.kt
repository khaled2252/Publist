package com.publist.features.terms


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.publist.R
import com.publist.core.platform.BaseActivity
import com.publist.core.platform.ViewModelFactory
import com.publist.features.splash.SplashActivity
import kotlinx.android.synthetic.main.activity_terms.*
import javax.inject.Inject


class TermsActivity : BaseActivity<TermsViewModel>() {

    @Inject
    lateinit var viewModel: TermsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        acceptButton.setOnClickListener {
            viewModel.acceptTermsAndConditions()
        }
        viewModel.acceptedTermsAndConditions.observe(this, Observer {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        })
    }
}

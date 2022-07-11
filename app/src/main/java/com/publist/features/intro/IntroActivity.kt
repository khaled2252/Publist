package com.publist.features.intro


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.publist.R
import com.publist.core.platform.BaseActivity
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.Utils.Constants.CATEGORY_IDS
import com.publist.core.utils.Utils.Constants.CHOOSE_CATEGORIES
import com.publist.core.utils.Utils.Constants.DEVICE_ID
import com.publist.core.utils.Utils.Constants.GUEST_CHOOSING_CATEGORY
import com.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import com.publist.core.utils.Utils.Constants.SKIP_CHOOSING_CATEGORIES
import com.publist.features.categories.CategoriesFragment
import com.publist.features.home.HomeActivity
import kotlinx.android.synthetic.main.activity_intro.*
import javax.inject.Inject


class IntroActivity : BaseActivity<IntroViewModel>() {

    @Inject
    lateinit var viewModel: IntroViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var categoriesFragment: CategoriesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        onCreated()
        setObservers()
        setListeners()
    }

    private fun onCreated() {
        categoriesFragment =
            supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        categoriesFragment.viewModel.getCategories()
    }

    private fun setObservers() {
        categoriesFragment.viewModel.actionButtonLiveData.observe(this, Observer { viable ->
            if (!viable) {
                Toast.makeText(
                    this,
                    resources.getQuantityText(
                        R.plurals.minimum_categories,
                        MINIMUM_SELECTED_CATEGORIES
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        categoriesFragment.viewModel.saveCategoriesLiveData.observe(this, Observer { isUser ->
            if (isUser)
                mFirebaseAnalytics.logEvent(CHOOSE_CATEGORIES, null)
            else {
                @SuppressLint("HardwareIds")
                val androidID = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                mFirebaseAnalytics.logEvent(
                    GUEST_CHOOSING_CATEGORY,
                    bundleOf(
                        Pair(DEVICE_ID, androidID),
                        Pair(CATEGORY_IDS, categoriesFragment.viewModel.getSelectedCategoriesIds())
                    )
                )

            }

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        })
    }

    private fun setListeners() {
        buttonFindWishes.setOnClickListener {
            categoriesFragment.viewModel.handleActionButton(false)
        }

        loginButton.setOnClickListener {
            finish()
        }

        skipTextView.setOnClickListener {
            mFirebaseAnalytics.logEvent(SKIP_CHOOSING_CATEGORIES, null)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}

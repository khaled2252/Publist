package co.publist.features.intro


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.CATEGORY_IDS
import co.publist.core.utils.Utils.Constants.CHOOSE_CATEGORIES
import co.publist.core.utils.Utils.Constants.DEVICE_ID
import co.publist.core.utils.Utils.Constants.GUEST_CHOOSING_CATEGORY
import co.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import co.publist.core.utils.Utils.Constants.SKIP_CHOOSING_CATEGORIES
import co.publist.features.categories.CategoriesFragment
import co.publist.features.home.HomeActivity
import com.google.firebase.analytics.FirebaseAnalytics
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

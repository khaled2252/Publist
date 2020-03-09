package co.publist.features.intro


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import co.publist.features.categories.CategoriesFragment
import co.publist.features.home.HomeActivity
import kotlinx.android.synthetic.main.activity_intro.*
import javax.inject.Inject


class IntroActivity : BaseActivity<IntroViewModel>() {

    @Inject
    lateinit var viewModel: IntroViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

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
        categoriesFragment.viewModel.getSelectedCategories()
    }

    private fun setObservers(){
        categoriesFragment.viewModel.actionButtonLiveData.observe(this, Observer { viable ->
            if (!viable)
            {
                Toast.makeText(
                    this,
                    getString(R.string.minimum_categories).format(MINIMUM_SELECTED_CATEGORIES),
                    Toast.LENGTH_SHORT
                ).show()
            }
         })

        categoriesFragment.viewModel.saveCategoriesLiveData.observe(this, Observer {
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        })
    }

    private fun setListeners(){
        buttonFindWishes.setOnClickListener {
            categoriesFragment.viewModel.handleActionButton(false)
        }

        loginButton.setOnClickListener {
            finish()
        }

        skipTextView.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }
    }
}

package co.publist.features.intro


import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.FIND_ACTION
import co.publist.features.categories.CategoriesFragment
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
        categoriesFragment= supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment

        setObservers()
        setListeners()
    }

    private fun setObservers(){
        categoriesFragment.viewModel.actionButtonLiveData.observe(this, Observer { viable ->
            if (viable)
            //todo navigate to home
            else
                Toast.makeText(
                    this,
                    getString(R.string.minimum_categories),
                    Toast.LENGTH_SHORT
                ).show()
        })
    }

    private fun setListeners(){
        buttonFindWishes.setOnClickListener {
            categoriesFragment.viewModel.handleActionButton(FIND_ACTION)
        }

        loginButton.setOnClickListener {
            finish()
        }

        skipTextView.setOnClickListener {
            //todo navigate to home
        }
    }
}

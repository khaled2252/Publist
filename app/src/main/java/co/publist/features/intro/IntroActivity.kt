package co.publist.features.intro


import android.os.Bundle
import android.widget.Toast
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val categoriesFragment= supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        buttonFindWishes.setOnClickListener {
            if(categoriesFragment.viewModel.selectedCategories.size<1)
                Toast.makeText(this,"You must select at least 1 category",
                    Toast.LENGTH_SHORT).show()
            else {
                //todo navigate to home
            }
        }

        loginButton.setOnClickListener {
            finish()
        }

        skipTextView.setOnClickListener {
            //todo navigate to home
        }
    }

}

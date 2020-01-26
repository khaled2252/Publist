package co.publist.features.intro


import android.os.Bundle
import androidx.fragment.app.Fragment
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.features.categories.CategoriesFragment
import co.publist.features.categories.CategoriesViewModel
import javax.inject.Inject


class IntroActivity : BaseActivity<CategoriesViewModel>() {

    @Inject
    lateinit var viewModel: CategoriesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

    }

    private fun addFragment(containerViewId: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().add(containerViewId, fragment, tag).commit()
    }
}

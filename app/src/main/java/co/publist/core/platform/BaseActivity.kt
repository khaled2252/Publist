package co.publist.core.platform


import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.utils.Extensions
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


abstract class BaseActivity<MBaseViewModel : BaseViewModel>
    : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var viewModel: MBaseViewModel

    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getBaseViewModel()
        viewModelFactory = getBaseViewModelFactory()

        viewModel.loading.observe(this, Observer {
            if (it) showLoading()
            else hideLoading()
        })

        viewModel.error.observe(this, Observer {
            hideLoading()
//            showError(it)
        })
    }

//    open fun showError(error: Error) {
//        val errorMessage: String =
//            if (error.error != null && error.error.isNotEmpty()) error.error
//            else if (error.errorRes != null) getString(error.errorRes)
//            else getString(R.string.generic_error)
//        showAlert(this, errorMessage, R.color.colorAccent)
//    }
//
//    open fun showSuccess(message: String) {
//        showAlert(this, message, R.color.success_message_color)
//    }

    open fun hideLoading() {
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular)
        if (progressBar != null)
            progressBar.visibility = View.GONE
    }

    open fun showLoading() {
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular)
        if (progressBar != null)
            progressBar.visibility = View.VISIBLE
    }

//    override fun attachBaseContext(newBase: Context) {
//        val language = Locale.getDefault().language
//        LocaleHelper.setLocale(newBase, language)
//        super.attachBaseContext(LocaleHelper.onAttach(newBase, language))
//    }

    abstract fun getBaseViewModel(): MBaseViewModel

    abstract fun getBaseViewModelFactory(): ViewModelFactory

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    //Hide keyboard when clicking anywhere (while keyboard is open)
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

    fun hideKeyboard() {
        if (window.currentFocus != null)
            Extensions.hideSoftKeyboard(this, window.currentFocus!!.windowToken)
    }

}

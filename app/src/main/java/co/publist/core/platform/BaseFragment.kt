package co.publist.core.platform

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.di.helper.Injectable
import co.publist.core.utils.Extensions.hideSoftKeyboard

abstract class BaseFragment<MBaseViewModel : BaseViewModel>
    : Fragment(), Injectable {

    private lateinit var vm: MBaseViewModel
    private lateinit var vmf: ViewModelFactory

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm = getBaseViewModel()
        vmf = getBaseViewModelFactory()
        vm.loading.observe(this.viewLifecycleOwner, Observer {
            if (it) showLoading()
            else hideLoading()
        })

        vm.error.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
//            showError(it)
        })
    }

//    open fun showError(error: Error) {
//        val errorMessage: String =
//            if (error.error != null && error.error.isNotEmpty()) error.error
//            else if (error.errorRes != null) getString(error.errorRes)
//            else getString(R.string.generic_error)
//        showAlert(activity!!, errorMessage, R.color.colorAccent)
//    }

//    open fun showSuccess(message: String) {
//        showAlert(activity!!, message, R.color.success_message_color)
//    }

    abstract fun getBaseViewModel(): MBaseViewModel

    abstract fun getBaseViewModelFactory(): ViewModelFactory

    open fun hideLoading() {
        if (activity != null) {
            val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
            if (progressBar != null)
                progressBar.visibility = View.GONE
        }
    }

    open fun showLoading() {
        if (activity != null) {
            val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
            if (progressBar != null)
                progressBar.visibility = View.VISIBLE
        }
    }

    fun hideKeyboard() {
        if (activity!!.window.currentFocus != null)
            hideSoftKeyboard(context!!, activity!!.window.currentFocus!!.windowToken)
    }
}
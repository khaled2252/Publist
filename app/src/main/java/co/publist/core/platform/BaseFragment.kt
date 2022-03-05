package com.publist.core.platform

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.publist.R
import com.publist.core.di.helper.Injectable
import com.publist.core.utils.Utils.hideSoftKeyboard

abstract class BaseFragment<MBaseViewModel : BaseViewModel>
    : Fragment(), Injectable {

    private lateinit var viewModel: MBaseViewModel
    private lateinit var vmf: ViewModelFactory

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getBaseViewModel()
        vmf = getBaseViewModelFactory()
        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it) showLoading()
            else hideLoading()
        })

//        vm.error.observe(this.viewLifecycleOwner, Observer {
//            hideLoading()
//            showError(it)
//        })

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


        viewModel.noInternetConnection.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                this.requireView(),
                getString(R.string.check_your_internet_connection),
                Snackbar.LENGTH_LONG
            ).show()

            val refreshLayout = view?.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            if (refreshLayout != null)
                refreshLayout.isRefreshing = false

            val noInternetPlaceHolder =
                view?.findViewById<LinearLayout>(R.id.noInternetConnectionPlaceholder)
            if (noInternetPlaceHolder != null)
                noInternetPlaceHolder.visibility = View.VISIBLE
        })
    }

    abstract fun getBaseViewModel(): MBaseViewModel

    abstract fun getBaseViewModelFactory(): ViewModelFactory

    open fun hideLoading() {
        if (activity != null) {
            val progressBar = view?.findViewById<ProgressBar>(R.id.progress_circular)
            if (progressBar != null)
                progressBar.visibility = View.GONE
        }
    }

    open fun showLoading() {
        if (activity != null) {
            val progressBar = view?.findViewById<ProgressBar>(R.id.progress_circular)
            if (progressBar != null)
                progressBar.visibility = View.VISIBLE
        }
    }

    fun hideKeyboard() {
        if (activity?.window?.currentFocus != null)
            hideSoftKeyboard(context!!, activity!!.window.currentFocus!!.windowToken)
    }
}
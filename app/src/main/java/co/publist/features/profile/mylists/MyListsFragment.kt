package co.publist.features.profile.mylists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import javax.inject.Inject

class MyListsFragment : BaseFragment<MyListsViewModel>() {
    @Inject
    lateinit var viewModelMy: MyListsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModelMy

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setObservers()
    }

    private fun setObservers() {

    }
}
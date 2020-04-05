package co.publist.features.mylists

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.wishes.WishesFragment
import kotlinx.android.synthetic.main.fragment_my_lists.*
import javax.inject.Inject

class MyListsFragment : BaseFragment<MyListsViewModel>() {
    @Inject
    lateinit var viewModelMy: MyListsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModelMy

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var wishesFragment: WishesFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wishesFragment =
            childFragmentManager.findFragmentById(R.id.wishesFragment) as WishesFragment
        setListeners()
    }

    override fun onStart() {
        wishesFragment.viewModel.loadData(LISTS)
        super.onStart()
    }

    private fun setListeners() {
        addListBtn.setOnClickListener {
            startActivity(Intent(this.context, CreateWishActivity::class.java))
        }
    }
}
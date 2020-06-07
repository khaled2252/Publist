package co.publist.features.myfavorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.FAVORITES
import co.publist.features.home.HomeActivity
import co.publist.features.wishes.WishesFragment
import javax.inject.Inject

class MyFavoritesFragment : BaseFragment<MyFavoritesViewModel>() {
    @Inject
    lateinit var viewModel: MyFavoritesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var wishesFragment: WishesFragment

    object Data {
        var isChanged = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_favorites, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wishesFragment =
            childFragmentManager.findFragmentById(R.id.wishesFragment) as WishesFragment
        wishesFragment.viewModel.loadWishes(FAVORITES)

        wishesFragment.viewModel.dataChangedLiveData.observe(viewLifecycleOwner, Observer {
            Data.isChanged = true
            HomeActivity.Data.isChanged = true
        })
    }

    override fun onStart() {
        if (Data.isChanged) {
            wishesFragment.clearLoadedData()
            wishesFragment.viewModel.loadWishes(FAVORITES)
            Data.isChanged = false
        }
        super.onStart()
    }

}
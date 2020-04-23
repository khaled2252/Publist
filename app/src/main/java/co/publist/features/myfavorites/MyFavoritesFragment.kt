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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wishesFragment =
            childFragmentManager.findFragmentById(R.id.wishesFragment) as WishesFragment
        wishesFragment.viewModel.isFavoriteAdded.observe(
            viewLifecycleOwner,
            Observer { isFavoriteAdded ->
                if (!isFavoriteAdded)
                    wishesFragment.viewModel.loadWishes(FAVORITES)
            })
    }

    override fun onStart() {
        wishesFragment.viewModel.loadWishes(FAVORITES)
        super.onStart()
    }
}
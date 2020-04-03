package co.publist.features.wishes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.SimpleItemAnimator
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.features.home.HomeActivity
import co.publist.features.profile.ProfileActivity
import co.publist.features.wishdetails.WishDetailsActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_wishes.*
import javax.inject.Inject


class WishesFragment : BaseFragment<WishesViewModel>() {

    @Inject
    lateinit var viewModel: WishesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private var wishesType = -1
    private lateinit var wishesQuery : Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.isRefreshing = true
        setObservers()
    }

    private fun setObservers() {
        viewModel.wishesType.observe(viewLifecycleOwner, Observer { type ->
            wishesType = type
            setListeners()

        })

        viewModel.wishesQueryLiveData.observe(viewLifecycleOwner, Observer { pair ->
            wishesQuery = pair.first
            wishesType = pair.second
            viewModel.getLikedItems()
        })

        viewModel.likedItemsLiveData.observe(viewLifecycleOwner, Observer {likedItemsList ->
            setAdapter(wishesQuery, wishesType ,likedItemsList)
        })

        viewModel.wishesListLiveData.observe(viewLifecycleOwner, Observer { list ->
            setAdapter(list)
            refreshLayout.isRefreshing = false
        })
    }

    private fun setListeners() {
        if (wishesType == PUBLIC) {
            refreshLayout.isEnabled = true
            refreshLayout.setOnRefreshListener {
                viewModel.loadData(PUBLIC)
            }
        } else
            refreshLayout.isEnabled = false
    }

    private fun setAdapter(query: Query, type: Int , likedItemsList : ArrayList<String>) {
        val options: FirestoreRecyclerOptions<Wish> =
            FirestoreRecyclerOptions.Builder<Wish>()
                .setQuery(query, Wish::class.java)
                .build()

        val adapter =
            WishesFirestoreAdapter(options, type,likedItemsList,userId = viewModel.userId!!, displayPlaceHolder = { displayPlaceHolder ->
                val view =
                    this.parentFragment?.view?.findViewById<LinearLayout>(R.id.placeHolderView)
                if (displayPlaceHolder)
                    view?.visibility = View.VISIBLE
                else
                    view?.visibility = View.GONE
            }, detailsListener = { wish ->
                (activity as ProfileActivity).showEditWishDialog(wish)
            }, unFavoriteListener = { wish ->
                viewModel.modifyFavorite(wish, false)
            },completeListener = {itemId , wish , isDone ->
                viewModel.completeItem(itemId,Mapper.mapToWishAdapterItem(wish),isDone)
            },likeListener = {itemId, wish, isLiked ->
                viewModel.likeItem(itemId,Mapper.mapToWishAdapterItem(wish),isLiked)
            },seenCountListener = {wishId ->
                viewModel.incrementSeenCount(wishId)
            })

        adapter.startListening()
        wishesRecyclerView.adapter = adapter
    }

    private fun setAdapter(list: ArrayList<WishAdapterItem>) {
        refreshLayout.isRefreshing = false
        if (list.isNotEmpty()) {
            val wishesAdapter = WishesAdapter(list, wishesType = wishesType,userId = viewModel.userId!!, detailsListener = { wish ->
                if (activity is HomeActivity)
                    (activity as HomeActivity).showEditWishDialog(Mapper.mapToWish(wish))
                else
                    (activity as WishDetailsActivity).showEditWishDialog()
            }, favoriteListener = { wish, isFavoriting ->
                viewModel.modifyFavorite(Mapper.mapToWish(wish), isFavoriting)
            },completeListener = {itemId , wish , isDone ->
                viewModel.completeItem(itemId,wish,isDone)
            },likeListener = {itemId, wish, isLiked ->
                viewModel.likeItem(itemId,wish,isLiked)
            },seenCountListener = {wishId ->
                viewModel.incrementSeenCount(wishId)
            }
            )
            (wishesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false //To disable animation when favoriting
            wishesRecyclerView.adapter = wishesAdapter
        } else {
            //todo display placeholder
        }

    }

}

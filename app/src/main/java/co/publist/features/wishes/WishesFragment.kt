package co.publist.features.wishes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.SimpleItemAnimator
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.core.utils.Utils.Constants.SEARCH
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

    var wishesType = -1

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
        viewModel.preLoadedWishesType.observe(viewLifecycleOwner, Observer { type ->
            wishesType = type
            setListeners()
        })

        viewModel.wishDataPairLiveData.observe(viewLifecycleOwner, Observer { dataPair ->
            val wishesQuery = dataPair.first
            val itemsAttributesPair = dataPair.second
            val doneItemsList = itemsAttributesPair.first
            val likedItemsList = itemsAttributesPair.second
            setAdapter(
                wishesQuery,
                wishesType,
                doneItemsList,
                likedItemsList
            )
        })

        viewModel.wishesListLiveData.observe(viewLifecycleOwner, Observer { list ->
            setAdapter(list)
            refreshLayout.isRefreshing = false
        })
    }

    private fun setListeners() {
        if (wishesType == PUBLIC || wishesType == SEARCH) {
            noResultsPlaceholder.visibility = View.GONE
            wishesFragmentContainer.setBackgroundColor(
                ContextCompat.getColor(
                    this.context!!,
                    R.color.platinum
                )
            )
            refreshLayout.isEnabled = true
            refreshLayout.isRefreshing = true
            refreshLayout.setOnRefreshListener {
                viewModel.loadWishes(wishesType)
            }
        } else
            refreshLayout.isEnabled = false
    }

    private fun setAdapter(
        query: Query,
        type: Int,
        doneItemsList: ArrayList<String>,
        likedItemsList: ArrayList<String>
    ) {
        val options: FirestoreRecyclerOptions<WishAdapterItem> =
            FirestoreRecyclerOptions.Builder<WishAdapterItem>()
                .setQuery(query, WishAdapterItem::class.java)
                .build()

        val adapter =
            WishesFirestoreAdapter(
                options,
                type,
                doneItemsList,
                likedItemsList,
                user = viewModel.user!!,
                displayPlaceHolder = { displayPlaceHolder ->
                    val view =
                        this.parentFragment?.view?.findViewById<LinearLayout>(R.id.placeHolderView)
                    if (displayPlaceHolder)
                        view?.visibility = View.VISIBLE
                    else
                        view?.visibility = View.GONE
                },
                detailsListener = { wish ->
                    (activity as ProfileActivity).showEditWishDialog(wish)
                },
                unFavoriteListener = { wish ->
                    viewModel.modifyFavorite(Mapper.mapToWish(wish), false)
                },
                completeListener = { itemId, wish, isDone ->
                    viewModel.completeItem(itemId, wish, isDone)
                },
                likeListener = { itemId, wish, isLiked ->
                    viewModel.likeItem(itemId, wish, isLiked)
                },
                seenCountListener = { wishId ->
                    viewModel.incrementSeenCount(wishId)
                }, scrollListener = {
//                    wishesRecyclerView.smoothScrollToPosition(0)
                })

        adapter.startListening()
        (wishesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false //To disable animation when changing holder information
        wishesRecyclerView.adapter = adapter
    }

    private fun setAdapter(list: ArrayList<WishAdapterItem>) {
        refreshLayout.isRefreshing = false
        if (list.isNotEmpty()) {
            val wishesAdapter = WishesAdapter(
                list,
                wishesType = wishesType,
                user = viewModel.user,
                detailsListener = { wish ->
                    if (activity is HomeActivity)
                        (activity as HomeActivity).showEditWishDialog(wish)
                    else
                        (activity as WishDetailsActivity).showEditWishDialog()
                },
                favoriteListener = { wish, isFavoriting ->
                    viewModel.modifyFavorite(Mapper.mapToWish(wish), isFavoriting)
                },
                completeListener = { itemId, wish, isDone ->
                    viewModel.completeItem(itemId, wish, isDone)
                },
                likeListener = { itemId, wish, isLiked ->
                    viewModel.likeItem(itemId, wish, isLiked)
                },
                seenCountListener = { wishId ->
                    viewModel.incrementSeenCount(wishId)
                }
            )
            (wishesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            wishesRecyclerView.adapter = wishesAdapter
        } else {
            if (wishesType == SEARCH) {
                wishesRecyclerView.adapter = null
                noResultsPlaceholder.visibility = View.VISIBLE
                wishesFragmentContainer.setBackgroundColor(
                    ContextCompat.getColor(this.context!!, R.color.white)
                )

            }
        }

    }

}

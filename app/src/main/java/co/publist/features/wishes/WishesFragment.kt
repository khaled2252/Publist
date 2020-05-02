package co.publist.features.wishes

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.OnLoadMoreListener
import co.publist.core.utils.RecyclerViewLoadMoreScroll
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.core.utils.Utils.Constants.SEARCH
import co.publist.core.utils.Utils.Constants.VISIBLE_THRESHOLD
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
    private lateinit var publicWishesAdapter: WishesAdapter
    private lateinit var scrollListener: RecyclerViewLoadMoreScroll

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
        setObservers()
        setAdapter()
    }

    private fun setObservers() {
        viewModel.preLoadedWishesType.observe(viewLifecycleOwner, Observer { type ->
            refreshLayout.isRefreshing = true
            wishesType = type
            if (wishesType == PUBLIC || wishesType == SEARCH) {
                noResultsPlaceholder.visibility = View.GONE
                wishesFragmentContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        this.context!!,
                        R.color.platinum
                    )
                )
            }
        })

        viewModel.wishDataPairLiveData.observe(viewLifecycleOwner, Observer { dataPair ->
            refreshLayout.isRefreshing = false
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
            if (refreshLayout.isRefreshing) //After initial load disable refreshing icon
            {
                refreshLayout.isRefreshing = false
                addWishesToAdapter(list)
            } else { //Display the next page
                publicWishesAdapter.renderLoadMoreUi(false)
                scrollListener.setLoaded()
                addWishesToAdapter(list)
            }
        })
    }

    private fun setListeners() {
        refreshLayout.setOnRefreshListener {
            clearLoadedData()
            viewModel.loadWishes(wishesType)
        }
    }

    fun clearLoadedData() {
        wishesRecyclerView.recycledViewPool.clear()
        wishesRecyclerView.layoutManager = null
        viewModel.resetCurrentPagingSate()
        setAdapter()
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
                completeListener = { itemId, wishId, isCreator, isDone ->
                    viewModel.completeItem(itemId, wishId, isCreator, isDone)
                },
                likeListener = { itemId, wishId, isLiked ->
                    viewModel.likeItem(itemId, wishId, isLiked)
                },
                seenCountListener = { wishId ->
                    viewModel.incrementSeenCount(wishId)
                },
                scrollListener = { position ->
                    Handler().postDelayed(
                        { wishesRecyclerView.smoothScrollToPosition(position) },
                        100
                    )
                })

        adapter.startListening()
        (wishesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false //To disable animation when changing holder information
        wishesRecyclerView.adapter = adapter
    }

    fun setAdapter() {
        publicWishesAdapter = WishesAdapter(
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
            completeListener = { itemId, wishId, isCreator, isDone ->
                viewModel.completeItem(itemId, wishId, isCreator, isDone)
            },
            likeListener = { itemId, wishId, isLiked ->
                viewModel.likeItem(itemId, wishId, isLiked)
            },
            seenCountListener = { wishId ->
                viewModel.incrementSeenCount(wishId)
            }, scrollListener = { position ->
                wishesRecyclerView.post {
                    wishesRecyclerView.smoothScrollToPosition(position)
                }
            })
        val linearLayoutManager = LinearLayoutManager(this.context)
        scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager)
        scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                if (viewModel.isLoadingMore) {
                    publicWishesAdapter.renderLoadMoreUi(true)
                    viewModel.loadWishes(wishesType)
                }
            }

        })
        wishesRecyclerView.layoutManager = linearLayoutManager
        wishesRecyclerView.addOnScrollListener(scrollListener)
        (wishesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        wishesRecyclerView.adapter = publicWishesAdapter
    }

    private fun addWishesToAdapter(wishesArray: ArrayList<WishAdapterItem>) {
        if (wishesArray.isNotEmpty()) {
            publicWishesAdapter.addWishes(wishesArray)
            if (scrollListener.lastVisibleItem > VISIBLE_THRESHOLD)
                wishesRecyclerView.smoothScrollBy(
                    0,
                    publicWishesAdapter.loadMoreViewHeight
                ) //Scroll by loadMore view height after loading next page
        }
        else {
            if (wishesType == SEARCH && publicWishesAdapter.itemCount == 0) //Empty search query
            {
                noResultsPlaceholder.visibility = View.VISIBLE
                wishesFragmentContainer.setBackgroundColor(
                    ContextCompat.getColor(this.context!!, R.color.white)
                )
            }
        }
    }

}

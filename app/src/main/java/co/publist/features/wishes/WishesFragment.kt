package co.publist.features.wishes

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.OnLoadMoreListener
import co.publist.core.utils.PlaceHolderAdapterDataObserver
import co.publist.core.utils.RecyclerViewLoadMoreScroll
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.LOAD_MORE_DELAY
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.core.utils.Utils.Constants.SEARCH
import co.publist.core.utils.Utils.Constants.VISIBLE_THRESHOLD
import co.publist.features.home.HomeActivity
import co.publist.features.profile.ProfileActivity
import co.publist.features.wishdetails.WishDetailsActivity
import kotlinx.android.synthetic.main.fragment_wishes.*
import javax.inject.Inject
import kotlin.math.absoluteValue


class WishesFragment : BaseFragment<WishesViewModel>() {

    @Inject
    lateinit var viewModel: WishesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    var wishesType = -1
    private lateinit var publicWishesAdapter: PublicWishesAdapter
    private lateinit var profileWishesAdapter: ProfileWishesAdapter
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
    }

    private fun setObservers() {
        viewModel.preLoadedWishesType.observe(viewLifecycleOwner, Observer { type ->
            refreshLayout.isRefreshing = true
            refreshLayout.setColorSchemeResources(R.color.refreshIconColor)
            refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimary)
            wishesType = type
            if (wishesType == PUBLIC || wishesType == SEARCH || wishesType == DETAILS) {
                setPublicWishesAdapter()
                noResultsPlaceholder.visibility = View.GONE
                noInternetConnectionPlaceholder.visibility = View.GONE
            } else
                setProfileWishesAdapter()
        })

        viewModel.profileWishesDataPairLiveData.observe(viewLifecycleOwner, Observer { dataPair ->
            refreshLayout.isRefreshing = false
            val wishesList = dataPair.first
            val itemsAttributesPair = dataPair.second
            val doneItemsList = itemsAttributesPair.first
            val likedItemsList = itemsAttributesPair.second
            profileWishesAdapter.addDoneAndLikedItems(doneItemsList, likedItemsList)
            if (refreshLayout.isRefreshing) {
                refreshLayout.isRefreshing = false
                addItemsToProfileWishesAdapter(wishesList)
            } else {
                Handler().postDelayed({
                    profileWishesAdapter.renderLoadMoreUi(false)
                    scrollListener.setLoaded()
                    addItemsToProfileWishesAdapter(wishesList)
                }, LOAD_MORE_DELAY)
            }
        })

        viewModel.publicWishesListLiveData.observe(viewLifecycleOwner, Observer { list ->
            if (refreshLayout.isRefreshing) //Initial load or was doing a refresh
            {
                refreshLayout.isRefreshing = false
                addItemsToPublicWishesAdapter(list)
            } else { //Display the next page
                Handler().postDelayed({
                    publicWishesAdapter.renderLoadMoreUi(false)
                    scrollListener.setLoaded()
                    addItemsToPublicWishesAdapter(list)
                }, LOAD_MORE_DELAY)
            }
        })

    }

    private fun setListeners() {
        refreshLayout.setOnRefreshListener {
            if (noInternetConnectionPlaceholder.isVisible)
                noInternetConnectionPlaceholder.visibility = View.GONE
            clearLoadedData()
            viewModel.loadWishes(wishesType)
        }
    }

    fun clearLoadedData() {
        wishesRecyclerView.recycledViewPool.clear()
        wishesRecyclerView.layoutManager = null
        viewModel.resetCurrentPagingSate()
        setPublicWishesAdapter()
    }

    private fun setProfileWishesAdapter() {
        profileWishesAdapter =
            ProfileWishesAdapter(
                wishesType,
                user = viewModel.user!!,
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
                seeMoreListener = { position ->
                    wishesRecyclerView.post {
                        //Scroll to the beginning of the wish (if  aprox 55 % or less of it is shown from the bottom side)
                        //i.e if 60 % of it is shown do nothing
                        val currentWishView = wishesRecyclerView.getChildAt(0)
                        if (currentWishView.top.absoluteValue > currentWishView.height * 0.55)
                            wishesRecyclerView.smoothScrollToPosition(position)
                    }
                }, getCategoryNameById = { categoryId ->
                    viewModel.getCategoryNameById(categoryId)
                })

        val linearLayoutManager = LinearLayoutManager(this.context)
        wishesRecyclerView.layoutManager = linearLayoutManager

        if (wishesType != DETAILS) {
            scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager)
            scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    if (viewModel.isLoadingMore) {
                        profileWishesAdapter.renderLoadMoreUi(true)
                        viewModel.loadWishes(wishesType)
                    }
                }

            })
            wishesRecyclerView.addOnScrollListener(scrollListener)
        }

        profileWishesAdapter.setHasStableIds(true)
        val placeHolder =
            this.parentFragment?.view?.findViewById<LinearLayout>(R.id.placeHolderView)
        profileWishesAdapter.registerAdapterDataObserver(
            PlaceHolderAdapterDataObserver(
                profileWishesAdapter,
                placeHolder!!
            )
        )

        wishesRecyclerView.adapter = profileWishesAdapter
    }

    private fun setPublicWishesAdapter() {
        publicWishesAdapter = PublicWishesAdapter(
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
            },
            seeMoreListener = { position ->
                wishesRecyclerView.post {
                    val currentWishView = wishesRecyclerView.getChildAt(0)
                    if (currentWishView.top.absoluteValue > currentWishView.height * 0.55)
                        wishesRecyclerView.smoothScrollToPosition(position)
                }
            },
            getCategoryNameById = { categoryId ->
                viewModel.getCategoryNameById(categoryId)
            })
        val linearLayoutManager = LinearLayoutManager(this.context)
        wishesRecyclerView.layoutManager = linearLayoutManager

        if (wishesType != DETAILS) {
            scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager)
            scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    if (viewModel.isLoadingMore) {
                        publicWishesAdapter.renderLoadMoreUi(true)
                        viewModel.loadWishes(wishesType)
                    }
                }

            })
            wishesRecyclerView.addOnScrollListener(scrollListener)
        }
        publicWishesAdapter.setHasStableIds(true)
        wishesRecyclerView.adapter = publicWishesAdapter
    }

    private fun addItemsToPublicWishesAdapter(wishesArray: ArrayList<WishAdapterItem>) {
        if (wishesArray.isNotEmpty()) {
            publicWishesAdapter.addWishes(wishesArray)
            if (wishesType != DETAILS && scrollListener.lastVisibleItem > VISIBLE_THRESHOLD)
                wishesRecyclerView.smoothScrollBy(
                    0,
                    publicWishesAdapter.loadMoreViewHeight
                ) //Scroll by loadMore view height after loading next page
        } else {
            if (wishesType == SEARCH && publicWishesAdapter.itemCount == 0) //Empty search query
                noResultsPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun addItemsToProfileWishesAdapter(wishesArray: ArrayList<WishAdapterItem>) {
            profileWishesAdapter.addWishes(wishesArray)
            if (scrollListener.lastVisibleItem > VISIBLE_THRESHOLD)
                wishesRecyclerView.smoothScrollBy(
                    0,
                    profileWishesAdapter.loadMoreViewHeight
                ) //Scroll by loadMore view height after loading next page
        }
}

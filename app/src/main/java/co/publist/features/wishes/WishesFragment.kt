package com.publist.features.wishes

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.publist.R
import com.publist.core.common.data.models.Mapper
import com.publist.core.common.data.models.wish.WishAdapterItem
import com.publist.core.platform.BaseFragment
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.OnLoadMoreListener
import com.publist.core.utils.PlaceHolderAdapterDataObserver
import com.publist.core.utils.RecyclerViewLoadMoreScroll
import com.publist.core.utils.Utils.Constants.CHECK_ITEM
import com.publist.core.utils.Utils.Constants.DETAILS
import com.publist.core.utils.Utils.Constants.DISLIKE_ITEM
import com.publist.core.utils.Utils.Constants.ITEM_ID
import com.publist.core.utils.Utils.Constants.LIKE_ITEM
import com.publist.core.utils.Utils.Constants.LOAD_MORE_DELAY
import com.publist.core.utils.Utils.Constants.PUBLIC
import com.publist.core.utils.Utils.Constants.SEARCH
import com.publist.core.utils.Utils.Constants.UNCHECK_ITEM
import com.publist.core.utils.Utils.Constants.VISIBLE_THRESHOLD
import com.publist.core.utils.Utils.Constants.WISH_ID
import com.publist.features.home.HomeActivity
import com.publist.features.profile.ProfileActivity
import com.publist.features.wishdetails.WishDetailsActivity
import kotlinx.android.synthetic.main.fragment_wishes.*
import javax.inject.Inject
import kotlin.math.absoluteValue


class WishesFragment : BaseFragment<WishesViewModel>() {

    @Inject
    lateinit var viewModel: WishesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

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

                    if (isDone)
                        mFirebaseAnalytics.logEvent(
                            CHECK_ITEM,
                            bundleOf(
                                Pair(WISH_ID, wishId),
                                Pair(ITEM_ID, itemId)
                            )
                        )
                    else
                        mFirebaseAnalytics.logEvent(
                            UNCHECK_ITEM,
                            bundleOf(
                                Pair(WISH_ID, wishId),
                                Pair(ITEM_ID, itemId)
                            )
                        )
                },
                likeListener = { itemId, wishId, isLiked ->
                    viewModel.likeItem(itemId, wishId, isLiked)

                    if (isLiked)
                        mFirebaseAnalytics.logEvent(
                            LIKE_ITEM,
                            bundleOf(
                                Pair(WISH_ID, wishId),
                                Pair(ITEM_ID, itemId)
                            )
                        )
                    else
                        mFirebaseAnalytics.logEvent(
                            DISLIKE_ITEM,
                            bundleOf(
                                Pair(WISH_ID, wishId),
                                Pair(ITEM_ID, itemId)
                            )
                        )
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

                if (isDone)
                    mFirebaseAnalytics.logEvent(
                        CHECK_ITEM,
                        bundleOf(
                            Pair(WISH_ID, wishId),
                            Pair(ITEM_ID, itemId)
                        )
                    )
                else
                    mFirebaseAnalytics.logEvent(
                        UNCHECK_ITEM,
                        bundleOf(
                            Pair(WISH_ID, wishId),
                            Pair(ITEM_ID, itemId)
                        )
                    )
            },
            likeListener = { itemId, wishId, isLiked ->
                viewModel.likeItem(itemId, wishId, isLiked)

                if (isLiked)
                    mFirebaseAnalytics.logEvent(
                        LIKE_ITEM,
                        bundleOf(
                            Pair(WISH_ID, wishId),
                            Pair(ITEM_ID, itemId)
                        )
                    )
                else
                    mFirebaseAnalytics.logEvent(
                        DISLIKE_ITEM,
                        bundleOf(
                            Pair(WISH_ID, wishId),
                            Pair(ITEM_ID, itemId)
                        )
                    )
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

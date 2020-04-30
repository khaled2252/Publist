package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.FAVORITES
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.MY_FAVORITES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.core.utils.Utils.Constants.SEARCH
import co.publist.core.utils.Utils.Constants.TOP_USERS_THRESHOLD
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.myfavorites.data.MyFavoritesRepositoryInterface
import co.publist.features.mylists.data.MyListsRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val myListsRepository: MyListsRepository,
    private val favoritesRepository: MyFavoritesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    val wishesListLiveData = MutableLiveData<ArrayList<WishAdapterItem>>()
    val wishDataPairLiveData =
        MutableLiveData<Pair<Query, Pair<ArrayList<String>, ArrayList<String>>>>()
    val preLoadedWishesType = MutableLiveData<Int>()
    val isFavoriteAdded = MutableLiveData<Boolean>()
    val wishDeletedLiveData = MutableLiveData<Boolean>()
    val editWishLiveData = MutableLiveData<WishAdapterItem>()
    val user = userRepository.getUser()
    var lastVisibleWishesPageDocumentSnapshot: DocumentSnapshot? = null
    var isLoadingMore = false
    lateinit var searchQuery: String
    lateinit var selectedWish: WishAdapterItem
    fun loadWishes(type: Int) {
        if (!isLoadingMore || type != preLoadedWishesType.value) //Check if first time loading data or changed from type to another
        {
            preLoadedWishesType.postValue(type)
            isLoadingMore = true
        }
        when (type) {
            PUBLIC -> {
                subscribe(categoryRepository.getLocalSelectedCategories()
                    .flatMap { categories ->
                        val wishesSingleObservable =
                            wishesRepository.getAllWishesPage(lastVisibleWishesPageDocumentSnapshot)
                        if (categories.isNullOrEmpty())
                            wishesSingleObservable.flatMap { wishesDataPair ->
                                val wishesList = wishesDataPair.first
                                lastVisibleWishesPageDocumentSnapshot = wishesDataPair.second
                                if (lastVisibleWishesPageDocumentSnapshot == null)
                                    isLoadingMore = false
                                Single.just(Mapper.mapToWishAdapterItemArrayList(wishesList)) //UnfilteredWishes for guest mode
                            }
                        else {
                            wishesSingleObservable.flatMap { wishesDataPair ->
                                val wishesList = wishesDataPair.first
                                lastVisibleWishesPageDocumentSnapshot = wishesDataPair.second
                                if (lastVisibleWishesPageDocumentSnapshot == null)
                                    isLoadingMore = false
                                var filteredWishes =
                                    filterWishesByCategories(wishesList, categories)
                                if (userRepository.getUser() == null)
                                    Single.just(filteredWishes) //FilteredWishes for guest mode
                                else {
                                    favoritesRepository.getUserFavoriteWishes()
                                        .flatMap { favoriteList ->
                                            wishesRepository.getDoneItemsInMyFavorites()
                                                .flatMap { doneItemsInFavoritesArrayList ->
                                                    filteredWishes = filterWishesByFavorites(
                                                        filteredWishes,
                                                        favoriteList,
                                                        doneItemsInFavoritesArrayList
                                                    )
                                                    wishesRepository.getDoneItemsInMyLists()
                                                        .flatMap { doneItemsInMyListsArrayList ->
                                                            filteredWishes = filterWishesByCreator(
                                                                filteredWishes,
                                                                user?.id!!,
                                                                doneItemsInMyListsArrayList
                                                            )
                                                            wishesRepository.getUserLikedItems()
                                                                .flatMap { likedItemsList ->
                                                                    filteredWishes =
                                                                        applyUserLikedItems(
                                                                            filteredWishes,
                                                                            likedItemsList
                                                                        )
                                                                    Single.just(filteredWishes)
                                                                }
                                                        }

                                                }
                                        }
                                }
                            }
                        }
                    }, Consumer { list ->
                    wishesListLiveData.postValue(list)
                }, showLoading = false
                )
            }
            LISTS -> {
                subscribe(
                    wishesRepository.getCorrespondingMyListsPublicWishes().flatMap { query ->
                        wishesRepository.getUserLikedItems().flatMap { likedItemsList ->
                            wishesRepository.getDoneItemsInMyLists().flatMap { doneItemsList ->
                                Single.just(Pair(doneItemsList, likedItemsList))
                            }.flatMap { itemsAttributesPair ->
                                Single.just(Pair(query, itemsAttributesPair))
                            }
                        }
                    },
                    Consumer { dataPair ->
                        wishDataPairLiveData.postValue(dataPair)
                    })
            }

            FAVORITES -> {
                subscribe(
                    wishesRepository.getCorrespondingMyFavoritesPublicWishes().flatMap { query ->
                        wishesRepository.getUserLikedItems().flatMap { likedItemsList ->
                            wishesRepository.getDoneItemsInMyFavorites().flatMap { doneItemsList ->
                                Single.just(Pair(doneItemsList, likedItemsList))
                            }.flatMap { itemsAttributesPair ->
                                Single.just(Pair(query, itemsAttributesPair))
                            }
                        }
                    },
                    Consumer { dataPair ->
                        wishDataPairLiveData.postValue(dataPair)
                    })
            }

            DETAILS -> {
                if (selectedWish.wishId != null) //Check because of iOS bug
                    subscribe(wishesRepository.getSpecificWish(selectedWish.wishId!!).flatMap { wish ->
                        if (user != null) {
                            favoritesRepository.getUserFavoriteWishes().flatMap { favoritesList ->
                                wishesRepository.getDoneItemsInMyLists()
                                    .flatMap { doneItemsInMyListsArrayList ->
                                        var oneElementList =
                                            arrayListOf(Mapper.mapToWishAdapterItem(wish))
                                        oneElementList =
                                            filterWishesByCreator(
                                                oneElementList,
                                                user.id!!,
                                                doneItemsInMyListsArrayList
                                            )
                                        wishesRepository.getDoneItemsInMyFavorites()
                                            .flatMap { doneItemsInMyFavoritesArrayList ->
                                                oneElementList = filterWishesByFavorites(
                                                    oneElementList,
                                                    favoritesList,
                                                    doneItemsInMyFavoritesArrayList
                                                )
                                                wishesRepository.getUserLikedItems()
                                                    .flatMap { likedItemsList ->
                                                        oneElementList = applyUserLikedItems(
                                                            oneElementList,
                                                            likedItemsList
                                                        )
                                                        Single.just(oneElementList)
                                                    }
                                            }

                                    }
                            }
                        } else
                            Single.just(arrayListOf(Mapper.mapToWishAdapterItem(wish)))

                    }, Consumer { oneElementList ->
                        wishesListLiveData.postValue(oneElementList)
                    })
            }

            SEARCH -> {
                subscribe(categoryRepository.fetchAllCategories().flatMap { allCategories ->
                    val categoriesNames = allCategories.map { it.name }
                    var selectedCategory: Category? = null
                    for (categoryIndex in categoriesNames.indices) {
                        if (searchQuery.equals(categoriesNames[categoryIndex], true)) {
                            selectedCategory = allCategories[categoryIndex]
                            break
                        }
                    }

                    val searchResultsObservable =
                        if (selectedCategory != null) wishesRepository.getWishesByCategoryPage(
                            selectedCategory.id!!,
                            lastVisibleWishesPageDocumentSnapshot
                        )
                        else wishesRepository.getWishesByTitle(
                            searchQuery,
                            lastVisibleWishesPageDocumentSnapshot
                        )

                    searchResultsObservable.flatMap { wishesDataPair ->
                        val wishesList = wishesDataPair.first
                        lastVisibleWishesPageDocumentSnapshot = wishesDataPair.second
                        if (lastVisibleWishesPageDocumentSnapshot == null)
                            isLoadingMore = false
                        if (wishesList.isNotEmpty()) {
                            var list = Mapper.mapToWishAdapterItemArrayList(wishesList)
                            if (user != null) {
                                favoritesRepository.getUserFavoriteWishes()
                                    .flatMap { favoriteList ->
                                        wishesRepository.getDoneItemsInMyFavorites()
                                            .flatMap { doneItemsInFavoritesArrayList ->
                                                list = filterWishesByFavorites(
                                                    list,
                                                    favoriteList,
                                                    doneItemsInFavoritesArrayList
                                                )
                                                wishesRepository.getDoneItemsInMyLists()
                                                    .flatMap { doneItemsInMyListsArrayList ->
                                                        list = filterWishesByCreator(
                                                            list,
                                                            user.id!!,
                                                            doneItemsInMyListsArrayList
                                                        )
                                                        wishesRepository.getUserLikedItems()
                                                            .flatMap { likedItemsList ->
                                                                list =
                                                                    applyUserLikedItems(
                                                                        list,
                                                                        likedItemsList
                                                                    )
                                                                Single.just(list)
                                                            }
                                                    }

                                            }
                                    }
                            } else
                                Single.just(Mapper.mapToWishAdapterItemArrayList(wishesList))
                        } else
                            Single.just(arrayListOf())
                    }

                }, Consumer { list ->
                    wishesListLiveData.postValue(list)
                }, showLoading = false)
            }

        }
    }

    private fun applyUserLikedItems(
        filteredWishes: ArrayList<WishAdapterItem>,
        likedItemsList: ArrayList<String>
    ): ArrayList<WishAdapterItem> {
        val wishes = ArrayList(filteredWishes)
        for (wish in filteredWishes)
            for (itemMap in wish.items!!)
                if (likedItemsList.contains(itemMap.key))
                    itemMap.value.isLiked = true

        return wishes
    }

    private fun filterWishesByCreator(
        wishes: ArrayList<WishAdapterItem>,
        id: String,
        doneItemsArrayList: ArrayList<String>
    ): ArrayList<WishAdapterItem> {
        val filteredWishes = ArrayList(wishes)
        for (wish in filteredWishes)
            if (wish.creator?.id == id) {
                wish.isCreator = true
                for (itemIndex in 0 until doneItemsArrayList.size) {
                    if (wish.itemsId!!.contains(doneItemsArrayList[itemIndex]))
                        wish.items?.get(doneItemsArrayList[itemIndex])?.done = true
                }
            }
        return filteredWishes
    }

    private fun filterWishesByFavorites(
        filteredWishes: ArrayList<WishAdapterItem>,
        favoriteList: ArrayList<Wish>,
        doneItemsArrayList: ArrayList<String>
    ): ArrayList<WishAdapterItem> {
        val favoriteFilteredList = ArrayList(filteredWishes)
        for (wish in favoriteList) {
            if (filteredWishes.map { it.wishId }.contains(wish.wishId)) {
                val index = filteredWishes.indexOfFirst {
                    it.wishId == wish.wishId
                }
                favoriteFilteredList[index].isFavorite = true
                for (itemIndex in 0 until doneItemsArrayList.size) {
                    if (favoriteFilteredList[index].itemsId!!.contains(doneItemsArrayList[itemIndex]))
                        favoriteFilteredList[index].items?.get(doneItemsArrayList[itemIndex])?.done =
                            true
                }
            }
        }
        return favoriteFilteredList
    }

    private fun filterWishesByCategories(
        list: ArrayList<Wish>,
        categories: ArrayList<CategoryAdapterItem>
    ): ArrayList<WishAdapterItem> {
        val filteredList = ArrayList(Mapper.mapToWishAdapterItemArrayList(list))
        for (wish in list) {
            if (!categories.map { it.id }.contains(wish.categoryId!![0]))
                filteredList.remove(Mapper.mapToWishAdapterItem(wish))
        }
        return filteredList
    }

    fun modifyFavorite(wish: Wish, isFavoriting: Boolean) {
        if (wish.wishId != null) //Check because of iOS bug
        {
            if (isFavoriting)
                subscribe(
                    favoritesRepository.addToMyFavoritesRemotely(wish)
                        .mergeWith(wishesRepository.incrementOrganicSeen(wish.wishId!!))
                    , Action {
                        isFavoriteAdded.postValue(true)
                    }, showLoading = false
                )
            else {
                val doneItems = arrayListOf<String>()
                for (itemIndex in wish.items!!.values.indices)
                    if (wish.items!!.values.elementAt(itemIndex).done!!)
                        doneItems.add(wish.items!!.keys.elementAt(itemIndex))
                subscribe(
                    favoritesRepository.deleteFromFavoritesRemotely(wish.wishId!!).mergeWith(
                        wishesRepository.decrementCompleteCountInDoneItems(wish.wishId!!, doneItems)
                    )
                        .mergeWith(
                            wishesRepository.removeUserIdFromTopCompletedItems(
                                doneItems,
                                wish.wishId!!
                            )
                        )
                    , Action {
                        isFavoriteAdded.postValue(false)
                    }, showLoading = false
                )
            }
        }
    }

    fun deleteSelectedWish() {
        //Merge operator runs both calls in parallel (independent calls)
        subscribe(wishesRepository.deleteWishFromWishes(Mapper.mapToWish(selectedWish))
            .mergeWith(wishesRepository.deleteWishFromMyLists(Mapper.mapToWish(selectedWish)))
            , Action {
                wishDeletedLiveData.postValue(true)
            })

    }

    fun editSelectedWish() {
        editWishLiveData.postValue(selectedWish)
    }

    fun completeItem(itemId: String, wishId: String, isCreator: Boolean, isDone: Boolean) {
        val collectionTobeEdited =
            if (isCreator) MY_LISTS_COLLECTION_PATH else MY_FAVORITES_COLLECTION_PATH

        subscribe(wishesRepository.checkItemDoneInProfile(
            itemId,
            wishId,
            collectionTobeEdited,
            isDone
        ).mergeWith(wishesRepository.incrementOrganicSeen(wishId))
            .andThen(
                wishesRepository.incrementCompleteCountInWishes(
                    itemId,
                    wishId,
                    isDone
                )
            )
            .flatMapCompletable { completeCount ->
                if (completeCount < TOP_USERS_THRESHOLD)
                    wishesRepository.addUserIdInTopCompletedUsersIdSubCollection(
                        itemId,
                        wishId,
                        isDone
                    )
                        .mergeWith(
                            wishesRepository.addUserIdInTopCompletedUsersIdField(
                                itemId, wishId,
                                isDone
                            )
                        )
                else
                    wishesRepository.addUserIdInTopCompletedUsersIdSubCollection(
                        itemId,
                        wishId,
                        isDone
                    )
            }
            , Action {
            }, showLoading = false
        )

    }

    fun likeItem(itemId: String, wishId: String, isLiked: Boolean) {
        subscribe(wishesRepository.addItemToUserViewedItems(itemId, isLiked)
            .mergeWith(wishesRepository.incrementOrganicSeen(wishId))
            .andThen(
                wishesRepository.incrementViewedCountInWishes(
                    itemId,
                    wishId,
                    isLiked
                )
            )
            .flatMapCompletable { likeCount ->
                if (likeCount < TOP_USERS_THRESHOLD)
                    wishesRepository.addUserIdInTopViewedUsersIdSubCollection(
                        itemId,
                        wishId,
                        isLiked
                    )
                        .mergeWith(
                            wishesRepository.addUserIdInTopViewedUsersIdField(
                                itemId, wishId,
                                isLiked
                            )
                        )
                else
                    wishesRepository.addUserIdInTopViewedUsersIdSubCollection(
                        itemId,
                        wishId,
                        isLiked
                    )
            }
            , Action {
            }, showLoading = false
        )
    }

    fun incrementSeenCount(wishId: String) {
        subscribe(wishesRepository.isWishSeen(wishId).flatMapCompletable { isSeen ->
            if (!isSeen) {
                wishesRepository.incrementSeenCountLocally(wishId)
                wishesRepository.incrementSeenCountRemotely(wishId)
            } else
                Completable.complete()

        }, Action {

        }, showLoading = false)
    }
}
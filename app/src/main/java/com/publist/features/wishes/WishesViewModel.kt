package com.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.publist.core.common.data.models.Mapper
import com.publist.core.common.data.models.category.Category
import com.publist.core.common.data.models.category.CategoryAdapterItem
import com.publist.core.common.data.models.wish.Wish
import com.publist.core.common.data.models.wish.WishAdapterItem
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import com.publist.core.platform.BaseViewModel
import com.publist.core.utils.Utils.Constants.DETAILS
import com.publist.core.utils.Utils.Constants.FAVORITES
import com.publist.core.utils.Utils.Constants.LISTS
import com.publist.core.utils.Utils.Constants.MY_FAVORITES_COLLECTION_PATH
import com.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import com.publist.core.utils.Utils.Constants.PUBLIC
import com.publist.core.utils.Utils.Constants.SEARCH
import com.publist.core.utils.Utils.Constants.TOP_USERS_THRESHOLD
import com.publist.core.utils.Utils.Constants.WISHES_NUM_PER_PAGE
import com.publist.core.utils.Utils.getField
import com.publist.features.categories.data.CategoriesRepositoryInterface
import com.publist.features.myfavorites.data.MyFavoritesRepositoryInterface
import com.publist.features.mylists.data.MyListsRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val myListsRepository: MyListsRepository,
    private val favoritesRepository: MyFavoritesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    //LiveData
    val publicWishesListLiveData = MutableLiveData<ArrayList<WishAdapterItem>>()
    val profileWishesDataPairLiveData =
        MutableLiveData<Pair<ArrayList<WishAdapterItem>, Pair<ArrayList<String>, ArrayList<String>>>>()
    val preLoadedWishesType = MutableLiveData<Int>()
    val isFavoriteAdded = MutableLiveData<Boolean>()
    val wishDeletedLiveData = MutableLiveData<Boolean>()
    val editWishLiveData = MutableLiveData<WishAdapterItem>()
    val dataChangedLiveData = MutableLiveData<Boolean>()

    //Paging properties
    var isLoadingMore = false
    var lastVisibleWishesPageDocumentSnapshot: DocumentSnapshot? = null
    var loadedUserCategoriesFilteredWishes = false
    var profileWishesIds: ArrayList<String>? = null
    var profileItemsAttributesPair: Pair<ArrayList<String>, ArrayList<String>>? = null

    lateinit var searchQuery: String
    var selectedWish: WishAdapterItem? = null
    val user = userRepository.getUser()
    var allCategories = arrayListOf<Category>()

    fun loadWishes(type: Int) {
        if (!isLoadingMore || type != preLoadedWishesType.value) //Check if first time loading data or changed from type to another
        {
            preLoadedWishesType.postValue(type)
            isLoadingMore = true
        }

        when (type) {
            PUBLIC -> {
                val publicWishesObservable = categoryRepository.getLocalSelectedCategories()
                    .flatMap { categories ->
                        val wishesPageObservable =
                            wishesRepository.getAllWishesPage(lastVisibleWishesPageDocumentSnapshot)
                        if (categories.isNullOrEmpty())
                            wishesPageObservable.flatMap { wishesDataPair ->
                                val wishesList = wishesDataPair.first
                                lastVisibleWishesPageDocumentSnapshot = wishesDataPair.second

                                if (lastVisibleWishesPageDocumentSnapshot == null) //Already at the last page
                                    isLoadingMore = false

                                Single.just(Mapper.mapToWishAdapterItemArrayList(wishesList)) //UnfilteredWishes for guest mode
                            }
                        else {
                            wishesPageObservable.flatMap { wishesDataPair ->
                                val wishesList = wishesDataPair.first
                                lastVisibleWishesPageDocumentSnapshot = wishesDataPair.second

                                if (lastVisibleWishesPageDocumentSnapshot == null && !loadedUserCategoriesFilteredWishes) //Already at the last page of user categories filtered wishes
                                    loadedUserCategoriesFilteredWishes = true
                                else if (lastVisibleWishesPageDocumentSnapshot == null && loadedUserCategoriesFilteredWishes) //Already at the last page after loading user categories filtered wishes and non user categories filtered wishes
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
                    }
                val publicWishesConsumer = Consumer<ArrayList<WishAdapterItem>> { list ->
                    if (list.isEmpty() && isLoadingMore) //Current loaded page is empty (no data due to categories filtration)
                        loadWishes(preLoadedWishesType.value!!) //Pass this page and load the next one
                    else
                        publicWishesListLiveData.postValue(list)
                }

                if (allCategories.isEmpty()) //To make sure its called once (first time only , other times are load mores)
                    subscribe(
                        categoryRepository.fetchAllCategories().flatMap { categoriesList ->
                            allCategories = categoriesList
                            publicWishesObservable
                        }, publicWishesConsumer
                    )
                else
                    subscribe(publicWishesObservable, publicWishesConsumer)
            }

            LISTS -> {
                val listsWishesObservable = if (profileWishesIds == null)
                    wishesRepository.getMyListsWishesIds().flatMap { wishIdsList ->
                        profileWishesIds = wishIdsList
                        wishesRepository.getUserLikedItems().flatMap { likedItemsList ->
                            wishesRepository.getDoneItemsInMyLists().flatMap { doneItemsList ->
                                Single.just(Pair(doneItemsList, likedItemsList))
                            }.flatMap { itemsAttributesPair ->
                                profileItemsAttributesPair = itemsAttributesPair
                                val currentWishIdsList = getCurrentPageWishesIds(profileWishesIds!!)
                                wishesRepository.getProfileWishesPageFromCorrespondingPublicWishes(
                                    currentWishIdsList
                                ).flatMap { wishList ->
                                    Single.just(
                                        Pair(
                                            Mapper.mapToWishAdapterItemArrayList(wishList),
                                            profileItemsAttributesPair!!
                                        )
                                    )
                                }
                            }
                        }
                    }
                else {
                    val currentWishIdsList = getCurrentPageWishesIds(profileWishesIds!!)
                    wishesRepository.getProfileWishesPageFromCorrespondingPublicWishes(
                        currentWishIdsList
                    ).flatMap { wishList ->
                        Single.just(
                            Pair(
                                Mapper.mapToWishAdapterItemArrayList(wishList),
                                profileItemsAttributesPair!!
                            )
                        )
                    }
                }

                val listsWishesConsumer =
                    Consumer<Pair<ArrayList<WishAdapterItem>, Pair<ArrayList<String>, ArrayList<String>>>>
                    { dataPair ->
                        profileWishesDataPairLiveData.postValue(dataPair)
                    }

                if (allCategories.isEmpty())
                    subscribe(
                        categoryRepository.fetchAllCategories().flatMap { categoriesList ->
                            allCategories = categoriesList
                            listsWishesObservable
                        }, listsWishesConsumer
                    )
                else
                    subscribe(listsWishesObservable, listsWishesConsumer)
            }

            FAVORITES -> {
                val favoritesWishesObservable = if (profileWishesIds == null)
                    wishesRepository.getMyFavoritesWishesIds().flatMap { wishIdsList ->
                        profileWishesIds = wishIdsList
                        wishesRepository.getUserLikedItems().flatMap { likedItemsList ->
                            wishesRepository.getDoneItemsInMyFavorites()
                                .flatMap { doneItemsList ->
                                    Single.just(Pair(doneItemsList, likedItemsList))
                                }.flatMap { itemsAttributesPair ->
                                    profileItemsAttributesPair = itemsAttributesPair
                                    val currentWishIdsList =
                                        getCurrentPageWishesIds(profileWishesIds!!)
                                    wishesRepository.getProfileWishesPageFromCorrespondingPublicWishes(
                                        currentWishIdsList
                                    ).flatMap { wishList ->
                                        Single.just(
                                            Pair(
                                                Mapper.mapToWishAdapterItemArrayList(
                                                    wishList
                                                ), profileItemsAttributesPair!!
                                            )
                                        )
                                    }
                                }
                        }
                    }
                else {
                    val currentWishIdsList = getCurrentPageWishesIds(profileWishesIds!!)
                    wishesRepository.getProfileWishesPageFromCorrespondingPublicWishes(
                        currentWishIdsList
                    ).flatMap { wishList ->
                        Single.just(
                            Pair(
                                Mapper.mapToWishAdapterItemArrayList(wishList),
                                profileItemsAttributesPair!!
                            )
                        )
                    }
                }

                val favoritesWishesConsumer =
                    Consumer<Pair<ArrayList<WishAdapterItem>, Pair<ArrayList<String>, ArrayList<String>>>> { dataPair ->
                        profileWishesDataPairLiveData.postValue(dataPair)
                    }

                if (allCategories.isEmpty())
                    subscribe(
                        categoryRepository.fetchAllCategories().flatMap { categoriesList ->
                            allCategories = categoriesList
                            favoritesWishesObservable
                        }, favoritesWishesConsumer
                    )
                else
                    subscribe(favoritesWishesObservable, favoritesWishesConsumer)
            }

            DETAILS -> {
                subscribe(categoryRepository.fetchAllCategories().flatMap { categoriesList ->
                    allCategories = categoriesList
                    wishesRepository.getSpecificWish(selectedWish?.wishId!!).flatMap { wish ->
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

                    }
                }, Consumer { oneElementList ->
                        publicWishesListLiveData.postValue(oneElementList)
                    })
            }

            SEARCH -> {
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

                subscribe(searchResultsObservable.flatMap { wishesDataPair ->
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
                }, Consumer { list ->
                    publicWishesListLiveData.postValue(list)
                }, showLoading = false)
            }

        }
    }

    //Filtration functions
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
        val filteredList = arrayListOf<WishAdapterItem>()
        for (wish in list) {
            val mappedWish = Mapper.mapToWishAdapterItem(wish)
            //Add wishes that are in user categories
            if (!loadedUserCategoriesFilteredWishes && categories.map { it.id }
                    .contains(wish.categoryId!![0]))
                filteredList.add(mappedWish)
            else if (loadedUserCategoriesFilteredWishes && !categories.map { it.id }
                    .contains(wish.categoryId!![0]))
            //According to business , load wishes [that are not in user categories] after user categories filtered wish pages are loaded
                filteredList.add(mappedWish)
        }
        return filteredList
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

    // Helper functions
    private fun getCurrentPageWishesIds(profileWishesIds: ArrayList<String>): ArrayList<String> {
        val currentWishIdsList = ArrayList<String>()
        for (index in 0 until WISHES_NUM_PER_PAGE) {
            if (profileWishesIds.isNotEmpty()) {
                currentWishIdsList.add(profileWishesIds.last())
                profileWishesIds.removeAt(profileWishesIds.size - 1)
            } else {
                isLoadingMore = false
                break
            }
        }
        return currentWishIdsList
    }

    fun resetCurrentPagingSate() {
        if (preLoadedWishesType.value == PUBLIC || preLoadedWishesType.value == SEARCH) {
            lastVisibleWishesPageDocumentSnapshot = null
            loadedUserCategoriesFilteredWishes = false
        } else {
            profileWishesIds = null
            profileItemsAttributesPair = null
        }
        isLoadingMore = false

    }

    fun getSuggestedCategoriesFromQuery(query: String): ArrayList<String> {
        val suggestedCategoriesNames = arrayListOf<String>()
        val currentDeviceLanguage = Locale.getDefault().language
        val localizedNames =
            allCategories.map { it.localizations?.getField<String>(currentDeviceLanguage) }
        for (categoryName in localizedNames)
            if (categoryName.equals(query, true))
                suggestedCategoriesNames.add(categoryName!!.capitalize())
        if (suggestedCategoriesNames.isEmpty())
            for (categoryName in localizedNames)
                if (categoryName!!.startsWith(query, true))
                    suggestedCategoriesNames.add(categoryName.capitalize())


        return suggestedCategoriesNames
    }

    fun getCategoryNameById(categoryId: String): String {
        val category = allCategories.find { it.id == categoryId }
        val currentDeviceLanguage = Locale.getDefault().language
        val localizedName =
            category?.localizations?.getField<String>(currentDeviceLanguage)?.capitalize()
                .toString()
        return localizedName
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

    //Wish actions functions
    fun modifyFavorite(wish: Wish, isFavoriting: Boolean) {
        dataChangedLiveData.postValue(true)
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

    fun deleteSelectedWish() {
        //Merge operator runs both calls in parallel (independent calls)
        subscribe(
            wishesRepository.deleteWishFromWishes(Mapper.mapToWish(selectedWish!!))
                .mergeWith(wishesRepository.deleteWishFromMyLists(Mapper.mapToWish(selectedWish!!)))
            , Action {
                dataChangedLiveData.postValue(true)
                wishDeletedLiveData.postValue(true)
            })

    }

    fun editSelectedWish() {
        editWishLiveData.postValue(selectedWish)
    }

    fun completeItem(itemId: String, wishId: String, isCreator: Boolean, isDone: Boolean) {
        dataChangedLiveData.postValue(true)
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
        dataChangedLiveData.postValue(true)
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

}
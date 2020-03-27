package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.FAVORITES
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.MY_FAVORITES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.core.utils.Utils.Constants.TOP_COMPLETED_USERS_MINIMUM_COUNT
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.myfavorites.data.MyFavoritesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.Single
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val favoritesRepository: MyFavoritesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    val wishesQueryLiveData = MutableLiveData<Pair<Query, Int>>()
    val wishesListLiveData = MutableLiveData<ArrayList<WishAdapterItem>>()
    val wishesType = MutableLiveData<Int>()
    val isFavoriteAdded = MutableLiveData<Boolean>()
    val wishDeletedLiveData = MutableLiveData<Boolean>()
    val editWishLiveData = MutableLiveData<Wish>()
    lateinit var selectedWish: Wish
    fun loadData(type: Int) {
        wishesType.postValue(type)
        when (type) {
            PUBLIC -> {
                subscribe(categoryRepository.getLocalSelectedCategories()
                    .flatMap { categories ->
                        val wishesSingleObservable = wishesRepository.getAllWishes()
                        if (categories.isNullOrEmpty())
                            wishesSingleObservable.flatMap { list ->
                                Single.just(Mapper.mapToWishAdapterItemArrayList(list)) //UnfilteredWishes for guest mode
                            }
                        else {
                            wishesSingleObservable.flatMap { list ->
                                var filteredWishes =
                                    filterWishesByCategories(list, categories)

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
                                                                userRepository.getUser()?.id!!,
                                                                doneItemsInMyListsArrayList
                                                            )
                                                            Single.just(filteredWishes)
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
            LISTS -> wishesQueryLiveData.postValue(
                Pair(
                    wishesRepository.getUserListWishesQuery(),
                    type
                )
            )
            FAVORITES -> wishesQueryLiveData.postValue(
                Pair(
                    wishesRepository.getUserFavoriteWishesQuery(),
                    type
                )
            )

            else -> {
                subscribe(favoritesRepository.getUserFavoriteWishes().flatMap { favoritesList ->
                    wishesRepository.getSpecificWish(selectedWish.wishId!!).flatMap { wish ->
                        wishesRepository.getDoneItemsInMyLists()
                            .flatMap { doneItemsInMyListsArrayList ->
                                var oneElementList = arrayListOf(Mapper.mapToWishAdapterItem(wish))
                                oneElementList =
                                    filterWishesByCreator(
                                        oneElementList,
                                        userRepository.getUser()?.id!!,
                                        doneItemsInMyListsArrayList
                                    )
                                wishesRepository.getDoneItemsInMyFavorites()
                                    .flatMap { doneItemsInMyFavoritesArrayList ->
                                        oneElementList = filterWishesByFavorites(
                                            oneElementList,
                                            favoritesList,
                                            doneItemsInMyFavoritesArrayList
                                        )
                                        Single.just(oneElementList)
                                    }

                            }

                    }
                }, Consumer { oneElementList ->
                    wishesListLiveData.postValue(oneElementList)
                })
            }

        }

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
        if (isFavoriting)
            subscribe(
                favoritesRepository.addToMyFavoritesRemotely(wish), Action {
                    isFavoriteAdded.postValue(true)
                }, showLoading = false
            )
        else
            subscribe(
                favoritesRepository.deleteFromFavoritesRemotely(wish.wishId!!), Action {
                    isFavoriteAdded.postValue(false)
                }, showLoading = false
            )
    }


    fun deleteSelectedWish() {
        //Merge operator runs both calls in parallel (independent calls)
        subscribe(wishesRepository.deleteWishFromWishes(selectedWish)
            .mergeWith(wishesRepository.deleteWishFromMyLists(selectedWish))
            , Action {
                wishDeletedLiveData.postValue(true)
            })

    }

    fun editSelectedWish() {
        editWishLiveData.postValue(selectedWish)
    }

    fun completeItem(itemId: String, wish: WishAdapterItem, isDone: Boolean) {
        val collectionTobeEdited =
            if (wish.isCreator) MY_LISTS_COLLECTION_PATH else MY_FAVORITES_COLLECTION_PATH
        subscribe(wishesRepository.checkItemDoneInProfile(
            itemId,
            wish.wishId!!,
            collectionTobeEdited,
            isDone
        )
            .andThen(wishesRepository.incrementCompleteCountInWishes(itemId, wish.wishId!!,isDone))
            .flatMapCompletable { completeCount ->
                if (completeCount < TOP_COMPLETED_USERS_MINIMUM_COUNT)
                    wishesRepository.addUserIdInTopCompletedUsersIdSubCollection(
                        itemId,
                        wish.wishId!!,
                        isDone
                    )
                        .mergeWith(
                            wishesRepository.addUserIdInTopCompletedUsersIdField(
                                itemId, wish.wishId!!,
                                isDone
                            )
                        )
                else
                    wishesRepository.addUserIdInTopCompletedUsersIdSubCollection(
                        itemId,
                        wish.wishId!!,
                        isDone
                    )
            }
            , Action {
            })

    }
}
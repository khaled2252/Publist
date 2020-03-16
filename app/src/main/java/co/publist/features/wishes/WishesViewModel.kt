package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.profile.myfavorites.data.MyFavoritesRepositoryInterface
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
    val wishDetailsLiveData = MutableLiveData<Boolean>()
    val wishDeletedLiveData = MutableLiveData<Boolean>()
    lateinit var selectedWish : Wish
    fun loadData(type: Int) {
        wishesType.postValue(type)
        when (type) {
            PUBLIC -> {
                subscribe(categoryRepository.getLocalSelectedCategories()
                    .flatMap { categories ->
                        val wishesSingleObservable = wishesRepository.getAllWishes()
                        if (categories.isNullOrEmpty())
                            wishesSingleObservable.flatMap {list ->
                                Single.just(Mapper.mapToWishAdapterItemArrayList(list)) //UnfilteredWishes for guest mode
                            }
                        else {
                            wishesSingleObservable.flatMap { list ->
                                var filteredWishes =
                                    filterWishesByCategories(list, categories)

                                if(userRepository.getUser() == null)
                                    Single.just(filteredWishes) //FilteredWishes for guest mode
                                else {
                                favoritesRepository.getUserFavoriteWishes()
                                    .flatMap { favoriteList ->
                                            filteredWishes = filterWishesByFavorites(
                                                filteredWishes,
                                                favoriteList
                                            )
                                            filteredWishes = filterWishesByCreator(
                                                filteredWishes,
                                                userRepository.getUser()?.id!!
                                            )
                                            Single.just(filteredWishes)
                                        }
                                    }
                            }
                        }
                    }, Consumer { list ->
                    wishesListLiveData.postValue(list)
                },showLoading = false)
            }
            LISTS -> wishesQueryLiveData.postValue(
                Pair(
                    wishesRepository.getUserListWishesQuery(),
                    type
                )
            )
            else -> wishesQueryLiveData.postValue(
                Pair(
                    wishesRepository.getUserFavoriteWishesQuery(),
                    type
                )
            )
        }

    }

    private fun filterWishesByCreator(
        wishes: ArrayList<WishAdapterItem>,
        id: String
    ): ArrayList<WishAdapterItem> {
        val filteredWishes = ArrayList(wishes)
        for (wish in filteredWishes)
            if (wish.creator?.id == id)
                wish.isCreator = true
        return filteredWishes
    }

    private fun filterWishesByFavorites(
        filteredWishes: ArrayList<WishAdapterItem>,
        favoriteList: ArrayList<Wish>
    ): ArrayList<WishAdapterItem> {
        val favoriteFilteredList = ArrayList(filteredWishes)
        for (wish in favoriteList) {
            if (filteredWishes.map { it.wishId }.contains(wish.wishId)) {
                val index = filteredWishes.indexOfFirst {
                    it.wishId == wish.wishId
                }
                favoriteFilteredList[index].isFavorite = true
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
                },showLoading = false)
        else
            subscribe(
                favoritesRepository.deleteFromFavoritesRemotely(wish.wishId!!), Action {
                    isFavoriteAdded.postValue(false)
                },showLoading = false)
    }


    fun deleteSelectedWish() {
        //Merge operator runs both calls in parallel (independent calls)
        subscribe(wishesRepository.deleteWishFromWishes(selectedWish).mergeWith(wishesRepository.deleteWishFromMyLists(selectedWish))
        , Action {
            wishDeletedLiveData.postValue(true)
        })

    }
}
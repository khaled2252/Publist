package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.Single
import io.reactivex.functions.Consumer
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface
) : BaseViewModel() {
    val wishesQueryLiveData = MutableLiveData<Query>()
    val wishesListLiveData = MutableLiveData<ArrayList<Wish>>()
    val wishesType = MutableLiveData<Int>()
    fun loadData(type: Int) {
        wishesType.postValue(type)
        if (type == PUBLIC) {
            subscribe(categoryRepository.getLocalSelectedCategories().flatMap { categories ->
                val wishesSingleObservable = wishesRepository.getAllWishes()
                if (categories.isNullOrEmpty())
                    wishesSingleObservable
                else {
                    wishesSingleObservable.flatMap { list ->
                        Single.just(filterWishesByCategories(list, categories))
                    }
                }
            }, Consumer { list ->
                wishesListLiveData.postValue(list)
            })
        } else if (type == LISTS)
            wishesQueryLiveData.postValue(wishesRepository.getUserListWishesQuery())
    }

    private fun filterWishesByCategories(
        list: ArrayList<Wish>,
        categories: ArrayList<CategoryAdapterItem>
    ): ArrayList<Wish> {
        val filteredList = ArrayList(list)
        for (wish in list) {
            if (!categories.map {it.id }.contains(wish.categoryId!![0]))
                filteredList.remove(wish)
        }
        return filteredList
    }

}
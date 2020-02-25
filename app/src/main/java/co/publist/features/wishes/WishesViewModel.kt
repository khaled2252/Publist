package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.functions.Consumer
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    categoryRepository: CategoriesRepositoryInterface
) : BaseViewModel() {
    val wishesQueryLiveData = MutableLiveData<Query>()

    init {
        subscribe(categoryRepository.getLocalSelectedCategories(), Consumer { categories ->
            if (categories.isNullOrEmpty())
                wishesQueryLiveData.postValue(wishesRepository.getAllWishesQuery())
            else
                wishesQueryLiveData.postValue(wishesRepository.getFilteredWishesQuery(ArrayList(categories.map{it.id})))
        })


    }

}
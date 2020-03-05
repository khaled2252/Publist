package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.PUBLIC
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
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
            subscribe(categoryRepository.getLocalSelectedCategories(), Consumer { categories ->
                if (categories.isNullOrEmpty())
                    wishesQueryLiveData.postValue(wishesRepository.getAllWishesQuery())
                else
                    wishesQueryLiveData.postValue(
                        wishesRepository.getFilteredWishesQuery(
                            ArrayList(
                                categories.map { it.id })
                        )
                    )
            })
        } else if (type == LISTS)
            wishesQueryLiveData.postValue(wishesRepository.getUserListWishesQuery())
    }
}
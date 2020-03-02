package co.publist.features.wishes

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.functions.Consumer
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    val wishesQueryLiveData = MutableLiveData<Query>()

    fun onCreated(isPublic : Boolean) {
        if(isPublic) {
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
        }
        else
            wishesQueryLiveData.postValue(wishesRepository.getUserListWishesQuery(userRepository.getUser()?.id!!))
    }

}
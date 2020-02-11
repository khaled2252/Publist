package co.publist.features.wishes

import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.wishes.data.WishesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.functions.Consumer
import javax.inject.Inject

class WishesViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    fun getWishesQuery(): Query {
        var categories: ArrayList<String>? = ArrayList()
        if (userRepository.getUser() == null)
            categories = categoryRepository.getGuestCategories()
        else
            subscribe(categoryRepository.getUserCategories(), Consumer {
                categories = it
            })

        return if (categories == null)
            wishesRepository.getAllWishesQuery()
        else
            wishesRepository.getFilteredWishesQuery(categories!!)

    }

}
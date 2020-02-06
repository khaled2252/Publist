package co.publist.features.wishes

import co.publist.core.platform.BaseViewModel
import co.publist.features.wishes.data.WishesRepositoryInterface
import com.google.firebase.firestore.Query
import javax.inject.Inject

class WishesViewModel @Inject constructor(private val wishesRepository: WishesRepositoryInterface
) : BaseViewModel() {

    var selectedCategoriesList = ArrayList<String>()

    fun getWishesQuery(): Query {
        return wishesRepository.getWishesQuery()
    }

}
package co.publist.features.wishes.data
import com.google.firebase.firestore.Query

interface WishesRepositoryInterface {
    fun getAllWishesQuery(): Query
    fun getFilteredWishesQuery(categoryList : ArrayList<String>): Query
}
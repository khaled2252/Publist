package co.publist.features.wishes.data
import com.google.firebase.firestore.Query
import io.reactivex.Completable

interface WishesRepositoryInterface {
    fun getAllWishesQuery(): Query
    fun getFilteredWishesQuery(categoryList : ArrayList<String>): Query
    fun createWish(wish : Wish): Completable
}
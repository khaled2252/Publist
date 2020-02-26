package co.publist.core.common.data.repositories.wish
import co.publist.core.common.data.models.wish.Wish
import com.google.firebase.firestore.Query
import io.reactivex.Completable
import io.reactivex.Single

interface WishesRepositoryInterface {
    fun getAllWishesQuery(): Query
    fun getFilteredWishesQuery(categoryList: ArrayList<String?>): Query
    fun createWish(wish : Wish): Completable
    fun uploadImage(imageUri : String) : Single<String>
}
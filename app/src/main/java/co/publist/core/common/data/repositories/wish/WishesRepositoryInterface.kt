package co.publist.core.common.data.repositories.wish
import co.publist.core.common.data.models.wish.Wish
import com.google.firebase.firestore.Query
import io.reactivex.Completable
import io.reactivex.Single

interface WishesRepositoryInterface {
    fun getAllWishesQuery(): Query
    fun getFilteredWishesQuery(categoryList: ArrayList<String?>): Query
    fun getUserListWishesQuery(): Query
    fun getUserFavoriteWishesQuery(): Query
    fun getSpecificWish(wishId : String): Single<Wish>
    fun getAllWishes(): Single<ArrayList<Wish>>
    fun getMyListWishes(): Single<ArrayList<Wish>>
    fun createWish(wish: Wish): Completable
    fun updateWish(wish: Wish): Completable
    fun uploadImage(imageUri : String) : Single<Pair<String,String>>
    fun deleteWishFromMyLists(selectedWish: Wish) : Completable
    fun deleteWishFromWishes(selectedWish: Wish) : Completable
}
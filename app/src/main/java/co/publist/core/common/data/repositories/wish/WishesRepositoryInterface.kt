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
    fun getDoneItemsInMyLists() : Single<ArrayList<String>>
    fun getDoneItemsInMyFavorites() : Single<ArrayList<String>>
    fun checkItemDoneInProfile(itemId: String, wishId : String,collectionToBeEdited : String, isDone: Boolean): Completable
    fun incrementCompleteCountInWishes(itemId: String , wishId : String, isDone: Boolean): Single<Int>
    fun addUserIdInTopCompletedUsersIdSubCollection(itemId : String , wishId : String,isAdding : Boolean): Completable
    fun addUserIdInTopCompletedUsersIdField(itemId : String, wishId : String,isAdding : Boolean): Completable
    fun decrementCompleteCountInDoneItems(wishId : String , doneItems: ArrayList<String>) : Completable
    fun removeUserIdFromTopCompletedItems(doneItems: ArrayList<String>, wishId: String) : Completable
    fun addItemToUserViewedItems(itemId: String,isLiked: Boolean): Completable
    fun incrementViewedCountInWishes(itemId: String , wishId : String, isLiked: Boolean): Single<Int>
    fun addUserIdInTopViewedUsersIdSubCollection(itemId: String, wishId: String,isAdding : Boolean): Completable
    fun addUserIdInTopViewedUsersIdField(itemId: String, wishId: String,isAdding : Boolean): Completable
    fun getUserLikedItems(): Single<ArrayList<String>>
    fun getUserPictureFromId(userId : String) : Single<String>
}
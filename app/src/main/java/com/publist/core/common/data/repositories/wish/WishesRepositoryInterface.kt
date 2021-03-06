package com.publist.core.common.data.repositories.wish

import com.google.firebase.firestore.DocumentSnapshot
import com.publist.core.common.data.models.wish.Wish
import io.reactivex.Completable
import io.reactivex.Single

interface WishesRepositoryInterface {
    fun getSpecificWish(wishId: String): Single<Wish>
    fun getAllWishesPage(lastVisibleWishesDocumentSnapshot: DocumentSnapshot?): Single<Pair<ArrayList<Wish>, DocumentSnapshot?>>
    fun getWishesByCategoryPage(
        categoryId: String,
        lastVisibleWishesDocumentSnapshot: DocumentSnapshot?
    ): Single<Pair<ArrayList<Wish>, DocumentSnapshot?>>
    fun getMyListWishes(): Single<ArrayList<Wish>>
    fun createWish(wish: Wish): Single<String>
    fun updateWish(wish: Wish): Completable
    fun uploadImage(imageUri: String): Single<Pair<String, String>>
    fun deleteWishFromMyLists(selectedWish: Wish): Completable
    fun deleteWishFromWishes(selectedWish: Wish): Completable
    fun getDoneItemsInMyLists(): Single<ArrayList<String>>
    fun getDoneItemsInMyFavorites(): Single<ArrayList<String>>
    fun checkItemDoneInProfile(
        itemId: String,
        wishId: String,
        collectionToBeEdited: String,
        isDone: Boolean
    ): Completable

    fun incrementCompleteCountInWishes(itemId: String, wishId: String, isDone: Boolean): Single<Int>
    fun addUserIdInTopCompletedUsersIdSubCollection(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable

    fun addUserIdInTopCompletedUsersIdField(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable

    fun decrementCompleteCountInDoneItems(wishId: String, doneItems: ArrayList<String>): Completable
    fun removeUserIdFromTopCompletedItems(doneItems: ArrayList<String>, wishId: String): Completable
    fun addItemToUserViewedItems(itemId: String, isLiked: Boolean): Completable
    fun incrementViewedCountInWishes(itemId: String, wishId: String, isLiked: Boolean): Single<Int>
    fun addUserIdInTopViewedUsersIdSubCollection(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable

    fun addUserIdInTopViewedUsersIdField(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable

    fun getUserLikedItems(): Single<ArrayList<String>>
    fun incrementOrganicSeen(wishId: String): Completable
    fun isWishSeen(wishId: String): Single<Boolean>
    fun incrementSeenCountRemotely(wishId: String): Completable
    fun incrementSeenCountLocally(wishId: String)
    fun getMyListsWishesIds(): Single<ArrayList<String>>
    fun getMyFavoritesWishesIds(): Single<ArrayList<String>>
    fun getProfileWishesPageFromCorrespondingPublicWishes(wishIdsArray: ArrayList<String>): Single<ArrayList<Wish>>
    fun getWishesByTitle(
        searchQuery: String,
        lastVisibleWishesDocumentSnapshot: DocumentSnapshot?
    ): Single<Pair<ArrayList<Wish>, DocumentSnapshot?>>
}
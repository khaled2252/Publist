package com.publist.features.myfavorites.data

import com.google.firebase.firestore.Query
import com.publist.core.common.data.models.wish.Wish
import io.reactivex.Completable
import io.reactivex.Single

interface MyFavoritesRepositoryInterface {
    fun getUserFavoriteWishesQuery(): Query
    fun getUserFavoriteWishes(): Single<ArrayList<Wish>>
    fun addToMyFavoritesRemotely(wish: Wish): Completable
    fun deleteFromFavoritesRemotely(wishId: String): Completable
}
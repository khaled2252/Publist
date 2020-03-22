package co.publist.features.myfavorites.data

import co.publist.core.common.data.models.wish.Wish
import io.reactivex.Completable
import io.reactivex.Single

interface MyFavoritesRepositoryInterface {
    fun getUserFavoriteWishes(): Single<ArrayList<Wish>>
    fun addToMyFavoritesRemotely(wish : Wish): Completable
    fun deleteFromFavoritesRemotely(wishId: String) : Completable
}
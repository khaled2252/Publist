package co.publist.features.mylists.data

import co.publist.core.common.data.models.wish.Wish
import io.reactivex.Completable
import io.reactivex.Single

interface MyListsRepositoryInterface {
    fun fetchMyLists() : Single<ArrayList<Wish>>
    fun getLocalMyLists(): Single<ArrayList<Wish>>
    fun addToMyListsRemotely(wish : Wish): Completable
    fun addToMyListsLocally(wish : Wish)
    fun addMyListsLocally(list: ArrayList<Wish>)
}
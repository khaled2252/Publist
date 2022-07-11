package com.publist.features.mylists.data

import com.google.firebase.firestore.Query
import com.publist.core.common.data.models.wish.Wish
import io.reactivex.Completable
import io.reactivex.Single

interface MyListsRepositoryInterface {
    fun getUserListWishesQuery(): Query
    fun getMyLists(): Single<ArrayList<Wish>>
    fun getLocalMyLists(): Single<ArrayList<Wish>>
    fun addToMyListsRemotely(wish: Wish): Completable
    fun addToMyListsLocally(wish: Wish)
    fun addMyListsLocally(list: ArrayList<Wish>)
}
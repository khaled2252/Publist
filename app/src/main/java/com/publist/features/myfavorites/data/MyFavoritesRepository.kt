package com.publist.features.myfavorites.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.publist.core.common.data.local.LocalDataSource
import com.publist.core.common.data.models.Mapper
import com.publist.core.common.data.models.wish.Wish
import com.publist.core.utils.Utils.Constants.DATE_FIELD
import com.publist.core.utils.Utils.Constants.MY_FAVORITES_COLLECTION_PATH
import com.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class MyFavoritesRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val mFirebaseFirestore: FirebaseFirestore
) : MyFavoritesRepositoryInterface {

    override fun getUserFavoriteWishesQuery(): Query {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
            .document(userId!!)
            .collection(MY_FAVORITES_COLLECTION_PATH)
            .orderBy(
                DATE_FIELD,
                Query.Direction.DESCENDING
            )
    }

    override fun getUserFavoriteWishes(): Single<ArrayList<Wish>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id

        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_FAVORITES_COLLECTION_PATH)
                .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    singleEmitter.onSuccess(Mapper.mapToWishAdapterItemArrayList(querySnapshot))
                }
        }
    }

    override fun addToMyFavoritesRemotely(wish: Wish): Completable {
        return Completable.create { completableEmitter ->
            val userId = localDataSource.getSharedPreferences().getUser()?.id
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_FAVORITES_COLLECTION_PATH)
                .document(wish.wishId!!)
                .set(wish)
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
        }
    }

    override fun deleteFromFavoritesRemotely(wishId: String): Completable {
        return Completable.create { completableEmitter ->
            val userId = localDataSource.getSharedPreferences().getUser()?.id
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_FAVORITES_COLLECTION_PATH)
                .document(wishId)
                .delete()
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }
}
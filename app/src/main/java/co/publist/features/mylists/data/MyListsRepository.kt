package com.publist.features.mylists.data

import android.os.AsyncTask
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.publist.core.common.data.local.LocalDataSource
import com.publist.core.common.data.models.Mapper
import com.publist.core.common.data.models.wish.Wish
import com.publist.core.utils.Utils
import com.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import com.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class MyListsRepository @Inject constructor(
    private val mFirebaseFirestore: FirebaseFirestore,
    private val localDataSource: LocalDataSource

) : MyListsRepositoryInterface {
    override fun getUserListWishesQuery(): Query {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
            .document(userId!!)
            .collection(MY_LISTS_COLLECTION_PATH)
            .orderBy(
                Utils.Constants.DATE_FIELD,
                Query.Direction.DESCENDING
            )
    }

    override fun getMyLists(): Single<ArrayList<Wish>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    singleEmitter.onSuccess(Mapper.mapToWishAdapterItemArrayList(querySnapshot))
                }
        }
    }

    override fun getLocalMyLists(): Single<ArrayList<Wish>> {
        return localDataSource.getPublistDataBase().getMyLists().flatMap {
            Single.just(Mapper.mapToWishAdapterItemArrayList(it))
        }
    }

    override fun addToMyListsRemotely(wish: Wish): Completable {
        return Completable.create { completableEmitter ->
            val userId = localDataSource.getSharedPreferences().getUser()?.id
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .add(wish)
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
        }

    }

    override fun addToMyListsLocally(wish: Wish) {
        AsyncTask.execute {
            localDataSource.getPublistDataBase()
                .insertIntoMyLists(Mapper.mapToListDbEntity(wish))
        }
    }

    override fun addMyListsLocally(list: ArrayList<Wish>) {
        AsyncTask.execute {
            localDataSource.getPublistDataBase()
                .addMyLists(Mapper.mapToMyListDbEntityList(list))
        }
    }

}
package co.publist.features.profile.mylists.data

import android.os.AsyncTask
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class MyListsRepository @Inject constructor(
    private val mFirebaseAuth: FirebaseAuth,
    private val mFirebaseFirestore: FirebaseFirestore,
    private val localDataSource: LocalDataSource

) : MyListsRepositoryInterface{
    override fun fetchMyLists(): Single<ArrayList<Wish>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .get()
                .addOnSuccessListener {querySnapshot ->
                    singleEmitter.onSuccess(Mapper.mapToWishArrayList(querySnapshot))
                }
        }    }

    override fun getLocalMyLists(): Single<ArrayList<Wish>> {
        return localDataSource.getPublistDataBase().getMyLists().flatMap {
            Single.just(Mapper.mapToWishArrayList(it))
        }
    }

    override fun addToMyListsRemotely(wish : Wish): Completable {
        return Completable.create { completableEmitter ->
            val userId = localDataSource.getSharedPreferences().getUser()?.id
            val collectionReference = mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)

            collectionReference.get()

                .addOnSuccessListener { documents ->
                    //todo
                   }

                }

        }


    override fun addToMyListsLocally(wish : Wish) {
        AsyncTask.execute {
            localDataSource.getPublistDataBase()
                .insertIntoMyLists(Mapper.mapToListDbEntity(wish))
        }    }

}
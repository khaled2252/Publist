package co.publist.features.categories.data

import co.publist.core.data.local.LocalDataSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class CategoriesRepository @Inject constructor(
    var mFirebaseFirestore: FirebaseFirestore,
    private val localDataSource: LocalDataSource
) :
    CategoriesRepositoryInterface {
    override fun getCategoriesQuery(): CollectionReference {
        return mFirebaseFirestore.collection("categories")
    }

    override fun getUserCategories(): Single<ArrayList<String>> {
        return Single.create { singleEmitter ->
            val user = localDataSource.getSharedPreferences().getUser()
            if (user?.myCategories == null) {
                val userCategories = ArrayList<String>()
                mFirebaseFirestore.collection("users")
                    .document(user?.id!!)
                    .collection("myCategories").get()
                    .addOnFailureListener { exception ->
                        singleEmitter.onError(exception)
                    }.addOnSuccessListener { documents ->
                        for (document in documents) {
                            userCategories.add(document.id)
                        }
                        localDataSource.getSharedPreferences().updateUserCategories(userCategories)
                        singleEmitter.onSuccess(userCategories)
                    }
            }
            else
                singleEmitter.onSuccess(user.myCategories!!)
        }
    }

    override fun updateUserCategories(selectedCategoriesList : ArrayList<String>) : Completable {
        return Completable.create {completableEmitter ->
            localDataSource.getSharedPreferences().updateUserCategories(selectedCategoriesList)

            val docId = localDataSource.getSharedPreferences().getUser()?.id
            val batch: WriteBatch = mFirebaseFirestore.batch()
            val collectionReference = mFirebaseFirestore
                .collection("users")
                .document(docId!!)
                .collection("myCategories")

            collectionReference.get().addOnSuccessListener {documents ->
                for (document in documents) {
                    collectionReference.document(document.id).delete()
                }

                for (categoryId in selectedCategoriesList) {
                    batch.set(collectionReference.document(categoryId), emptyMap<String , String>())
                }

                batch.commit()
                    .addOnFailureListener { exception ->
                        completableEmitter.onError(exception)
                    }.addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
            }

        }

    }
}
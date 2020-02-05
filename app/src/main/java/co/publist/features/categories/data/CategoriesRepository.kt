package co.publist.features.categories.data

import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.utils.Utils.Constants.CATEGORIES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.MY_CATEGORIES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
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
        return mFirebaseFirestore.collection(CATEGORIES_COLLECTION_PATH)
    }

    override fun getUserCategories(): Single<ArrayList<String>> {
        return Single.create { singleEmitter ->
            val userCategories = ArrayList<String>()
            val user = localDataSource.getSharedPreferences().getUser()
            if (user !=null && user.myCategories == null) {
                mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                    .document(user.id!!)
                    .collection(MY_CATEGORIES_COLLECTION_PATH).get()
                    .addOnFailureListener { exception ->
                        singleEmitter.onError(exception)
                    }.addOnSuccessListener { documents ->
                        for (document in documents) {
                            userCategories.add(document.id)
                        }

                        localDataSource.getSharedPreferences()
                                .updateUserCategories(userCategories)

                        singleEmitter.onSuccess(userCategories) //Registered user with a new list
                    }
            } else
                if(user?.myCategories !=null)
                singleEmitter.onSuccess(user.myCategories!!) //Registered user with already saved list
            else
                    singleEmitter.onSuccess(userCategories) //Guest user (empty list emitted)
        }
    }

    override fun updateUserCategories(selectedCategoriesList: ArrayList<String>): Completable {
        return Completable.create { completableEmitter ->
            localDataSource.getSharedPreferences().updateUserCategories(selectedCategoriesList)

            val docId = localDataSource.getSharedPreferences().getUser()?.id
            val batch: WriteBatch = mFirebaseFirestore.batch()
            val collectionReference = mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(docId!!)
                .collection(MY_CATEGORIES_COLLECTION_PATH)

            collectionReference.get()

                .addOnSuccessListener { documents ->
                    //will create myCategories if is not created

                    //Delete existing saved categories
                    for (document in documents) {
                        collectionReference.document(document.id).delete()
                    }

                    //Add new saved categories
                    for (categoryId in selectedCategoriesList) {
                        batch.set(
                            collectionReference.document(categoryId),
                            emptyMap<String, String>()
                        )
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

    override fun saveGuestCategories(selectedCategoriesList: ArrayList<String>) {
        localDataSource.getSharedPreferences().saveTemporaryCategories(selectedCategoriesList)
    }
}
package co.publist.features.categories.data

import android.os.AsyncTask
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.Category
import co.publist.core.utils.Extensions.Constants.CATEGORIES_COLLECTION_PATH
import co.publist.core.utils.Extensions.Constants.MY_CATEGORIES_COLLECTION_PATH
import co.publist.core.utils.Extensions.Constants.USERS_COLLECTION_PATH
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
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

    override fun fetchUserSelectedCategories(userId: String): Single<ArrayList<Category>> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId)
                .collection(MY_CATEGORIES_COLLECTION_PATH).get()
                .addOnFailureListener { exception ->
                    singleEmitter.onError(exception)
                }.addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        mFirebaseFirestore.collection(CATEGORIES_COLLECTION_PATH)
                            .whereIn(
                                FieldPath.documentId(),
                                Mapper.mapToCategoryArrayList(documents).map { it.id })
                            .get()
                            .addOnFailureListener { exception ->
                                singleEmitter.onError(exception)
                            }.addOnSuccessListener { querySnapshot ->
                                singleEmitter.onSuccess(Mapper.mapToCategoryArrayList(querySnapshot))
                            }
                    }
                    else
                        singleEmitter.onSuccess(arrayListOf()) //Emit empty arrayList if user didn't save any previous categories
                }
        }
    }

    override fun getLocalSelectedCategories(): Single<ArrayList<Category>> {
        return localDataSource.getPublistDataBase().getCategories().flatMap {
            Single.just(Mapper.mapToCategoryArrayList(it))
        }
    }

    override fun updateRemoteSelectedCategories(selectedCategoriesList: ArrayList<Category>): Completable {
        return Completable.create { completableEmitter ->
            val docId = localDataSource.getSharedPreferences().getUser()?.id
            val batch: WriteBatch = mFirebaseFirestore.batch()
            val collectionReference = mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(docId!!)
                .collection(MY_CATEGORIES_COLLECTION_PATH)

            collectionReference.get()

                .addOnSuccessListener { documents ->
                    //will create myCategories in firestore if is not created
                    for (document in documents) {
                        if (!selectedCategoriesList.contains(document.toObject(Category::class.java)))
                            collectionReference.document(document.id).delete()  //Delete deselected categories

                        else
                            selectedCategoriesList.remove(document.toObject(Category::class.java)) //Delete not changed categories from list to be submitted
                    }

                    //Add new saved categories
                    for (category in selectedCategoriesList) {
                        batch.set(
                            collectionReference.document(category.id!!),
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

    override fun clearLocalSelectedCategories() {
        AsyncTask.execute {
            localDataSource.getPublistDataBase().deleteCategories()
        }
    }

    override fun updateLocalSelectedCategories(selectedCategoriesList: ArrayList<Category>) {
        AsyncTask.execute {
            localDataSource.getPublistDataBase()
                .updateCategories(Mapper.mapToCategoryDbEntityList(selectedCategoriesList))
        }
    }

    override fun getCategoryFromId(categoryId: String): Single<Category> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(CATEGORIES_COLLECTION_PATH)
                .document(categoryId).get()
                .addOnFailureListener { exception ->
                    singleEmitter.onError(exception)
                }.addOnSuccessListener { documentSnapshot ->
                    val category = documentSnapshot.toObject(Category::class.java)
                    category?.id = categoryId
                    singleEmitter.onSuccess(category!!)
                }
        }
    }

}

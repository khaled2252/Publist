package co.publist.features.categories.data

import android.os.AsyncTask
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.utils.Utils.Constants.CATEGORIES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.MY_CATEGORIES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import com.google.firebase.firestore.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CategoriesRepository @Inject constructor(
    var mFirebaseFirestore: FirebaseFirestore,
    private val localDataSource: LocalDataSource
) :
    CategoriesRepositoryInterface {
    override fun getCategoriesQuery(): CollectionReference {
        return mFirebaseFirestore.collection(CATEGORIES_COLLECTION_PATH)
    }

    override fun fetchAllCategories(): Single<ArrayList<Category>> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(CATEGORIES_COLLECTION_PATH)
                .get(Source.SERVER)
                .addOnSuccessListener { querySnapshot ->
                    val categories = ArrayList<Category>()
                    for (document in querySnapshot) {
                        val category = document.toObject(Category::class.java)
                        category.id = document.id
                        categories.add(category)
                    }
                    singleEmitter.onSuccess(categories)
                }
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }
        }
    }

    override fun fetchUserSelectedCategories(): Single<ArrayList<Category>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
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
                    } else
                        singleEmitter.onSuccess(arrayListOf()) //Emit empty arrayList if user didn't save any previous categories
                }
        }
    }

    override fun getLocalSelectedCategories(): Single<ArrayList<CategoryAdapterItem>> {
        return localDataSource.getPublistDataBase().getSelectedCategories().flatMap {
            Single.just(Mapper.mapToCategoryAdapterItemList(it))
        }.subscribeOn(Schedulers.io())
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
                            collectionReference.document(document.id)
                                .delete()  //Delete deselected categories

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
            localDataSource.getPublistDataBase().deleteSelectedCategories()
        }
    }

    override fun updateLocalSelectedCategories(selectedCategoriesList: ArrayList<Category>) {
        AsyncTask.execute {
            localDataSource.getPublistDataBase()
                .updateSelectedCategories(Mapper.mapToCategoryDbEntityList(selectedCategoriesList))
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

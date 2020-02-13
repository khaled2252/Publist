package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import io.reactivex.Completable
import io.reactivex.Single

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery(): CollectionReference
    fun fetchSelectedCategories(userId: String): Single<ArrayList<String>>
    fun getLocalSelectedCategories(): Single<ArrayList<String>>
    fun updateRemoteSelectedCategories(selectedCategoriesList: ArrayList<String>): Completable
    fun updateLocalSelectedCategories(selectedCategoriesList: ArrayList<String>)
    fun clearLocalSelectedCategories()
}

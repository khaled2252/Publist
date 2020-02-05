package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import io.reactivex.Completable
import io.reactivex.Single

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery() : CollectionReference
    fun getUserCategories(): Single<ArrayList<String>>
    fun updateUserCategories(selectedCategoriesList : ArrayList<String>) : Completable
    fun saveGuestCategories(selectedCategoriesList: ArrayList<String>)
}

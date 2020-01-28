package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import io.reactivex.Single

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery() : CollectionReference
    fun getCategories(selectedCategories : ArrayList<String>?) : Single<ArrayList<String>>
}

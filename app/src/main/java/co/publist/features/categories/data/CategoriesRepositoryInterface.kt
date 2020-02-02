package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import io.reactivex.Single

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery() : CollectionReference
    fun getUserCategories(userDocId : String?) : Single<ArrayList<String>>
}

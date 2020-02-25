package co.publist.features.categories.data

import co.publist.core.common.data.models.category.Category
import com.google.firebase.firestore.CollectionReference
import io.reactivex.Completable
import io.reactivex.Single

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery(): CollectionReference
    fun fetchUserSelectedCategories(userId: String): Single<ArrayList<Category>>
    fun getLocalSelectedCategories(): Single<ArrayList<Category>>
    fun updateRemoteSelectedCategories(selectedCategoriesList: ArrayList<Category>): Completable
    fun updateLocalSelectedCategories(selectedCategoriesList: ArrayList<Category>)
    fun clearLocalSelectedCategories()
    fun getCategoryFromId(categoryId : String) : Single<Category>
}

package com.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import com.publist.core.common.data.models.category.Category
import com.publist.core.common.data.models.category.CategoryAdapterItem
import io.reactivex.Completable
import io.reactivex.Single

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery(): CollectionReference
    fun fetchAllCategories(): Single<ArrayList<Category>>
    fun fetchUserSelectedCategories(): Single<ArrayList<Category>>
    fun getLocalSelectedCategories(): Single<ArrayList<CategoryAdapterItem>>
    fun updateRemoteSelectedCategories(selectedCategoriesList: ArrayList<Category>): Completable
    fun updateLocalSelectedCategories(selectedCategoriesList: ArrayList<Category>)
    fun clearLocalSelectedCategories()
    fun getCategoryFromId(categoryId: String): Single<Category>
}

package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference

interface CategoriesRepositoryInterface {
    fun getCategoriesQuery() : CollectionReference
}

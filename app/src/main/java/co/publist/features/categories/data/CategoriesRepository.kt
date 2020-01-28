package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class CategoriesRepository @Inject constructor(var mFirebaseFirestore: FirebaseFirestore) :
    CategoriesRepositoryInterface {
    override fun getCategoriesQuery(): CollectionReference {
        return mFirebaseFirestore.collection("categories")
    }
}
package co.publist.features.categories.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Single
import javax.inject.Inject

class CategoriesRepository @Inject constructor(var mFirebaseFirestore: FirebaseFirestore) :
    CategoriesRepositoryInterface {
    override fun getCategoriesQuery(): CollectionReference {
        return mFirebaseFirestore.collection("categories")
    }

    override fun getCategories(selectedCategories: ArrayList<String>?): Single<ArrayList<String>> {
        return Single.create {
            if (selectedCategories != null) {
                it.onSuccess(selectedCategories)
            }
        }

    }
}
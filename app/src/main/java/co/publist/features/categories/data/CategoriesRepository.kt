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

    override fun getUserCategories(userDocId: String?): Single<ArrayList<String>> {
        return Single.create { singleEmitter ->
            if (userDocId == null) {
                singleEmitter.onSuccess(ArrayList())
            } else {
                val userCategories = ArrayList<String>()
                mFirebaseFirestore.collection("users")
                    .document(userDocId)
                    .collection("myCategories").get()
                    .addOnFailureListener { exception ->
                        singleEmitter.onError(exception)
                    }.addOnSuccessListener { documents ->
                        for (document in documents) {
                            userCategories.add(document.id)
                        }
                        singleEmitter.onSuccess(userCategories)
                    }
            }
        }
    }
}
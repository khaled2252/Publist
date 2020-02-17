package co.publist.features.wishes.data

import co.publist.core.utils.Utils.Constants.CATEGORY_ID_FIELD
import co.publist.core.utils.Utils.Constants.DATE_FIELD
import co.publist.core.utils.Utils.Constants.WISHES_COLLECTION_PATH
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.Completable
import javax.inject.Inject

class WishesRepository @Inject constructor(
    var mFirebaseFirestore: FirebaseFirestore
) : WishesRepositoryInterface {
    override fun getAllWishesQuery(): Query {
        return mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)//Latest first
    }

    override fun getFilteredWishesQuery(categoryList: ArrayList<String>): Query {
        return mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
            .whereArrayContainsAny(CATEGORY_ID_FIELD, categoryList)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
    }

    override fun createWish(wish: Wish): Completable {

        return Completable.create {completableEmitter ->
            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH).add(wish).addOnFailureListener {
                completableEmitter.onError(it)
            }.addOnSuccessListener {
                completableEmitter.onComplete()
            }
        }
    }
}
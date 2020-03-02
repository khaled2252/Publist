package co.publist.core.common.data.repositories.wish

import android.net.Uri
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.utils.Utils.Constants.CATEGORY_ID_FIELD
import co.publist.core.utils.Utils.Constants.DATE_FIELD
import co.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.WISHES_COLLECTION_PATH
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class WishesRepository @Inject constructor(
    var mFirebaseFirestore: FirebaseFirestore,
    var mFirebaseStorage: FirebaseStorage
) : WishesRepositoryInterface {
    override fun getAllWishesQuery(): Query {
        return mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)//Latest first
    }

    override fun getFilteredWishesQuery(categoryList: ArrayList<String?>): Query {
        return mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
            .whereArrayContainsAny(CATEGORY_ID_FIELD, categoryList)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
    }

    override fun getUserListWishesQuery(userId : String): Query {
        return mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
            .document(userId)
            .collection(MY_LISTS_COLLECTION_PATH)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
    }

    override fun createWish(wish: Wish): Completable {

        return Completable.create { completableEmitter ->
            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH).add(wish)
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }.addOnSuccessListener {
                    mFirebaseFirestore.collection(WISHES_COLLECTION_PATH).document(it.id)
                        .update("wishId", it.id)
                        .addOnSuccessListener {
                            completableEmitter.onComplete()
                        }
                        .addOnFailureListener {
                        }
                }
        }
    }

    override fun uploadImage(imageUri: String): Single<String> {
        return Single.create { completableEmitter ->
            val reference =
                mFirebaseStorage.reference.child("WishListCoverPhoto/" + UUID.randomUUID().toString().toUpperCase()+".jpeg")
            var metadata = StorageMetadata.Builder()
                .setContentType("application/octet-stream")
                .build()
            val uploadTask = reference.putFile(Uri.parse(imageUri),metadata)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                return@Continuation reference.downloadUrl
            }).addOnCompleteListener { task ->
                val downloadUri = task.result
                completableEmitter.onSuccess(downloadUri.toString())
            }.addOnFailureListener {
                completableEmitter.onError(it)
            }

        }
    }
}

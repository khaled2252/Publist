package co.publist.core.common.data.repositories.wish

import android.net.Uri
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.utils.Utils.Constants.CATEGORY_ID_FIELD
import co.publist.core.utils.Utils.Constants.COMPLETED_USERS_IDS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.COMPLETE_COUNT_FIELD
import co.publist.core.utils.Utils.Constants.DATE_FIELD
import co.publist.core.utils.Utils.Constants.ID_FIELD
import co.publist.core.utils.Utils.Constants.IS_DONE_FIELD
import co.publist.core.utils.Utils.Constants.ITEMS_FIELD
import co.publist.core.utils.Utils.Constants.ITEMS_ID_SUB_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.LIKED_USERS_IDS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.LIKE_COUNT_FIELD
import co.publist.core.utils.Utils.Constants.MY_FAVORITES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.MY_LISTS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.TOP_COMPLETED_USER_IDS_FIELD
import co.publist.core.utils.Utils.Constants.TOP_VIEWED_USER_IDS_FIELD
import co.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.USER_ID_FIELD
import co.publist.core.utils.Utils.Constants.USER_VIEWED_ITEMS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.WISHES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.WISH_ID_FIELD
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class WishesRepository @Inject constructor(
    var mFirebaseFirestore: FirebaseFirestore,
    var mFirebaseStorage: FirebaseStorage,
    var localDataSource: LocalDataSource
) : WishesRepositoryInterface {
    private val userId = localDataSource.getSharedPreferences().getUser()?.id

    override fun getAllWishesQuery(): Query {
        return mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)//Latest first
    }

    override fun getFilteredWishesQuery(categoryList: ArrayList<String?>): Query {
        return mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
            .whereArrayContainsAny(CATEGORY_ID_FIELD, categoryList)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
    }

    override fun getUserListWishesQuery(): Query {
        return mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
            .document(userId!!)
            .collection(MY_LISTS_COLLECTION_PATH)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
    }

    override fun getUserFavoriteWishesQuery(): Query {
        return mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
            .document(userId!!)
            .collection(MY_FAVORITES_COLLECTION_PATH)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
    }

    override fun getSpecificWish(wishId: String): Single<Wish> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                .document(wishId)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { wish ->
                    singleEmitter.onSuccess(wish.toObject(Wish::class.java)!!)
                }
        }
    }


    override fun getAllWishes(): Single<ArrayList<Wish>> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    singleEmitter.onSuccess(Mapper.mapToWishAdapterItemArrayList(querySnapshot))
                }
        }
    }

    override fun getMyListWishes(): Single<ArrayList<Wish>> {
        return localDataSource.getPublistDataBase().getMyLists()
            .flatMap {
                Single.just(Mapper.mapToWishAdapterItemArrayList(it))
            }
    }

    private fun addWishToWishes(wish: Wish): Single<Wish> {

        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH).add(wish)
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { documentReference ->
                    mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                        .document(documentReference.id)
                        .update(WISH_ID_FIELD, documentReference.id)
                        .addOnSuccessListener {
                            wish.wishId = documentReference.id
                            singleEmitter.onSuccess(wish)
                        }
                        .addOnFailureListener { error ->
                            singleEmitter.onError(error)
                        }
                }
        }
    }

    private fun addWishToMyLists(wish: Wish): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .document(wish.wishId!!)
                .set(wish)
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }.addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }

    override fun createWish(wish: Wish): Completable {
        return addWishToWishes(wish).flatMapCompletable {
            addWishToMyLists(it)
        }

    }

    override fun updateWish(wish: Wish): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .document(wish.wishId!!)
                .set(wish)
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }.addOnSuccessListener {
                    mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                        .document(wish.wishId!!)
                        .set(wish)
                        .addOnSuccessListener {
                            completableEmitter.onComplete()
                        }
                        .addOnFailureListener { error ->
                            completableEmitter.onError(error)
                        }
                }
        }
    }

    override fun uploadImage(imageUri: String): Single<Pair<String, String>> {
        return Single.create { completableEmitter ->
            val photoName = UUID.randomUUID().toString().toUpperCase(Locale.getDefault()) + ".jpeg"
            val reference =
                mFirebaseStorage.reference.child("WishListCoverPhoto/$photoName")
            val metadata = StorageMetadata.Builder()
                .setContentType("application/octet-stream")
                .build()
            val uploadTask = reference.putFile(Uri.parse(imageUri), metadata)
            uploadTask.continueWithTask {
                reference.downloadUrl
            }.addOnCompleteListener { task ->
                val downloadUri = task.result
                completableEmitter.onSuccess(Pair(downloadUri.toString(), photoName))
            }.addOnFailureListener {
                completableEmitter.onError(it)
            }

        }
    }

    override fun deleteWishFromMyLists(selectedWish: Wish): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .document(selectedWish.wishId!!)
                .delete()
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }

    override fun deleteWishFromWishes(selectedWish: Wish): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(selectedWish.wishId!!)
                .delete()
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }

    override fun getDoneItemsInMyLists(): Single<ArrayList<String>> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    val wishList = Mapper.mapToWishAdapterItemArrayList(querySnapshot)
                    val doneItems = arrayListOf<String>()
                    for (wish in wishList) {
                        for (item in wish.items!!) {
                            if (item.value.done!!)
                                doneItems.add(item.key)
                        }
                    }
                    singleEmitter.onSuccess(doneItems)
                }
        }
    }

    override fun getDoneItemsInMyFavorites(): Single<ArrayList<String>> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_FAVORITES_COLLECTION_PATH)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    val wishList = Mapper.mapToWishAdapterItemArrayList(querySnapshot)
                    val doneItems = arrayListOf<String>()
                    for (wish in wishList) {
                        for (item in wish.items!!) {
                            if (item.value.done!!)
                                doneItems.add(item.key)
                        }
                    }
                    singleEmitter.onSuccess(doneItems)
                }
        }
    }

    override fun checkItemDoneInProfile(
        itemId: String,
        wishId: String,
        collectionToBeEdited: String,
        isDone: Boolean
    ): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(collectionToBeEdited)
                .document(wishId)
                .update("$ITEMS_FIELD.$itemId.$IS_DONE_FIELD", isDone)
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }

    override fun incrementCompleteCountInWishes(
        itemId: String,
        wishId: String,
        isDone: Boolean
    ): Single<Int> {
        return Single.create { singleEmitter ->
            val wishRef =
                mFirebaseFirestore
                    .collection(WISHES_COLLECTION_PATH)
                    .document(wishId)
            val incrementAmount = if (isDone) 1 else -1
            wishRef
                .update(
                    "$ITEMS_FIELD.$itemId.$COMPLETE_COUNT_FIELD",
                    FieldValue.increment(incrementAmount.toDouble())
                )
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }
                .addOnSuccessListener {
                    wishRef
                        .get()
                        .addOnSuccessListener {
                            singleEmitter.onSuccess(
                                it.get("$ITEMS_FIELD.$itemId.$COMPLETE_COUNT_FIELD").toString()
                                    .toDouble().toInt()
                            )
                        }
                        .addOnFailureListener {
                            singleEmitter.onError(it)
                        }

                }
        }
    }

    override fun addUserIdInTopCompletedUsersIdSubCollection(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable {
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(wishId)
                .collection(ITEMS_ID_SUB_COLLECTION_PATH)
                .document(itemId)
                .collection(COMPLETED_USERS_IDS_COLLECTION_PATH)

            if (isAdding)
                ref.document(userId!!)
                    .set(hashMapOf(USER_ID_FIELD to userId))
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
            else
                ref.document(userId!!)
                    .delete()
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
        }
    }

    override fun addUserIdInTopCompletedUsersIdField(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable {
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(wishId)

            if (isAdding)
                ref.update(
                    "$ITEMS_FIELD.$itemId.$TOP_COMPLETED_USER_IDS_FIELD",
                    FieldValue.arrayUnion(userId)
                )
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
            else
                ref.update(
                    "$ITEMS_FIELD.$itemId.$TOP_COMPLETED_USER_IDS_FIELD",
                    FieldValue.arrayRemove(userId)
                )
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
        }
    }

    override fun decrementCompleteCountInDoneItems(
        wishId: String,
        doneItems: ArrayList<String>
    ): Completable {
        return Completable.create { completableEmitter ->
            val batch: WriteBatch = mFirebaseFirestore.batch()
            val wishRef =
                mFirebaseFirestore
                    .collection(WISHES_COLLECTION_PATH)
                    .document(wishId)

            for (doneItem in doneItems)
                batch.update(
                    wishRef,
                    "$ITEMS_FIELD.$doneItem.$COMPLETE_COUNT_FIELD",
                    FieldValue.increment(-1)
                )
            batch.commit()
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }

    override fun removeUserIdFromTopCompletedItems(
        doneItems: ArrayList<String>,
        wishId: String
    ): Completable {
        return Completable.create { completableEmitter ->
            val batch: WriteBatch = mFirebaseFirestore.batch()
            val wishRef =
                mFirebaseFirestore
                    .collection(WISHES_COLLECTION_PATH)
                    .document(wishId)

            for (doneItem in doneItems)
                batch.update(
                    wishRef,
                    "$ITEMS_FIELD.$doneItem.$TOP_COMPLETED_USER_IDS_FIELD",
                    FieldValue.arrayRemove(userId)
                )

            for (doneItem in doneItems)
                batch.delete(
                    wishRef
                        .collection(ITEMS_ID_SUB_COLLECTION_PATH)
                        .document(doneItem)
                        .collection(COMPLETED_USERS_IDS_COLLECTION_PATH)
                        .document(userId!!)
                )

            batch.commit()
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
        }
    }

    override fun addItemToUserViewedItems(itemId: String, isLiked: Boolean): Completable {
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(USER_VIEWED_ITEMS_COLLECTION_PATH)
                .document(itemId)
            if (isLiked)
                ref
                    .set(hashMapOf(ID_FIELD to itemId))
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
            else
                ref
                    .delete()
                    .addOnFailureListener {
                    completableEmitter.onError(it)
                }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
        }
    }


    override fun incrementViewedCountInWishes(
        itemId: String,
        wishId: String,
        isLiked: Boolean
    ): Single<Int> {
        return Single.create { singleEmitter ->
            val wishRef =
                mFirebaseFirestore
                    .collection(WISHES_COLLECTION_PATH)
                    .document(wishId)
            val incrementAmount = if (isLiked) 1 else -1
            wishRef
                .update(
                    "$ITEMS_FIELD.$itemId.$LIKE_COUNT_FIELD",
                    FieldValue.increment(incrementAmount.toDouble())
                )
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }
                .addOnSuccessListener {
                    wishRef
                        .get()
                        .addOnSuccessListener {
                            singleEmitter.onSuccess(
                                it.get("$ITEMS_FIELD.$itemId.$LIKE_COUNT_FIELD").toString()
                                    .toDouble().toInt()
                            )
                        }
                        .addOnFailureListener {
                            singleEmitter.onError(it)
                        }

                }
        }
    }

    override fun addUserIdInTopViewedUsersIdSubCollection(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable {
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(wishId)
                .collection(ITEMS_ID_SUB_COLLECTION_PATH)
                .document(itemId)
                .collection(LIKED_USERS_IDS_COLLECTION_PATH)

            if (isAdding)
                ref.document(userId!!)
                    .set(hashMapOf(USER_ID_FIELD to userId))
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
            else
                ref.document(userId!!)
                    .delete()
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
        }
    }

    override fun addUserIdInTopViewedUsersIdField(
        itemId: String,
        wishId: String,
        isAdding: Boolean
    ): Completable {
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(wishId)

            if (isAdding)
                ref.update(
                    "$ITEMS_FIELD.$itemId.$TOP_VIEWED_USER_IDS_FIELD",
                    FieldValue.arrayUnion(userId)
                )
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
            else
                ref.update(
                    "$ITEMS_FIELD.$itemId.$TOP_VIEWED_USER_IDS_FIELD",
                    FieldValue.arrayRemove(userId)
                )
                    .addOnFailureListener {
                        completableEmitter.onError(it)
                    }
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
        }
    }

    override fun getUserLikedItems(): Single<ArrayList<String>> {
        val viewedItemsArrayList = arrayListOf<String>()
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(USER_VIEWED_ITEMS_COLLECTION_PATH)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents)
                        viewedItemsArrayList.add(document.id)
                    singleEmitter.onSuccess(viewedItemsArrayList)
                }
        }
    }

}

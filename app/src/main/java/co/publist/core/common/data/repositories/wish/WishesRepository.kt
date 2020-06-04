package co.publist.core.common.data.repositories.wish

import android.net.Uri
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.utils.Utils.Constants.ALGOLIA_DATABASE_INDEX
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
import co.publist.core.utils.Utils.Constants.ORGANIC_SEEN_FOR_WISH_CLOUD_FUNCTION
import co.publist.core.utils.Utils.Constants.SEEN_FOR_WISH_CLOUD_FUNCTION
import co.publist.core.utils.Utils.Constants.TOP_COMPLETED_USER_IDS_FIELD
import co.publist.core.utils.Utils.Constants.TOP_VIEWED_USER_IDS_FIELD
import co.publist.core.utils.Utils.Constants.USERS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.USER_DOC_ID_FIELD
import co.publist.core.utils.Utils.Constants.USER_VIEWED_ITEMS_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.WISHES_COLLECTION_PATH
import co.publist.core.utils.Utils.Constants.WISHES_NUM_PER_PAGE
import co.publist.core.utils.Utils.Constants.WISH_DOC_ID_FIELD
import co.publist.core.utils.Utils.Constants.WISH_ID_FIELD
import com.algolia.search.saas.Client
import com.algolia.search.saas.CompletionHandler
import com.google.firebase.firestore.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class WishesRepository @Inject constructor(
    var mFirebaseFirestore: FirebaseFirestore,
    var mFirebaseStorage: FirebaseStorage,
    var mFirebaseFunctions: FirebaseFunctions,
    var mAlgoliaClient: Client,
    var localDataSource: LocalDataSource
) : WishesRepositoryInterface {
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


    override fun getAllWishesPage(lastVisibleWishesDocumentSnapshot: DocumentSnapshot?): Single<Pair<ArrayList<Wish>, DocumentSnapshot?>> {
        return Single.create { singleEmitter ->
            var ref =
                mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                    .orderBy(
                        DATE_FIELD,
                        Query.Direction.DESCENDING
                    )
                    .limit(WISHES_NUM_PER_PAGE.toLong())

            if (lastVisibleWishesDocumentSnapshot != null)
                ref = ref.startAfter(lastVisibleWishesDocumentSnapshot)
            ref.get(Source.SERVER)
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    // Get the last visible document
                    val lastVisibleWishesDocument: DocumentSnapshot? =
                        if (querySnapshot.documents.isNotEmpty())
                            querySnapshot.documents[querySnapshot.size() - 1]
                        else
                            null
                    val wishesList = Mapper.mapToWishAdapterItemArrayList(querySnapshot)
                    singleEmitter.onSuccess(Pair(wishesList, lastVisibleWishesDocument))
                }
        }
    }

    override fun getWishesByCategoryPage(
        categoryId: String,
        lastVisibleWishesDocumentSnapshot: DocumentSnapshot?
    ): Single<Pair<ArrayList<Wish>, DocumentSnapshot?>> {
        return Single.create { singleEmitter ->
            var ref = mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                .whereArrayContains(CATEGORY_ID_FIELD, categoryId)
                .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
                .limit(WISHES_NUM_PER_PAGE.toLong())

            if (lastVisibleWishesDocumentSnapshot != null)
                ref = ref.startAfter(lastVisibleWishesDocumentSnapshot)
            ref.get(Source.SERVER)
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    val lastVisibleWishesDocument: DocumentSnapshot? =
                        if (querySnapshot.documents.isNotEmpty())
                            querySnapshot.documents[querySnapshot.size() - 1]
                        else
                            null
                    val wishesList = Mapper.mapToWishAdapterItemArrayList(querySnapshot)
                    singleEmitter.onSuccess(Pair(wishesList, lastVisibleWishesDocument))
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Completable.create { completableEmitter ->
            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                .document(wish.wishId!!)
                .set(wish)
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }.addOnSuccessListener {
                    val userEditedWish =
                        Mapper.mapToEditedWishItem(wish)//Change wishItem in items to EditedWishItem (to set done,liked items)
                    mFirebaseFirestore
                        .collection(USERS_COLLECTION_PATH)
                        .document(userId!!)
                        .collection(MY_LISTS_COLLECTION_PATH)
                        .document(wish.wishId!!)
                        .set(userEditedWish)
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    val doneItems = arrayListOf<String>()
                    for (wishDocument in querySnapshot.documents) {
                        val items = wishDocument[ITEMS_FIELD] as HashMap<*, *>
                        for (item in items) {
                            val itemValue = item.value as HashMap<*, *>
                            if (itemValue[IS_DONE_FIELD] as? Boolean == true)
                                doneItems.add(item.key as String)
                        }
                    }
                    singleEmitter.onSuccess(doneItems)
                }
        }
    }

    override fun getDoneItemsInMyFavorites(): Single<ArrayList<String>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_FAVORITES_COLLECTION_PATH)
                .get()
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { querySnapshot ->
                    val doneItems = arrayListOf<String>()
                    for (wishDocument in querySnapshot.documents) {
                        val items = wishDocument[ITEMS_FIELD] as HashMap<*, *>
                        for (item in items) {
                            val itemValue = item.value as HashMap<*, *>
                            if (itemValue[IS_DONE_FIELD] as? Boolean == true)
                                doneItems.add(item.key as String)
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
            val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(wishId)
                .collection(ITEMS_ID_SUB_COLLECTION_PATH)
                .document(itemId)
                .collection(COMPLETED_USERS_IDS_COLLECTION_PATH)

            if (isAdding)
                ref.document(userId!!)
                    .set(hashMapOf(ID_FIELD to userId))
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Completable.create { completableEmitter ->
            val ref = mFirebaseFirestore
                .collection(WISHES_COLLECTION_PATH)
                .document(wishId)
                .collection(ITEMS_ID_SUB_COLLECTION_PATH)
                .document(itemId)
                .collection(LIKED_USERS_IDS_COLLECTION_PATH)

            if (isAdding)
                ref.document(userId!!)
                    .set(hashMapOf(ID_FIELD to userId))
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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
        val userId = localDataSource.getSharedPreferences().getUser()?.id
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

    override fun incrementOrganicSeen(wishId: String): Completable {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Completable.create { completableEmitter ->
            mFirebaseFunctions.getHttpsCallable(ORGANIC_SEEN_FOR_WISH_CLOUD_FUNCTION)
                .call(hashMapOf(USER_DOC_ID_FIELD to userId, WISH_DOC_ID_FIELD to wishId))
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
        }
    }

    override fun isWishSeen(wishId: String): Single<Boolean> {
        return localDataSource.getPublistDataBase().isWishSeen(wishId)
            .subscribeOn(Schedulers.io())
    }


    override fun incrementSeenCountRemotely(wishId: String): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFunctions.getHttpsCallable(SEEN_FOR_WISH_CLOUD_FUNCTION)
                .call(hashMapOf(WISH_DOC_ID_FIELD to wishId))
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }
                .addOnFailureListener {
                    completableEmitter.onError(it)
                }
        }
    }

    override fun incrementSeenCountLocally(wishId: String) {
        localDataSource.getPublistDataBase().insertSeenWish(wishId)
    }

    override fun getMyListsWishesIds(): Single<ArrayList<String>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_LISTS_COLLECTION_PATH)
                .get(Source.SERVER)
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { myListsQuerySnapshot ->
                    val myListWishes = Mapper.mapToWishArrayList(myListsQuerySnapshot)
                    val myListWishesId = ArrayList(myListWishes.map { it.wishId!! })
                    singleEmitter.onSuccess(myListWishesId)
                }
        }
    }

    override fun getMyFavoritesWishesIds(): Single<ArrayList<String>> {
        val userId = localDataSource.getSharedPreferences().getUser()?.id
        return Single.create { singleEmitter ->
            mFirebaseFirestore.collection(USERS_COLLECTION_PATH)
                .document(userId!!)
                .collection(MY_FAVORITES_COLLECTION_PATH)
                .get(Source.SERVER)
                .addOnFailureListener {
                    singleEmitter.onError(it)
                }.addOnSuccessListener { myFavoritesQuerySnapshot ->
                    val myFavoritesWishes = Mapper.mapToWishArrayList(myFavoritesQuerySnapshot)
                    val myFavoritesWishesId = ArrayList(myFavoritesWishes.map { it.wishId!! })
                    singleEmitter.onSuccess(myFavoritesWishesId)
                }
        }
    }

    override fun getProfileWishesPageFromCorrespondingPublicWishes(wishIdsArray: ArrayList<String>): Single<ArrayList<Wish>> {
        return Single.create { singleEmitter ->
            if (wishIdsArray.isEmpty())
                singleEmitter.onSuccess(arrayListOf()) //Return empty array instead of doing Firestore network call (because whereIn filter requires non empty array)
            else
                mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                    .whereIn(FieldPath.documentId(), wishIdsArray)
                    .get(Source.SERVER)
                    .addOnSuccessListener { querySnapshot ->
                        singleEmitter.onSuccess(Mapper.mapToWishArrayList(querySnapshot))
                    }
                    .addOnFailureListener {
                        singleEmitter.onError(it)
                    }
        }
    }

    override fun getWishesByTitle(
        searchQuery: String,
        lastVisibleWishesDocumentSnapshot: DocumentSnapshot?
    ): Single<Pair<ArrayList<Wish>, DocumentSnapshot?>> {
        return Single.create { singleEmitter ->
            val completionHandler = CompletionHandler { content, error ->
                if (error == null) {
                    val wishIdsList = Mapper.mapToWishIdsArrayList(content!!)
                    if (wishIdsList.isNotEmpty()) {
                        var ref =
                            mFirebaseFirestore.collection(WISHES_COLLECTION_PATH)
                                .whereIn(FieldPath.documentId(), wishIdsList)
                                .limit(WISHES_NUM_PER_PAGE.toLong())

                        if (lastVisibleWishesDocumentSnapshot != null)
                            ref = ref.startAfter(lastVisibleWishesDocumentSnapshot)
                        ref.get()
                            .addOnFailureListener {
                                singleEmitter.onError(it)
                            }.addOnSuccessListener { querySnapshot ->
                                val lastVisibleWishesDocument: DocumentSnapshot? =
                                    if (querySnapshot.documents.isNotEmpty())
                                        querySnapshot.documents[querySnapshot.size() - 1]
                                    else
                                        null
                                val wishesList = Mapper.mapToWishAdapterItemArrayList(querySnapshot)
                                singleEmitter.onSuccess(Pair(wishesList, lastVisibleWishesDocument))
                            }
                    } else
                        singleEmitter.onSuccess(Pair(arrayListOf(), null))
                } else
                    singleEmitter.onError(error)
            }

            mAlgoliaClient.getIndex(ALGOLIA_DATABASE_INDEX)
                .searchAsync(com.algolia.search.saas.Query(searchQuery), completionHandler)

        }
    }
}

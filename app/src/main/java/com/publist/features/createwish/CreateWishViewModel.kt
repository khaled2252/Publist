package com.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.publist.core.common.data.models.wish.*
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import com.publist.core.platform.BaseViewModel
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject

class CreateWishViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {

    //LiveData
    val validationLiveData = MutableLiveData<Boolean>()
    val addingWishLiveData = MutableLiveData<String?>()
    val editedWishLiveData = MutableLiveData<Boolean>()

    //Current user data
    var category: CategoryWish? = null
    var title = ""
    var wishImageUri = ""
    var wishItemsMap = emptyMap<String, WishItem>()
    var isInEditingWishItemsMode = false

    //Edited wish data
    var deletedOldPhoto = false
    private var isEditing = false
    private var oldListMap = emptyMap<String, WishItem>()
    private var oldWishId = ""
    private var oldPhotoName: String? = null
    private var oldWishImageUrl: String? = null
    private lateinit var oldTimeStamp: Timestamp

    fun validateEntries() {
        if (category != null && title.isNotBlank() && (wishItemsMap.isNotEmpty()) && !isInEditingWishItemsMode)
            validationLiveData.postValue(true)
        else
            validationLiveData.postValue(false)
    }

    fun postWish() {
        if (wishItemsMap.size < 3)
            addingWishLiveData.postValue(null)
        else {
            val wish = makeWish(category!!, title, wishItemsMap)
            postWish(wish)
        }
    }

    private fun makeWish(
        category: CategoryWish,
        title: String,
        wishItemsMap: Map<String, WishItem>
    ): Wish {
        val categoryWish = category
        val user = userRepository.getUser()
        val creator = Creator(
            user!!.id!!,
            user.profilePictureUrl!!,
            user.name!!
        )
        val date = Timestamp(Calendar.getInstance().time)

        return Wish(
            category = arrayListOf(categoryWish),
            categoryId = arrayListOf(category.id!!),
            creator = creator,
            date = date,
            title = title,
            items = wishItemsMap,
            itemsId = ArrayList(wishItemsMap.keys)
        )
    }

    private fun postWish(wish: Wish) {
        if (isEditing) {
            if (wishImageUri.isNotEmpty()) // user uploaded a new image
            {
                subscribe(wishesRepository.uploadImage(wishImageUri).flatMapCompletable { result ->
                    val wishImageUrl = result.first
                    val photoName = result.second
                    wish.wishPhotoURL = wishImageUrl
                    wish.photoName = photoName
                    wish.wishId = oldWishId
                    wish.date = oldTimeStamp
                    wishesRepository.updateWish(wish)
                }, Action {
                    editedWishLiveData.postValue(true)
                })
            } else {
                if (!deletedOldPhoto) { // get old image if user didn't delete it
                    wish.wishPhotoURL = oldWishImageUrl
                    wish.photoName = oldPhotoName
                }
                wish.wishId = oldWishId
                wish.date = oldTimeStamp
                subscribe(wishesRepository.updateWish(wish), Action {
                    editedWishLiveData.postValue(true)
                })

            }
        } else { // Creating a new wish
            if (wishImageUri.isNotEmpty()) {
                subscribe(wishesRepository.uploadImage(wishImageUri).flatMap { result ->
                    val wishImageUrl = result.first
                    val photoName = result.second
                    wish.wishPhotoURL = wishImageUrl
                    wish.photoName = photoName
                    wishesRepository.createWish(wish)
                }, Consumer { wishId ->
                    addingWishLiveData.postValue(wishId)
                })
            } else {
                subscribe(wishesRepository.createWish(wish), Consumer { wishId ->
                    addingWishLiveData.postValue(wishId)
                })
            }
        }
    }

    fun populateEditedWishData(editedWish: WishAdapterItem) {
        isEditing = true
        category = editedWish.category?.get(0)
        title = editedWish.title!!
        oldListMap = (editedWish.items as MutableMap<String, WishItem>).toList().sortedBy {
            it.second.orderId
        }.toMap()
        wishItemsMap = oldListMap
        oldWishId = editedWish.wishId!!
        oldTimeStamp = editedWish.date!!
        oldPhotoName = editedWish.photoName
        oldWishImageUrl = editedWish.wishPhotoURL
        validateEntries()
    }

    fun updateWishItems(wishItems: ArrayList<Pair<String, WishItem>>) {
        wishItems.forEachIndexed { index, pair ->
            pair.second.orderId = index
        }
        wishItemsMap = wishItems.toMap()
    }

}


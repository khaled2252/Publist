package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.wish.CategoryWish
import co.publist.core.common.data.models.wish.Creator
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishItem
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import com.google.firebase.Timestamp
import io.reactivex.functions.Action
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CreateWishViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {

    val validationLiveData = MutableLiveData<Boolean>()
    val addingWishLiveData = MutableLiveData<Boolean>()
    var category: CategoryWish? = null
    var title = ""
    var wishImageUri = ""
    var items = ArrayList<String>()

    var deletedOldPhoto = false
    private var isEditing = false
    private var oldListMap = emptyMap<String, WishItem>()
    private var oldWishId = ""
    private var oldPhotoName: String? = null
    private var oldWishImageUrl: String? = null
    private lateinit var oldTimeStamp: Timestamp


    fun validateEntries() {
        if (category != null && title.isNotBlank() && (items.size > 0))
            validationLiveData.postValue(true)
        else
            validationLiveData.postValue(false)
    }

    fun postWish() {
        if (items.size < 3)
            addingWishLiveData.postValue(false)
        else {
            val wish = makeWish(category!!, title, items)
            postWish(wish)
        }
    }

    private fun makeWish(category: CategoryWish, title: String, items: ArrayList<String>) : Wish {

        val categoryWish = category
        val user = userRepository.getUser()
        val creator = Creator(
            user!!.id!!,
            user.profilePictureUrl!!,
            user.name!!
        )
        val date = Timestamp(Calendar.getInstance().time)
        val newItemIdList = arrayListOf<String>()
        val newListMap = mutableMapOf<String, WishItem>()

        val wish = Wish(
            category = arrayListOf(categoryWish),
            categoryId = arrayListOf(category.id!!),
            creator = creator,
            date = date,
            title = title
        )

        if(isEditing) {
            for (itemPosition in 0 until items.size) {
                //getting old items from wish that is being edited
                val oldItemValue = oldListMap.values.find { it.name == items[itemPosition] }
                if (oldItemValue != null) {
                    val oldItemKey = oldListMap.filterValues { it == oldItemValue }.keys.first()
                    newItemIdList.add(oldItemKey)
                    oldItemValue.orderId = itemPosition
                    newListMap[oldItemKey] = oldItemValue
                    continue
                }
            }
        }
        else{
            for (itemPosition in 0 until items.size) {

                val id = UUID.randomUUID().toString().toUpperCase(Locale.getDefault())
                newItemIdList.add(id)
                val item = WishItem(
                    name = items[itemPosition],
                    orderId = itemPosition
                )
                newListMap[id] = item
            }
        }
        wish.items = newListMap
        wish.itemsId = newItemIdList

        return wish
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
                    addingWishLiveData.postValue(true)
                })
            } else {
                if (!deletedOldPhoto) { // get old image if user didn't delete it
                    wish.wishPhotoURL = oldWishImageUrl
                    wish.photoName = oldPhotoName
                }
                wish.wishId = oldWishId
                wish.date = oldTimeStamp
                subscribe(wishesRepository.updateWish(wish), Action {
                    addingWishLiveData.postValue(true)
                })

            }
        } else { // Creating a new wish
            if (wishImageUri.isNotEmpty()) {
                subscribe(wishesRepository.uploadImage(wishImageUri).flatMapCompletable { result ->
                    val wishImageUrl = result.first
                    val photoName = result.second
                    wish.wishPhotoURL = wishImageUrl
                    wish.photoName = photoName
                    wishesRepository.createWish(wish)
                }, Action {
                    addingWishLiveData.postValue(true)
                })
            } else {
                subscribe(wishesRepository.createWish(wish), Action {
                    addingWishLiveData.postValue(true)
                })
            }
        }
    }

    fun populateEditedWishData(editedWish: Wish) {
        isEditing = true
        category = editedWish.category?.get(0)
        title = editedWish.title!!
        oldListMap = (editedWish.items as MutableMap<String, WishItem>).toList().sortedBy {
            it.second.orderId
        }.toMap()
        items = ArrayList(oldListMap.values.map { it.name!! })
        oldWishId = editedWish.wishId!!
        oldTimeStamp = editedWish.date!!
        oldPhotoName = editedWish.photoName
        oldWishImageUrl = editedWish.wishPhotoURL
        validateEntries()
    }

}


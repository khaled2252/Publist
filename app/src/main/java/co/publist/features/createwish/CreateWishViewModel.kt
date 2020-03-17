package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.wish.CategoryWish
import co.publist.core.common.data.models.wish.Creator
import co.publist.core.common.data.models.wish.Item
import co.publist.core.common.data.models.wish.Wish
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
    var oldItemIdList = ArrayList<String>()
    var oldListMap = mutableMapOf<String, Item>()
    var oldWishId = ""
    lateinit var oldTimeStamp: Timestamp


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
            createWish(category!!, title, items)
        }
    }

    private fun createWish(category: CategoryWish, title: String, items: ArrayList<String>) {

        val categoryWish = category
        val user = userRepository.getUser()
        val creator = Creator(
            user!!.id!!,
            user.profilePictureUrl!!,
            user.name!!
        )
        val date = Timestamp(Calendar.getInstance().time)
        val newListMap = mutableMapOf<String, Item>()
        val newItemIdList = arrayListOf<String>()
        for (itemPosition in 0 until items.size) {

            //getting old items from wish that is being edited
            val oldItem = oldListMap.values.find { it.name == items[itemPosition] }
            if (oldItem != null) {
                newListMap[oldItemIdList[itemPosition]] = oldItem
                continue
            }

            val id = UUID.randomUUID().toString().toUpperCase()
            newItemIdList.add(id)
            val item = Item(
                name = items[itemPosition],
                orderId = itemPosition
            )
            newListMap[id] = item
        }
        val orderedNewListMap = newListMap.toList().sortedBy {
             it.second.orderId
         }.toMap()

        val wish = Wish(
            category = arrayListOf(categoryWish),
            categoryId = arrayListOf(category.id!!),
            creator = creator,
            date = date,
            items = orderedNewListMap,
            itemsId = newItemIdList,
            title = title
        )

        if (wishImageUri.isNotEmpty()) {
            subscribe(wishesRepository.uploadImage(wishImageUri).flatMapCompletable { wishImageUrl ->
                wish.wishPhotoURL = wishImageUrl
                if (oldListMap.isNotEmpty()) {
                    wish.wishId = oldWishId
                    wish.date = oldTimeStamp
                    wishesRepository.updateWish(wish)
                } else
                    wishesRepository.createWish(wish)
            }, Action {
                addingWishLiveData.postValue(true)
            })
        } else {
            if (oldListMap.isNotEmpty()) {
                wish.wishId = oldWishId
                wish.date = oldTimeStamp
                subscribe(wishesRepository.updateWish(wish), Action {
                    addingWishLiveData.postValue(true)
                })
            } else
                subscribe(wishesRepository.createWish(wish), Action {
                    addingWishLiveData.postValue(true)
                })

        }
    }

    fun populateWishData(editedWish: Wish) {
        category = editedWish.category?.get(0)
        title = editedWish.title!!

        if (!editedWish.wishPhotoURL.isNullOrEmpty())
            wishImageUri = editedWish.wishPhotoURL!!

        items = ArrayList(editedWish.items!!.values.map { it.name!! })
        oldItemIdList = ArrayList(editedWish.items!!.keys)
        oldListMap = editedWish.items as MutableMap<String, Item>
        oldWishId = editedWish.wishId!!
        oldTimeStamp = editedWish.date!!
        validateEntries()
    }

}


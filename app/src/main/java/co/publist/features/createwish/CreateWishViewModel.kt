package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.wish.Creator
import co.publist.core.common.data.models.wish.Item
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import com.google.firebase.Timestamp
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CreateWishViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {

    val validationLiveData = MutableLiveData<Boolean>()
    val addingWishLiveData = MutableLiveData<Boolean>()
    var category : Category? = null
    var title = ""
    var wishImageUri = ""
    var items = ArrayList<String>()

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

    private fun createWish(category: Category, title: String, items: ArrayList<String>) {

            val user = userRepository.getUser()
            val creator = Creator(
                user!!.id!!,
                user.profilePictureUrl!!,
                user.name!!
            )
            val date = Timestamp(Calendar.getInstance().time)
            val listMap = mutableMapOf<String, Item>()
            val itemIdList = ArrayList<String>()
            for (itemPosition in 0 until items.size) {
                val id = UUID.randomUUID().toString().toUpperCase()
                val item = Item(
                    name = items[itemPosition],
                    orderId = itemPosition
                )
                listMap[id] = item
                itemIdList.add(id)
            }

            val wish = Wish(
                category = arrayListOf(category),
                categoryId = arrayListOf(category.id),
                creator = creator,
                date = date,
                items = listMap,
                itemsId = itemIdList,
                title = title
            )

            if (wishImageUri.isNotEmpty()) {
                subscribe(wishesRepository.uploadImage(wishImageUri), Consumer { wishImageUrl ->
                    wish.wishPhotoURL = wishImageUrl
                    subscribe(wishesRepository.createWish(wish), Action {
                        addingWishLiveData.postValue(true)
                    })
                })
            } else
                subscribe(wishesRepository.createWish(wish), Action {
                    addingWishLiveData.postValue(true)
                })

    }

}


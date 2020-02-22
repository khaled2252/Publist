package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Creator
import co.publist.core.common.data.models.Item
import co.publist.core.common.data.models.Wish
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.wishes.data.WishesRepositoryInterface
import com.google.firebase.Timestamp
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CreateWishViewModel @Inject constructor(
    private val wishesRepository: WishesRepositoryInterface,
    private val userRepository: UserRepositoryInterface,
    private val categoriesRepository: CategoriesRepositoryInterface
) : BaseViewModel() {

    val validationLiveData = MutableLiveData<Boolean>()
    val addingWishLiveData = MutableLiveData<Boolean>()
    var categoryId = ""
    var title = ""
    var wishImageUri = ""
    var items = ArrayList<String>()

    fun validateEntries() {
        if (categoryId.isNotEmpty() && title.isNotBlank() && (items.size > 0))
            validationLiveData.postValue(true)
        else
            validationLiveData.postValue(false)
    }

    fun postWish() {
        if (items.size < 3)
            addingWishLiveData.postValue(false)
        else {
            createWish(categoryId, title, items)
        }
    }

    private fun createWish(categoryId: String, title: String, items: ArrayList<String>) {
        subscribe(categoriesRepository.getCategoryFromId(categoryId), Consumer { category ->
            val user = userRepository.getUser()
            val creator = Creator(user!!.id!!, user.profilePictureUrl!!, user.name!!)
            val date = Timestamp(Calendar.getInstance().time)
            val listMap = mutableMapOf<String, Item>()
            val itemIdList = ArrayList<String>()
            for (itemPosition in 0 until items.size) {
                val id = UUID.randomUUID().toString().toUpperCase()
                val item = Item(name = items[itemPosition], orderId = itemPosition)
                listMap[id] = item
                itemIdList.add(id)
            }

            val wish = Wish(
                category = arrayListOf(category),
                categoryId = arrayListOf(categoryId),
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
        })
    }

}


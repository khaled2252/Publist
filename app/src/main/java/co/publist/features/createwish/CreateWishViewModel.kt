package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Category
import co.publist.core.common.data.models.Creator
import co.publist.core.common.data.models.Item
import co.publist.core.common.data.models.Wish
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.wishes.data.WishesRepositoryInterface
import com.google.firebase.Timestamp
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CreateWishViewModel @Inject constructor(private val wishesRepository: WishesRepositoryInterface , private val userRepository: UserRepositoryInterface) : BaseViewModel() {

    val validationLiveData = MutableLiveData<Boolean>()
    val addingWishLiveData = MutableLiveData<Boolean>()
    var category = ""
    var title = ""
    var wishImageUri = ""
    var items = ArrayList<String>()

    fun validateEntries() {
        if (category.isNotEmpty() && title.isNotBlank() && (items.size > 0))
            validationLiveData.postValue(true)
        else
            validationLiveData.postValue(false)
    }

    fun postWish() {
        if (items.size < 3)
            addingWishLiveData.postValue(false)
        else {
            createWish(category, title, items)
        }
    }

    private fun createWish(category: String, title: String, items: ArrayList<String>) {
        //todo wish.category =
        val user = userRepository.getUser()
        val creator = Creator(user!!.id, user.profilePictureUrl, user.name)
        val date = Timestamp(Calendar.getInstance().time)
        val listMap = mutableMapOf<String, Item>()
        for (itemPosition in 0..items.size) {
            val id = UUID.randomUUID().toString().capitalize()
            val item = Item(name = items[itemPosition], orderId = itemPosition)
            listMap[id] = item
        }

        subscribe(wishesRepository.uploadImage(wishImageUri), Consumer { wishImageUrl ->

            val wish = Wish(
                title = title,
                date = date,
                creator = creator,
                items = listMap,
                wishPhotoURL = wishImageUrl,
                category = arrayListOf(Category()) //todo
            )
        })


    }

}

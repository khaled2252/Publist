package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Wish
import co.publist.core.platform.BaseViewModel
import co.publist.features.wishes.data.WishesRepositoryInterface
import io.reactivex.functions.Action
import javax.inject.Inject

class CreateWishViewModel @Inject constructor(val wishesRepository: WishesRepositoryInterface) : BaseViewModel() {

    val validationLiveData = MutableLiveData<Boolean>()
    val addingWishLiveData = MutableLiveData<Boolean>()
    var category = ""
    var title = ""
    var items = ArrayList<String>()

    fun validateEntries(){
        if (category.isNotEmpty() && title.isNotBlank() && (items.size>0))
            validationLiveData.postValue(true)
        else
            validationLiveData.postValue(false)
    }

    fun postWish() {
        if(items.size < 3)
            addingWishLiveData.postValue(false)
        else
        {
            subscribe(wishesRepository.createWish(createWish(category,title,items)), Action {
                addingWishLiveData.postValue(true)
            })
        }
    }

    private fun createWish(category: String, title: String, items: java.util.ArrayList<String>) : Wish {
        val wish = Wish()
        //todo populate wish
        return wish
    }

}

package co.publist.features.createwish

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject

class CreateWishViewModel @Inject constructor() : BaseViewModel() {

    val validationLiveData = MutableLiveData<Boolean>()
    var category = ""
    var title = ""
    var items = ArrayList<String>()

    fun validateEntries(){
        if (category.isNotEmpty() && title.isNotEmpty() && (items.size>0))
            validationLiveData.postValue(true)
    }

    fun addWish() {
        //todo call repository to add wish
    }

}

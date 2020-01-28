package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject

class CategoriesViewModel @Inject constructor() : BaseViewModel() {

    val selectedCategories = ArrayList<String>()
    val reachedMaximumSelection = MutableLiveData<Int>()
    fun addSelectedCategory(
        categoryId: String?,
        isAdding: Boolean,
        buttonId: Int
    ) {
        if (isAdding) {
            if (selectedCategories.size < 5)
                selectedCategories.add(categoryId!!)
            else
                reachedMaximumSelection.postValue(buttonId)
        } else
            selectedCategories.remove(categoryId!!)
    }
}
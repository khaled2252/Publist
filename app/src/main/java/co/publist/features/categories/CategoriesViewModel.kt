package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import com.google.android.material.button.MaterialButton
import javax.inject.Inject

class CategoriesViewModel @Inject constructor() : BaseViewModel() {

    val selectedCategories = ArrayList<String>()
    val addSelectedCategory = MutableLiveData<MaterialButton>()
    val removeSelectedCategory = MutableLiveData<MaterialButton>()
    val reachedMaximumSelection = MutableLiveData<Boolean>()
    fun addCategory(
        categoryId: String?,
        buttonId: MaterialButton
    ) {
        if (!selectedCategories.contains(categoryId)) {
            if (selectedCategories.size < 5) {
                selectedCategories.add(categoryId!!)
                addSelectedCategory.postValue(buttonId)
            } else
                reachedMaximumSelection.postValue(true)
        } else {
            selectedCategories.remove(categoryId!!)
            removeSelectedCategory.postValue(buttonId)
        }
    }
}
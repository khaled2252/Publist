package co.publist.features.categories

import co.publist.core.platform.BaseViewModel
import javax.inject.Inject

 class CategoriesViewModel @Inject constructor() : BaseViewModel() {

    val selectedCategories = ArrayList<String>()
    fun addSelectedCategory(categoryId: String) {
        selectedCategories.add(categoryId)
    }

    fun removeSelectedCategory(categoryId: String) {
        selectedCategories.remove(categoryId)
    }


}
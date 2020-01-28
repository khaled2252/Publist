package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(private val categoriesRepository: CategoriesRepositoryInterface) : BaseViewModel() {

    val selectedCategories = ArrayList<String>()
    val addSelectedCategory = MutableLiveData<Boolean>()
    val removeSelectedCategory = MutableLiveData<Boolean>()
    val reachedMaximumSelection = MutableLiveData<Boolean>()

    fun addCategory(categoryId: String?) {
        if (!selectedCategories.contains(categoryId)) {
            if (selectedCategories.size < 5) {
                selectedCategories.add(categoryId!!)
                addSelectedCategory.postValue(true)
            } else
                reachedMaximumSelection.postValue(true)
        } else {
            selectedCategories.remove(categoryId!!)
            removeSelectedCategory.postValue(true)
        }
    }

    fun getCategoriesQuery(): Query {
        return categoriesRepository.getCategoriesQuery()
    }
}
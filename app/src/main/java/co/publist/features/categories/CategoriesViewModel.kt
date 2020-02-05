package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.FIND_ACTION
import co.publist.core.utils.Utils.Constants.MAXIMUM_SELECTED_CATEGORIES
import co.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import co.publist.core.utils.Utils.Constants.SAVE_ACTION
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepositoryInterface
) : BaseViewModel() {
    init {
        getSelectedCategories()
    }

    var selectedCategoriesList = ArrayList<String>()
    var previouslySelectedCategoriesList = MutableLiveData<ArrayList<String>>()
    val selectedCategory = MutableLiveData<Boolean>()
    val reachedMaximumSelection = MutableLiveData<Boolean>()
    val actionButtonLiveData = MutableLiveData<Boolean>()
    val saveCategoriesLiveData = MutableLiveData<Boolean>()

    fun addCategory(categoryId: String?) {
        if (!selectedCategoriesList.contains(categoryId)) {
            if (selectedCategoriesList.size < MAXIMUM_SELECTED_CATEGORIES) {
                selectedCategoriesList.add(categoryId!!)
                selectedCategory.postValue(true)
            } else
                reachedMaximumSelection.postValue(true)
        } else {
            selectedCategoriesList.remove(categoryId!!)
            selectedCategory.postValue(false)
        }
    }

    fun getCategoriesQuery(): Query {
        return categoriesRepository.getCategoriesQuery()
    }

    private fun getSelectedCategories() {
        subscribe(categoriesRepository.getUserCategories(), Consumer { list ->
            selectedCategoriesList = list
            previouslySelectedCategoriesList.postValue(list)
        })
    }

    fun handleActionButton(action : String?) {
        if(selectedCategoriesList.size< MINIMUM_SELECTED_CATEGORIES)
            actionButtonLiveData.postValue(false)
        else {
            if(action == SAVE_ACTION)
                saveUserCategories()
            else if (action == FIND_ACTION)
                saveGuestCategories()

            actionButtonLiveData.postValue(true)
        }
    }

    private fun saveGuestCategories() {
        categoriesRepository.saveGuestCategories(selectedCategoriesList)
    }

    private fun saveUserCategories() {
        subscribe(categoriesRepository.updateUserCategories(selectedCategoriesList), Action {
            saveCategoriesLiveData.postValue(true)
        })
    }

}
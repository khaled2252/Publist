package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.MAXIMUM_SELECTED_CATEGORIES
import co.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.google.firebase.firestore.Query
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepositoryInterface,
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    var selectedCategoriesList = ArrayList<String>()
    var previouslySelectedCategoriesList = MutableLiveData<ArrayList<String>>()
    val selectedCategory = MutableLiveData<Boolean>()
    val reachedMaximumSelection = MutableLiveData<Boolean>()
    val actionButtonLiveData = MutableLiveData<Boolean>()
    val saveCategoriesLiveData = MutableLiveData<Boolean>()
    var isCreatingWish = false

    fun addCategory(categoryId: String?) {
        if (!selectedCategoriesList.contains(categoryId)) {
            if (!isCreatingWish) {
                if (selectedCategoriesList.size < MAXIMUM_SELECTED_CATEGORIES) {
                    selectedCategoriesList.add(categoryId!!)
                    selectedCategory.postValue(true)
                } else
                    reachedMaximumSelection.postValue(false)
            } else {
                if (selectedCategoriesList.size < 1) {
                    selectedCategoriesList.add(categoryId!!)
                    selectedCategory.postValue(true)
                } else
                    reachedMaximumSelection.postValue(true)
            }
        } else {
            selectedCategoriesList.remove(categoryId!!)
            selectedCategory.postValue(false)
        }
    }

    fun getCategoriesQuery(): Query {
        return categoriesRepository.getCategoriesQuery()
    }

    fun getSelectedCategories() {
        val user = userRepository.getUser()
        if (user == null) {
                subscribe(
                    categoriesRepository.getLocalSelectedCategories(),
                    Consumer { localCategories ->
                        selectedCategoriesList = localCategories
                        previouslySelectedCategoriesList.postValue(selectedCategoriesList)
                    })
        } else {
            if (!isCreatingWish)
            subscribe(categoriesRepository.fetchSelectedCategories(user.id!!), Consumer { list ->
                //Update categories in database
                categoriesRepository.updateLocalSelectedCategories(list)

                selectedCategoriesList = list
                previouslySelectedCategoriesList.postValue(selectedCategoriesList)
            })
            else
                previouslySelectedCategoriesList.postValue(selectedCategoriesList) //Empty selected categories on start when creating wish
        }

    }

    fun handleActionButton(isUser: Boolean) {
            when {
                selectedCategoriesList.size < MINIMUM_SELECTED_CATEGORIES -> actionButtonLiveData.postValue(
                    false
                )
                isUser -> saveUserSelectedCategories()
                else -> saveGuestSelectedCategories()
            }

    }

    private fun saveUserSelectedCategories() {
        categoriesRepository.updateLocalSelectedCategories(selectedCategoriesList)
        subscribe(
            categoriesRepository.updateRemoteSelectedCategories(selectedCategoriesList),
            Action {
                saveCategoriesLiveData.postValue(true)
            })
    }

    private fun saveGuestSelectedCategories() {
        categoriesRepository.updateLocalSelectedCategories(selectedCategoriesList)
        saveCategoriesLiveData.postValue(true)
    }

}
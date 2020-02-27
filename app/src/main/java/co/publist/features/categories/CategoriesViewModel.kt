package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Extensions.Constants.MAXIMUM_SELECTED_CATEGORIES
import co.publist.core.utils.Extensions.Constants.MINIMUM_SELECTED_CATEGORIES
import co.publist.features.categories.data.CategoriesRepositoryInterface
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepositoryInterface
) : BaseViewModel() {
    var initialCategoriesList = MutableLiveData<ArrayList<CategoryAdapterItem>>()
    val updatedCategoryList = MutableLiveData<ArrayList<CategoryAdapterItem>>()
    var selectedCategoriesList = ArrayList<CategoryAdapterItem>()
    val reachedMaximumSelection = MutableLiveData<Boolean>()
    val actionButtonLiveData = MutableLiveData<Boolean>()
    val saveCategoriesLiveData = MutableLiveData<Boolean>()
    var isCreatingWish = false

    fun addCategory(category: CategoryAdapterItem) {
        if (!selectedCategoriesList.contains(category)) {
            if (!isCreatingWish) {
                if (selectedCategoriesList.size < MAXIMUM_SELECTED_CATEGORIES) {
                    selectedCategoriesList.add(category)
                    initialCategoriesList.value?.find {
                        it == category
                    }?.isSelected = true
                    updatedCategoryList.postValue(initialCategoriesList.value)
                } else
                    reachedMaximumSelection.postValue(false)
            } else {
                if (selectedCategoriesList.size < 1) {
                    selectedCategoriesList.add(category)
                    initialCategoriesList.value?.find {
                        it == category
                    }?.isSelected = true
                    updatedCategoryList.postValue(initialCategoriesList.value)
                } else
                    reachedMaximumSelection.postValue(true)
            }
        } else {
            selectedCategoriesList.remove(category)
            initialCategoriesList.value?.find {
                it == category
            }?.isSelected = false
            updatedCategoryList.postValue(initialCategoriesList.value)
        }
    }

    fun getSelectedCategories() {
        subscribe(categoriesRepository.fetchAllCategories(), Consumer { categories ->
            if(isCreatingWish) {
                initialCategoriesList.postValue(Mapper.mapToCategoryAdapterItemList(categories))
            }
            else
                subscribe(
                    categoriesRepository.getLocalSelectedCategories(),
                    Consumer { selectedCategories ->
                        selectedCategoriesList = selectedCategories
                        initialCategoriesList.postValue(
                            compareCategories(
                                categories,
                                selectedCategories
                            )
                        )
                    })
        })
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


    private fun compareCategories(
        categories: ArrayList<Category>,
        selectedCategories: ArrayList<CategoryAdapterItem>
    ): ArrayList<CategoryAdapterItem>? {
        val categoriesAdapterItemList = ArrayList<CategoryAdapterItem>()
        for(category in categories)
        {
            val categoryAdapterItem = Mapper.mapToCategoryAdapterItem(category)
            if (selectedCategories.map { it.id }.contains(category.id))
                categoryAdapterItem.isSelected = true
            categoriesAdapterItemList.add(categoryAdapterItem)
        }

        return categoriesAdapterItemList
    }

    private fun saveUserSelectedCategories() {
        categoriesRepository.updateLocalSelectedCategories(Mapper.mapToCategoryArrayList(selectedCategoriesList))
        subscribe(
            categoriesRepository.updateRemoteSelectedCategories(Mapper.mapToCategoryArrayList(selectedCategoriesList)),
            Action {
                saveCategoriesLiveData.postValue(true)
            })
    }

    private fun saveGuestSelectedCategories() {
        categoriesRepository.updateLocalSelectedCategories(Mapper.mapToCategoryArrayList(selectedCategoriesList))
        saveCategoriesLiveData.postValue(true)
    }

}
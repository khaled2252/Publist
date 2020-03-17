package co.publist.features.categories

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.wish.CategoryWish
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.MAXIMUM_SELECTED_CATEGORIES
import co.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import co.publist.features.categories.data.CategoriesRepositoryInterface
import io.reactivex.Single
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepositoryInterface
) : BaseViewModel() {
    val categoriesListLiveData = MutableLiveData<ArrayList<CategoryAdapterItem>>()
    var selectedCategoriesList = ArrayList<CategoryAdapterItem>()
    val reachedMaximumSelectionLiveData = MutableLiveData<Boolean>()
    val actionButtonLiveData = MutableLiveData<Boolean>()
    val saveCategoriesLiveData = MutableLiveData<Boolean>()
    var isCreatingWish = false

    fun addCategory(category: CategoryAdapterItem) {
        if (!selectedCategoriesList.contains(category)) {
            if (!isCreatingWish) {
                if (selectedCategoriesList.size < MAXIMUM_SELECTED_CATEGORIES) {
                    selectedCategoriesList.add(category)
                    categoriesListLiveData.value?.find {
                        it == category
                    }?.isSelected = true
                    categoriesListLiveData.postValue(categoriesListLiveData.value)
                } else
                    reachedMaximumSelectionLiveData.postValue(false)
            } else {
                if (selectedCategoriesList.size < 1) {
                    selectedCategoriesList.add(category)
                    categoriesListLiveData.value?.find {
                        it == category
                    }?.isSelected = true
                    categoriesListLiveData.postValue(categoriesListLiveData.value)
                } else
                    reachedMaximumSelectionLiveData.postValue(true)
            }
        } else {
            selectedCategoriesList.remove(category)
            categoriesListLiveData.value?.find {
                it == category
            }?.isSelected = false
            categoriesListLiveData.postValue(categoriesListLiveData.value)
        }
    }

    fun getCategories(vararg editedWishCategory: CategoryWish) {
        //fixme if fetchAllCategories() is called first then getLocalSelectedCategories() in flatMap , the latter doesn't get called maybe because of backPressure?
        subscribe(categoriesRepository.getLocalSelectedCategories()
            .flatMap { selectedCategories ->
                categoriesRepository.fetchAllCategories()
                    .flatMap { allCategories ->
                        if (isCreatingWish) {
                            val categoryAdapterItemList =
                                Mapper.mapToCategoryAdapterItemList(allCategories)
                            if (editedWishCategory.isNotEmpty()) {
                                val selectedCategory = Mapper.mapToCategoryAdapterItem(editedWishCategory[0])
                                selectedCategory.isSelected = true
                                selectedCategoriesList.add(selectedCategory)
                                val index =
                                    categoryAdapterItemList.indexOfFirst { it.id == editedWishCategory[0].id }
                                categoryAdapterItemList[index].isSelected = true
                            }
                            Single.just(categoryAdapterItemList)
                        } else {
                            selectedCategoriesList = selectedCategories
                            Single.just(
                                applyPreviouslySelectedCategories(
                                    allCategories,
                                    selectedCategories
                                )
                            )
                        }
                    }
            }, Consumer { categories ->
            categoriesListLiveData.postValue(categories)
        }, showLoading = !isCreatingWish //Shows loading only if not creatingWish
        )
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


    private fun applyPreviouslySelectedCategories(
        categories: ArrayList<Category>,
        selectedCategories: ArrayList<CategoryAdapterItem>
    ): ArrayList<CategoryAdapterItem>? {
        val categoriesAdapterItemList = ArrayList<CategoryAdapterItem>()
        for (category in categories) {
            val categoryAdapterItem = Mapper.mapToCategoryAdapterItem(category)
            if (selectedCategories.map { it.id }.contains(category.id))
                categoryAdapterItem.isSelected = true
            categoriesAdapterItemList.add(categoryAdapterItem)
        }
        return categoriesAdapterItemList
    }

    private fun saveUserSelectedCategories() {
        categoriesRepository.updateLocalSelectedCategories(
            Mapper.mapToCategoryArrayList(
                selectedCategoriesList
            )
        )
        subscribe(
            categoriesRepository.updateRemoteSelectedCategories(
                Mapper.mapToCategoryArrayList(
                    selectedCategoriesList
                )
            ),
            Action {
                saveCategoriesLiveData.postValue(true)
            })
    }

    private fun saveGuestSelectedCategories() {
        categoriesRepository.updateLocalSelectedCategories(
            Mapper.mapToCategoryArrayList(
                selectedCategoriesList
            )
        )
        saveCategoriesLiveData.postValue(true)
    }

}
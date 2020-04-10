package co.publist.features.home

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import io.reactivex.functions.Consumer
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface
) :
    BaseViewModel() {

    val user = userRepository.getUser()
    var userLiveData = MutableLiveData<User>()
    var profilePictureClickLiveData = MutableLiveData<Boolean>()
    var addWishClickLiveData = MutableLiveData<Boolean>()

    private var allCategories = arrayListOf<Category>()

    init {
        subscribe(categoryRepository.fetchAllCategories(), Consumer {categoriesList ->
            allCategories = categoriesList
        })
    }

    fun onCreated() {
        userLiveData.postValue(user)
    }

    fun handleEditProfile() {
        if (user == null)
            profilePictureClickLiveData.postValue(true)
        else
            profilePictureClickLiveData.postValue(false)
    }

    fun clearGuestSelectedCategories() {
        if(user == null)
        categoryRepository.clearLocalSelectedCategories()
    }


    fun handleAddWish() {
        if(user == null)
        addWishClickLiveData.postValue(true)
        else
            addWishClickLiveData.postValue(false)

    }

    fun getSuggestedCategoriesFromQuery(query: String): ArrayList<String> {
        val suggestedCategoriesNames = arrayListOf<String>()
        for (categoryName in allCategories.map {it.name})
            if(categoryName.equals(query,true))
                suggestedCategoriesNames.add(categoryName!!.capitalize())
        if(suggestedCategoriesNames.isEmpty())
            for (categoryName in allCategories.map {it.name})
                if (categoryName!!.startsWith(query,true))
                    suggestedCategoriesNames.add(categoryName.capitalize())


        return suggestedCategoriesNames
    }

}
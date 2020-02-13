package co.publist.features.home

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface
) :
    BaseViewModel() {

    var userLiveData = MutableLiveData<User>()
    var logoutLiveData = MutableLiveData<Boolean>()
    var isGuest = MutableLiveData<Boolean>()
    val user = userRepository.getUser()

    fun onCreated() {
        userLiveData.postValue(user)
    }

    fun handleLogout() {
        userRepository.deleteCurrentUser()
        categoryRepository.clearLocalSelectedCategories()
        logoutLiveData.postValue(true)
    }

    fun handleEditProfile() {
        if (user == null)
            isGuest.postValue(true)
        else
            isGuest.postValue(false)
    }

    fun clearGuestSelectedCategories() {
        if(user == null)
        categoryRepository.clearLocalSelectedCategories()
    }
}
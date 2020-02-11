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
    var guestLiveData = MutableLiveData<ArrayList<String>>()
    var logoutLiveData = MutableLiveData<Boolean>()
    var isGuest = MutableLiveData<Boolean>()
    val user = userRepository.getUser()

    fun onCreated() {
        if (user != null)
            userLiveData.postValue(user)
        else
            guestLiveData.postValue(categoryRepository.getGuestCategories())
    }

    fun handleLogout() {
        userRepository.deleteCurrentUser()
        logoutLiveData.postValue(true)
    }

    fun handleEditProfile() {
        if (user == null)
            isGuest.postValue(true)
        else
            isGuest.postValue(false)
    }

    fun clearGuestCategories(){
        categoryRepository.clearGuestCategories()
    }
}
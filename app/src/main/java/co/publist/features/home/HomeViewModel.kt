package co.publist.features.home

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val userRepository: UserRepositoryInterface) :
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
        logoutLiveData.postValue(true)
    }

    fun handleEditProfile() {
        if (user == null)
            isGuest.postValue(true)
        else
            isGuest.postValue(false)
    }
}
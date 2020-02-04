package co.publist.features.home

import androidx.lifecycle.MutableLiveData
import co.publist.core.data.models.User
import co.publist.core.platform.BaseViewModel
import co.publist.features.home.data.HomeRepositoryInterface
import javax.inject.Inject


class HomeViewModel @Inject constructor(private val homeRepository: HomeRepositoryInterface) :
    BaseViewModel() {

    var userLiveData = MutableLiveData<User>()
    var logoutLiveData = MutableLiveData<Boolean>()
    var isGuest = MutableLiveData<Boolean>()
    val user = homeRepository.getUserInformation()

    fun onCreated() {
        userLiveData.postValue(user)
    }

    fun handleLogout() {
        homeRepository.deleteCurrentUser()
        logoutLiveData.postValue(true)
    }

    fun handleEditProfile() {
        if (user == null)
            isGuest.postValue(true)
        else
            isGuest.postValue(false)
    }
}
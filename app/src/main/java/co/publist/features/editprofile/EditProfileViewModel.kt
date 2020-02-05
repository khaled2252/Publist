package co.publist.features.editprofile

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {

    var userLiveData = MutableLiveData<User>()

    fun onCreated() {
        val user = userRepository.getUser()
        if (user != null) {
            userLiveData.postValue(user)
        }
    }
}
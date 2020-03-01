package co.publist.features.profile

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface
) : BaseViewModel() {
    var userLiveData = MutableLiveData<User>()

    init {
        userLiveData.postValue(userRepository.getUser())
    }
}
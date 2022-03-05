package com.publist.features.editprofile

import androidx.lifecycle.MutableLiveData
import com.publist.core.common.data.models.User
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.platform.BaseViewModel
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
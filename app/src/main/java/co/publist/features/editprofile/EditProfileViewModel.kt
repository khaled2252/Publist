package co.publist.features.editprofile

import androidx.lifecycle.MutableLiveData
import co.publist.core.data.models.User
import co.publist.core.platform.BaseViewModel
import co.publist.features.editprofile.data.EditProfileRepositoryInterface
import javax.inject.Inject


class EditProfileViewModel @Inject constructor(
    private val editProfileRepository: EditProfileRepositoryInterface
) : BaseViewModel() {

    var userLiveData = MutableLiveData<User>()

    fun onCreated() {
        val user = editProfileRepository.getUserInformation()
        if (user != null) {
            userLiveData.postValue(user)
        }
    }
}
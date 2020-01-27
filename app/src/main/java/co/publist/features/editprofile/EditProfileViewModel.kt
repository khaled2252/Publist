package co.publist.features.editprofile

import co.publist.core.data.User
import co.publist.core.platform.BaseViewModel
import co.publist.features.editprofile.data.EditProfileRepository
import javax.inject.Inject


class EditProfileViewModel @Inject constructor(private val editProfileRepository: EditProfileRepository) : BaseViewModel() {

    var user : User? =null

    fun onCreated() : User? {
        user = editProfileRepository.getUserInformation()
        return if (user!=null)
            user as User
        else
            null
    }

}
package co.publist.features.editprofile.data

import co.publist.core.data.User

interface EditProfileRepositoryInterface {
    fun getUserInformation() : User
}
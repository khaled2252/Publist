package co.publist.features.editprofile.data

import co.publist.core.data.models.User

interface EditProfileRepositoryInterface {
    fun getUserInformation() : User?
}
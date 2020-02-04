package co.publist.features.home.data

import co.publist.core.data.models.User

interface HomeRepositoryInterface {
    fun getUserInformation() : User?
    fun deleteCurrentUser()
}
package co.publist.core.common.data.repositories.user

import co.publist.core.common.data.models.User

interface UserRepositoryInterface {
    fun getUser(): User?
    fun deleteCurrentUser()
}
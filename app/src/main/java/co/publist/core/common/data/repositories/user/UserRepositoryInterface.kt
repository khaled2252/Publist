package com.publist.core.common.data.repositories.user

import com.publist.core.common.data.models.User

interface UserRepositoryInterface {
    fun getUser(): User?
    fun deleteCurrentUser()
}
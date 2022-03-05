package com.publist.core.common.data.repositories.user

import com.publist.core.common.data.local.LocalDataSource
import com.publist.core.common.data.models.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val localDataSource: LocalDataSource

) : UserRepositoryInterface {

    override fun getUser(): User? {
        return localDataSource.getSharedPreferences().getUser()
    }

    override fun deleteCurrentUser() {
        localDataSource.getSharedPreferences().deleteUser()
    }
}

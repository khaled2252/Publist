package co.publist.features.home.data

import co.publist.core.data.local.LocalDataSource
import co.publist.core.data.models.User
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localDataSource: LocalDataSource

)  : HomeRepositoryInterface{

    override fun getUserInformation(): User? {
        return localDataSource.getSharedPreferences().getUser()
    }

    override fun deleteCurrentUser() {
        localDataSource.getSharedPreferences().deleteUser()
    }
}
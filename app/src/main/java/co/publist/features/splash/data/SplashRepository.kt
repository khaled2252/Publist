package co.publist.features.splash.data

import co.publist.core.data.local.LocalDataSource
import co.publist.core.data.models.User
import javax.inject.Inject

class SplashRepository @Inject constructor(
    private val localDataSource: LocalDataSource

)  : SplashRepositoryInterface{

    override fun getUser(): User? {
        return localDataSource.getSharedPreferences().getUser()
    }
}
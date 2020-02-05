package co.publist.features.home.data

import co.publist.core.common.data.local.LocalDataSource
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localDataSource: LocalDataSource

)  : HomeRepositoryInterface{

    override fun getGuestCategories(): ArrayList<String>? {
        return localDataSource.getSharedPreferences().getTemporaryCategories()
    }

}
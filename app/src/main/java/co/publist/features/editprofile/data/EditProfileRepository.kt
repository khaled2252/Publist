package co.publist.features.editprofile.data

import co.publist.core.data.User
import co.publist.core.data.local.LocalDataSource
import co.publist.core.platform.BaseRepository
import javax.inject.Inject

class EditProfileRepository @Inject constructor(
    private val localDataSource: LocalDataSource

) : BaseRepository(), EditProfileRepositoryInterface {
    override fun getUserInformation(): User {
        return localDataSource.getSharedPreferences().getUser()!!
    }


}
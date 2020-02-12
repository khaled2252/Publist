package co.publist.core.common.data.local

import co.publist.core.common.data.local.db.DataBaseInterface

interface LocalDataSource {

    fun getSharedPreferences(): PublistSharedPreferencesInterface
    fun getPublistDataBase(): DataBaseInterface

}
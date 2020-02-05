package co.publist.core.common.data.local

interface LocalDataSource {

    fun getSharedPreferences(): PublistSharedPreferencesInterface

}
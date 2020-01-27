package co.publist.core.data.local

interface LocalDataSource {

    fun getSharedPreferences(): PublistSharedPreferencesInterface

}
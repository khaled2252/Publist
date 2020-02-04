package co.publist.core.data.local

import android.content.Context

class LocalDataSourceImpl(val context: Context) :
    LocalDataSource {
    override fun getSharedPreferences(): PublistSharedPreferencesInterface =
        SharedPreferencesUtils(context)
}
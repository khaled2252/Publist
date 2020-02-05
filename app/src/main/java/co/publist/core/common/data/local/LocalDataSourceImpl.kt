package co.publist.core.common.data.local

import android.content.Context

class LocalDataSourceImpl(val context: Context) :
    LocalDataSource {
    override fun getSharedPreferences(): PublistSharedPreferencesInterface =
        SharedPreferencesUtils(context)
}
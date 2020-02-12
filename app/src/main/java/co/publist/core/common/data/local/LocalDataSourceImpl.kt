package co.publist.core.common.data.local

import android.content.Context
import co.publist.core.common.data.local.db.DataBaseAccess
import co.publist.core.common.data.local.db.DataBaseInterface

class LocalDataSourceImpl(val context: Context) :
    LocalDataSource {
    override fun getSharedPreferences(): PublistSharedPreferencesInterface =
        SharedPreferencesUtils(context)

    override fun getPublistDataBase(): DataBaseInterface =
        DataBaseAccess(context)

}
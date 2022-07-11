package com.publist.core.common.data.local

import com.publist.core.common.data.local.db.DataBaseInterface

interface LocalDataSource {

    fun getSharedPreferences(): PublistSharedPreferencesInterface
    fun getPublistDataBase(): DataBaseInterface
}
package com.publist.features.home.data

import com.publist.core.common.data.local.LocalDataSource
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localDataSource: LocalDataSource

) : HomeRepositoryInterface
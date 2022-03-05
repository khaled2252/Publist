package com.publist.features.splash

import androidx.lifecycle.MutableLiveData
import com.publist.core.common.data.local.LocalDataSource
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.platform.BaseViewModel
import com.publist.features.categories.data.CategoriesRepositoryInterface
import io.reactivex.functions.Consumer
import javax.inject.Inject


class SplashViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val localDataSource: LocalDataSource
) :
    BaseViewModel() {

    val userLoggedIn = MutableLiveData<Triple<Boolean, Boolean, Boolean>>()

    fun onCreated() {
        var isNewUser = false
        var isMyCategoriesEmpty = false
        var isOnBoardingFinished = false

        val user = userRepository.getUser()
        if (user == null)
            isNewUser = true

        if (localDataSource.getSharedPreferences().getOnBoardingStatus())
            isOnBoardingFinished = true

        //Checking Local , because if user saved categories before they are stored in local
        subscribe(categoryRepository.getLocalSelectedCategories(), Consumer {
            if (it.isNullOrEmpty())
                isMyCategoriesEmpty = true

            userLoggedIn.postValue(Triple(isNewUser, isMyCategoriesEmpty, isOnBoardingFinished))
        })

    }
}


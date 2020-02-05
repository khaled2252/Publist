package co.publist.features.splash

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.SPLASH_DELAY
import javax.inject.Inject


class SplashViewModel @Inject constructor(private val userRepository: UserRepositoryInterface) :
    BaseViewModel() {

    val userLoggedIn = MutableLiveData<Pair<Boolean, Boolean>>()

    fun onCreated() {
        var isNewUser = false
        var isMyCategoriesEmpty = false
        val user = userRepository.getUser()

        if (user == null)
            isNewUser = true
        if(user?.myCategories.isNullOrEmpty())
            isMyCategoriesEmpty = true

        Handler().postDelayed({
            userLoggedIn.postValue(Pair(isNewUser, isMyCategoriesEmpty))
        }, SPLASH_DELAY)
    }
}


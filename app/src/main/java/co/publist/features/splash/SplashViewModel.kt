package co.publist.features.splash

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.SPLASH_DELAY
import co.publist.features.splash.data.SplashRepositoryInterface
import javax.inject.Inject


class SplashViewModel @Inject constructor(private val splashRepository: SplashRepositoryInterface) :
    BaseViewModel() {

    val userLoggedIn = MutableLiveData<Pair<Boolean, Boolean>>()

    fun onCreated() {
        var isNewUser = false
        var isMyCategoriesEmpty = false
        val user = splashRepository.getUser()

        if (user == null)
            isNewUser = true
        if(user?.myCategories.isNullOrEmpty())
            isMyCategoriesEmpty = true

        Handler().postDelayed({
            userLoggedIn.postValue(Pair(isNewUser, isMyCategoriesEmpty))
        }, SPLASH_DELAY)
    }
}


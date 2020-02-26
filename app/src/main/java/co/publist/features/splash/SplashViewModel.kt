package co.publist.features.splash

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Extensions.Constants.SPLASH_DELAY
import co.publist.features.categories.data.CategoriesRepositoryInterface
import io.reactivex.functions.Consumer
import javax.inject.Inject


class SplashViewModel @Inject constructor(private val userRepository: UserRepositoryInterface, private val categoryRepository: CategoriesRepositoryInterface) :
    BaseViewModel() {

    val userLoggedIn = MutableLiveData<Pair<Boolean, Boolean>>()

    fun onCreated() {
        var isNewUser = false
        var isMyCategoriesEmpty = false
        val user = userRepository.getUser()

        if (user == null)
            isNewUser = true

        //Checking Local , because if user saved categories before they are stored in local
        subscribe(categoryRepository.getLocalSelectedCategories(), Consumer {
            if(it.isNullOrEmpty())
                isMyCategoriesEmpty = true

            Handler().postDelayed({
                userLoggedIn.postValue(Pair(isNewUser, isMyCategoriesEmpty))
            }, SPLASH_DELAY)
        })

    }
}


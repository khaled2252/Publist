package co.publist.features.splash

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.SPLASH_DELAY
import javax.inject.Inject


class SplashViewModel @Inject constructor() : BaseViewModel() {

    val loaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun onScreenCreate() {
        Handler().postDelayed({
            loaded.value = true
        }, SPLASH_DELAY)
    }

}
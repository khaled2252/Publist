package co.publist.features.interests

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject


class InterestsViewModel @Inject constructor() : BaseViewModel() {

    val loaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun onScreenCreate() {
        Handler().postDelayed({
            loaded.value = true
        }, 2000)
    }

}
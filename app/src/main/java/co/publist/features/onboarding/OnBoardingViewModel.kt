package co.publist.features.onboarding

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.platform.BaseViewModel
import javax.inject.Inject


class OnBoardingViewModel @Inject constructor(
    private val localDataSource: LocalDataSource
) : BaseViewModel() {
    val onBoardingFinished = MutableLiveData<Boolean>()

    fun finishedOnBoarding() {
        localDataSource.getSharedPreferences().setOnBoardingFinished()
        onBoardingFinished.postValue(true)
    }
}
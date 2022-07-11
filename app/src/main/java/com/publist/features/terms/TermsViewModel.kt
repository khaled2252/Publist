package com.publist.features.terms

import androidx.lifecycle.MutableLiveData
import com.publist.core.common.data.local.LocalDataSource
import com.publist.core.platform.BaseViewModel
import javax.inject.Inject


class TermsViewModel @Inject constructor(
    private val localDataSource: LocalDataSource
) :
    BaseViewModel() {

    val acceptedTermsAndConditions = MutableLiveData<Boolean>()

    fun acceptTermsAndConditions() {
        localDataSource.getSharedPreferences().setTermsAndConditionsAccepted()
        acceptedTermsAndConditions.postValue(true)
    }
}


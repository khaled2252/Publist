package com.publist.features.home

import androidx.lifecycle.MutableLiveData
import com.publist.core.common.data.models.User
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.platform.BaseViewModel
import com.publist.features.categories.data.CategoriesRepositoryInterface
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface
) :
    BaseViewModel() {

    val user = userRepository.getUser()
    var userLiveData = MutableLiveData<User>()
    var profilePictureClickLiveData = MutableLiveData<Boolean>()
    var addWishClickLiveData = MutableLiveData<Boolean>()

    fun onCreated() {
        userLiveData.postValue(user)
    }

    fun handleEditProfile() {
        if (user == null)
            profilePictureClickLiveData.postValue(true)
        else
            profilePictureClickLiveData.postValue(false)
    }

    fun clearGuestSelectedCategories() {
        if (user == null)
            categoryRepository.clearLocalSelectedCategories()
    }

    fun handleAddWish() {
        if (user == null)
            addWishClickLiveData.postValue(true)
        else
            addWishClickLiveData.postValue(false)

    }

}
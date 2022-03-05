package com.publist.features.profile

import androidx.lifecycle.MutableLiveData
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.publist.core.common.data.models.User
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.platform.BaseViewModel
import com.publist.features.categories.data.CategoriesRepositoryInterface
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val mGoogleSignInClient: GoogleSignInClient,
    private val mLoginManager: LoginManager

) : BaseViewModel() {
    var userLiveData = MutableLiveData<User>()
    var logoutLiveData = MutableLiveData<Boolean>()

    init {
        userLiveData.postValue(userRepository.getUser())
    }

    fun handleLogout() {
        //Locally
        categoryRepository.clearLocalSelectedCategories()
        userRepository.deleteCurrentUser()

        //Remotely
        mGoogleSignInClient.signOut().addOnSuccessListener {//Google
            logoutLiveData.postValue(true)
        }

        GraphRequest( //Facebook
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            GraphRequest.Callback {
                mLoginManager.logOut()
                logoutLiveData.postValue(true)
            })
            .executeAsync()
    }
}
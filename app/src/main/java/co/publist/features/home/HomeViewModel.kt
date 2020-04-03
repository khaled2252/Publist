package co.publist.features.home

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val mGoogleSignInClient: GoogleSignInClient,
    private val mLoginManager: LoginManager

) :
    BaseViewModel() {

    val user = userRepository.getUser()
    var userLiveData = MutableLiveData<User>()
    var profilePictureClickLiveData = MutableLiveData<Boolean>()
    var addWishClickLiveData = MutableLiveData<Boolean>()

    fun onCreated() {
        userLiveData.postValue(user)
    }

    fun handleLogout() {
        //Locally
        categoryRepository.clearLocalSelectedCategories()
        userRepository.deleteCurrentUser()

        //Remotely
        mGoogleSignInClient.signOut() //Google

        GraphRequest( //Facebook
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            GraphRequest.Callback { mLoginManager.logOut() })
            .executeAsync()
    }

    fun handleEditProfile() {
        if (user == null)
            profilePictureClickLiveData.postValue(true)
        else
            profilePictureClickLiveData.postValue(false)
    }

    fun clearGuestSelectedCategories() {
        if(user == null)
        categoryRepository.clearLocalSelectedCategories()
    }

    fun handleAddWish() {
        if(user == null)
        addWishClickLiveData.postValue(true)
        else
            addWishClickLiveData.postValue(false)

    }
}
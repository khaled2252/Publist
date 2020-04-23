package co.publist.features.profile

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

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepositoryInterface,
    private val categoryRepository: CategoriesRepositoryInterface,
    private val mGoogleSignInClient: GoogleSignInClient,
    private val mLoginManager: LoginManager

) : BaseViewModel() {
    var userLiveData = MutableLiveData<User>()

    init {
        userLiveData.postValue(userRepository.getUser())
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
}
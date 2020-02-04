package co.publist.features.login

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.PLATFORM_FACEBOOK
import co.publist.core.utils.Utils.Constants.PLATFORM_GOOGLE
import co.publist.features.login.data.LoginRepositoryInterface
import co.publist.features.login.data.RegisteringUser
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepositoryInterface,
    private val mGoogleSignInClient: GoogleSignInClient,
    private val mCallbackManager: CallbackManager
) :
    BaseViewModel() {

    val googleSignInClientLiveData = MutableLiveData<GoogleSignInClient>()
    val callbackManagerLiveData = MutableLiveData<CallbackManager>()
    val newUserLoggedIn = MutableLiveData<Boolean>()

    private lateinit var registeringUser: RegisteringUser

    fun postLiveData() {
        googleSignInClientLiveData.postValue(mGoogleSignInClient)
        callbackManagerLiveData.postValue(mCallbackManager)
    }

    fun googleFirebaseAuth(user: GoogleSignInAccount) {
        subscribe(
            loginRepository.authenticateGoogleUserWithFirebase(user.idToken!!),
            Consumer { uId ->
                registeringUser = RegisteringUser(
                    user.email!!,
                    user.displayName!!,
                    user.id!!,
                    user.idToken!!,
                    user.photoUrl.toString(),
                    uId, PLATFORM_GOOGLE
                )
                getDocumentId(user.email!!)
            })
    }

    fun facebookFirebaseAuth(accessToken: AccessToken) {
        subscribe(
            loginRepository.authenticateFacebookUserWithFirebase(accessToken.token),
            Consumer { uId ->
                setFaceBookGraphRequest(accessToken, uId)
            })
    }

    private fun getDocumentId(email: String) {
        subscribe(loginRepository.fetchUserDocId(email), Consumer { documentId ->
            registerUser(registeringUser, documentId)
        })
    }

    private fun updateProfilePicture(documentId: String, profilePictureUrl: String) {
        subscribe(loginRepository.updateProfilePictureUrl(documentId, profilePictureUrl), Action {
        })
    }

    private fun addUidInUserAccounts(documentId: String, uId: String, platform: String) {
        subscribe(loginRepository.addUidInUserAccounts(documentId, uId, platform), Action {
            handleLoggedInUser(documentId, false)
        })
    }

    private fun handleLoggedInUser(documentId: String, newUser: Boolean) {
        subscribe(loginRepository.fetchUserInformation(documentId), Consumer {
            newUserLoggedIn.postValue(newUser)
        })
    }

    private fun addNewUser(
        email: String,
        name: String,
        pictureUrl: String,
        uid: String,
        platform: String
    ) {
        subscribe(
            loginRepository.addNewUser(email, name, pictureUrl, uid, platform),
            Consumer { documentId ->
                addNewUserAccount(documentId, uid, platform)
            })
    }

    private fun addNewUserAccount(documentId: String, uId: String, platform: String) {
        subscribe(loginRepository.addNewUserAccount(documentId, uId, platform), Action {
            handleLoggedInUser(documentId, true)
        })
    }

    private fun registerUser(
        registeringUser: RegisteringUser,
        documentId: String?
    ) {
        if (documentId == "null") {
            addNewUser(
                registeringUser.email!!,
                registeringUser.name!!,
                registeringUser.profilePictureUrl!!,
                registeringUser.uId!!,
                registeringUser.platform!!
            )

        } else {
            updateProfilePicture(documentId!!, registeringUser.profilePictureUrl!!)
            addUidInUserAccounts(documentId, registeringUser.uId!!, registeringUser.platform!!)
        }
    }

    private fun setFaceBookGraphRequest(accessToken: AccessToken, uId: String) {
        subscribe(
            loginRepository.setFaceBookGraphRequest(accessToken),
            Consumer {
                registeringUser = it
                registeringUser.uId = uId
                registeringUser.platform = PLATFORM_FACEBOOK
                getDocumentId(it.email!!)
            })
    }
}
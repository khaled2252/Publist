package co.publist.features.login

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.features.login.data.LoginRepository
import co.publist.features.login.data.RegisteringUser
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    BaseViewModel() {

    val mGoogleSignInClient = MutableLiveData<GoogleSignInClient>()
    val mCallbackManager = MutableLiveData<CallbackManager>()
    val newUserLoggedIn = MutableLiveData<Boolean>()

    private lateinit var registeringUser: RegisteringUser

    fun postLiveData() {
        mGoogleSignInClient.postValue(loginRepository.mGoogleSignInClient)
        mCallbackManager.postValue(loginRepository.mCallbackManager)
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
                    uId, "google"
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
            newUserLoggedIn.postValue(false)
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

    private fun addNewUserAccount(docId: String, uId: String, platform: String) {
        subscribe(loginRepository.addNewUserAccount(docId, uId, platform), Action {
            newUserLoggedIn.postValue(true)
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
                registeringUser.platform = "facebook"
                getDocumentId(it.email!!)
            })
    }
}
package co.publist.features.login

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.features.login.data.LoginRepository
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    BaseViewModel() {

    val mFirebaseAuth = MutableLiveData<FirebaseAuth>()
    val mFirebaseFirestore = MutableLiveData<FirebaseFirestore>()
    val mGoogleSignInClient = MutableLiveData<GoogleSignInClient>()
    val mCallbackManager = MutableLiveData<CallbackManager>()

    internal val docIdLiveData = MutableLiveData<String?>()

    fun postLiveData() {
        mFirebaseAuth.postValue(loginRepository.mFirebaseAuth)
        mFirebaseFirestore.postValue(loginRepository.mFirebaseFirestore)
        mGoogleSignInClient.postValue(loginRepository.mGoogleSignInClient)
        mCallbackManager.postValue(loginRepository.mCallbackManager)
    }

    fun getDocumentId(email : String){
        subscribe(loginRepository.fetchUserDocId(email), Consumer {
            docIdLiveData.postValue(it)
        })
    }

    private fun updateProfilePicture(documentId : String, profilePictureUrl : String){
        subscribe(loginRepository.updateProfilePictureUrl(documentId,profilePictureUrl), Action {
            //todo toast
        } )
    }

    private fun addUidInUserAccounts(documentId : String, uId : String, platform : String){
        subscribe(loginRepository.addUidInUserAccounts(documentId,uId,platform), Action {
            //todo toast
            //Login existing user completed
            //Navigate to home
        } )
    }

    private fun addNewUser(
        email: String,
        name: String,
        pictureUrl: String,
        uid: String,
        platform: String)
    {
        subscribe(loginRepository.addNewUser(email,name,pictureUrl,uid,platform), Consumer {documentId ->
            addNewUserAccount(documentId, uid, platform)
        } )
    }

    private fun addNewUserAccount(docId: String, uId: String, platform: String){
        subscribe(loginRepository.addNewUserAccount(docId,uId,platform), Action {
            //todo toast
            //Login as a new user completed
            //Navigate to home
        } )
    }

    internal fun registerUser(
        email: String,
        name: String,
        profilePictureUrl: String,
        uId: String,
        platform: String,
        documentId: String?
    ) {
        if (documentId.isNullOrEmpty()) {
            addNewUser(
                email,
                name,
                profilePictureUrl,
                uId,
                platform
            )

        } else {
            updateProfilePicture(documentId, profilePictureUrl)
            addUidInUserAccounts(documentId, uId, platform)
        }
    }
}
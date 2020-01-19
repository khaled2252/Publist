package co.publist.features.login

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.features.login.data.LoginRepository
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    BaseViewModel() {

    val mCallbackManager = MutableLiveData<CallbackManager>()
    val mFirebaseAuth = MutableLiveData<FirebaseAuth>()
    val mGoogleSignInClient = MutableLiveData<GoogleSignInClient>()

    fun postLiveData() {
        mCallbackManager.postValue(loginRepository.callbackManager)
        mFirebaseAuth.postValue(loginRepository.mFirebaseAuth)
        mGoogleSignInClient.postValue(loginRepository.mGoogleSignInClient)
    }
}
package co.publist.features.login

import co.publist.core.platform.BaseViewModel
import co.publist.features.login.data.LoginRepository
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository): BaseViewModel(){

    fun getCallbackManager (): CallbackManager {
       return  loginRepository.callbackManager
    }

    fun getFirebaseAuth (): FirebaseAuth {
        return  loginRepository.mFirebaseAuth
    }

    fun getGoogleSignInClient (): GoogleSignInClient {
        return  loginRepository.mGoogleSignInClient
    }
}
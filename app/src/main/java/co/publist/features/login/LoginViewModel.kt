package co.publist.features.login

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.features.login.data.LoginRepository
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    BaseViewModel() {

    val mFirebaseAuth = MutableLiveData<FirebaseAuth>()
    val mFirebaseFirestore = MutableLiveData<FirebaseFirestore>()
    val mGoogleSignInClient = MutableLiveData<GoogleSignInClient>()
    val mCallbackManager = MutableLiveData<CallbackManager>()

    fun postLiveData() {
        mFirebaseAuth.postValue(loginRepository.mFirebaseAuth)
        mFirebaseFirestore.postValue(loginRepository.mFirebaseFirestore)
        mGoogleSignInClient.postValue(loginRepository.mGoogleSignInClient)
        //mCallbackManager.postValue(loginRepository.callbackManager)
    }
}
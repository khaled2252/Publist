package co.publist.features.login.data

import co.publist.core.platform.BaseRepository
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginRepository @Inject constructor(
    var callbackManager: CallbackManager,
    var mFirebaseAuth: FirebaseAuth,
    var mGoogleSignInClient: GoogleSignInClient
) : BaseRepository(), LoginRepositoryInterface {
    override fun test() {
    }
}
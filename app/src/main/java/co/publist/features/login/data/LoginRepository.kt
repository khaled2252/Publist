package co.publist.features.login.data

import co.publist.core.platform.BaseRepository
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class LoginRepository @Inject constructor(
    var mFirebaseAuth: FirebaseAuth,
    var mFirebaseFirestore: FirebaseFirestore,
    var mGoogleSignInClient: GoogleSignInClient,
    var mCallbackManager: CallbackManager
) : BaseRepository(), LoginRepositoryInterface {
    override fun test() {
    }
}
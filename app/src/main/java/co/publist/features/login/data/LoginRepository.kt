package co.publist.features.login.data

import android.util.Log
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
    override fun fetchUserDocId(email: String,listener : (String?)->Unit){
        mFirebaseFirestore.let {
            it.collection("users")
                .get()
                .addOnFailureListener { exception ->
                    Log.e("LoginRepository", "Error getting documents: ", exception)
                }.addOnSuccessListener{result ->
                for (document in result!!) {
                    if (document.data.containsValue(email)) {
                        listener(document.id)
                    }
                }
                    listener(null)
            }
        }
    }
}
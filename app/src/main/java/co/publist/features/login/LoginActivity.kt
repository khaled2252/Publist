package co.publist.features.login


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject


class LoginActivity : BaseActivity<LoginViewModel>() {

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var credential: AuthCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("AppLog", "key:" + FacebookSdk.getApplicationSignature(this))

        viewModel.postLiveData()
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.mCallbackManager.observe(this, Observer {
            it.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        })
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuth(account!!.idToken!!, "Google")
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }

    }

    private fun setListeners() {
        viewModel.mCallbackManager.observe(this, Observer {
            facebookLoginButton.setPermissions("email", "public_profile")
            facebookLoginButton.registerCallback(it, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    firebaseAuth(loginResult.accessToken.token, "Facebook")
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                }
            })
        })


        buttonFacebook.setOnClickListener {
            facebookLoginButton.performClick()
        }

        buttonGoogle.setOnClickListener {
            viewModel.mGoogleSignInClient.observe(this, Observer {
                val signInIntent = it.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            })
        }
    }

    private fun firebaseAuth(token: String, service: String) {

        if (service == "Facebook")
            credential = FacebookAuthProvider.getCredential(token)
        else if (service == "Google")
            credential = GoogleAuthProvider.getCredential(token, null)

        viewModel.mFirebaseAuth.observe(this, Observer {
            it.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = it.currentUser
                        val docId = getUserDocId(user!!.email!!)
//                        if (docId == null) {
//                            addNewUserAccount(addNewUser(user))
//                            //Login as a new user completed
//                            //Navigate to home
//                        } else {
//                            addUidInUserAccounts(docId)
//                            //Login existing user completed
//                            //Navigate to home
//                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        })
    }

    private fun addUidInUserAccounts(docId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun addNewUserAccount(docId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun addNewUser(user: FirebaseUser): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getUserDocId(email: String): String? {
        viewModel.mFirebaseFirestore.observe(this, Observer {
            it.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if(document.data.containsValue(email))
                            Log.e("kkkk",document.id)
                    }

                    Log.e("kkkk","none")

                }
                .addOnFailureListener{

                }
        })
        return null
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }

}

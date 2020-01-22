package co.publist.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject


class LoginActivity : BaseActivity<LoginViewModel>() {

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseFirestore: FirebaseFirestore? = null
    private var mCallbackManager: CallbackManager? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    lateinit var email: String
    lateinit var name: String
    lateinit var id: String
    lateinit var profilePictureUrl: String
    lateinit var uId: String
    lateinit var platform: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setObservers()
        viewModel.postLiveData()
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager?.let {
            it.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                googleFirebaseAuth(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e)
                // ...
            }
        }

    }

    private fun setListeners() {
        buttonFacebook.setOnClickListener {
            facebookLoginButton.performClick()
        }

        buttonGoogle.setOnClickListener {
            mGoogleSignInClient?.let {
                val signInIntent = it.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun setObservers() {
        viewModel.mFirebaseAuth.observe(this, Observer {
            mFirebaseAuth = it
        })

        viewModel.mFirebaseFirestore.observe(this, Observer {
            mFirebaseFirestore = it
        })

        viewModel.mCallbackManager.observe(this, Observer {
            mCallbackManager = it
            facebookLoginButton.setPermissions("email", "public_profile")
            facebookLoginButton.registerCallback(
                mCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d(TAG, "facebook:onSuccess:$loginResult")
                        facebookFirebaseAuth(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "facebook:onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Log.d(TAG, "facebook:onError", error)
                    }
                })
        })

        viewModel.mGoogleSignInClient.observe(this, Observer {
            mGoogleSignInClient = it
        })

        viewModel.docIdLiveData.observe(this, Observer { documentId ->
            viewModel.registerUser(email, name, profilePictureUrl, uId, platform, documentId)
        })
    }

    private fun googleFirebaseAuth(user: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(user.idToken, null)
        mFirebaseAuth?.let {
            it.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        email = user.email!!
                        name = user.displayName!!
                        id = user.id!!
                        profilePictureUrl = user.photoUrl.toString()
                        uId = it.currentUser!!.uid
                        platform = "google"
                        viewModel.getDocumentId(email)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun facebookFirebaseAuth(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        mFirebaseAuth?.let {
            it.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val request = GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
                            try {
                                email = jsonObject.getString("email")
                                name = jsonObject.getString("name")
                                id = jsonObject.getString("id")
                                profilePictureUrl =
                                    "https://graph.facebook.com/$id/picture?type=large"
                                uId = it.currentUser!!.uid
                                platform = "facebook"
                                viewModel.getDocumentId(email)
                            } catch (e: Exception) {
                                Log.e(TAG, "graphRequest:failure", e)
                                Toast.makeText(
                                    baseContext, "Retrieving user info failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "name,email,id")
                        request.parameters = parameters
                        request.executeAsync()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    companion object {
        private const val TAG = "LoginActivityTag"
        private const val RC_SIGN_IN = 9001
    }

}

package co.publist.features.login


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject


class LoginActivity : BaseActivity<LoginViewModel>() {

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var callbackManager : CallbackManager
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Log.d("AppLog", "key:" + FacebookSdk.getApplicationSignature(this))
        //CJd4ocudeMyO-cyv5X_brcfL_0Y
        mFirebaseAuth = FirebaseAuth.getInstance()

        callbackManager=CallbackManager.Factory.create()

        login_button.setPermissions("email", "public_profile")
        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })

        buttonFacebook.setOnClickListener {

           login_button.performClick()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
//            if (result.isSuccess) { // Google Sign-In was successful, authenticate with Firebase
//                val account = result.signInAccount
//                firebaseAuthWithGoogle(account!!)
//            } else { // Google Sign-In failed
//                Log.e(TAG, "Google Sign-In failed.")
//            }
//        }
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mFirebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mFirebaseAuth.currentUser
                    Log.i("facebook",user.toString())
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show() }
            }
    }

//    private fun signIn() {
//        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }

//    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
//        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
//        mFirebaseAuth.signInWithCredential(credential)
//            .addOnCompleteListener(
//                this
//            ) { task ->
//                Log.d(
//                    TAG,
//                    "signInWithCredential:onComplete:" + task.isSuccessful
//                )
//                // If sign in fails, display a message to the user. If sign in succeeds
//                // the auth state listener will be notified and logic to handle the
//                // signed in user can be handled in the listener.
//                if (!task.isSuccessful) {
//                    Log.w(
//                        TAG,
//                        "signInWithCredential",
//                        task.exception
//                    )
//                    Toast.makeText(
//                        this@SignInActivity, "Authentication failed.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
//                    finish()
//                }
//            }
//    }
    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }

}

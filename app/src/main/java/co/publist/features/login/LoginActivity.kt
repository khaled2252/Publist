package co.publist.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.features.login.data.RegisteringUser
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

    private var mCallbackManager: CallbackManager? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

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
                viewModel.googleFirebaseAuth(account!!)
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
        viewModel.mCallbackManager.observe(this, Observer {
            mCallbackManager = it
            facebookLoginButton.setPermissions("email", "public_profile")
            facebookLoginButton.registerCallback(
                mCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d(TAG, "facebook:onSuccess:$loginResult")
                        viewModel.facebookFirebaseAuth(loginResult.accessToken)
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
    }

    companion object {
        private const val TAG = "LoginActivityTag"
        private const val RC_SIGN_IN = 9001
    }

}

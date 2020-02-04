package co.publist.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.EMAIL_PERMISSION
import co.publist.core.utils.Utils.Constants.PROFILE_PICTURE_PERMISSION
import co.publist.features.editprofile.EditProfileActivity
import co.publist.features.intro.IntroActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
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
                Timber.e(e, "Google sign in failed")
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

        buttonGuest.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }

    private fun setObservers() {
        viewModel.callbackManagerLiveData.observe(this, Observer {
            mCallbackManager = it
            facebookLoginButton.setPermissions(EMAIL_PERMISSION, PROFILE_PICTURE_PERMISSION)
            facebookLoginButton.registerCallback(
                mCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        viewModel.facebookFirebaseAuth(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        Timber.d("facebook:onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Timber.d(error, "facebook:onError")
                    }
                })
        })

        viewModel.googleSignInClientLiveData.observe(this, Observer {
            mGoogleSignInClient = it
        })

        viewModel.userLoggedIn.observe(this, Observer {
            if (it)
                Toast.makeText(this, getString(R.string.registered_successfully), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, getString(R.string.welcome_back), Toast.LENGTH_SHORT).show()

            finish()
            startActivity(Intent(this,EditProfileActivity::class.java))
        })
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

}

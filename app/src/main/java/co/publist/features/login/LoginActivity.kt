package com.publist.features.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.publist.R
import com.publist.core.platform.BaseActivity
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.Utils
import com.publist.core.utils.Utils.Constants.CONNECT_WITH_FACEBOOK
import com.publist.core.utils.Utils.Constants.CONTINUE_AS_GUEST
import com.publist.core.utils.Utils.Constants.DEVICE_ID
import com.publist.core.utils.Utils.Constants.EMAIL_PERMISSION
import com.publist.core.utils.Utils.Constants.FB_ID
import com.publist.core.utils.Utils.Constants.PROFILE_PICTURE_PERMISSION
import com.publist.features.editprofile.EditProfileActivity
import com.publist.features.home.HomeActivity
import com.publist.features.intro.IntroActivity
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject


class LoginActivity : BaseActivity<LoginViewModel>() {

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var mCallbackManager: CallbackManager

    @Inject
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setObservers()
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) { //Coming from GoogleSignIn
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                viewModel.googleFirebaseAuth(account!!)
            } catch (e: ApiException) {
                Timber.e(e, "Google sign in failed")
            }
        } else //Coming from Facebook CallBackManager
            mCallbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setListeners() {
        buttonFacebook.setOnClickListener {
            facebookLoginButton.performClick()
        }

        buttonGoogle.setOnClickListener {
            mGoogleSignInClient.let {
                val signInIntent = it.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

        buttonGuest.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))

            @SuppressLint("HardwareIds")
            val androidID = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
            mFirebaseAnalytics.logEvent(
                CONTINUE_AS_GUEST,
                bundleOf(Pair(DEVICE_ID, androidID))
            )
        }

        facebookLoginButton.setPermissions(EMAIL_PERMISSION, PROFILE_PICTURE_PERMISSION)
        facebookLoginButton.registerCallback(
            mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    viewModel.facebookFirebaseAuth(loginResult.accessToken)
                    mFirebaseAnalytics.logEvent(
                        CONNECT_WITH_FACEBOOK,
                        bundleOf(Pair(FB_ID, loginResult.accessToken.userId))
                    )
                }

                override fun onCancel() {
                    Timber.d("facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    if (!Utils.isConnectedToNetwork(this@LoginActivity))
                        Snackbar.make(
                            this@LoginActivity.window.decorView.rootView,
                            getString(R.string.check_your_internet_connection),
                            Snackbar.LENGTH_LONG
                        ).show()
                }
            })
    }

    private fun setObservers() {
        viewModel.userLoggedIn.observe(this, Observer { pair ->
            val isNewUser = pair.first
            val isMyCategoriesEmpty = pair.second
            if (isMyCategoriesEmpty) {
                if (isNewUser) {
                    Toast.makeText(
                        this,
                        getString(R.string.registered_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, EditProfileActivity::class.java))
                } else {
                    Toast.makeText(this, getString(R.string.welcome_back), Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, EditProfileActivity::class.java))

                }
            } else {
                Toast.makeText(this, getString(R.string.welcome_back), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()

        })
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

}

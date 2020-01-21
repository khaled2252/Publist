package co.publist.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.features.login.data.User
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject


class LoginActivity : BaseActivity<LoginViewModel>() {

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private val docId = MutableLiveData<String?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel.postLiveData()
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.mCallbackManager.observe(this, Observer {
            it.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        })

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                googleFirebaseAuth(account!!)
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

    private fun googleFirebaseAuth(user: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(user.idToken, null)
        viewModel.mFirebaseAuth.observe(this, Observer {
            it.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        getUserDocId(user.email!!)
                        docId.observe(this, Observer { documentId ->
                            if (documentId.isNullOrEmpty()) {
                                addNewUser(
                                    user.email!!,
                                    user.displayName!!,
                                    user.photoUrl.toString(),
                                    it?.currentUser!!.uid,
                                    "google"
                                )

                            } else {
                                updateProfilePictureUrl(documentId,user.photoUrl.toString())
                                addUidInUserAccounts(documentId, it.currentUser!!.uid,"google")
                                //Login existing user completed
                                //Navigate to home
                            }
                        })
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

    private fun facebookFirebaseAuth(accessToken: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        viewModel.mFirebaseAuth.observe(this, Observer {
            it.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val request = GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
                            try {
                                val email = jsonObject.getString("email")
                                val name = jsonObject.getString("name")
                                val id = jsonObject.getString("id")
                                val profilePictureUrl = "https://graph.facebook.com/$id/picture?type=large"
                                getUserDocId(email)
                                docId.observe(this, Observer { documentId ->
                                    if (documentId.isNullOrEmpty()) {
                                        addNewUser(
                                            email,
                                            name,
                                            profilePictureUrl,
                                            it?.currentUser!!.uid,
                                            "facebook"
                                        )

                                    } else {
                                        updateProfilePictureUrl(documentId,profilePictureUrl)
                                        addUidInUserAccounts(documentId, it.currentUser!!.uid,"facebook")
                                        //Login existing user completed
                                        //Navigate to home
                                    }
                                })

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "name,email,id")
                        request.parameters = parameters
                        request.executeAsync()


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

    private fun updateProfilePictureUrl(documentId: String, profilePictureUrl: String) {
        viewModel.mFirebaseFirestore.observe(this, Observer {
            // Get a reference to the restaurants collection
            val users: CollectionReference = it.collection("users")
            val data = hashMapOf("profilePictureUrl" to profilePictureUrl)
            users.document(documentId).set(data, SetOptions.merge())
        })
    }

    private fun addUidInUserAccounts(docId: String, uId: String,platform: String) {
        viewModel.mFirebaseFirestore.observe(this, Observer {
            // Get a reference to the restaurants collection
            val userAccounts: CollectionReference = it.collection("userAccounts")
            val userAccount = hashMapOf(
                platform to uId
            )
            userAccounts.document(docId).set(userAccount,SetOptions.merge())
        })    }

    private fun addNewUserAccount(docId: String, uId: String, platform: String) {
        viewModel.mFirebaseFirestore.observe(this, Observer {
            // Get a reference to the restaurants collection
            val userAccounts: CollectionReference = it.collection("userAccounts")
            val userAccount = hashMapOf(
                platform to uId
            )
            userAccounts.document(docId).set(userAccount)
        })
    }

    private fun addNewUser(
        email: String,
        name: String,
        pictureUrl: String,
        uid: String,
        platform: String
    ) {
        viewModel.mFirebaseFirestore.observe(this, Observer {
            // Get a reference to the restaurants collection
            val users: CollectionReference = it.collection("users")
            users.add(User(email, name, pictureUrl)).addOnSuccessListener { documentReference ->
                addNewUserAccount(documentReference.id, uid, platform)
                //Login as a new user completed
                //Navigate to home
            }
        })
    }

    private fun getUserDocId(email: String) {
        viewModel.mFirebaseFirestore.observe(this, Observer {
            it.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.data.containsValue(email))
                        {
                            docId.postValue(document.id)
                            return@addOnSuccessListener
                        }
                    }
                    docId.postValue(null)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        })
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }

}

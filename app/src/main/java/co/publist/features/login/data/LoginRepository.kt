package co.publist.features.login.data

import android.os.Bundle
import android.util.Log
import co.publist.core.platform.BaseRepository
import co.publist.features.login.LoginActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class LoginRepository @Inject constructor(
    var mFirebaseAuth: FirebaseAuth,
    var mFirebaseFirestore: FirebaseFirestore,
    var mGoogleSignInClient: GoogleSignInClient,
    var mCallbackManager: CallbackManager
) : BaseRepository(), LoginRepositoryInterface {
    override fun fetchUserDocId(email: String): Single<String?> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.let {
                it.collection("users")
                    .get()
                    .addOnFailureListener { exception ->
                        Log.e("LoginRepository", "Error getting documents: ", exception)
                        singleEmitter.onError(exception)
                    }.addOnSuccessListener { result ->
                        for (document in result!!) {
                            if (document.data.containsValue(email)) {
                                singleEmitter.onSuccess(document.id)
                                return@addOnSuccessListener
                            }
                        }
                        singleEmitter.onSuccess("null")
                    }
            }
        }
    }

    override fun updateProfilePictureUrl(
        documentId: String,
        profilePictureUrl: String
    ): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore.let {
                // Get a reference to the restaurants collection
                val users: CollectionReference = it.collection("users")
                val data = hashMapOf("profilePictureUrl" to profilePictureUrl)
                users.document(documentId).set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("LoginRepository", "update profile picture successfully")
                        completableEmitter.onComplete()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("LoginRepository", exception.message.toString())
                        completableEmitter.onError(exception)
                    }
            }
        }
    }

    override fun addUidInUserAccounts(docId: String, uId: String, platform: String): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore.let {
                // Get a reference to the restaurants collection
                val userAccounts: CollectionReference = it.collection("userAccounts")
                val userAccount = hashMapOf(
                    platform to uId
                )
                userAccounts.document(docId).set(userAccount, SetOptions.merge())
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                        Log.d("LoginRepository", "Added uid in user accounts successfully")
                    }.addOnFailureListener { exception ->
                        completableEmitter.onError(exception)
                    Log.e("LoginRepository", exception.message.toString())
                }
            }
        }
    }

    override fun addNewUserAccount(docId: String, uId: String, platform: String) : Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore.let {
                // Get a reference to the restaurants collection
                val userAccounts: CollectionReference = it.collection("userAccounts")
                val userAccount = hashMapOf(
                    platform to uId
                )
                userAccounts.document(docId).set(userAccount).addOnSuccessListener {
                    completableEmitter.onComplete()
                    Log.d("LoginRepository", "Added new user account successfully")
                }.addOnFailureListener { exception ->
                    completableEmitter.onError(exception)
                    Log.e("LoginRepository", exception.message.toString())
                }
            }
        }
    }
    override fun addNewUser(
        email: String,
        name: String,
        pictureUrl: String,
        uid: String,
        platform: String
    ) : Single<String> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.let { it ->

                // Get a reference to the restaurants collection
                val users: CollectionReference = it.collection("users")
                users.add(User(email, name, pictureUrl)).addOnSuccessListener { documentReference ->
                    singleEmitter.onSuccess(documentReference.id)
                }.addOnFailureListener { exception ->
                    Log.e("LoginRepository", exception.message.toString())
                    singleEmitter.onError(exception)
                }
            }
        }
    }

    override fun authenticateGoogleUserWithFirebase(
        userIdToken: String
    ): Single<String>{
        return Single.create { singleEmitter ->
            mFirebaseAuth.let {
                val credential = GoogleAuthProvider.getCredential(userIdToken, null)
                it.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LoginRepository", "signInWithCredential:success")
                        singleEmitter.onSuccess(result.user!!.uid)
                    }.addOnFailureListener{exception ->
                        singleEmitter.onError(exception)
                            // If sign in fails, display a message to the user.
                            Log.e("LoginRepository", "signInWithCredential:failure", exception)
                        }
                    }
            }
    }

    override fun authenticateFacebookUserWithFirebase(
        accessToken: String
    ): Single<String>{
        return Single.create { singleEmitter ->
            mFirebaseAuth.let {
                val credential = FacebookAuthProvider.getCredential(accessToken)
                it.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LoginRepository", "signInWithCredential:success")
                        singleEmitter.onSuccess(result.user!!.uid)
                    }.addOnFailureListener{exception ->
                        singleEmitter.onError(exception)
                        // If sign in fails, display a message to the user.
                        Log.e("LoginRepository", "signInWithCredential:failure", exception)
                    }
            }
        }
    }

    override fun setFaceBookGraphRequest(
        accessToken: AccessToken
    ): Single<RegisteringUser>{
        return Single.create { singleEmitter ->

            val request = GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
                try {
                    val email = jsonObject.getString("email")
                    val name = jsonObject.getString("name")
                    val id = jsonObject.getString("id")
                    val profilePictureUrl =
                        "https://graph.facebook.com/$id/picture?type=large"
                    singleEmitter.onSuccess(RegisteringUser(email,name,profilePictureUrl=profilePictureUrl))
                } catch (e: Exception) {
                    Log.e("LoginRepository", "graphRequest:failure", e)
                    singleEmitter.onError(e)
                }
            }

            val parameters = Bundle()
            parameters.putString("fields", "name,email,id")
            request.parameters = parameters
            request.executeAsync()
        }
    }


}
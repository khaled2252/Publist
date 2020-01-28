package co.publist.features.login.data

import android.os.Bundle
import co.publist.core.data.local.LocalDataSource
import co.publist.core.data.models.User
import co.publist.core.platform.BaseRepository
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.GraphRequest
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
    private val mFirebaseAuth: FirebaseAuth,
    private val mFirebaseFirestore: FirebaseFirestore,
    val mGoogleSignInClient: GoogleSignInClient, //todo move google,callback to viewmodel
    val mCallbackManager: CallbackManager,
    private val localDataSource: LocalDataSource

) : BaseRepository(), LoginRepositoryInterface {
    override fun fetchUserDocId(email: String): Single<String?> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.let {
                it.collection("users")
                    .get()
                    .addOnFailureListener { exception ->
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
                val users: CollectionReference = it.collection("users")
                val data = hashMapOf("profilePictureUrl" to profilePictureUrl)
                users.document(documentId).set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }
                    .addOnFailureListener { exception ->
                        completableEmitter.onError(exception)
                    }
            }
        }
    }

    override fun addUidInUserAccounts(docId: String, uId: String, platform: String): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore.let {
                val userAccounts: CollectionReference = it.collection("userAccounts")
                val userAccount = hashMapOf(
                    platform to uId
                )
                userAccounts.document(docId).set(userAccount, SetOptions.merge())
                    .addOnSuccessListener {
                        completableEmitter.onComplete()
                    }.addOnFailureListener { exception ->
                        completableEmitter.onError(exception)
                    }
            }
        }
    }

    override fun addNewUserAccount(docId: String, uId: String, platform: String): Completable {
        return Completable.create { completableEmitter ->
            mFirebaseFirestore.let {
                val userAccounts: CollectionReference = it.collection("userAccounts")
                val userAccount = hashMapOf(
                    platform to uId
                )
                userAccounts.document(docId).set(userAccount).addOnSuccessListener {
                    completableEmitter.onComplete()
                }.addOnFailureListener { exception ->
                    completableEmitter.onError(exception)
                }
            }
        }
    }

    override fun addNewUser(
        email: String,
        name: String,
        profilePictureUrl: String,
        uid: String,
        platform: String
    ): Single<String> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.let { it ->
                val users: CollectionReference = it.collection("users")
                val data = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "profilePictureUrl" to profilePictureUrl
                )
                users.add(data).addOnSuccessListener { documentReference ->
                    singleEmitter.onSuccess(documentReference.id)
                }.addOnFailureListener { exception ->
                    singleEmitter.onError(exception)
                }
            }
        }
    }

    override fun authenticateGoogleUserWithFirebase(
        userIdToken: String
    ): Single<String> {
        return Single.create { singleEmitter ->
            mFirebaseAuth.let {
                val credential = GoogleAuthProvider.getCredential(userIdToken, null)
                it.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        singleEmitter.onSuccess(result.user!!.uid)
                    }.addOnFailureListener { exception ->
                        singleEmitter.onError(exception)
                    }
            }
        }
    }

    override fun authenticateFacebookUserWithFirebase(
        accessToken: String
    ): Single<String> {
        return Single.create { singleEmitter ->
            mFirebaseAuth.let {
                val credential = FacebookAuthProvider.getCredential(accessToken)
                it.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        singleEmitter.onSuccess(result.user!!.uid)
                    }.addOnFailureListener { exception ->
                        singleEmitter.onError(exception)
                    }
            }
        }
    }

    override fun setFaceBookGraphRequest(
        accessToken: AccessToken
    ): Single<RegisteringUser> {
        return Single.create { singleEmitter ->

            val request = GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
                try {
                    val email = jsonObject.getString("email")
                    val name = jsonObject.getString("name")
                    val id = jsonObject.getString("id")
                    val profilePictureUrl =
                        "https://graph.facebook.com/$id/picture?type=large"
                    singleEmitter.onSuccess(
                        RegisteringUser(
                            email,
                            name,
                            profilePictureUrl = profilePictureUrl
                        )
                    )
                } catch (e: Exception) {
                    singleEmitter.onError(e)
                }
            }

            val parameters = Bundle()
            parameters.putString("fields", "name,email,id")
            request.parameters = parameters
            request.executeAsync()
        }
    }

    override fun fetchUserInformation(userDocId: String): Single<User> {
        return Single.create { singleEmitter ->
            mFirebaseFirestore.let {
                it.collection("users")
                    .document(userDocId)
                    .get().addOnFailureListener { exception ->
                        singleEmitter.onError(exception)
                    }.addOnSuccessListener { documentSnapshot ->
                        val user = documentSnapshot.toObject(User::class.java)
                        singleEmitter.onSuccess(user!!)
                    }
            }
        }
    }

    override fun saveUserToSharedPreferences(user: User) {
        localDataSource.getSharedPreferences().saveUser(user)
    }

}
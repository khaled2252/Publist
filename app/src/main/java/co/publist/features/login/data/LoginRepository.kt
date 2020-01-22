package co.publist.features.login.data

import android.util.Log
import co.publist.core.platform.BaseRepository
import co.publist.features.login.LoginActivity
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
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
                            }
                        }
                        singleEmitter.onSuccess(null.toString())
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


}
package co.publist.features.login.data

import co.publist.core.common.data.models.User
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.Completable
import io.reactivex.Single

interface LoginRepositoryInterface {
    fun fetchUserDocId(email : String): Single<String?>
    fun updateProfilePictureUrl(documentId: String, profilePictureUrl: String): Completable
    fun addUidInUserAccounts(docId: String, uId: String, platform: String): Completable
    fun addNewUser(
        email: String,
        name: String,
        profilePictureUrl: String,
        uid: String,
        platform: String
    ): Single<String>

    fun addNewUserAccount(docId: String, uId: String, platform: String): Completable
    fun authenticateGoogleUserWithFirebase(userIdToken: String): Single<String>
    fun authenticateFacebookUserWithFirebase(accessToken: String): Single<String>
    fun setFaceBookGraphRequest(accessToken: AccessToken): Single<RegisteringUser>
    fun fetchUserInformation(userDocId: String): Single<User>
    fun setUserInformation(user : User)
    fun googleSignInOneObservable(user : GoogleSignInAccount) : Single<User>
}
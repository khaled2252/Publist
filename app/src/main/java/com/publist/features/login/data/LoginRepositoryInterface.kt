package com.publist.features.login.data

import com.facebook.AccessToken
import com.publist.core.common.data.models.User
import io.reactivex.Completable
import io.reactivex.Single

interface LoginRepositoryInterface {
    fun fetchUserDocId(email: String): Single<String?>
    fun updateProfilePictureUrl(userDocumentId: String, profilePictureUrl: String): Completable
    fun addUidInUserAccounts(docId: String, uId: String, platform: String): Completable
    fun addNewUser(
        email: String,
        name: String,
        profilePictureUrl: String,
        facebookId: String?
    ): Single<String>

    fun authenticateGoogleUserWithFirebase(userIdToken: String): Single<String>
    fun authenticateFacebookUserWithFirebase(accessToken: String): Single<String>
    fun setFaceBookGraphRequest(accessToken: AccessToken): Single<RegisteringUser>
    fun fetchUserInformation(userDocId: String): Single<User>
    fun setUserInformation(user: User)
}
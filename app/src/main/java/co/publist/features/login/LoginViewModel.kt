package co.publist.features.login

import androidx.lifecycle.MutableLiveData
import co.publist.core.platform.BaseViewModel
import co.publist.core.utils.Utils.Constants.NULL_STRING
import co.publist.core.utils.Utils.Constants.PLATFORM_FACEBOOK
import co.publist.core.utils.Utils.Constants.PLATFORM_GOOGLE
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.login.data.LoginRepositoryInterface
import co.publist.features.login.data.RegisteringUser
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.Single
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepositoryInterface,
    private val categoriesRepository: CategoriesRepositoryInterface
) :
    BaseViewModel() {

    val userLoggedIn = MutableLiveData<Pair<Boolean, Boolean>>()

    private lateinit var registeringUser: RegisteringUser

    private fun registerFlow(registeringUser: RegisteringUser): Single<Pair<Boolean, Boolean>> {
        return loginRepository.fetchUserDocId(registeringUser.email!!)
            .flatMap { documentId ->
                registerUser(registeringUser, documentId)
            }.flatMap { registeredUser ->
                val userDocumentId = registeredUser.first
                val isNewUser = registeredUser.second
                loginRepository.fetchUserInformation(userDocumentId)
                    .flatMap { user ->
                        loginRepository.setUserInformation(user)  // Set user in shared preferences
                        //Fetching selected categories
                        //Checking remote , because if user didn't save categories it will not be in remote,
                        //not checking local , because it will be empty in both cases (saved or not saved),
                        //because new user is logging in i.e previous data is cleared after logout
                        categoriesRepository.fetchUserSelectedCategories()
                            .flatMap { categoryList ->
                                categoriesRepository.updateLocalSelectedCategories(
                                    categoryList
                                )
                                Single.just(Pair(isNewUser, categoryList.isEmpty()))
                            }
                    }
            }
    }

    private fun registerUser(
        registeringUser: RegisteringUser,
        documentId: String?
    ): Single<Pair<String, Boolean>> {
        if (documentId == NULL_STRING) {
            return loginRepository.addNewUser(
                registeringUser.email!!,
                registeringUser.name!!,
                registeringUser.profilePictureUrl!!,
                registeringUser.uId!!,
                registeringUser.platform!!
            ).flatMap { newDocumentId ->
                loginRepository.addUidInUserAccounts(
                    newDocumentId,
                    registeringUser.uId!!,
                    registeringUser.platform!!
                ).andThen(Single.just(Pair(newDocumentId, true)))
            }
        } else {
            return loginRepository.addUidInUserAccounts(
                documentId!!,
                registeringUser.uId!!,
                registeringUser.platform!!
            )
                .mergeWith(
                    loginRepository.updateProfilePictureUrl(
                        documentId,
                        registeringUser.profilePictureUrl!!
                    )
                ).andThen(Single.just(Pair(documentId, false)))
        }
    }

    fun googleFirebaseAuth(user: GoogleSignInAccount) {
        subscribe(
            loginRepository.authenticateGoogleUserWithFirebase(user.idToken!!).flatMap { uId ->
                registeringUser = RegisteringUser(
                    user.email!!,
                    user.displayName!!,
                    user.id!!,
                    user.idToken!!,
                    user.photoUrl.toString(),
                    uId, PLATFORM_GOOGLE
                )
                registerFlow(registeringUser)
            },
            Consumer { registeredUser ->
                val isNewUser = registeredUser.first
                val isCategoriesEmpty = registeredUser.second
                userLoggedIn.postValue(Pair(isNewUser, isCategoriesEmpty))
            })
    }

    fun facebookFirebaseAuth(accessToken: AccessToken) {
        subscribe(
            loginRepository.authenticateFacebookUserWithFirebase(accessToken.token).flatMap { uId ->
                loginRepository.setFaceBookGraphRequest(accessToken)
                    .flatMap { registeringUser ->
                        registeringUser.uId = uId
                        registeringUser.platform = PLATFORM_FACEBOOK
                        registerFlow(registeringUser)
                    }
            },
            Consumer { registeredUser ->
                val isNewUser = registeredUser.first
                val isCategoriesEmpty = registeredUser.second
                userLoggedIn.postValue(Pair(isNewUser, isCategoriesEmpty))
            })
    }

}
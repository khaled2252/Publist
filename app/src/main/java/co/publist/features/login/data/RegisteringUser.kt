package co.publist.features.login.data

data class RegisteringUser(
    var email: String? = null,
    var name: String? = null,
    var id: String? = null,
    var idToken: String? = null,
    var profilePictureUrl: String? = null,
    var uId: String? = null,
    var platform: String? = null
)
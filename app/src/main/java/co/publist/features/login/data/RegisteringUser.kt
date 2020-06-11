package co.publist.features.login.data

data class RegisteringUser(
    //This will go to 'users' in Firestore database
    var email: String? = null,
    var name: String? = null,
    var profilePictureUrl: String? = null,
    var facebookId: String? = null,

    //This will go to 'userAccounts' in Firestore database
    var uId: String? = null, //Firebase unique authentication identifier
    var platform: String? = null
)
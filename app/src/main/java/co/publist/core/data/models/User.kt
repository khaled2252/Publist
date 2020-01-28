package co.publist.core.data.models

data class User (
    var myCategories : ArrayList<String>? = null,
    var myFavorites : ArrayList<String>? = null,
    var myLists : ArrayList<String>? = null,
    var email : String? = null,
    var name : String? = null,
    var profilePictureUrl : String? = null
)
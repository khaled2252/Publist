package co.publist.features.login.data

interface LoginRepositoryInterface {
    fun fetchUserDocId(email : String,listener : (String?)->Unit)
}
package co.publist.features.home.data


interface HomeRepositoryInterface {
    fun getGuestCategories() : ArrayList<String>?
}
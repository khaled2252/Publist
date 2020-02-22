package co.publist.core.common.data.models

import com.google.firebase.Timestamp

data class Wish (
    var category :ArrayList<Category>,
    var date :Timestamp,
    var title :String,
    val creator : Creator,
    val favoritesCount : Int = 0,
    var wishPhotoURL :String? = null,
    var items :Map<String, Item>
)
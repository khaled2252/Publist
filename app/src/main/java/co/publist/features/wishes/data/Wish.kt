package co.publist.features.wishes.data

import co.publist.core.common.data.models.Category
import com.google.firebase.Timestamp

class Wish {
    var category :ArrayList<Category>? = null
    var date :Timestamp? = null
    var title :String? = null
    val creator : Creator? = null
    var wishPhotoURL :String? = null
    var items :Map<String,Todo>? = null
}
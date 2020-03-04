package co.publist.core.common.data.models.wish

import co.publist.core.common.data.models.category.Category
import com.google.firebase.Timestamp

data class Wish(
    var category:ArrayList<Category>? = null,
    var categoryId: ArrayList<String> ,
    var date:Timestamp? = null,
    var title:String? = null,
    val creator: Creator? = null,
    val favoritesCount: Int = 0,
    var wishPhotoURL:String? = null,
    var items: Map<String, Item>? = null,
    var itemsId: ArrayList<String>? = null,
    var wishId:String? = null
)
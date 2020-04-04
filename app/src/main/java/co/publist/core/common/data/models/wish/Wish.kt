package co.publist.core.common.data.models.wish

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
@Parcelize
data class Wish(
    var category: ArrayList<CategoryWish>? = null,
    var categoryId: ArrayList<String>? = null,
    var date: Timestamp? = null,
    var title: String? = null,
    val creator: Creator? = null,
    val favoritesCount: Int = 0,
    var wishPhotoURL: String? = null,
    var photoName: String? = null,
    var items: Map<String, WishItem>? = null,
    var itemsId: ArrayList<String>? = null,
    var wishId: String? = null,
    var seenCount : Int? = null,
    var organicSeenCount : Int? = null,
    var organicSeenUserIds : ArrayList<String>? = null
) : Parcelable
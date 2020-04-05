package co.publist.core.common.data.models.wish

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WishAdapterItem(
    var category: ArrayList<CategoryWish>? = null,
    var categoryId: ArrayList<String>? = null,
    var date: Timestamp? = null,
    var title: String? = null,
    val creator: Creator? = null,
    val favoritesCount: Int = 0,
    var wishPhotoURL: String? = "",
    var photoName: String? = "",
    var items: Map<String, WishItem>? = null,
    var itemsId: ArrayList<String>? = null,
    var wishId: String? = null,
    var seenCount : Int? = 0,
    var organicSeenCount : Int? = 0,
    @set:Exclude @get:Exclude var isCreator: Boolean = false,
    @set:Exclude @get:Exclude var isFavorite: Boolean = false
) : Parcelable
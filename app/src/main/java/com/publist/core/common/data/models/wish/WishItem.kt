package com.publist.core.common.data.models.wish

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class WishItem(
    var name: String? = null,
    var orderId: Int? = null,
    var completeCount: Int? = 0,
    var viewedCount: Int? = 0,
    var topCompletedUsersId: ArrayList<String>? = arrayListOf(),
    var topViewedUsersId: ArrayList<String>? = arrayListOf(),
    @get:Exclude var done: Boolean? = false, //will get attribute but not set it in firestore
    @set:Exclude @get:Exclude var isLiked: Boolean? = false
) : Serializable
package com.publist.core.common.data.models.wish

import java.io.Serializable

data class EditedWishItem(
    var name: String? = null,
    var orderId: Int? = null,
    var completeCount: Int? = 0,
    var viewedCount: Int? = 0,
    var topCompletedUsersId: ArrayList<String>? = arrayListOf(),
    var topViewedUsersId: ArrayList<String>? = arrayListOf(),
    var done: Boolean? = false,
    var isLiked: Boolean? = false
) : Serializable
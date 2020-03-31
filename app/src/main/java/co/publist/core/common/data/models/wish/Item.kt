package co.publist.core.common.data.models.wish

import java.io.Serializable

data class Item(
    var name: String? = null,
    var orderId: Int? = null,
    var completeCount: Int? = 0,
    var viewedCount: Int? = 0,
    var topCompletedUsersId: ArrayList<String>? = null,
    var topViewedUsersId: ArrayList<String>? = null,
    var done : Boolean? = false,
    var isLiked : Boolean? = false
) : Serializable
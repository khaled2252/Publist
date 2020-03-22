package co.publist.core.common.data.models.wish

import java.io.Serializable

data class Item (
    var completeCount :Int? = 0,
    var name :String? = null,
    var orderId :Int? = null,
    var viewedCount :Int? = 0
) : Serializable
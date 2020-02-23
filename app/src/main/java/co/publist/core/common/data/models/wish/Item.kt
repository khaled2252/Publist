package co.publist.core.common.data.models.wish

data class Item (
    var completeCount :Int? = 0,
    var name :String? = null,
    var orderId :Int? = null,
    var topCompletedUsersId :ArrayList<String>? = null,
    var topViewedUsersId :ArrayList<String>? = null,
    var viewedCount :Int? = 0
)
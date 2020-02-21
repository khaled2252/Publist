package co.publist.core.common.data.models

class Todo {
    var completeCount :Int? = null
    var name :String? = null
    var done :Boolean? = null
    var orderId :Int? = null
    var topCompletedUsersId :ArrayList<String>? = null
    var topViewedUsersId :ArrayList<String>? = null
    var viewedCount :Int? = null
}
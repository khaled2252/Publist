package co.publist.core.common.data.models.wish

import java.io.Serializable

data class Creator(
    var id: String? = null,
    var imagePath: String? = null,
    var name: String? = null
) : Serializable

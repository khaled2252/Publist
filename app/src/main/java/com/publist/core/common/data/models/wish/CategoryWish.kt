package com.publist.core.common.data.models.wish

import java.io.Serializable

data class CategoryWish(
    var id: String? = null,
    var lang: String? = null,
    var name: String? = null
) : Serializable
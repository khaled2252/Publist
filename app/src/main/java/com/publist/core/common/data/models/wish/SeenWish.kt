package com.publist.core.common.data.models.wish

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "SeenWishes")
data class SeenWish(
    @PrimaryKey @field:SerializedName("wishId") val wish_id: String
)
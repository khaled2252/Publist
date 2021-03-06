package com.publist.core.common.data.models.wish

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

@Entity(tableName = "MyFavorites")
data class MyFavoritesDbEntity(
    @PrimaryKey @field:SerializedName("wishId") val wish_id: String,
    @field:SerializedName("category") val category: ArrayList<CategoryWish>,
    @field:SerializedName("categoryId") val category_id: ArrayList<String>,
    @field:SerializedName("date") val date: Timestamp,
    @field:SerializedName("title") val title: String,
    @field:SerializedName("creator") val creator: Creator,
    @field:SerializedName("favoritesCount") val favorites_count: Int,
    @field:SerializedName("wishPhotoUrl") val wish_photo_url: String,
    @field:SerializedName("photoName") val photo_name: String,
    @field:SerializedName("items") val items: Map<String, WishItem>,
    @field:SerializedName("itemsId") val items_id: ArrayList<String>

)
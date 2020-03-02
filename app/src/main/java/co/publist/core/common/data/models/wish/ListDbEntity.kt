package co.publist.core.common.data.models.wish

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.publist.core.common.data.models.category.Category
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Lists")
data class ListDbEntity(
    @PrimaryKey @field:SerializedName("wishId") val wishId: String,
    @field:SerializedName("category") val category: ArrayList<Category>,
    @field:SerializedName("categoryId") val categoryId: ArrayList<String>,
    @field:SerializedName("date") val date: Timestamp,
    @field:SerializedName("title") val title: String,
    @field:SerializedName("creator") val creator: Creator,
    @field:SerializedName("favoritesCount") val favoritesCount: Int,
    @field:SerializedName("wishPhotoUrl") val wishPhotoUrl: String,
    @field:SerializedName("items") val items: Map<String, Item>,
    @field:SerializedName("itemsId") val itemsId: ArrayList<String>

)
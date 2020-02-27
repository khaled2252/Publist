package co.publist.core.common.data.models.category

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Categories")
data class CategoryDbEntity(
    @PrimaryKey @field:SerializedName("id") val id: String,
    @field:SerializedName("localizations") val localizations: Localization,
    @field:SerializedName("name") val name: String

)
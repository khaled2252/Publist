package co.publist.core.common.data.local.db

import androidx.room.TypeConverter
import co.publist.core.common.data.models.category.Localization
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun toLocalization(json: String): Localization {
        val type = object : TypeToken<Localization>(){}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toJson(localization: Localization): String {
        val type = object: TypeToken<Localization>(){}.type
        return Gson().toJson(localization, type)
    }

}
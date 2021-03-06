package com.publist.core.common.data.local.db

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.publist.core.common.data.models.category.Category
import com.publist.core.common.data.models.category.Localization
import com.publist.core.common.data.models.wish.CategoryWish
import com.publist.core.common.data.models.wish.Creator
import com.publist.core.common.data.models.wish.WishItem
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun toLocalization(json: String): Localization {
        val type = object : TypeToken<Localization>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toCategory(json: String): Category {
        val type = object : TypeToken<Category>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toCategoryWish(json: String): CategoryWish {
        val type = object : TypeToken<CategoryWish>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toDate(json: String): Timestamp {
        val type = object : TypeToken<Timestamp>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toCreator(json: String): Creator {
        val type = object : TypeToken<Creator>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toCategoryArrayList(value: String?): ArrayList<CategoryWish?>? {
        val listType: Type =
            object : TypeToken<ArrayList<CategoryWish>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toItemMap(value: String?): Map<String, WishItem?>? {
        val listType: Type =
            object : TypeToken<Map<String, WishItem?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toStringArrayList(value: String?): ArrayList<String?>? {
        val listType: Type =
            object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toJson(localization: Localization): String {
        val type = object : TypeToken<Localization>() {}.type
        return Gson().toJson(localization, type)
    }

    @TypeConverter
    fun toJson(category: CategoryWish): String {
        val type = object : TypeToken<CategoryWish>() {}.type
        return Gson().toJson(category, type)
    }

    @TypeConverter
    fun toJson(list: ArrayList<*>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toJson(map: Map<*, *>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun toJson(timestamp: Timestamp): String {
        val type = object : TypeToken<Timestamp>() {}.type
        return Gson().toJson(timestamp, type)
    }

    @TypeConverter
    fun toJson(creator: Creator): String {
        val type = object : TypeToken<Creator>() {}.type
        return Gson().toJson(creator, type)
    }


}
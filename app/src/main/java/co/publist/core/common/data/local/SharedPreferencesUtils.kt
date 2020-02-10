package co.publist.core.common.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import co.publist.core.common.data.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

import javax.inject.Inject

class SharedPreferencesUtils @Inject constructor(context: Context) :
    PublistSharedPreferencesInterface, SharedPreferencesInterface {
    private val mPrefs: SharedPreferences

    init {
        mPrefs = context.getSharedPreferences(MY_PREFS, MODE_PRIVATE)
    }

    override fun getPref(): SharedPreferences {
        return mPrefs
    }

    override fun putString(key: String, value: String) {
        getPref().edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return getPref().getString(key, null)
    }

    override fun clearData() {
        getPref().edit().clear().apply()
    }

    override fun setUser(user: User) {
        val json = Gson().toJson(user)
        putString(USER_TAG, json)
    }

    override fun getUser(): User? {
        val json = getPref().getString(USER_TAG, null)
        return Gson().fromJson<User>(json, User::class.java)
    }

    override fun updateUserCategories(categoriesList: ArrayList<String>) {
        val gson = Gson()
        val json = getPref().getString(USER_TAG, null)
        val userObject = gson.fromJson<User>(json, User::class.java)
        userObject.myCategories = categoriesList
        putString(USER_TAG, gson.toJson(userObject))
    }

    override fun deleteUser() {
        getPref().edit().remove(USER_TAG).apply()
    }

    override fun saveTemporaryCategories(selectedCategoriesList: ArrayList<String>) {
        val json = Gson().toJson(selectedCategoriesList)
        putString(TEMPORARY_CATEGORIES_TAG, json)
    }

    override fun getTemporaryCategories(): ArrayList<String>? {
        val json = getPref().getString(TEMPORARY_CATEGORIES_TAG, null)
        val groupListType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(json, groupListType)
    }

    companion object {
        private const val MY_PREFS = "SHARED_PREFERENCES"
        private const val USER_TAG = "User"
        private const val TEMPORARY_CATEGORIES_TAG = "Categories"
    }
}

interface SharedPreferencesInterface {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

interface PublistSharedPreferencesInterface {
    fun getUser(): User?
    fun setUser(user: User)
    fun deleteUser()
    fun updateUserCategories(categoriesList: ArrayList<String>)
    fun clearData()
    fun getPref(): SharedPreferences
    fun saveTemporaryCategories(selectedCategoriesList: ArrayList<String>)
    fun getTemporaryCategories(): ArrayList<String>?
}

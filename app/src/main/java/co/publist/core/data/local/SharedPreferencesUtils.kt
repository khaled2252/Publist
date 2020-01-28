package co.publist.core.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import co.publist.core.data.models.User
import com.google.gson.Gson

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
        mPrefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return mPrefs.getString(key, null)
    }

    override fun setToken(token: String) {
        putString(TOKEN, token)
    }

    override fun getToken(): String? {
        return getString(TOKEN)
    }

    override fun clearData() {
        mPrefs.edit().clear().apply()
    }

    override fun saveUser(user: User) {
        val gson = Gson()
        val json = gson.toJson(user)
        mPrefs.edit().putString(USER_TAG, json).apply()
    }

    override fun getUser(): User? {
        val gson = Gson()
        val json = mPrefs.getString(USER_TAG, null)
        return gson.fromJson<User>(json, User::class.java)
    }

    companion object {
        private const val MY_PREFS = "SHARED_PREFERENCES"
        private const val TOKEN = "TOKEN"
        private const val USER_TAG = "User"
    }
}

interface SharedPreferencesInterface {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

interface PublistSharedPreferencesInterface {
    fun getUser(): User?
    fun saveUser(user: User)
    fun clearData()
    fun setToken(token: String)
    fun getToken(): String?
    fun getPref(): SharedPreferences
    }

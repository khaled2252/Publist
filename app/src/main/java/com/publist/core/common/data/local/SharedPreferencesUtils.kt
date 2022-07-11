package com.publist.core.common.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.publist.core.common.data.models.User
import javax.inject.Inject

class SharedPreferencesUtils @Inject constructor(context: Context) :
    PublistSharedPreferencesInterface, SharedPreferencesInterface {
    private val mPrefs: SharedPreferences

    init {
        mPrefs = context.getSharedPreferences(MY_PREFS, MODE_PRIVATE)
    }

    override fun getString(key: String): String? {
        return getPref().getString(key, null)
    }

    override fun putString(key: String, value: String) {
        getPref().edit().putString(key, value).apply()
    }

    override fun getPref(): SharedPreferences {
        return mPrefs
    }

    override fun clearPref() {
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

    override fun deleteUser() {
        getPref().edit().remove(USER_TAG).apply()
    }

    override fun setOnBoardingFinished() {
        putString(ON_BOARDING_STATUS_TAG, FINISHED_TAG)
    }

    override fun getOnBoardingStatus(): Boolean {
        return getString(ON_BOARDING_STATUS_TAG) == FINISHED_TAG
    }

    override fun setTermsAndConditionsAccepted() {
        putString(IS_TERMS_AND_CONDITIONS_ACCEPTED, FINISHED_TAG)
    }

    override fun getTermsAndConditionsAccepted(): Boolean {
        return getString(IS_TERMS_AND_CONDITIONS_ACCEPTED) == FINISHED_TAG
    }

    //    override fun updateUserCategories(categoriesList: ArrayList<String>) {
//        val gson = Gson()
//        val json = getPref().getString(USER_TAG, null)
//        val userObject = gson.fromJson<User>(json, User::class.java)
//        userObject.myCategories = categoriesList
//        putString(USER_TAG, gson.toJson(userObject))
//    }
//
//    override fun saveTemporaryCategories(selectedCategoriesList: ArrayList<String>) {
//        val json = Gson().toJson(selectedCategoriesList)
//        putString(TEMPORARY_CATEGORIES_TAG, json)
//    }
//
//    override fun getTemporaryCategories(): ArrayList<String>? {
//        val json = getPref().getString(TEMPORARY_CATEGORIES_TAG, null)
//        val groupListType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
//        return Gson().fromJson(json, groupListType)
//    }
//
//    override fun clearGuestCategories() {
//        getPref().edit().remove(TEMPORARY_CATEGORIES_TAG).apply()
//    }
//
    companion object {
        private const val MY_PREFS = "SHARED_PREFERENCES"
        private const val USER_TAG = "User"
    private const val ON_BOARDING_STATUS_TAG = "isOnBoardingFinished"
        private const val FINISHED_TAG = "finished"
        private const val IS_TERMS_AND_CONDITIONS_ACCEPTED = "IS_TERMS_AND_CONDITIONS_ACCEPTED"
    }
}

interface SharedPreferencesInterface {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

interface PublistSharedPreferencesInterface {
    fun getPref(): SharedPreferences
    fun clearPref()
    fun getUser(): User?
    fun setUser(user: User)
    fun deleteUser()
    fun setOnBoardingFinished()
    fun getOnBoardingStatus(): Boolean
    fun setTermsAndConditionsAccepted()
    fun getTermsAndConditionsAccepted(): Boolean
//    fun updateUserCategories(categoriesList: ArrayList<String>)
//    fun saveTemporaryCategories(selectedCategoriesList: ArrayList<String>)
//    fun getTemporaryCategories(): ArrayList<String>?
//    fun clearGuestCategories()
}

package com.addmusictovideos.audiovideomixer.sk.utils

import android.content.Context

//class SharedPref(context: Context) {
//    private val sharePref =
//        context.applicationContext.getSharedPreferences("apero_ad_pref", Context.MODE_PRIVATE)
//
//    private val KEY_SELECTED_LANGUAGE_CODE = "KEY_SELECTED_LANGUAGE"
//
//    fun getYourRemoteConfig(): String {
//        return sharePref.getString(KEY_YOUR_REMOTE, "default_value") ?: "default_value"
//    }
//
//    fun setYourRemoteConfig(value: String) {
//        sharePref.edit().putString(KEY_YOUR_REMOTE, value).apply()
//    }
//
//    fun getSelectedLanguageCode(): String {
//        return sharePref.getString(KEY_SELECTED_LANGUAGE_CODE, "") ?: ""
//    }
//
//    fun setSelectedLanguageCode(languageCode: String) {
//        sharePref.edit().putString(KEY_SELECTED_LANGUAGE_CODE, languageCode).apply()
//    }
//
//    companion object {
//        const val KEY_YOUR_REMOTE = "KEY_YOUR_REMOTE"
//
//        @Volatile
//        private var instance: SharedPref? = null
//
//        fun initPrefs(context: Context) {
//            if (instance == null) {
//                synchronized(this) {
//                    if (instance == null) {
//                        instance = SharedPref(context)
//                    }
//                }
//            }
//        }
//
//        fun getPrefsInstance(): SharedPref {
//            return instance ?: error("SharedPref not initialized!")
//        }
//    }
//}
package com.example.qash_finalproject.ui

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_MODE = "is_dark_mode" // Kunci Dark Mode
    }

    fun createLoginSession(userId: Int, name: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun logout() {
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_NAME)
        editor.apply()
    }

    // --- FITUR DARK MODE ---
    fun setDarkMode(isEnable: Boolean) {
        editor.putBoolean(KEY_DARK_MODE, isEnable)
        editor.apply()
    }

    fun isDarkMode(): Boolean {
        // Default false = Mode Terang
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
}
package com.automind.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.automind.app.data.model.UserProfile

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("automind_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_PASSWORD = "user_password"
        private const val KEY_LOGGED_IN = "is_logged_in"
    }

    fun saveUser(name: String, email: String, password: String) {
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password.hashCode().toString())
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun login(email: String, password: String): Boolean {
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        val savedPassHash = prefs.getString(KEY_PASSWORD, null)
        if (savedEmail == email && savedPassHash == password.hashCode().toString()) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
            return true
        }
        return false
    }

    fun getUser(): UserProfile {
        return UserProfile(
            name = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false)
        )
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getUserName(): String = prefs.getString(KEY_NAME, "User") ?: "User"

    fun getUserEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun hasAccount(): Boolean = prefs.getString(KEY_EMAIL, null) != null
}

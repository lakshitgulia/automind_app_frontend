package com.automind.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.automind.app.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("automind_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCOUNTS = "accounts_json"
        private const val KEY_CURRENT_EMAIL = "current_email"
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val MAX_ACCOUNTS = 5
    }

    fun saveUser(name: String, email: String, password: String) {
        val normalizedEmail = normalizeEmail(email)
        val accounts = loadAccounts()
            .filterNot { it.optString("email") == normalizedEmail }
            .toMutableList()

        accounts.add(
            JSONObject().apply {
                put("name", name.trim())
                put("email", normalizedEmail)
                put("password_hash", hashPassword(password))
                put("last_used", System.currentTimeMillis())
            }
        )

        persistAccounts(accounts)
        prefs.edit()
            .putString(KEY_CURRENT_EMAIL, normalizedEmail)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun login(email: String, password: String): Boolean {
        val normalizedEmail = normalizeEmail(email)
        val passwordHash = hashPassword(password)
        val accounts = loadAccounts().toMutableList()
        val index = accounts.indexOfFirst {
            it.optString("email") == normalizedEmail &&
                it.optString("password_hash") == passwordHash
        }

        if (index == -1) return false

        val account = accounts[index]
        account.put("last_used", System.currentTimeMillis())
        accounts[index] = account
        persistAccounts(accounts)

        prefs.edit()
            .putString(KEY_CURRENT_EMAIL, normalizedEmail)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
        return true
    }

    fun getUser(): UserProfile {
        val current = currentAccount()
        return UserProfile(
            name = current?.optString("name", "") ?: "",
            email = current?.optString("email", "") ?: "",
            isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false)
        )
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getUserName(): String = currentAccount()?.optString("name", "User") ?: "User"

    fun getUserEmail(): String = currentAccount()?.optString("email", "") ?: ""

    fun getCurrentUserEmail(): String? = prefs.getString(KEY_CURRENT_EMAIL, null)

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun hasAccount(): Boolean = loadAccounts().isNotEmpty()

    private fun currentAccount(): JSONObject? {
        val currentEmail = prefs.getString(KEY_CURRENT_EMAIL, null) ?: return null
        return loadAccounts().firstOrNull { it.optString("email") == currentEmail }
    }

    private fun loadAccounts(): List<JSONObject> {
        val json = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { index -> array.getJSONObject(index) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistAccounts(accounts: List<JSONObject>) {
        val sorted = accounts
            .sortedByDescending { it.optLong("last_used", 0L) }
            .take(MAX_ACCOUNTS)

        val array = JSONArray()
        sorted.forEach { array.put(it) }
        prefs.edit().putString(KEY_ACCOUNTS, array.toString()).apply()
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}

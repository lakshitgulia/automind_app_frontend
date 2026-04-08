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
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_PASSWORD = "user_password"
        private const val MAX_ACCOUNTS = 5
    }

    fun saveUser(name: String, email: String, password: String) {
        val normalizedEmail = normalizeEmail(email)
        val accounts = (loadAccounts() + listOfNotNull(loadLegacyAccount()))
            .distinctBy { it.optString("email") }
            .filterNot { it.optString("email") == normalizedEmail }
            .toMutableList()

        accounts.add(
            JSONObject().apply {
                put("name", name.trim())
                put("email", normalizedEmail)
                put("password_hash", hashPassword(password))
                put("legacy_password_hash", legacyHash(password))
                put("last_used", System.currentTimeMillis())
            }
        )

        persistAccounts(accounts)
        prefs.edit()
            .putString(KEY_CURRENT_EMAIL, normalizedEmail)
            .putBoolean(KEY_LOGGED_IN, true)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun login(email: String, password: String): Boolean {
        val normalizedEmail = normalizeEmail(email)
        val passwordHash = hashPassword(password)
        val legacyPasswordHash = legacyHash(password)
        val accounts = (loadAccounts() + listOfNotNull(loadLegacyAccount()))
            .distinctBy { it.optString("email") }
            .toMutableList()

        val index = accounts.indexOfFirst {
            it.optString("email") == normalizedEmail && (
                it.optString("password_hash") == passwordHash ||
                    it.optString("legacy_password_hash") == legacyPasswordHash
                )
        }

        if (index == -1) return false

        val account = accounts[index]
        account.put("password_hash", passwordHash)
        account.put("legacy_password_hash", legacyPasswordHash)
        account.put("last_used", System.currentTimeMillis())
        accounts[index] = account
        persistAccounts(accounts)

        prefs.edit()
            .putString(KEY_CURRENT_EMAIL, normalizedEmail)
            .putBoolean(KEY_LOGGED_IN, true)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
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

    fun deleteCurrentAccount(): Boolean {
        val currentEmail = getCurrentUserEmail()?.let(::normalizeEmail)
            ?: currentAccount()?.optString("email")?.takeIf { it.isNotBlank() }?.let(::normalizeEmail)
            ?: return false

        val remainingAccounts = (loadAccounts() + listOfNotNull(loadLegacyAccount()))
            .distinctBy { it.optString("email") }
            .filterNot { normalizeEmail(it.optString("email")) == currentEmail }

        val editor = prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_CURRENT_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)

        if (remainingAccounts.isEmpty()) {
            editor.remove(KEY_ACCOUNTS)
        } else {
            val array = JSONArray()
            remainingAccounts
                .sortedByDescending { it.optLong("last_used", 0L) }
                .take(MAX_ACCOUNTS)
                .forEach { array.put(it) }
            editor.putString(KEY_ACCOUNTS, array.toString())
        }

        editor.apply()
        return true
    }

    fun hasAccount(): Boolean = loadAccounts().isNotEmpty() || loadLegacyAccount() != null

    private fun currentAccount(): JSONObject? {
        val currentEmail = prefs.getString(KEY_CURRENT_EMAIL, null) ?: return loadLegacyAccount()
        return (loadAccounts() + listOfNotNull(loadLegacyAccount()))
            .firstOrNull { it.optString("email") == currentEmail }
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

    private fun loadLegacyAccount(): JSONObject? {
        val email = prefs.getString(KEY_EMAIL, null)?.trim()?.lowercase() ?: return null
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val legacyPass = prefs.getString(KEY_PASSWORD, null) ?: return null
        return JSONObject().apply {
            put("name", name)
            put("email", email)
            put("legacy_password_hash", legacyPass)
            put("last_used", 0L)
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

    private fun legacyHash(password: String): String = password.hashCode().toString()
}

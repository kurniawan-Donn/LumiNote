package com.example.luminote

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("LumiNoteSession", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"

        // ✅ TAMBAHAN BARU: Keys untuk bahasa
        private const val KEY_LANGUAGE = "app_language"
    }

    // Simpan session login
    fun createLoginSession(idNama: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, idNama)
            apply()
        }
    }

    // Cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Mendapatkan ID user yang sedang login
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    // Logout - hapus session (KECUALI bahasa)
    fun logout() {
        // ✅ PERBAIKAN: Simpan bahasa sebelum clear
        val currentLanguage = getLanguage()

        prefs.edit().clear().apply()

        // ✅ Restore bahasa setelah logout
        setLanguage(currentLanguage)
    }

    // ========================================
    // ✅ TAMBAHAN BARU: Language Management
    // ========================================

    /**
     * Simpan bahasa yang dipilih
     */
    fun setLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /**
     * Ambil bahasa yang dipilih (default: Indonesia)
     */
    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "id") ?: "id"
    }

    /**
     * Ambil nama display bahasa untuk UI
     */
    fun getLanguageDisplayName(): String {
        return when (getLanguage()) {
            "en" -> "English"
            "jv" -> "Jawa"
            else -> "Indonesia"
        }
    }
}
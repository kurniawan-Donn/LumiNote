package com.example.luminote

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * ThemeHelper - Simple Dark Mode Management
 * Tidak perlu BaseActivity, cukup panggil di setiap Activity
 *
 * Usage di setiap Activity:
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     ThemeHelper.applyTheme(this)  // ← Tambahkan ini sebelum super.onCreate()
 *     super.onCreate(savedInstanceState)
 *     setContentView(R.layout.activity_your)
 * }
 */
object ThemeHelper {

    private const val PREFS_NAME = "app_theme"
    private const val KEY_DARK_MODE = "is_dark_mode"

    /**
     * ✅ APPLY THEME - Panggil ini di SETIAP Activity.onCreate() SEBELUM super.onCreate()
     */
    fun applyTheme(activity: AppCompatActivity) {
        val isDark = isDarkMode(activity)

        val nightMode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        android.util.Log.d("ThemeHelper", "Applying theme for ${activity.javaClass.simpleName}: isDark=$isDark")

        // Set untuk activity ini
        activity.delegate.localNightMode = nightMode
    }

    /**
     * ✅ SAVE DARK MODE - Panggil ini saat user toggle switch
     */
    fun setDarkMode(context: Context, enabled: Boolean) {
        android.util.Log.d("ThemeHelper", "setDarkMode: $enabled")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // GUNAKAN COMMIT untuk synchronous save
        val result = prefs.edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .commit()

        android.util.Log.d("ThemeHelper", "Save result: $result")

        // Verify
        val verify = prefs.getBoolean(KEY_DARK_MODE, false)
        android.util.Log.d("ThemeHelper", "Verified: $verify")

        if (verify != enabled) {
            android.util.Log.e("ThemeHelper", "⚠️ SAVE FAILED!")
        }
    }

    /**
     * ✅ GET DARK MODE STATUS
     */
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * ✅ TOGGLE DARK MODE
     */
    fun toggleDarkMode(context: Context): Boolean {
        val newState = !isDarkMode(context)
        setDarkMode(context, newState)
        return newState
    }
}
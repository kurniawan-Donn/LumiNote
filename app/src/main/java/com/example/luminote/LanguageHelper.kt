package com.example.luminote

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

object LanguageHelper {

    private const val PREFS_NAME = "app_language"
    private const val KEY_LANGUAGE = "selected_language"

    const val LANGUAGE_INDONESIA = "id"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_JAVANESE = "jv"

    fun applyLanguage(activity: AppCompatActivity) {
        val languageCode = getLanguage(activity)
        setLocale(activity, languageCode)
    }

    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        // Apply configuration
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply() // Menggunakan apply() lebih baik daripada commit() untuk UI thread
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_INDONESIA) ?: LANGUAGE_INDONESIA
    }

    fun getLanguageDisplayName(context: Context): String {
        return getDisplayNameByCode(context, getLanguage(context))
    }

    fun getDisplayNameByCode(context: Context, languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> context.getString(R.string.lang_english)
            LANGUAGE_JAVANESE -> context.getString(R.string.lang_javanese)
            else -> context.getString(R.string.lang_indonesia)
        }
    }

    fun isLanguageSelected(context: Context, languageCode: String): Boolean {
        return getLanguage(context) == languageCode
    }

    fun changeLanguage(activity: Activity, languageCode: String) {
        setLanguage(activity, languageCode)
        activity.recreate()
    }

    // ✅ FUNGSI BARU: Wrap context untuk BroadcastReceiver dan Service
    /**
     * Membuat context baru dengan locale yang sesuai pengaturan bahasa user.
     * Digunakan untuk BroadcastReceiver, Service, dan komponen yang berjalan
     * di luar Activity agar getString() menghasilkan text sesuai bahasa pilihan.
     */
    fun wrap(context: Context): Context {
        val languageCode = getLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            context.createConfigurationContext(configuration)
        }
    }
}
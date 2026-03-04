package com.example.luminote

import android.content.Context
import android.content.SharedPreferences

class PemberitahuanPreferences(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "PemberitahuanPreferences"

        // Keys
        private const val KEY_SETEL_PEMBERITAHUAN = "setel_pemberitahuan"
        private const val KEY_SETEL_ALARM = "setel_alarm"
        private const val KEY_TUGAS_MENDATANG = "tugas_mendatang"
        private const val KEY_TUGAS_TERLAMBAT = "tugas_terlambat"
        private const val KEY_NADA_DERING = "nada_dering"
        private const val KEY_NADA_DERING_URI = "nada_dering_uri"
        private const val KEY_AKTIFKAN_GETARAN = "aktifkan_getaran"
        private const val KEY_ULANGI = "ulangi"
        private const val KEY_LABEL_ALARM = "label_alarm"
        private const val KEY_VOLUME = "volume"
        private const val KEY_WAKTU_TENANG = "waktu_tenang"
        private const val KEY_WAKTU_MULAI = "waktu_mulai"
        private const val KEY_WAKTU_SELESAI = "waktu_selesai"

        private const val KEY_HARI_KUSTOM = "hari_kustom"
        private const val KEY_WARNA_NADA_DERING = "warna_nada_dering"
    }

    // Setel Pemberitahuan

    fun setHariKustom(days: List<Int>) {
        val daysString = days.joinToString(",")
        prefs.edit().putString(KEY_HARI_KUSTOM, daysString).apply()
    }

    fun getHariKustom(): List<Int> {
        val daysString = prefs.getString(KEY_HARI_KUSTOM, "") ?: ""
        return if (daysString.isEmpty()) {
            emptyList()
        } else {
            daysString.split(",").mapNotNull { it.toIntOrNull() }
        }
    }

    fun setSetelPemberitahuan(value: Boolean) {
        prefs.edit().putBoolean(KEY_SETEL_PEMBERITAHUAN, value).apply()
    }

    fun getSetelPemberitahuan(): Boolean {
        return prefs.getBoolean(KEY_SETEL_PEMBERITAHUAN, true)
    }

    // Setel Alarm
    fun setSetelAlarm(value: Boolean) {
        prefs.edit().putBoolean(KEY_SETEL_ALARM, value).apply()
    }

    fun getSetelAlarm(): Boolean {
        return prefs.getBoolean(KEY_SETEL_ALARM, true)
    }

    // Tugas Mendatang
    fun setTugasMendatang(value: Boolean) {
        prefs.edit().putBoolean(KEY_TUGAS_MENDATANG, value).apply()
    }

    fun getTugasMendatang(): Boolean {
        return prefs.getBoolean(KEY_TUGAS_MENDATANG, false)
    }

    // Tugas Terlambat
    fun setTugasTerlambat(value: Boolean) {
        prefs.edit().putBoolean(KEY_TUGAS_TERLAMBAT, value).apply()
    }

    fun getTugasTerlambat(): Boolean {
        return prefs.getBoolean(KEY_TUGAS_TERLAMBAT, false)
    }

    // Nada Dering
    fun setNadaDering(name: String, uri: String) {
        prefs.edit().apply {
            putString(KEY_NADA_DERING, name)
            putString(KEY_NADA_DERING_URI, uri)
            apply()
        }
    }

    fun setWarnaNadaDering(color: Int) {
        prefs.edit().putInt(KEY_WARNA_NADA_DERING, color).apply()
    }

    fun getNadaDering(): String {
        return prefs.getString(KEY_NADA_DERING, context.getString(R.string.ringtone_default))
            ?: context.getString(R.string.ringtone_default)
    }

    fun getNadaDeringUri(): String {
        return prefs.getString(KEY_NADA_DERING_URI, "") ?: ""
    }

    fun getWarnaNadaDering(): Int {
        // Default warna hijau (#00ff00) jika belum disetel
        return prefs.getInt(KEY_WARNA_NADA_DERING, android.graphics.Color.parseColor("#00ff00"))
    }

    // Aktifkan Getaran
    fun setAktifkanGetaran(value: Boolean) {
        prefs.edit().putBoolean(KEY_AKTIFKAN_GETARAN, value).apply()
    }

    fun getAktifkanGetaran(): Boolean {
        return prefs.getBoolean(KEY_AKTIFKAN_GETARAN, false)
    }

    // Ulangi
    fun setUlangi(value: String) {
        prefs.edit().putString(KEY_ULANGI, value).apply()
    }

    fun getUlangi(): String {
        return prefs.getString(KEY_ULANGI, context.getString(R.string.repeat_once))
            ?: context.getString(R.string.repeat_once)
    }

    // Label Alarm
    fun setLabelAlarm(value: String) {
        prefs.edit().putString(KEY_LABEL_ALARM, value).apply()
    }

    fun getLabelAlarm(): String {
        return prefs.getString(KEY_LABEL_ALARM, "") ?: ""
    }

    // Volume
    fun setVolume(value: Int) {
        prefs.edit().putInt(KEY_VOLUME, value).apply()
    }

    fun getVolume(): Int {
        return prefs.getInt(KEY_VOLUME, 80)
    }

    // Waktu Tenang
    fun setWaktuTenang(value: Boolean) {
        prefs.edit().putBoolean(KEY_WAKTU_TENANG, value).apply()
    }

    fun getWaktuTenang(): Boolean {
        return prefs.getBoolean(KEY_WAKTU_TENANG, false)
    }

    // Waktu Mulai
    fun setWaktuMulai(value: String) {
        prefs.edit().putString(KEY_WAKTU_MULAI, value).apply()
    }

    fun getWaktuMulai(): String {
        return prefs.getString(KEY_WAKTU_MULAI, "22:00") ?: "22:00"
    }

    // Waktu Selesai
    fun setWaktuSelesai(value: String) {
        prefs.edit().putString(KEY_WAKTU_SELESAI, value).apply()
    }

    fun getWaktuSelesai(): String {
        return prefs.getString(KEY_WAKTU_SELESAI, "07:00") ?: "07:00"
    }

    // Reset semua ke default
    fun resetToDefault() {
        prefs.edit().clear().apply()
    }
}
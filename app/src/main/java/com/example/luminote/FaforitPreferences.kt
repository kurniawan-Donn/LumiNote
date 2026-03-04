package com.example.luminote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FaforitPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FaforitPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val sessionManager = SessionManager(context)

    // Key untuk favorit catatan dan tugas per user
    private fun getKeyCatatanFavorit(): String {
        val userId = sessionManager.getUserId() ?: "default"
        return "catatan_favorit_$userId"
    }

    private fun getKeyTugasFavorit(): String {
        val userId = sessionManager.getUserId() ?: "default"
        return "tugas_favorit_$userId"
    }

    // =====================================
    // CATATAN FAVORIT
    // =====================================

    // Simpan ID catatan favorit
    fun saveFavoritCatatan(catatanIdList: List<String>) {
        val json = gson.toJson(catatanIdList)
        prefs.edit().putString(getKeyCatatanFavorit(), json).apply()
    }

    // Load ID catatan favorit
    fun getFavoritCatatan(): List<String> {
        val json = prefs.getString(getKeyCatatanFavorit(), null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    // Cek apakah catatan adalah favorit
    fun isCatatanFavorit(catatanId: String): Boolean {
        return getFavoritCatatan().contains(catatanId)
    }

    // Toggle favorit catatan
    fun toggleCatatanFavorit(catatanId: String) {
        val currentList = getFavoritCatatan().toMutableList()
        if (currentList.contains(catatanId)) {
            currentList.remove(catatanId)
        } else {
            currentList.add(catatanId)
        }
        saveFavoritCatatan(currentList)
    }

    // =====================================
    // TUGAS FAVORIT
    // =====================================

    // Simpan ID tugas favorit
    fun saveFavoritTugas(tugasIdList: List<String>) {
        val json = gson.toJson(tugasIdList)
        prefs.edit().putString(getKeyTugasFavorit(), json).apply()
    }

    // Load ID tugas favorit
    fun getFavoritTugas(): List<String> {
        val json = prefs.getString(getKeyTugasFavorit(), null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    // Cek apakah tugas adalah favorit
    fun isTugasFavorit(tugasId: String): Boolean {
        return getFavoritTugas().contains(tugasId)
    }

    // Toggle favorit tugas
    fun toggleTugasFavorit(tugasId: String) {
        val currentList = getFavoritTugas().toMutableList()
        if (currentList.contains(tugasId)) {
            currentList.remove(tugasId)
        } else {
            currentList.add(tugasId)
        }
        saveFavoritTugas(currentList)
    }
}
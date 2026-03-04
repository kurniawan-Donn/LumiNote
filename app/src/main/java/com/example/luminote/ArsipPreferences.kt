package com.example.luminote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ArsipPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ArsipPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val sessionManager = SessionManager(context)

    // Key untuk arsip catatan dan tugas per user
    private fun getKeyCatatanArsip(): String {
        val userId = sessionManager.getUserId() ?: "default"
        return "catatan_arsip_$userId"
    }

    private fun getKeyTugasArsip(): String {
        val userId = sessionManager.getUserId() ?: "default"
        return "tugas_arsip_$userId"
    }

    // =====================================
    // CATATAN ARSIP
    // =====================================

    // Simpan ID catatan arsip
    fun saveArsipCatatan(catatanIdList: List<String>) {
        val json = gson.toJson(catatanIdList)
        prefs.edit().putString(getKeyCatatanArsip(), json).apply()
    }

    // Load ID catatan arsip
    fun getArsipCatatan(): List<String> {
        val json = prefs.getString(getKeyCatatanArsip(), null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    // Cek apakah catatan diarsipkan
    fun isCatatanArsip(catatanId: String): Boolean {
        return getArsipCatatan().contains(catatanId)
    }

    // Arsipkan catatan
    fun arsipkanCatatan(catatanId: String) {
        val currentList = getArsipCatatan().toMutableList()
        if (!currentList.contains(catatanId)) {
            currentList.add(catatanId)
            saveArsipCatatan(currentList)
        }
    }

    // Pulihkan catatan (hapus dari arsip)
    fun pulihkanCatatan(catatanId: String) {
        val currentList = getArsipCatatan().toMutableList()
        currentList.remove(catatanId)
        saveArsipCatatan(currentList)
    }

    // =====================================
    // TUGAS ARSIP
    // =====================================

    // Simpan ID tugas arsip
    fun saveArsipTugas(tugasIdList: List<String>) {
        val json = gson.toJson(tugasIdList)
        prefs.edit().putString(getKeyTugasArsip(), json).apply()
    }

    // Load ID tugas arsip
    fun getArsipTugas(): List<String> {
        val json = prefs.getString(getKeyTugasArsip(), null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    // Cek apakah tugas diarsipkan
    fun isTugasArsip(tugasId: String): Boolean {
        return getArsipTugas().contains(tugasId)
    }

    // Arsipkan tugas
    fun arsipkanTugas(tugasId: String) {
        val currentList = getArsipTugas().toMutableList()
        if (!currentList.contains(tugasId)) {
            currentList.add(tugasId)
            saveArsipTugas(currentList)
        }
    }

    // Pulihkan tugas (hapus dari arsip)
    fun pulihkanTugas(tugasId: String) {
        val currentList = getArsipTugas().toMutableList()
        currentList.remove(tugasId)
        saveArsipTugas(currentList)
    }
}
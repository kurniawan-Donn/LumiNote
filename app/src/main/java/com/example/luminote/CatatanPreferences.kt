package com.example.luminote

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CatatanPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "catatan_prefs"
        private const val KEY_CATATAN_LIST = "catatan_list"
    }

    fun saveCatatanList(catatanList: List<Catatan>) {
        val json = gson.toJson(catatanList)
        prefs.edit {
            putString(KEY_CATATAN_LIST, json)
        }
    }

    fun getCatatanList(): List<Catatan> {
        val json = prefs.getString(KEY_CATATAN_LIST, null) ?: return emptyList()
        val type: Type = object : TypeToken<List<Catatan>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addCatatan(catatan: Catatan) {
        val currentList = getCatatanList().toMutableList()
        currentList.add(catatan)
        saveCatatanList(currentList)
    }

    fun updateCatatan(updatedCatatan: Catatan) {
        val list = getCatatanList().toMutableList()
        val index = list.indexOfFirst { it.id == updatedCatatan.id }

        if (index != -1) {
            list[index] = updatedCatatan
        }
        saveCatatanList(list)
    }

    fun deleteCatatan(id: String) {
        val currentList = getCatatanList().toMutableList()
        currentList.removeAll { it.id == id }
        saveCatatanList(currentList)
    }

    fun getCatatanById(id: String): Catatan? {
        return getCatatanList().find { it.id == id }
    }
}
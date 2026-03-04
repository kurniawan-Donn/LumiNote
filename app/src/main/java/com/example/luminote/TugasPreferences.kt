package com.example.luminote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TugasPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tugas_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_TUGAS_LIST = "key_tugas_list"
    }

    fun getAllTugas(): List<Tugas> {
        val json = prefs.getString(KEY_TUGAS_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<Tugas>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addTugas(tugas: Tugas) {
        val list = getAllTugas().toMutableList()
        list.add(tugas)
        saveList(list)
    }

    fun updateTugas(tugas: Tugas) {
        val list = getAllTugas().toMutableList()
        val index = list.indexOfFirst { it.id == tugas.id }

        if (index != -1) {
            list[index] = tugas
            saveList(list)
        }
    }

    fun deleteTugas(id: String) {
        val list = getAllTugas().toMutableList()
        list.removeAll { it.id == id }
        saveList(list)
    }

    fun getTugasById(id: String): Tugas? {
        return getAllTugas().find { it.id == id }
    }

    fun saveList(list: List<Tugas>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_TUGAS_LIST, json).apply()
    }
}
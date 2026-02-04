package com.example.LumiNote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class UserManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("LumiNoteUsers", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USERS = "users_list"
    }

    // Mendapatkan semua user
    fun getAllUsers(): MutableList<User> {
        val json = prefs.getString(KEY_USERS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<User>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Menyimpan semua user
    private fun saveAllUsers(users: List<User>) {
        val json = gson.toJson(users)
        prefs.edit { putString(KEY_USERS, json) }
    }

    // Registrasi user baru
    fun registerUser(idNama: String, password: String): Boolean {
        val users = getAllUsers()

        // Cek apakah ID sudah digunakan
        if (users.any { it.idNama == idNama }) {
            return false
        }

        val newUser = User(
            idNama = idNama,
            password = password,
            nama = context.getString(R.string.default_user_name),
            bio = context.getString(R.string.default_user_bio),
            fotoProfil = ""
        )
        users.add(newUser)
        saveAllUsers(users)
        return true
    }

    // Login user
    fun loginUser(idNama: String, password: String): User? {
        val users = getAllUsers()
        return users.find { it.idNama == idNama && it.password == password }
    }

    // Update user
    fun updateUser(updatedUser: User): Boolean {
        val users = getAllUsers()
        val index = users.indexOfFirst { it.idNama == updatedUser.idNama }

        return if (index != -1) {
            users[index] = updatedUser
            saveAllUsers(users)
            true
        } else {
            false
        }
    }

    // Mendapatkan user berdasarkan ID
    fun getUserById(idNama: String): User? {
        val users = getAllUsers()
        return users.find { it.idNama == idNama }
    }

    // Cek apakah ID sudah ada
    fun isIdExists(idNama: String): Boolean {
        val users = getAllUsers()
        return users.any { it.idNama == idNama }
    }

    // Reset password (untuk forgot password)
    fun resetPassword(idNama: String, newPassword: String): Boolean {
        val users = getAllUsers()
        val user = users.find { it.idNama == idNama }

        return if (user != null) {
            user.password = newPassword
            saveAllUsers(users)
            true
        } else {
            false
        }
    }
}
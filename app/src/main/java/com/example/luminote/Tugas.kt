package com.example.luminote

import java.util.UUID

data class Tugas(
    val id: String = UUID.randomUUID().toString(),
    val judul: String,
    val deskripsi: String = "",
    val tanggal: String? = null,
    val waktu: String? = null,
    val isSelesai: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    var isFavorit: Boolean = false
) {
    fun isChecked(): Boolean = isSelesai

    fun matchesQuery(katakunci: String): Boolean {
        val kata = katakunci.lowercase()
        val judulLower = judul.lowercase()
        val deskripsiLower = deskripsi.lowercase()
        return judulLower.contains(kata) || deskripsiLower.contains(kata)
    }
}
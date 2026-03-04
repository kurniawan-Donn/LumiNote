package com.example.luminote

import java.util.UUID

data class Catatan(
    val id: String = UUID.randomUUID().toString(),
    val judul: String,
    val deskripsi: String,
    val note: String? = null,
    val tanggal: String? = null,
    val waktu: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    var isFavorit: Boolean = false
) {
    fun getFormatTanggal(): String {
        if (tanggal != null && waktu != null) {
            return "$tanggal\n$waktu"
        }
        if (tanggal != null) {
            return tanggal
        }
        if (waktu != null) {
            return waktu
        }
        return ""
    }

    fun querypencocokan(query: String): Boolean {
        val searchText = query.lowercase()
        val titleText = judul.lowercase()
        val descriptionText = deskripsi.lowercase()
        val containsInTitle = titleText.contains(searchText)
        val containsInDescription = descriptionText.contains(searchText)
        return containsInTitle || containsInDescription
    }
}
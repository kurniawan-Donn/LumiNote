package com.example.luminote

// Data class untuk menggabungkan Catatan dan Tugas dalam arsip
data class ArsipItem(
    val id: String,
    val tipe: String, // "Catatan" atau "Tugas"
    val judul: String,
    val deskripsi: String,
    val tanggal: String,
    val timestamp: Long,
    val catatanOriginal: Catatan? = null,
    val tugasOriginal: Tugas? = null
) {
    companion object {
        // Convert Catatan ke ArsipItem
        fun fromCatatan(catatan: Catatan): ArsipItem {
            val tanggalDisplay = if (!catatan.tanggal.isNullOrEmpty() && !catatan.waktu.isNullOrEmpty()) {
                "${catatan.tanggal} ${catatan.waktu}"
            } else if (!catatan.tanggal.isNullOrEmpty()) {
                catatan.tanggal
            } else if (!catatan.waktu.isNullOrEmpty()) {
                catatan.waktu
            } else {
                ""
            }

            return ArsipItem(
                id = catatan.id,
                tipe = "Catatan",
                judul = catatan.judul,
                deskripsi = catatan.deskripsi,
                tanggal = tanggalDisplay,
                timestamp = catatan.timestamp,
                catatanOriginal = catatan
            )
        }

        // Convert Tugas ke ArsipItem
        fun fromTugas(tugas: Tugas): ArsipItem {
            val tanggalDisplay = if (!tugas.tanggal.isNullOrEmpty() && !tugas.waktu.isNullOrEmpty()) {
                "${tugas.tanggal} ${tugas.waktu}"
            } else if (!tugas.tanggal.isNullOrEmpty()) {
                tugas.tanggal
            } else if (!tugas.waktu.isNullOrEmpty()) {
                tugas.waktu
            } else {
                ""
            }

            return ArsipItem(
                id = tugas.id,
                tipe = "Tugas",
                judul = tugas.judul,
                deskripsi = tugas.deskripsi,
                tanggal = tanggalDisplay,
                timestamp = tugas.timestamp,
                tugasOriginal = tugas
            )
        }
    }

    // Fungsi untuk search
    fun matchesQuery(query: String): Boolean {
        val searchText = query.lowercase()
        return judul.lowercase().contains(searchText) ||
                deskripsi.lowercase().contains(searchText)
    }
}
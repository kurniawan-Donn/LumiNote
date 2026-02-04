package com.example.LumiNote

data class User(
    var idNama: String, // Ubah dari val ke var agar bisa diubah
    var password: String,
    var nama: String = "Nama",
    var bio: String = "Deskripsi Bio...",
    var fotoProfil: String = "" // URI path foto
)
package com.example.luminote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageHelper {

    // Simpan foto ke internal storage
    fun saveImageToInternalStorage(context: Context, uri: Uri, userId: String): String? {
        try {
            // Baca bitmap dari URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Buat folder untuk menyimpan foto profil
                val directory = File(context.filesDir, "profile_images")
                if (!directory.exists()) {
                    directory.mkdirs()
                }

                // Nama file berdasarkan userId
                val filename = "profile_$userId.jpg"
                val file = File(directory, filename)

                // Simpan bitmap ke file
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.close()

                // Return path file
                return file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Load bitmap dari internal storage
    fun loadImageFromInternalStorage(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Hapus foto dari internal storage
    fun deleteImageFromInternalStorage(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
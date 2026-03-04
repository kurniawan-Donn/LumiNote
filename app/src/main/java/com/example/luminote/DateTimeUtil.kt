package com.example.luminote

import java.util.*

object DateTimeUtil {

    fun getTimeInMillis(tanggal: String): Long {
        // Format: "DD/MM/YYYY HH:MM"
        val parts = tanggal.split(" ")
        val date = parts[0].split("/")
        val time = parts[1].split(":")

        val calendar = Calendar.getInstance()
        calendar.set(
            date[2].toInt(),
            date[1].toInt() - 1,
            date[0].toInt(),
            time[0].toInt(),
            time[1].toInt(),
            0
        )
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }
}

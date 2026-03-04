package com.example.luminote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // ✅ PENTING: Wrap context dengan bahasa yang dipilih user
        val localizedContext = LanguageHelper.wrap(context)

        val prefs = PemberitahuanPreferences(localizedContext)

        // Cek master alarm
        if (!prefs.getSetelAlarm()) return

        // Cek waktu tenang
        if (prefs.getWaktuTenang() && isInWaktuTenang(prefs)) return

        val tugasId = intent.getStringExtra("tugas_id") ?: return

        // ✅ Gunakan localizedContext untuk getString
        val judul = intent.getStringExtra("judul")
            ?: localizedContext.getString(R.string.notification_task_title1)

        val deskripsi = intent.getStringExtra("deskripsi")
            ?: localizedContext.getString(R.string.notification_task_description1)

        val reminderTypeStr = intent.getStringExtra("reminder_type")

        val reminderType = try {
            TugasReminderType.valueOf(reminderTypeStr ?: TugasReminderType.TEPAT_WAKTU.name)
        } catch (e: Exception) {
            TugasReminderType.TEPAT_WAKTU
        }

        // KHUSUS TERLAMBAT
        if (reminderType == TugasReminderType.TERLAMBAT) {
            val tugasPrefs = TugasPreferences(localizedContext)
            val tugas = tugasPrefs.getTugasById(tugasId)
            if (tugas?.isSelesai == true) return
        }

        // ✅ Gunakan localizedContext untuk showNotification
        NotificationHelper.showNotification(
            context = localizedContext,
            judul = judul,
            deskripsi = deskripsi,
            prefs = prefs,
            tugasId = tugasId
        )
    }

    private fun isInWaktuTenang(prefs: PemberitahuanPreferences): Boolean {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val mulai = prefs.getWaktuMulai().split(":")
        val selesai = prefs.getWaktuSelesai().split(":")

        val startMinutes = mulai[0].toInt() * 60 + mulai[1].toInt()
        val endMinutes = selesai[0].toInt() * 60 + selesai[1].toInt()

        return if (startMinutes < endMinutes) {
            nowMinutes in startMinutes until endMinutes
        } else {
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }
}
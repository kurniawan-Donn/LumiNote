package com.example.luminote

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_SELESAI = "com.example.LumiNote.ACTION_SELESAI"
        const val ACTION_SNOOZE = "com.example.LumiNote.ACTION_SNOOZE"
        const val EXTRA_TUGAS_ID = "extra_tugas_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_JUDUL = "extra_judul"
        const val EXTRA_DESKRIPSI = "extra_deskripsi"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // ✅ PENTING: Wrap context dengan bahasa yang dipilih user
        val localizedContext = LanguageHelper.wrap(context)

        val action = intent.action ?: return
        val tugasId = intent.getStringExtra(EXTRA_TUGAS_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        // ✅ Gunakan localizedContext untuk getString
        val judul = intent.getStringExtra(EXTRA_JUDUL) ?: localizedContext.getString(R.string.default_task_title)
        val deskripsi = intent.getStringExtra(EXTRA_DESKRIPSI) ?: ""

        when (action) {
            ACTION_SELESAI -> handleSelesai(localizedContext, tugasId, notificationId)
            ACTION_SNOOZE -> handleSnooze(localizedContext, tugasId, judul, deskripsi, notificationId)
        }
    }

    private fun handleSelesai(context: Context, tugasId: String, notificationId: Int) {
        try {
            val tugasPrefs = TugasPreferences(context)
            val tugas = tugasPrefs.getTugasById(tugasId)

            if (tugas != null) {
                val updatedTugas = tugas.copy(isSelesai = true)
                tugasPrefs.updateTugas(updatedTugas)

                val alarmScheduler = AlarmScheduler(context)
                alarmScheduler.cancelAllReminders(tugasId)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)

                // ✅ Context sudah di-wrap, jadi getString otomatis sesuai bahasa
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_task_done, tugas.judul),
                    Toast.LENGTH_SHORT
                ).show()

                val refreshIntent = Intent("com.example.LumiNote.REFRESH_TUGAS")
                context.sendBroadcast(refreshIntent)

            } else {
                Toast.makeText(context, context.getString(R.string.toast_task_not_found), Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSnooze(
        context: Context,
        tugasId: String,
        judul: String,
        deskripsi: String,
        notificationId: Int
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)

            val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 menit

            val alarmScheduler = AlarmScheduler(context)
            alarmScheduler.scheduleSnoozeReminder(
                timeInMillis = snoozeTime,
                tugasId = tugasId,
                judul = context.getString(R.string.reminder_prefix, judul),
                deskripsi = deskripsi
            )

            // ✅ Context sudah di-wrap, jadi getString otomatis sesuai bahasa
            Toast.makeText(context, context.getString(R.string.toast_snooze_active), Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error snooze: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
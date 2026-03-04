package com.example.luminote

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    fun scheduleAllReminders(
        deadlineMillis: Long,
        tugasId: String,
        judul: String,
        deskripsi: String
    ) {
        val prefs = PemberitahuanPreferences(context)
        val now = System.currentTimeMillis()
        val ulangi = prefs.getUlangi()

        when (ulangi) {
            context.getString(R.string.repeat_once1) -> {
                scheduleSingleAlarm(deadlineMillis, tugasId, judul, deskripsi, prefs, now)
            }

            context.getString(R.string.repeat_daily) -> {
                scheduleForDays(deadlineMillis, tugasId, judul, deskripsi, (0..6).toList(), prefs, now)
            }

            context.getString(R.string.repeat_weekday) -> {
                scheduleForDays(deadlineMillis, tugasId, judul, deskripsi, listOf(1, 2, 3, 4, 5), prefs, now)
            }

            context.getString(R.string.repeat_weekend1) -> {
                scheduleForDays(deadlineMillis, tugasId, judul, deskripsi, listOf(0, 6), prefs, now)
            }

            context.getString(R.string.repeat_custom1) -> {
                val hariKustom = prefs.getHariKustom()
                if (hariKustom.isEmpty()) {
                    scheduleSingleAlarm(deadlineMillis, tugasId, judul, deskripsi, prefs, now)
                } else {
                    scheduleForDays(deadlineMillis, tugasId, judul, deskripsi, hariKustom, prefs, now)
                }
            }

            else -> {
                scheduleSingleAlarm(deadlineMillis, tugasId, judul, deskripsi, prefs, now)
            }
        }
    }

    fun scheduleSnoozeReminder(
        timeInMillis: Long,
        tugasId: String,
        judul: String,
        deskripsi: String
    ) {
        scheduleAlarm(
            timeInMillis = timeInMillis,
            tugasId = "${tugasId}_snooze_${System.currentTimeMillis()}",
            judul = judul,
            deskripsi = deskripsi,
            reminderType = TugasReminderType.TEPAT_WAKTU
        )
    }

    private fun scheduleSingleAlarm(
        deadlineMillis: Long,
        tugasId: String,
        judul: String,
        deskripsi: String,
        prefs: PemberitahuanPreferences,
        now: Long
    ) {
        if (prefs.getTugasMendatang()) {
            val mendatangTime = deadlineMillis - (30 * 60 * 1000)
            if (mendatangTime > now) {
                scheduleAlarm(
                    timeInMillis = mendatangTime,
                    tugasId = tugasId,
                    judul = context.getString(R.string.reminder_soon_title, judul),
                    deskripsi = context.getString(R.string.reminder_soon_desc),
                    reminderType = TugasReminderType.MENDATANG
                )
            }
        }

        if (deadlineMillis > now) {
            scheduleAlarm(
                timeInMillis = deadlineMillis,
                tugasId = tugasId,
                judul = judul,
                deskripsi = deskripsi,
                reminderType = TugasReminderType.TEPAT_WAKTU
            )
        }

        if (prefs.getTugasTerlambat()) {
            val terlambatTime = deadlineMillis + (30 * 60 * 1000)
            scheduleAlarm(
                timeInMillis = terlambatTime,
                tugasId = tugasId,
                judul = context.getString(R.string.reminder_late_title, judul),
                deskripsi = context.getString(R.string.reminder_late_desc),
                reminderType = TugasReminderType.TERLAMBAT
            )
        }
    }

    private fun scheduleForDays(
        baseTimeMillis: Long,
        tugasId: String,
        judul: String,
        deskripsi: String,
        daysOfWeek: List<Int>,
        prefs: PemberitahuanPreferences,
        now: Long
    ) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = baseTimeMillis

        val targetHour = calendar.get(Calendar.HOUR_OF_DAY)
        val targetMinute = calendar.get(Calendar.MINUTE)

        var instanceNumber = 0

        for (dayOffset in 0..29) {
            calendar.timeInMillis = baseTimeMillis + (dayOffset * 24 * 60 * 60 * 1000L)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

            if (daysOfWeek.contains(dayOfWeek)) {
                calendar.set(Calendar.HOUR_OF_DAY, targetHour)
                calendar.set(Calendar.MINUTE, targetMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val alarmTime = calendar.timeInMillis

                if (alarmTime > now) {
                    if (prefs.getTugasMendatang()) {
                        val mendatangTime = alarmTime - (30 * 60 * 1000)
                        if (mendatangTime > now) {
                            scheduleAlarm(
                                timeInMillis = mendatangTime,
                                tugasId = "$tugasId-$instanceNumber-m",
                                judul = "⏰ Segera! $judul",
                                deskripsi = "Deadline dalam 30 menit",
                                reminderType = TugasReminderType.MENDATANG
                            )
                        }
                    }

                    scheduleAlarm(
                        timeInMillis = alarmTime,
                        tugasId = "$tugasId-$instanceNumber",
                        judul = judul,
                        deskripsi = deskripsi,
                        reminderType = TugasReminderType.TEPAT_WAKTU
                    )

                    if (prefs.getTugasTerlambat()) {
                        val terlambatTime = alarmTime + (30 * 60 * 1000)
                        scheduleAlarm(
                            timeInMillis = terlambatTime,
                            tugasId = "$tugasId-$instanceNumber-t",
                            judul = "⚠️ Terlambat! $judul",
                            deskripsi = "Tugas belum selesai",
                            reminderType = TugasReminderType.TERLAMBAT
                        )
                    }

                    instanceNumber++
                }
            }
        }
    }

    private fun scheduleAlarm(
        timeInMillis: Long,
        tugasId: String,
        judul: String,
        deskripsi: String,
        reminderType: TugasReminderType
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("tugas_id", tugasId)
            putExtra("judul", judul)
            putExtra("deskripsi", deskripsi)
            putExtra("reminder_type", reminderType.name)
        }

        val requestCode = when (reminderType) {
            TugasReminderType.MENDATANG -> tugasId.hashCode() + 1
            TugasReminderType.TEPAT_WAKTU -> tugasId.hashCode()
            TugasReminderType.TERLAMBAT -> tugasId.hashCode() + 2
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAllReminders(tugasId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (reminderType in TugasReminderType.values()) {
            val intent = Intent(context, AlarmReceiver::class.java)

            val requestCode = when (reminderType) {
                TugasReminderType.MENDATANG -> tugasId.hashCode() + 1
                TugasReminderType.TEPAT_WAKTU -> tugasId.hashCode()
                TugasReminderType.TERLAMBAT -> tugasId.hashCode() + 2
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        for (instanceNumber in 0..29) {
            for (reminderType in TugasReminderType.values()) {
                val intent = Intent(context, AlarmReceiver::class.java)

                val suffix = when (reminderType) {
                    TugasReminderType.MENDATANG -> "-$instanceNumber-m"
                    TugasReminderType.TEPAT_WAKTU -> "-$instanceNumber"
                    TugasReminderType.TERLAMBAT -> "-$instanceNumber-t"
                }

                val modifiedId = "$tugasId$suffix"
                val requestCode = modifiedId.hashCode()

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun rescheduleAll() {
        val tugasPreferences = TugasPreferences(context)
        val semuaTugas = tugasPreferences.getAllTugas()

        for (tugas in semuaTugas) {
            if (!tugas.isSelesai) {
                val tanggal = tugas.tanggal ?: continue
                val waktu = tugas.waktu ?: continue

                val waktuMillis = DateTimeUtil.getTimeInMillis("$tanggal $waktu")

                if (waktuMillis > System.currentTimeMillis()) {
                    scheduleAllReminders(
                        deadlineMillis = waktuMillis,
                        tugasId = tugas.id,
                        judul = tugas.judul,
                        deskripsi = tugas.deskripsi
                    )
                }
            }
        }
    }
}
package com.example.luminote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {

    private fun getChannelId(prefs: PemberitahuanPreferences): String {
        val ringtoneUri = prefs.getNadaDeringUri()
        val getaran = prefs.getAktifkanGetaran()
        val settingsHash = "${ringtoneUri}_${getaran}".hashCode()
        return "tugas_channel_$settingsHash"
    }

    private fun isDarkModeActive(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun showNotification(
        context: Context,
        judul: String,
        deskripsi: String,
        prefs: PemberitahuanPreferences,
        tugasId: String = ""
    ) {
        // ✅ CATATAN: context yang masuk sudah di-wrap di AlarmReceiver,
        // jadi semua getString() otomatis sesuai bahasa

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val customRingtoneUri = prefs.getNadaDeringUri()
        val soundUri = if (customRingtoneUri.isNotEmpty()) {
            Uri.parse(customRingtoneUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val labelAlarm = prefs.getLabelAlarm()
        val channelId = getChannelId(prefs)
        val isDark = isDarkModeActive(context)

        // Colors
        val headerBgColor = if (isDark) "#1E1E1E" else "#0067FF"
        val headerTextColor = "#FFFFFF"
        val headerTextSecondaryColor = if (isDark) "#B0B0B0" else "#E0E0E0"
        val contentBgColor = if (isDark) "#1E2A38" else "#D9EAFD"
        val contentTextPrimary = if (isDark) "#E0E0E0" else "#333333"
        val contentTextSecondary = if (isDark) "#B0B0B0" else "#666666"
        val iconTint = "#FFFFFF"

        val notificationLayout = RemoteViews(context.packageName, R.layout.notifikasi_tugas)

        // Set Backgrounds
        try {
            notificationLayout.setInt(R.id.notificationHeader, "setBackgroundColor", Color.parseColor(headerBgColor))
            notificationLayout.setInt(R.id.notificationContent, "setBackgroundColor", Color.parseColor(contentBgColor))
        } catch (e: Exception) { e.printStackTrace() }

        // Set Texts
        notificationLayout.setTextViewText(R.id.tvJudul, judul)
        notificationLayout.setTextColor(R.id.tvJudul, Color.parseColor(contentTextPrimary))

        notificationLayout.setTextViewText(R.id.tvDeskripsi, deskripsi)
        notificationLayout.setTextColor(R.id.tvDeskripsi, Color.parseColor(contentTextSecondary))

        // ✅ Handle Label with multi-language
        if (labelAlarm.isNotEmpty()) {
            notificationLayout.setTextViewText(R.id.tvLabelHeader, labelAlarm)
            notificationLayout.setTextColor(R.id.tvLabelHeader, Color.parseColor(headerTextColor))

            // ✅ Menggunakan formatted string 🏷️ %1$s
            notificationLayout.setTextViewText(R.id.tvLabelContent, context.getString(R.string.notif_content_label, labelAlarm))
            notificationLayout.setTextColor(R.id.tvLabelContent, Color.parseColor(contentTextSecondary))
            notificationLayout.setViewVisibility(R.id.tvLabelContent, android.view.View.VISIBLE)
        } else {
            // ✅ Gunakan string resource untuk default label
            notificationLayout.setTextViewText(R.id.tvLabelHeader, context.getString(R.string.notif_label_default))
            notificationLayout.setTextColor(R.id.tvLabelHeader, Color.parseColor(headerTextColor))
            notificationLayout.setViewVisibility(R.id.tvLabelContent, android.view.View.GONE)
        }

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        notificationLayout.setTextViewText(R.id.tvWaktu, currentTime)
        notificationLayout.setTextColor(R.id.tvWaktu, Color.parseColor(headerTextSecondaryColor))

        try {
            notificationLayout.setInt(R.id.notificationIcon, "setColorFilter", Color.parseColor(iconTint))
        } catch (e: Exception) { e.printStackTrace() }

        val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500)

        // ✅ Channel Configuration dengan multi-language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc)

                if (prefs.getAktifkanGetaran()) {
                    enableVibration(true)
                    setVibrationPattern(vibrationPattern)
                } else {
                    enableVibration(false)
                }

                setSound(soundUri, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM).build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notificationId = System.currentTimeMillis().toInt()

        // Action Selesai
        val selesaiIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SELESAI
            putExtra(NotificationActionReceiver.EXTRA_TUGAS_ID, tugasId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val selesaiPendingIntent = PendingIntent.getBroadcast(context, notificationId + 1, selesaiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action Snooze
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_TUGAS_ID, tugasId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_JUDUL, judul)
            putExtra(NotificationActionReceiver.EXTRA_DESKRIPSI, deskripsi)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, notificationId + 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // ✅ Notification Builder dengan action labels multi-language
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(false)
            .setTimeoutAfter(60000)
            .addAction(R.drawable.ic_check, context.getString(R.string.notif_action_done), selesaiPendingIntent)
            .addAction(R.drawable.ic_snooze, context.getString(R.string.notif_action_snooze), snoozePendingIntent)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setSound(soundUri, AudioManager.STREAM_ALARM)
            if (prefs.getAktifkanGetaran()) notificationBuilder.setVibrate(vibrationPattern)

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volumePercent = prefs.getVolume()
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val targetVolume = ((volumePercent / 100f) * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolume, 0)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
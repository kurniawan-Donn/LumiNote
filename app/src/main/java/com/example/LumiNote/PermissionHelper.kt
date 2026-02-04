package com.example.LumiNote

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    const val REQUEST_ALARM_PERMISSION = 1001
    const val REQUEST_NOTIFICATION_PERMISSION = 1002

    /**
     * Cek apakah aplikasi bisa schedule exact alarm
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Request permission untuk schedule exact alarm
     */
    fun requestExactAlarmPermission(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_izin_alarm, null)
            val dialog = AlertDialog.Builder(activity)
                .setView(dialogView)
                .create()

            val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_izin)
            val btnOke = dialogView.findViewById<Button>(R.id.btn_oke_izin)

            btnBatal.setOnClickListener { dialog.dismiss() }

            btnOke.setOnClickListener {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    /**
     * Cek dan request permission notifikasi (Android 13+)
     */
    fun checkAndRequestNotificationPermission(activity: AppCompatActivity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                return false
            }
        }
        return true
    }
}
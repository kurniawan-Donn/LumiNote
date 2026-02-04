package com.example.LumiNote

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class TambahTugasActivity : AppCompatActivity() {

    // View Components
    private lateinit var etJudul: EditText
    private lateinit var etIsiTugas: EditText
    private lateinit var layoutTanggal: LinearLayout
    private lateinit var tvTanggal: TextView
    private lateinit var layoutWaktu: LinearLayout
    private lateinit var tvWaktu: TextView
    private lateinit var btnSimpan: Button

    // Data variables
    private var selectedTanggal: String? = null
    private var selectedWaktu: String? = null

    private lateinit var tugasPreferences: TugasPreferences

    // Edit mode variables
    private var isEditMode = false
    private var editTugasId: String? = null
    private var editIsSelesai: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_tugas)

        // Init views
        etJudul = findViewById(R.id.et_judul_tugas)
        etIsiTugas = findViewById(R.id.et_isi_tugas)
        layoutTanggal = findViewById(R.id.layout_tanggal_tugas)
        tvTanggal = findViewById(R.id.tv_tanggal_tugas)
        layoutWaktu = findViewById(R.id.layout_waktu_tugas)
        tvWaktu = findViewById(R.id.tv_waktu_tugas)
        btnSimpan = findViewById(R.id.btn_simpan)

        hideSystemUI()

        tugasPreferences = TugasPreferences(this)
        checkEditMode()

        setupDatePicker()
        setupTimePicker()
        setupSimpanButton()
    }

    private fun checkEditMode() {
        val id = intent.getStringExtra("id")
        val judul = intent.getStringExtra("judul")
        val deskripsi = intent.getStringExtra("deskripsi")
        val tanggal = intent.getStringExtra("tanggal")
        val isSelesai = intent.getBooleanExtra("isSelesai", false)

        if (id != null) {
            isEditMode = true
            editTugasId = id
            editIsSelesai = isSelesai

            etJudul.setText(judul)
            etIsiTugas.setText(deskripsi)

            if (tanggal != null) {
                val parts = tanggal.split(" ")
                if (parts.isNotEmpty()) {
                    selectedTanggal = parts[0]
                    tvTanggal.text = selectedTanggal

                    if (parts.size > 1) {
                        selectedWaktu = parts[1]
                        tvWaktu.text = selectedWaktu
                    }
                }
            }

            btnSimpan.text = getString(R.string.btn_update)
        } else {
            isEditMode = false
            btnSimpan.text = getString(R.string.simpan)
        }
    }

    private fun setupDatePicker() {
        layoutTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedTanggal = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                tvTanggal.text = selectedTanggal
            }, year, month, day).show()
        }
    }

    private fun setupTimePicker() {
        layoutWaktu.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                selectedWaktu = String.format("%02d:%02d", selectedHour, selectedMinute)
                tvWaktu.text = selectedWaktu
            }, hour, minute, true).show()
        }
    }

    private fun getAlarmTimeInMillis(tanggal: String, waktu: String): Long {
        val dateParts = tanggal.split("/")
        val timeParts = waktu.split(":")

        val day = dateParts[0].toInt()
        val month = dateParts[1].toInt() - 1
        val year = dateParts[2].toInt()

        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    private fun checkPermissionsAndSchedule(
        alarmTime: Long,
        tugasId: String,
        judul: String,
        deskripsi: String
    ) {
        if (!PermissionHelper.canScheduleExactAlarms(this)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_izin_diperlukan_title))
                .setMessage(getString(R.string.dialog_izin_diperlukan_message))
                .setPositiveButton(getString(R.string.dialog_ya)) { _, _ ->
                    PermissionHelper.requestExactAlarmPermission(this)
                }
                .setNegativeButton(getString(R.string.dialog_tidak)) { _, _ ->
                    Toast.makeText(this, getString(R.string.toast_alarm_tidak_dapat_dijadwalkan), Toast.LENGTH_LONG).show()
                }
                .show()
            return
        }

        if (!PermissionHelper.checkAndRequestNotificationPermission(this)) {
            Toast.makeText(this, getString(R.string.toast_izin_notifikasi_diperlukan), Toast.LENGTH_SHORT).show()
            return
        }

        val alarmScheduler = AlarmScheduler(this)
        alarmScheduler.scheduleAllReminders(
            deadlineMillis = alarmTime,
            tugasId = tugasId,
            judul = judul,
            deskripsi = deskripsi
        )
    }

    private fun setupSimpanButton() {
        btnSimpan.setOnClickListener {
            val judul = etJudul.text.toString().trim()
            val isiTugas = etIsiTugas.text.toString().trim()

            if (judul.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_judul_kosong), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isiTugas.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_isi_tugas_kosong), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTanggal.isNullOrEmpty() || selectedWaktu.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.toast_tanggal_waktu_harus_diisi), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tanggalWaktu = "$selectedTanggal $selectedWaktu"
            val alarmTime = getAlarmTimeInMillis(selectedTanggal!!, selectedWaktu!!)

            if (alarmTime <= System.currentTimeMillis()) {
                Toast.makeText(this, getString(R.string.toast_waktu_alarm_masa_depan), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode && editTugasId != null) {
                // MODE EDIT
                val tugasUpdate = Tugas(
                    id = editTugasId!!,
                    judul = judul,
                    deskripsi = isiTugas,
                    tanggal = tanggalWaktu,
                    waktu = selectedWaktu,
                    isSelesai = editIsSelesai,
                    timestamp = System.currentTimeMillis()
                )

                tugasPreferences.updateTugas(tugasUpdate)

                val alarmScheduler = AlarmScheduler(this)
                alarmScheduler.cancelAllReminders(editTugasId!!)

                checkPermissionsAndSchedule(
                    alarmTime,
                    tugasUpdate.id,
                    tugasUpdate.judul,
                    tugasUpdate.deskripsi
                )

                Toast.makeText(this, getString(R.string.toast_tugas_diupdate), Toast.LENGTH_SHORT).show()

            } else {
                // MODE TAMBAH
                val tugasBaru = Tugas(
                    judul = judul,
                    deskripsi = isiTugas,
                    tanggal = tanggalWaktu,
                    waktu = selectedWaktu,
                    isSelesai = false,
                    timestamp = System.currentTimeMillis()
                )

                tugasPreferences.addTugas(tugasBaru)

                checkPermissionsAndSchedule(
                    alarmTime,
                    tugasBaru.id,
                    tugasBaru.judul,
                    tugasBaru.deskripsi
                )

                Toast.makeText(this, getString(R.string.toast_tugas_disimpan), Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionHelper.REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.toast_izin_notifikasi_diberikan), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.toast_izin_notifikasi_ditolak), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionHelper.canScheduleExactAlarms(this)) {
            // Permission sudah diberikan
        }
    }
}
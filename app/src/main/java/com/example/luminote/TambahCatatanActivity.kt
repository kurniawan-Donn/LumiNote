package com.example.luminote

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class TambahCatatanActivity : AppCompatActivity() {

    private lateinit var etJudul: EditText
    private lateinit var layoutTanggal: LinearLayout
    private lateinit var tvTanggal: TextView
    private lateinit var layoutWaktu: LinearLayout
    private lateinit var tvWaktu: TextView
    private lateinit var btnLanjut: Button
    private lateinit var etDeskripsiPreview: EditText

    private var tanggalDipilih: String? = null
    private var waktuDipilih: String? = null

    private var isEditMode = false
    private var editCatatanId: String? = null
    private var editNote: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_catatan)

        etJudul = findViewById(R.id.et_judul_catatan)
        etDeskripsiPreview = findViewById(R.id.et_Deskripsi)
        layoutTanggal = findViewById(R.id.layout_tanggal_catatan)
        tvTanggal = findViewById(R.id.tv_tanggal_catatan)
        layoutWaktu = findViewById(R.id.layout_waktu_catatan)
        tvWaktu = findViewById(R.id.tv_waktu_catatan)
        btnLanjut = findViewById(R.id.btn_Lanjut)

        hideSystemUI()

        checkEditMode()

        setupDatePicker()
        setupTimePicker()
        setupLanjutButton()
    }

    private fun checkEditMode() {
        val id = intent.getStringExtra("id")

        if (id != null) {
            isEditMode = true
            editCatatanId = id

            val catatanPref = CatatanPreferences(this)
            val catatan = catatanPref.getCatatanById(id)
            if (catatan != null) {
                etJudul.setText(catatan.judul)
                etDeskripsiPreview.setText(catatan.deskripsi)
                tanggalDipilih = catatan.tanggal
                tvTanggal.text = tanggalDipilih
                waktuDipilih = catatan.waktu
                tvWaktu.text = waktuDipilih
                editNote = catatan.note
            }

            btnLanjut.text = getString(R.string.simpan)
        } else {
            isEditMode = false
            btnLanjut.text = getString(R.string.lanjut)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupDatePicker() {
        layoutTanggal.setOnClickListener {
            val kalender = Calendar.getInstance()
            val tahun = kalender.get(Calendar.YEAR)
            val bulan = kalender.get(Calendar.MONTH)
            val hari = kalender.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                tanggalDipilih = String.format("%02d/%02d/%04d", d, m + 1, y)
                tvTanggal.text = tanggalDipilih
            }, tahun, bulan, hari).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupTimePicker() {
        layoutWaktu.setOnClickListener {
            val kalender = Calendar.getInstance()
            val jam = kalender.get(Calendar.HOUR_OF_DAY)
            val menit = kalender.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                waktuDipilih = String.format("%02d:%02d", h, m)
                tvWaktu.text = waktuDipilih
            }, jam, menit, true).show()
        }
    }

    private fun setupLanjutButton() {
        btnLanjut.setOnClickListener {
            val judul = etJudul.text.toString().trim()
            val deskripsi = etDeskripsiPreview.text.toString().trim()

            if (judul.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_judul_kosong), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode && editCatatanId != null) {
                val catatanPref = CatatanPreferences(this)
                val catatanUpdate = Catatan(
                    id = editCatatanId!!,
                    judul = judul,
                    deskripsi = deskripsi,
                    note = editNote ?: "",
                    tanggal = tanggalDipilih,
                    waktu = waktuDipilih,
                    timestamp = System.currentTimeMillis()
                )
                catatanPref.updateCatatan(catatanUpdate)
                Toast.makeText(this, getString(R.string.toast_catatan_diupdate), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val intent = Intent(this, CatatanActivity::class.java).apply {
                    putExtra("judul", judul)
                    putExtra("deskripsi", deskripsi)
                    putExtra("tanggal", tanggalDipilih)
                    putExtra("waktu", waktuDipilih)
                    putExtra("note", editNote)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
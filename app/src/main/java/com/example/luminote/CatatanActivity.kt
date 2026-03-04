package com.example.luminote

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class CatatanActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var saveButton: ImageView
    private lateinit var noteTitle: TextView
    private lateinit var dateTimeText: TextView
    private lateinit var noteContent: TextView

    private lateinit var catatanPreferences: CatatanPreferences

    private var itemId: String? = null
    private var judul: String? = null
    private var deskripsi: String? = null
    private var note: String? = null
    private var tanggal: String? = null
    private var waktu: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catatan)

        catatanPreferences = CatatanPreferences(this)

        initViews()
        hideSystemUI()
        getIntentData()
        setupUI()
        setupListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        noteTitle = findViewById(R.id.noteTitle)
        dateTimeText = findViewById(R.id.dateTimeText)
        noteContent = findViewById(R.id.noteContent)
    }

    private fun getIntentData() {
        itemId = intent.getStringExtra("id")
        judul = intent.getStringExtra("judul")
        deskripsi = intent.getStringExtra("deskripsi")
        note = intent.getStringExtra("note")
        tanggal = intent.getStringExtra("tanggal")
        waktu = intent.getStringExtra("waktu")
    }

    private fun setupUI() {
        if (itemId != null) {
            loadExistingCatatan()
        } else {
            // Menggunakan string resource untuk judul default
            noteTitle.text = judul ?: getString(R.string.default_note_title)

            val tanggalText = tanggal ?: ""
            val waktuText = waktu ?: ""

            dateTimeText.text = when {
                tanggalText.isNotEmpty() && waktuText.isNotEmpty() -> "$tanggalText\n$waktuText"
                tanggalText.isNotEmpty() -> tanggalText
                waktuText.isNotEmpty() -> waktuText
                else -> getString(R.string.no_date_time)
            }
        }
    }

    private fun loadExistingCatatan() {
        val catatan = catatanPreferences.getCatatanById(itemId!!) ?: return

        noteTitle.text = catatan.judul
        noteContent.text = catatan.note
        dateTimeText.text = catatan.getFormatTanggal()

        judul = catatan.judul
        tanggal = catatan.tanggal
        waktu = catatan.waktu
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveCatatan() }
    }

    private fun saveCatatan() {
        val isiCatatan = noteContent.text.toString().trim()

        // Validasi dengan string resource
        if (judul.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_title), Toast.LENGTH_SHORT).show()
            return
        }
        if (isiCatatan.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_content), Toast.LENGTH_SHORT).show()
            return
        }

        val catatan = Catatan(
            id = itemId ?: UUID.randomUUID().toString(),
            judul = judul.orEmpty(),
            deskripsi = deskripsi.orEmpty(),
            note = isiCatatan,
            tanggal = tanggal,
            waktu = waktu
        )

        if (itemId != null) {
            catatanPreferences.updateCatatan(catatan)
            Toast.makeText(this, getString(R.string.toast_note_updated), Toast.LENGTH_SHORT).show()
        } else {
            catatanPreferences.addCatatan(catatan)
            Toast.makeText(this, getString(R.string.toast_note_saved), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etIdNama: EditText
    private lateinit var etPassword: EditText
    private lateinit var etKonfirmasiPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleKonfirmasiPassword: ImageButton
    private lateinit var btnDaftar: Button
    private lateinit var tvMasuk: TextView

    private lateinit var userManager: UserManager
    private var isPasswordVisible = false
    private var isKonfirmasiPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        userManager = UserManager(this)
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etIdNama = findViewById(R.id.etIdNama)
        etPassword = findViewById(R.id.etPassword)
        etKonfirmasiPassword = findViewById(R.id.etKonfirmasiPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleKonfirmasiPassword = findViewById(R.id.btnToggleKonfirmasiPassword)
        btnDaftar = findViewById(R.id.btnDaftar)
        tvMasuk = findViewById(R.id.tvMasuk)
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Toggle password visibility
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnToggleKonfirmasiPassword.setOnClickListener {
            toggleKonfirmasiPasswordVisibility()
        }

        // Daftar button
        btnDaftar.setOnClickListener {
            register()
        }

        // Masuk
        tvMasuk.setOnClickListener {
            finish()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_on)
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text.length)
    }

    private fun toggleKonfirmasiPasswordVisibility() {
        if (isKonfirmasiPasswordVisible) {
            etKonfirmasiPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnToggleKonfirmasiPassword.setImageResource(R.drawable.ic_eye_off)
        } else {
            etKonfirmasiPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnToggleKonfirmasiPassword.setImageResource(R.drawable.ic_eye_on)
        }
        isKonfirmasiPasswordVisible = !isKonfirmasiPasswordVisible
        etKonfirmasiPassword.setSelection(etKonfirmasiPassword.text.length)
    }

    private fun register() {
        val idNama = etIdNama.text.toString().trim()
        val password = etPassword.text.toString()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString()

        // Validasi ID Nama
        if (idNama.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_id_nama_kosong), Toast.LENGTH_SHORT).show()
            return
        }

        if (idNama.length < 4) {
            Toast.makeText(this, getString(R.string.toast_id_nama_minimal), Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi hanya huruf kecil dan angka
        if (!idNama.matches(Regex("^[a-z0-9]+$"))) {
            Toast.makeText(this, getString(R.string.toast_id_nama_format), Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi Password
        if (password.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_password_kosong), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 5) {
            Toast.makeText(this, getString(R.string.toast_password_minimal), Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi Konfirmasi Password
        if (konfirmasiPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_konfirmasi_password_kosong), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != konfirmasiPassword) {
            Toast.makeText(this, getString(R.string.toast_password_tidak_cocok), Toast.LENGTH_SHORT).show()
            return
        }

        // Cek apakah ID sudah digunakan
        if (userManager.isIdExists(idNama)) {
            Toast.makeText(this, getString(R.string.toast_id_sudah_digunakan), Toast.LENGTH_SHORT).show()
            return
        }

        // Register user
        val success = userManager.registerUser(idNama, password)

        if (success) {
            Toast.makeText(this, getString(R.string.toast_registrasi_berhasil), Toast.LENGTH_SHORT).show()

            // Kembali ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.toast_registrasi_gagal), Toast.LENGTH_SHORT).show()
        }
    }
}
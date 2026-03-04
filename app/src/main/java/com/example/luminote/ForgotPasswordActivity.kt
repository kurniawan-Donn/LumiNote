package com.example.luminote

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etIdNama: EditText
    private lateinit var etPasswordBaru: EditText
    private lateinit var etKonfirmasiPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleKonfirmasiPassword: ImageButton
    private lateinit var btnResetPassword: Button

    private lateinit var userManager: UserManager
    private var isPasswordVisible = false
    private var isKonfirmasiPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initViews()
        userManager = UserManager(this)
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etIdNama = findViewById(R.id.etIdNama)
        etPasswordBaru = findViewById(R.id.etPasswordBaru)
        etKonfirmasiPassword = findViewById(R.id.etKonfirmasiPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleKonfirmasiPassword = findViewById(R.id.btnToggleKonfirmasiPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnToggleKonfirmasiPassword.setOnClickListener {
            toggleKonfirmasiPasswordVisibility()
        }

        btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPasswordBaru.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        } else {
            etPasswordBaru.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_on)
        }
        isPasswordVisible = !isPasswordVisible
        etPasswordBaru.setSelection(etPasswordBaru.text.length)
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

    private fun resetPassword() {
        val idNama = etIdNama.text.toString().trim()
        val passwordBaru = etPasswordBaru.text.toString()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString()

        // Validasi ID Nama
        if (idNama.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_id_empty), Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi Password Baru
        if (passwordBaru.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_password_new_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru.length < 12) {
            Toast.makeText(this, getString(R.string.error_password_too_short_12), Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi Konfirmasi Password
        if (konfirmasiPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_confirm_password_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru != konfirmasiPassword) {
            Toast.makeText(this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        // Cek apakah ID Nama ada
        if (!userManager.isIdExists(idNama)) {
            Toast.makeText(this, getString(R.string.error_id_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        // Reset password
        val success = userManager.resetPassword(idNama, passwordBaru)

        if (success) {
            Toast.makeText(this, getString(R.string.toast_reset_success), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.toast_reset_failed), Toast.LENGTH_SHORT).show()
        }
    }
}
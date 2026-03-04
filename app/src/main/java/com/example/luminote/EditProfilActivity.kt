package com.example.luminote

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import kotlin.math.min

class EditProfilActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var imgProfile: ImageView
    private lateinit var btnChangePhoto: ImageButton
    private lateinit var etNama: EditText
    private lateinit var etBio: EditText
    private lateinit var etIdNama: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnBatal: Button
    private lateinit var btnSimpan: Button

    private lateinit var userManager: UserManager
    private lateinit var sessionManager: SessionManager
    private lateinit var currentUser: User

    private var isPasswordVisible = false
    private var selectedImageUri: Uri? = null

    // Launcher untuk memilih foto dari galeri
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Tampilkan foto yang dipilih dengan cara native
                loadCircularImageFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profil)

        initViews()
        userManager = UserManager(this)
        sessionManager = SessionManager(this)
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        imgProfile = findViewById(R.id.imgProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        etNama = findViewById(R.id.etNama)
        etBio = findViewById(R.id.etBio)
        etIdNama = findViewById(R.id.etIdNama)
        etPassword = findViewById(R.id.etPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnBatal = findViewById(R.id.btnBatal)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()

        if (userId != null) {
            val user = userManager.getUserById(userId)
            if (user != null) {
                currentUser = user

                // Set data ke form
                etNama.setText(user.nama)
                etBio.setText(user.bio)
                etIdNama.setText(user.idNama)
                etPassword.setText(user.password)

                // Load foto profil jika ada dengan cara native
                if (user.fotoProfil.isNotEmpty()) {
                    loadCircularImageFromUri(Uri.parse(user.fotoProfil))
                }
            }
        }
    }

    private fun loadCircularImageFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            if (inputStream != null) {
                var bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                // Rotate jika perlu (handle rotasi dari EXIF)
                bitmap = rotateImageIfRequired(bitmap, uri)

                // Crop menjadi circular
                val circularBitmap = getCircularBitmap(bitmap)

                imgProfile.setImageBitmap(circularBitmap)
            }
        } catch (e: Exception) {
            imgProfile.setImageResource(R.drawable.ic_person)
            e.printStackTrace()
        }
    }

    // Fungsi untuk rotate image sesuai EXIF
    private fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (_: Exception) {
            return bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Fungsi untuk membuat circular bitmap
    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint()
        val rect = android.graphics.Rect(0, 0, size, size)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squaredBitmap, rect, rect, paint)

        return output
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnChangePhoto.setOnClickListener {
            openGallery()
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnBatal.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            showKonfirmasiDialog()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
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

    private fun showKonfirmasiDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_konfirmasi)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnBatal = dialog.findViewById<Button>(R.id.btnBatal)
        val btnYa = dialog.findViewById<Button>(R.id.btnYa)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnYa.setOnClickListener {
            dialog.dismiss()
            simpanPerubahan()
        }

        dialog.show()
    }

    private fun simpanPerubahan() {
        val nama = etNama.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val idNamaBaru = etIdNama.text.toString().trim()
        val passwordBaru = etPassword.text.toString()

        // Validasi Nama
        if (nama.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_name_empty), Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi ID Nama (jika diubah)
        if (idNamaBaru != currentUser.idNama) {
            if (idNamaBaru.length < 4) {
                Toast.makeText(this, getString(R.string.error_id_too_short), Toast.LENGTH_SHORT).show()
                return
            }

            if (!idNamaBaru.matches(Regex("^[a-z0-9]+$"))) {
                Toast.makeText(this, getString(R.string.error_id_format), Toast.LENGTH_SHORT).show()
                return
            }

            if (userManager.isIdExists(idNamaBaru)) {
                Toast.makeText(this, getString(R.string.error_id_exists), Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Validasi Password
        if (passwordBaru.length < 5) {
            Toast.makeText(this, getString(R.string.error_password_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        // Update user
        currentUser.nama = nama
        currentUser.bio = bio
        currentUser.password = passwordBaru

        // Update foto profil
        if (selectedImageUri != null) {
            val savedPath = ImageHelper.saveImageToInternalStorage(
                this,
                selectedImageUri!!,
                currentUser.idNama
            )
            if (savedPath != null) {
                currentUser.fotoProfil = savedPath
            }
        }

        // Jika ID Nama berubah, hapus user lama dan buat user baru
        if (idNamaBaru != currentUser.idNama) {
            val allUsers = userManager.getAllUsers()
            allUsers.removeAll { it.idNama == currentUser.idNama }

            currentUser.idNama = idNamaBaru
            allUsers.add(currentUser)

            val usersJson = com.google.gson.Gson().toJson(allUsers)
            getSharedPreferences("LumiNoteUsers", MODE_PRIVATE)
                .edit()
                .putString("users_list", usersJson)
                .apply()

            // Update session
            sessionManager.createLoginSession(idNamaBaru)
        } else {
            userManager.updateUser(currentUser)
        }

        Toast.makeText(this, getString(R.string.toast_profile_updated), Toast.LENGTH_SHORT).show()

        // Kembali ke ProfilActivity
        setResult(RESULT_OK)
        finish()
    }
}
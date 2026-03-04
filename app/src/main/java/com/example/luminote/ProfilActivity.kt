package com.example.luminote

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import java.io.InputStream

class ProfilActivity : AppCompatActivity() {

    // Views
    private lateinit var backButton: ImageView
    private lateinit var btnEdit: Button
    private lateinit var imgProfile: ImageView
    private lateinit var tvNama: TextView
    private lateinit var tvBio: TextView

    private lateinit var layoutPengaturanPemberitahuan: LinearLayout

    // Menu Items
    private lateinit var layoutFsforit: LinearLayout
    private lateinit var layoutArsip: LinearLayout
    private lateinit var layoutStatistik: LinearLayout

    // Pengaturan Items
    private lateinit var switchModeGelap: SwitchCompat
    private lateinit var switchPemberitahuan: SwitchCompat
    private lateinit var layoutBahasa: LinearLayout
    private lateinit var tvBahasa: TextView

    // Tentang Aplikasi Items
    private lateinit var layoutBackup: LinearLayout
    private lateinit var layoutHapusData: LinearLayout
    private lateinit var layoutTentangKami: LinearLayout
    private lateinit var layoutLogout: LinearLayout

    private lateinit var userManager: UserManager
    private lateinit var sessionManager: SessionManager
    private lateinit var pemberitahuanPrefs: PemberitahuanPreferences

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.toast_profil_diperbarui), Toast.LENGTH_SHORT).show()
            loadUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🔥 PERBAIKAN: Apply language DAN theme sebelum super.onCreate()
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        try {
            initViews()
            userManager = UserManager(this)
            pemberitahuanPrefs = PemberitahuanPreferences(this)
            sessionManager = SessionManager(this)
            loadUserData()
            setupListeners()

            // 🔥 Load status dark mode ke switch
            loadDarkModeSwitch()

            // ✅ TAMBAHAN BARU: Load bahasa yang dipilih
            loadLanguageDisplay()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error, e.message), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPemberitahuanSettings()
        // 🔥 Refresh dark mode switch saat kembali ke activity
        loadDarkModeSwitch()
        // ✅ TAMBAHAN BARU: Refresh language display
        loadLanguageDisplay()
    }

    private fun initViews() {
        // Header
        backButton = findViewById(R.id.backButton)
        btnEdit = findViewById(R.id.btnEdit)

        // Profile Info
        imgProfile = findViewById(R.id.imgProfile)
        tvNama = findViewById(R.id.tvNama)
        tvBio = findViewById(R.id.tvBio)

        // Menu
        layoutFsforit = findViewById(R.id.layoutFsforit)
        layoutArsip = findViewById(R.id.layoutArsip)
        layoutStatistik = findViewById(R.id.layoutStatistik)

        // Pengaturan
        switchModeGelap = findViewById(R.id.switchModeGelap)
        switchPemberitahuan = findViewById(R.id.switchPemberitahuan)
        layoutPengaturanPemberitahuan = findViewById(R.id.layoutPengaturanPemberitahuan)

        layoutBahasa = findViewById(R.id.layoutBahasa)
        tvBahasa = findViewById(R.id.tvBahasa)

        // Tentang Aplikasi
        layoutBackup = findViewById(R.id.layoutBackup)
        layoutHapusData = findViewById(R.id.layoutHapusData)
        layoutTentangKami = findViewById(R.id.layoutTentangKami)
        layoutLogout = findViewById(R.id.layoutLogout)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        btnEdit.setOnClickListener { openEditProfil() }

        layoutFsforit.setOnClickListener { openFaforit() }
        layoutArsip.setOnClickListener { openArsip() }
        layoutStatistik.setOnClickListener { openStatistik() }

        // 🔥 PERBAIKAN: Toggle dark mode dengan recreate activity
        switchModeGelap.setOnCheckedChangeListener { _, isChecked ->
            toggleModeGelap(isChecked)
        }

        switchPemberitahuan.setOnCheckedChangeListener { _, isChecked ->
            togglePemberitahuan(isChecked)
        }

        layoutPengaturanPemberitahuan.setOnClickListener {
            openPengaturanPemberitahuan()
        }

        // ✅ TAMBAHAN BARU: Buka bottom sheet bahasa
        layoutBahasa.setOnClickListener { showBahasaBottomSheet() }

        layoutBackup.setOnClickListener { openBackupRestore() }
        layoutHapusData.setOnClickListener { showHapusDataDialog() }
        layoutTentangKami.setOnClickListener { openTentangKami() }
        layoutLogout.setOnClickListener { showLogoutDialog() }
    }

    // 🔥 FUNGSI BARU: Load status dark mode ke switch
    private fun loadDarkModeSwitch() {
        val isDarkMode = ThemeHelper.isDarkMode(this)

        // Set switch tanpa trigger listener
        switchModeGelap.setOnCheckedChangeListener(null)
        switchModeGelap.isChecked = isDarkMode

        // Pasang kembali listener
        switchModeGelap.setOnCheckedChangeListener { _, isChecked ->
            toggleModeGelap(isChecked)
        }
    }

    // ✅ FUNGSI BARU: Load bahasa yang dipilih ke TextView
    private fun loadLanguageDisplay() {
        val languageName = LanguageHelper.getLanguageDisplayName(this)
        tvBahasa.text = languageName
    }

    private fun loadUserData() {
        try {
            val userId = sessionManager.getUserId()

            if (userId != null) {
                val user = userManager.getUserById(userId)
                if (user != null) {
                    tvNama.text = user.nama
                    tvBio.text = user.bio

                    if (user.fotoProfil.isNotEmpty()) {
                        loadImageFromInternalStorage(user.fotoProfil)
                    } else {
                        imgProfile.setImageResource(R.drawable.ic_person)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_user_tidak_ditemukan), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_session_tidak_valid), Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error_loading_data, e.message), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun loadImageFromInternalStorage(filePath: String) {
        try {
            val bitmap = ImageHelper.loadImageFromInternalStorage(filePath)
            if (bitmap != null) {
                val circularBitmap = getCircularBitmap(bitmap)
                imgProfile.setImageBitmap(circularBitmap)
            } else {
                imgProfile.setImageResource(R.drawable.ic_person)
            }
        } catch (e: Exception) {
            imgProfile.setImageResource(R.drawable.ic_person)
            e.printStackTrace()
        }
    }

    private fun loadCircularImageFromUri(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            if (inputStream != null) {
                var bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                bitmap = rotateImageIfRequired(bitmap, uri)
                val circularBitmap = getCircularBitmap(bitmap)
                imgProfile.setImageBitmap(circularBitmap)
            } else {
                imgProfile.setImageResource(R.drawable.ic_person)
            }
        } catch (e: Exception) {
            imgProfile.setImageResource(R.drawable.ic_person)
            e.printStackTrace()
        }
    }

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

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
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

    private fun openEditProfil() {
        try {
            val intent = Intent(this, EditProfilActivity::class.java)
            editProfileLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFaforit() {
        val intent = Intent(this, FaforitActivity::class.java)
        startActivity(intent)
    }

    private fun openArsip() {
        val intent = Intent(this, ArsipActivity::class.java)
        startActivity(intent)
    }

    private fun openStatistik() {
        startActivity(Intent(this, StatistikActivity::class.java))
    }

    // 🔥 PERBAIKAN: Simpan ke SharedPreferences dan recreate activity
    private fun toggleModeGelap(isEnabled: Boolean) {
        // Simpan status ke SharedPreferences
        ThemeHelper.setDarkMode(this, isEnabled)

        val message = if (isEnabled) getString(R.string.toast_mode_gelap_aktif)
        else getString(R.string.toast_mode_terang_aktif)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Recreate activity untuk apply theme
        recreate()
    }

    private fun loadPemberitahuanSettings() {
        val isEnabled = pemberitahuanPrefs.getSetelPemberitahuan()

        switchPemberitahuan.setOnCheckedChangeListener(null)
        switchPemberitahuan.isChecked = isEnabled

        switchPemberitahuan.setOnCheckedChangeListener { _, checked ->
            togglePemberitahuan(checked)
        }

        layoutPengaturanPemberitahuan.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
    }

    private fun togglePemberitahuan(isEnabled: Boolean) {
        pemberitahuanPrefs.setSetelPemberitahuan(isEnabled)

        layoutPengaturanPemberitahuan.visibility =
            if (isEnabled) View.VISIBLE else View.GONE

        val message = if (isEnabled) {
            getString(R.string.toast_pemberitahuan_aktif)
        } else {
            getString(R.string.toast_pemberitahuan_nonaktif)
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openPengaturanPemberitahuan() {
        startActivity(Intent(this, PemberitahuanActivity::class.java))
    }

    // ✅ FUNGSI BARU: Tampilkan bottom sheet bahasa
    private fun showBahasaBottomSheet() {
        val bottomSheet = BahasaBottomSheet.newInstance()
        bottomSheet.show(supportFragmentManager, BahasaBottomSheet.TAG)
    }

    private fun openBackupRestore() {
        startActivity(Intent(this, BackupRestoreActivity::class.java))
    }

    private fun showHapusDataDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_hapus_data, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
            val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
            val btnYa = dialogView.findViewById<Button>(R.id.btnYa)

            tvTitle.text = getString(R.string.dialog_hapus_data_title)

            btnBatal.setOnClickListener {
                dialog.dismiss()
                Toast.makeText(this, getString(R.string.toast_penghapusan_dibatalkan), Toast.LENGTH_SHORT).show()
            }

            btnYa.setOnClickListener {
                dialog.dismiss()
                hapusSemuaData()
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error_dialog, e.message), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun hapusSemuaData() {
        try {
            val catatanPrefs = CatatanPreferences(this)
            val jumlahCatatan = catatanPrefs.getCatatanList().size

            val tugasPrefs = TugasPreferences(this)
            val jumlahTugas = tugasPrefs.getAllTugas().size

            val arsipPrefs = ArsipPreferences(this)
            val jumlahArsipCatatan = arsipPrefs.getArsipCatatan().size
            val jumlahArsipTugas = arsipPrefs.getArsipTugas().size

            catatanPrefs.saveCatatanList(emptyList())
            tugasPrefs.saveList(emptyList())
            arsipPrefs.saveArsipCatatan(emptyList())
            arsipPrefs.saveArsipTugas(emptyList())

            val totalDihapus = jumlahCatatan + jumlahTugas + jumlahArsipCatatan + jumlahArsipTugas

            showHapusDataSuccessDialog(
                total = totalDihapus,
                catatan = jumlahCatatan,
                tugas = jumlahTugas,
                arsipCatatan = jumlahArsipCatatan,
                arsipTugas = jumlahArsipTugas
            )

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_gagal_hapus_data, e.message), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun showHapusDataSuccessDialog(
        total: Int,
        catatan: Int,
        tugas: Int,
        arsipCatatan: Int,
        arsipTugas: Int
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hapus_data_success, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val imgIcon = dialogView.findViewById<ImageView>(R.id.imgStatusIcon)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitleSuccess)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessageSuccess)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOkSuccess)

        if (total == 0) {
            imgIcon.setImageResource(R.drawable.img_confused)
            tvTitle.text = getString(R.string.dialog_tidak_ada_data_title)
            tvMessage.text = getString(R.string.dialog_tidak_ada_data_message)
        } else {
            imgIcon.setImageResource(R.drawable.img_dog_meme)
            tvTitle.text = getString(R.string.dialog_data_berhasil_dihapus_title)

            val summary = buildString {
                append(getString(R.string.dialog_total_item_dihapus, total))
                append("\n")
                if (catatan > 0) append(getString(R.string.dialog_item_catatan, catatan) + "\n")
                if (tugas > 0) append(getString(R.string.dialog_item_tugas, tugas) + "\n")
                if (arsipCatatan > 0) append(getString(R.string.dialog_item_arsip_catatan, arsipCatatan) + "\n")
                if (arsipTugas > 0) append(getString(R.string.dialog_item_arsip_tugas, arsipTugas))
            }
            tvMessage.text = summary
            btnOk.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        }

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openTentangKami() {
        startActivity(Intent(this, TentangKamiActivity::class.java))
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnLogoutAction = dialogView.findViewById<Button>(R.id.btnLogoutAction)
        val btnStay = dialogView.findViewById<Button>(R.id.btnStay)

        btnLogoutAction.setOnClickListener {
            logout()
            dialog.dismiss()
        }

        btnStay.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logout() {
        sessionManager.logout()

        Toast.makeText(this, getString(R.string.toast_berhasil_logout), Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
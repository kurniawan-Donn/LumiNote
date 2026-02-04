package com.example.LumiNote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class TentangKamiActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var tvEmail: TextView

    // Social & Share Cards
    private lateinit var cardGithub: CardView
    private lateinit var cardWhatsApp: CardView
    private lateinit var cardInstagram: CardView
    private lateinit var cardShareApp: CardView

    // Links & Info
    private val githubUrl = "https://github.com/kurniawan-Donn/LumiNote_1.1.0"
    private val whatsappNumber = "+6282133237136"
    private val instagramUsername = "kurniawandony7"
    private val emailAddress = "donykurniawan1298@gmail.com"

    // APK Info
    private val apkDownloadUrl = "https://github.com/kurniawan-Donn/LumiNote_1.1.0/releases/download/v1.1.0/LumiNote-v1.1.0.apk"
    private val appVersion = "1.1.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentang_kami)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        tvEmail = findViewById(R.id.tvEmail)

        // Initialize social cards
        cardGithub = findViewById(R.id.cardGithub)
        cardWhatsApp = findViewById(R.id.cardWhatsApp)
        cardInstagram = findViewById(R.id.cardInstagram)
        cardShareApp = findViewById(R.id.cardShareApp)
    }

    private fun setupListeners() {
        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Email - auto link sudah di XML, tapi tambah copy to clipboard
        tvEmail.setOnLongClickListener {
            copyToClipboard(emailAddress, getString(R.string.label_email))
            true
        }

        // GitHub Repository
        cardGithub.setOnClickListener {
            openUrl(githubUrl, "GitHub")
        }

        // WhatsApp Contact
        cardWhatsApp.setOnClickListener {
            openWhatsApp()
        }

        // Instagram
        cardInstagram.setOnClickListener {
            openInstagram()
        }

        // Share App
        cardShareApp.setOnClickListener {
            showShareDialog()
        }
    }

    // =====================================
    // OPEN LINKS
    // =====================================

    private fun openUrl(url: String, platform: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_tidak_dapat_membuka, platform), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun openWhatsApp() {
        try {
            // Format pesan default
            val message = getString(R.string.whatsapp_default_message)
            val encodedMessage = Uri.encode(message)

            // Coba buka WhatsApp langsung
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$whatsappNumber?text=$encodedMessage")

            // Cek apakah WhatsApp terinstall
            if (isAppInstalled("com.whatsapp")) {
                intent.setPackage("com.whatsapp")
            }

            startActivity(intent)
        } catch (_: Exception) {
            // Fallback: Copy nomor ke clipboard
            copyToClipboard(whatsappNumber, getString(R.string.label_nomor_whatsapp))
            Toast.makeText(this, getString(R.string.toast_whatsapp_tidak_ditemukan), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openInstagram() {
        try {
            // Coba buka Instagram app
            val intent = Intent(Intent.ACTION_VIEW)

            if (isAppInstalled("com.instagram.android")) {
                // Buka di app Instagram
                intent.data = Uri.parse("http://instagram.com/_u/$instagramUsername")
                intent.setPackage("com.instagram.android")
            } else {
                // Buka di browser
                intent.data = Uri.parse("https://www.instagram.com/$instagramUsername")
            }

            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_tidak_dapat_membuka_instagram), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // =====================================
    // SHARE APP
    // =====================================

    private fun showShareDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_share_app, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi Klik Opsi
        dialogView.findViewById<LinearLayout>(R.id.optionShareApk).setOnClickListener {
            shareApkFile()
            dialog.dismiss()
        }

        dialogView.findViewById<LinearLayout>(R.id.optionShareLink).setOnClickListener {
            shareDownloadLink()
            dialog.dismiss()
        }

        dialogView.findViewById<LinearLayout>(R.id.optionCopyLink).setOnClickListener {
            copyDownloadLink()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancelShare).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun shareApkFile() {
        try {
            val packageName = packageName
            val packageManager = packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir

            if (apkPath == null) {
                Toast.makeText(this, getString(R.string.toast_error_apk_tidak_ditemukan), Toast.LENGTH_SHORT).show()
                shareDownloadLink()
                return
            }

            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                java.io.File(apkPath)
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_apk_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_apk_text, appVersion, githubUrl))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_apk_chooser)))
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, getString(R.string.toast_error_apk_tidak_ditemukan), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            shareDownloadLink()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error_bagikan_apk, e.message), Toast.LENGTH_LONG).show()
            e.printStackTrace()

            // Fallback ke share link
            shareDownloadLink()
        }
    }

    private fun shareDownloadLink() {
        val shareText = getString(R.string.share_link_text, apkDownloadUrl, githubUrl, appVersion)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_apk_subject))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_link_chooser)))
    }

    private fun copyDownloadLink() {
        // 1. Proses Copy ke Clipboard
        copyToClipboard(apkDownloadUrl, getString(R.string.label_link_download))

        // 2. Tampilkan Custom Dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_copy_link_success, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Membuat background bawaan dialog menjadi transparan agar CardView terlihat rounded
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set URL ke TextView di dalam kotak abu-abu
        val tvUrlDisplay = dialogView.findViewById<TextView>(R.id.tvUrlDisplay)
        tvUrlDisplay.text = apkDownloadUrl

        // Tombol Oke
        val btnOk = dialogView.findViewById<Button>(R.id.btnOkCopy)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // =====================================
    // HELPER FUNCTIONS
    // =====================================

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.toast_disalin_ke_clipboard, label), Toast.LENGTH_SHORT).show()
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
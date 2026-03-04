package com.example.luminote

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var tvSimpanTerakhir: TextView
    private lateinit var btnCadangkan: Button
    private lateinit var progressBackup: ProgressBar
    private lateinit var btnPulihkan: Button
    private lateinit var tvWarningRestore: TextView

    private lateinit var catatanPrefs: CatatanPreferences
    private lateinit var tugasPrefs: TugasPreferences
    private lateinit var arsipPrefs: ArsipPreferences
    private lateinit var userManager: UserManager
    private lateinit var sessionManager: SessionManager
    private lateinit var backupPrefs: BackupPreferences

    private val gson = Gson()

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { saveBackupToFile(it) }
    }

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { showRestoreDialog(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_restore)

        initViews()
        initPreferences()
        loadLastBackupInfo()

        backButton.setOnClickListener { finish() }
        btnCadangkan.setOnClickListener { startBackup() }
        btnPulihkan.setOnClickListener { startRestore() }
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        tvSimpanTerakhir = findViewById(R.id.tvSimpanTerakhir)
        btnCadangkan = findViewById(R.id.btnCadangkan)
        progressBackup = findViewById(R.id.progressBackup)
        btnPulihkan = findViewById(R.id.btnPulihkan)
        tvWarningRestore = findViewById(R.id.tvWarningRestore)
    }

    private fun initPreferences() {
        catatanPrefs = CatatanPreferences(this)
        tugasPrefs = TugasPreferences(this)
        arsipPrefs = ArsipPreferences(this)
        userManager = UserManager(this)
        sessionManager = SessionManager(this)
        backupPrefs = BackupPreferences(this)
    }

    private fun loadLastBackupInfo() {
        val lastBackup = backupPrefs.getLastBackupDate()
        val statusText = lastBackup ?: getString(R.string.last_backup_none)
        tvSimpanTerakhir.text = getString(R.string.last_backup_label, statusText)
    }

    private fun startBackup() {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId == null) {
            Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "LumiNote_Backup_$timestamp.json"
        createFileLauncher.launch(filename)
    }

    private fun saveBackupToFile(uri: Uri) {
        progressBackup.visibility = View.VISIBLE
        btnCadangkan.isEnabled = false

        try {
            val backupData = createBackupData()
            val jsonString = gson.toJson(backupData)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            backupPrefs.saveLastBackupDate(currentDate)

            loadLastBackupInfo()
            Toast.makeText(this, getString(R.string.toast_backup_success), Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.toast_backup_failed, e.message), Toast.LENGTH_SHORT).show()
        } finally {
            progressBackup.visibility = View.GONE
            btnCadangkan.isEnabled = true
        }
    }

    private fun createBackupData(): BackupData {
        val currentUserId = sessionManager.getUserId() ?: ""
        val currentUser = userManager.getUserById(currentUserId)

        return BackupData(
            version = "1.0",
            backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            user = currentUser,
            catatan = catatanPrefs.getCatatanList(),
            tugas = tugasPrefs.getAllTugas(),
            arsipCatatanIds = arsipPrefs.getArsipCatatan(),
            arsipTugasIds = arsipPrefs.getArsipTugas()
        )
    }

    private fun startRestore() {
        openFileLauncher.launch(arrayOf("application/json"))
    }

    private fun showRestoreDialog(uri: Uri) {
        tvWarningRestore.visibility = View.VISIBLE
        val dialogView = layoutInflater.inflate(R.layout.dialog_backup_restore, null)

        val radioReplace = dialogView.findViewById<RadioButton>(R.id.radioReplace)
        val radioMerge = dialogView.findViewById<RadioButton>(R.id.radioMerge)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnRestore = dialogView.findViewById<Button>(R.id.btnRestore)

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnRestore.setOnClickListener {
            when {
                radioReplace.isChecked -> {
                    restoreFromFile(uri, RestoreMode.REPLACE)
                    dialog.dismiss()
                }
                radioMerge.isChecked -> {
                    restoreFromFile(uri, RestoreMode.MERGE)
                    dialog.dismiss()
                }
                else -> {
                    Toast.makeText(this, getString(R.string.toast_select_method), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun restoreFromFile(uri: Uri, mode: RestoreMode) {
        try {
            val jsonString = readJsonFromUri(uri)
            val backupData = gson.fromJson(jsonString, BackupData::class.java)

            if (!validateBackupData(backupData)) {
                Toast.makeText(this, getString(R.string.toast_restore_invalid_format), Toast.LENGTH_SHORT).show()
                return
            }

            showRestoreConfirmation(backupData, mode)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.toast_restore_invalid_format), Toast.LENGTH_SHORT).show()
        }
    }

    private fun readJsonFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun validateBackupData(backupData: BackupData): Boolean {
        return backupData.catatan != null && backupData.tugas != null
    }

    private fun showRestoreConfirmation(backupData: BackupData, mode: RestoreMode) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_restore_confirm, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
        val tvDataSummary = dialogView.findViewById<TextView>(R.id.tvDataSummary)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        tvDescription.text = if (mode == RestoreMode.REPLACE) {
            getString(R.string.desc_restore_replace)
        } else {
            getString(R.string.desc_restore_merge)
        }

        val summary = StringBuilder()
            .append(getString(R.string.summary_notes, backupData.catatan?.size ?: 0)).append("\n")
            .append(getString(R.string.summary_tasks, backupData.tugas?.size ?: 0)).append("\n")
            .append(getString(R.string.summary_archived_notes, backupData.arsipCatatanIds?.size ?: 0)).append("\n")
            .append(getString(R.string.summary_archived_tasks, backupData.arsipTugasIds?.size ?: 0))
            .toString()

        tvDataSummary.text = summary

        btnConfirm.setOnClickListener {
            performRestore(backupData, mode)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun performRestore(backupData: BackupData, mode: RestoreMode) {
        try {
            when (mode) {
                RestoreMode.REPLACE -> {
                    catatanPrefs.saveCatatanList(emptyList())
                    tugasPrefs.saveList(emptyList())
                    arsipPrefs.saveArsipCatatan(emptyList())
                    arsipPrefs.saveArsipTugas(emptyList())

                    backupData.catatan?.let { catatanPrefs.saveCatatanList(it) }
                    backupData.tugas?.let { tugasPrefs.saveList(it) }
                    backupData.arsipCatatanIds?.let { arsipPrefs.saveArsipCatatan(it) }
                    backupData.arsipTugasIds?.let { arsipPrefs.saveArsipTugas(it) }
                }

                RestoreMode.MERGE -> {
                    backupData.catatan?.let { backupCatatan ->
                        val existing = catatanPrefs.getCatatanList().toMutableList()
                        val existingIds = existing.map { it.id }.toSet()
                        existing.addAll(backupCatatan.filter { it.id !in existingIds })
                        catatanPrefs.saveCatatanList(existing)
                    }

                    backupData.tugas?.let { backupTugas ->
                        val existing = tugasPrefs.getAllTugas().toMutableList()
                        val existingIds = existing.map { it.id }.toSet()
                        existing.addAll(backupTugas.filter { it.id !in existingIds })
                        tugasPrefs.saveList(existing)
                    }

                    backupData.arsipCatatanIds?.let { backupIds ->
                        val existingIds = arsipPrefs.getArsipCatatan().toMutableSet()
                        existingIds.addAll(backupIds)
                        arsipPrefs.saveArsipCatatan(existingIds.toList())
                    }

                    backupData.arsipTugasIds?.let { backupIds ->
                        val existingIds = arsipPrefs.getArsipTugas().toMutableSet()
                        existingIds.addAll(backupIds)
                        arsipPrefs.saveArsipTugas(existingIds.toList())
                    }
                }
            }

            backupData.user?.let { backupUser ->
                val currentUserId = sessionManager.getUserId()
                if (currentUserId != null) {
                    val updatedUser = backupUser.copy(idNama = currentUserId)
                    userManager.updateUser(updatedUser)
                }
            }

            Toast.makeText(this, getString(R.string.toast_restore_success), Toast.LENGTH_SHORT).show()
            showRestartDialog()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.toast_backup_failed, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRestartDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_restore_succes, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnRestart = dialogView.findViewById<Button>(R.id.btnRestart)
        btnRestart.setOnClickListener {
            restartApp()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
    }

    data class BackupData(
        val version: String,
        val backupDate: String,
        val user: User?,
        val catatan: List<Catatan>?,
        val tugas: List<Tugas>?,
        val arsipCatatanIds: List<String>?,
        val arsipTugasIds: List<String>?
    )

    enum class RestoreMode { REPLACE, MERGE }
}

class BackupPreferences(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("BackupPrefs", android.content.Context.MODE_PRIVATE)
    fun saveLastBackupDate(date: String) = prefs.edit().putString("last_backup_date", date).apply()
    fun getLastBackupDate(): String? = prefs.getString("last_backup_date", null)
}
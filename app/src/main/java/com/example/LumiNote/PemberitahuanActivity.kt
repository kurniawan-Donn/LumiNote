package com.example.LumiNote

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.RadioButton

class PemberitahuanActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageView
    private lateinit var switchSetelPemberitahuan: SwitchCompat
    private lateinit var switchSetelAlarm: SwitchCompat

    // Pemberitahuan Card
    private lateinit var cardPemberitahuan: CardView
    private lateinit var switchTugasMendatang: SwitchCompat
    private lateinit var switchTugasTerlambat: SwitchCompat

    // Alarm Card
    private lateinit var cardAlarm: CardView
    private lateinit var tvBawaan: TextView
    private lateinit var switchAktifkanGetaran: SwitchCompat
    private lateinit var tvSekali: TextView
    private lateinit var layoutUlangi: RelativeLayout
    private lateinit var seekBarVolume: SeekBar

    private var selectedHariKustom = mutableListOf<Int>()

    // Jangan Ganggu Card
    private lateinit var janganGanggu: CardView
    private lateinit var switchWaktuTenang: SwitchCompat
    private lateinit var cardWaktuTenangDetail: CardView
    private lateinit var tvMulaiTime: TextView
    private lateinit var tvSelesaiTime: TextView

    // Clickable items
    private lateinit var layoutNadaDering: RelativeLayout
    private lateinit var layoutBeriLabel: RelativeLayout
    private lateinit var layoutMulai: RelativeLayout
    private lateinit var layoutSelesai: RelativeLayout

    private lateinit var prefs: PemberitahuanPreferences
    private var ringtonePlayer: android.media.Ringtone? = null

    // ✅ MODERN WAY - Activity Result Launcher untuk Ringtone Picker
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // ✅ PERBAIKAN: getParcelableExtra modern way
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }

            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(this, uri)
                val ringtoneName = ringtone.getTitle(this)

                prefs.setNadaDering(ringtoneName, uri.toString())
                tvBawaan.text = ringtoneName
                showToast(getString(R.string.toast_nada_dering_diubah, ringtoneName))
            } else {
                prefs.setNadaDering(getString(R.string.ringtone_default), "")
                tvBawaan.text = getString(R.string.ringtone_default)
                showToast(getString(R.string.toast_nada_dering_default))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pemberitahuan)

        initViews()
        prefs = PemberitahuanPreferences(this)
        loadSettings()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)

        switchSetelPemberitahuan = findViewById(R.id.switchSetelPemberitahuan)
        switchSetelAlarm = findViewById(R.id.switchSetelAlarm)

        cardPemberitahuan = findViewById(R.id.cardPemberitahuan)
        cardAlarm = findViewById(R.id.cardAlarm)
        janganGanggu = findViewById(R.id.janganGanggu)

        switchTugasMendatang = findViewById(R.id.switchTugasMendatang)
        switchTugasTerlambat = findViewById(R.id.switchTugasTerlambat)

        tvBawaan = findViewById(R.id.tvBawaan)
        switchAktifkanGetaran = findViewById(R.id.switchAktifkanGetaran)
        tvSekali = findViewById(R.id.tvSekali)
        layoutUlangi = findViewById(R.id.layoutUlangi)
        seekBarVolume = findViewById(R.id.seekBarVolume)

        switchWaktuTenang = findViewById(R.id.switchWaktuTenang)
        cardWaktuTenangDetail = findViewById(R.id.cardWaktuTenangDetail)
        tvMulaiTime = findViewById(R.id.tvMulaiTime)
        tvSelesaiTime = findViewById(R.id.tvSelesaiTime)

        layoutNadaDering = findViewById(R.id.layoutNadaDering)
        layoutBeriLabel = findViewById(R.id.layoutBeriLabel)
        layoutMulai = findViewById(R.id.layoutMulai)
        layoutSelesai = findViewById(R.id.layoutSelesai)
    }

    private fun loadSettings() {
        switchSetelPemberitahuan.isChecked = prefs.getSetelPemberitahuan()
        switchSetelAlarm.isChecked = prefs.getSetelAlarm()
        switchTugasMendatang.isChecked = prefs.getTugasMendatang()
        switchTugasTerlambat.isChecked = prefs.getTugasTerlambat()
        tvBawaan.setTextColor(prefs.getWarnaNadaDering())
        switchAktifkanGetaran.isChecked = prefs.getAktifkanGetaran()
        seekBarVolume.progress = prefs.getVolume()

        val ulangiSetting = prefs.getUlangi()

        if (ulangiSetting == getString(R.string.repeat_custom)) {
            val days = resources.getStringArray(R.array.days_of_week)
            val savedDays = prefs.getHariKustom()

            tvSekali.text = if (savedDays.isNotEmpty()) {
                savedDays.map { days[it] }.joinToString(", ")
            } else {
                getString(R.string.repeat_custom)
            }
        } else {
            tvSekali.text = ulangiSetting
        }

        val waktuTenangAktif = prefs.getWaktuTenang()
        switchWaktuTenang.isChecked = waktuTenangAktif
        tvMulaiTime.text = prefs.getWaktuMulai()
        tvSelesaiTime.text = prefs.getWaktuSelesai()

        updateCardVisibility()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        switchSetelPemberitahuan.setOnCheckedChangeListener { _, isChecked ->
            prefs.setSetelPemberitahuan(isChecked)
            updateCardVisibility()
            showToast(if (isChecked) getString(R.string.toast_pemberitahuan_aktif) else getString(R.string.toast_pemberitahuan_nonaktif))
        }

        layoutNadaDering.setOnLongClickListener {
            showColorPickerDialog()
            true // return true agar tidak memicu click biasa
        }

        switchSetelAlarm.setOnCheckedChangeListener { _, isChecked ->
            prefs.setSetelAlarm(isChecked)
            updateCardVisibility()
            showToast(if (isChecked) getString(R.string.toast_alarm_aktif) else getString(R.string.toast_alarm_nonaktif))
        }

        switchTugasMendatang.setOnCheckedChangeListener { _, isChecked ->
            prefs.setTugasMendatang(isChecked)
            showToast(if (isChecked) getString(R.string.toast_tugas_mendatang_aktif) else getString(R.string.toast_tugas_mendatang_nonaktif))
        }

        switchTugasTerlambat.setOnCheckedChangeListener { _, isChecked ->
            prefs.setTugasTerlambat(isChecked)
            showToast(if (isChecked) getString(R.string.toast_tugas_terlambat_aktif) else getString(R.string.toast_tugas_terlambat_nonaktif))
        }

        layoutNadaDering.setOnClickListener { openRingtonePicker() }

        // TAMBAHKAN INI:
        layoutNadaDering.setOnLongClickListener {
            showColorPickerDialog()
            true // return true agar tidak memicu click biasa
        }

        switchAktifkanGetaran.setOnCheckedChangeListener { _, isChecked ->
            prefs.setAktifkanGetaran(isChecked)
            showToast(if (isChecked) getString(R.string.toast_getaran_aktif) else getString(R.string.toast_getaran_nonaktif))
        }

        layoutUlangi.setOnClickListener { showUlangiDialog() }
        layoutBeriLabel.setOnClickListener { showLabelDialog() }

        setupVolumeSeekBar()

        switchWaktuTenang.setOnCheckedChangeListener { _, isChecked ->
            prefs.setWaktuTenang(isChecked)
            updateCardVisibility()
            showToast(if (isChecked) getString(R.string.toast_waktu_tenang_aktif) else getString(R.string.toast_waktu_tenang_nonaktif))
        }

        layoutMulai.setOnClickListener {
            if (switchWaktuTenang.isChecked) {
                showTimePicker(true)
            } else {
                showToast(getString(R.string.toast_aktifkan_waktu_tenang))
            }
        }

        layoutSelesai.setOnClickListener {
            if (switchWaktuTenang.isChecked) {
                showTimePicker(false)
            } else {
                showToast(getString(R.string.toast_aktifkan_waktu_tenang))
            }
        }
    }

    private fun setupVolumeSeekBar() {
        val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    prefs.setVolume(progress)

                    val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                    val targetVolume = ((progress / 100f) * maxVolume).toInt()
                    audioManager.setStreamVolume(
                        android.media.AudioManager.STREAM_ALARM,
                        targetVolume,
                        0
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playRingtonePreview()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                stopRingtonePreview()
                showToast(getString(R.string.toast_volume_alarm, seekBar?.progress))
            }
        })
    }

    private fun showColorPickerDialog() {
        val colors = intArrayOf(
            android.graphics.Color.RED,
            android.graphics.Color.BLUE,
            android.graphics.Color.GREEN,
            android.graphics.Color.MAGENTA,
            android.graphics.Color.BLACK,
            android.graphics.Color.parseColor("#FF8800") // Orange
        )
        val colorNames = resources.getStringArray(R.array.color_names)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_pilih_warna_teks))
        builder.setItems(colorNames) { _, which ->
            val selectedColor = colors[which]

            // Simpan ke Preferences
            prefs.setWarnaNadaDering(selectedColor)

            // Update UI secara langsung
            tvBawaan.setTextColor(selectedColor)
            showToast(getString(R.string.toast_warna_diubah))
        }
        builder.show()
    }

    private fun playRingtonePreview() {
        try {
            stopRingtonePreview()

            val customUri = prefs.getNadaDeringUri()
            val uri = if (customUri.isNotEmpty()) {
                Uri.parse(customUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            ringtonePlayer = RingtoneManager.getRingtone(this, uri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtonePlayer?.audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                ringtonePlayer?.streamType = android.media.AudioManager.STREAM_ALARM
            }

            ringtonePlayer?.play()

        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.toast_gagal_preview))
        }
    }

    private fun stopRingtonePreview() {
        try {
            ringtonePlayer?.stop()
            ringtonePlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        stopRingtonePreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtonePreview()
    }

    private fun updateCardVisibility() {
        cardPemberitahuan.visibility =
            if (switchSetelPemberitahuan.isChecked) View.VISIBLE else View.GONE

        cardAlarm.visibility =
            if (switchSetelAlarm.isChecked) View.VISIBLE else View.GONE

        janganGanggu.visibility =
            if (switchSetelPemberitahuan.isChecked || switchSetelAlarm.isChecked)
                View.VISIBLE else View.GONE

        cardWaktuTenangDetail.visibility =
            if (switchWaktuTenang.isChecked) View.VISIBLE else View.GONE
    }

    private fun openRingtonePicker() {
        try {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.picker_pilih_nada))
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)

                val currentUri = prefs.getNadaDeringUri()
                if (currentUri.isNotEmpty()) {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUri))
                }
            }
            ringtonePickerLauncher.launch(intent)

        } catch (e: Exception) {
            showToast(getString(R.string.toast_gagal_buka_picker))
            e.printStackTrace()
        }
    }

    private fun showUlangiDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ulangi_alarm, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val rgUlangi = dialogView.findViewById<RadioGroup>(R.id.rg_ulangi)
        val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_ulangi)

        // Set radio button yang aktif sesuai prefs saat ini
        val currentSetting = prefs.getUlangi()
        for (i in 0 until rgUlangi.childCount) {
            val rb = rgUlangi.getChildAt(i) as RadioButton
            if (rb.text.toString() == currentSetting) {
                rb.isChecked = true
                break
            }
        }

        // Listener saat RadioButton diklik
        rgUlangi.setOnCheckedChangeListener { group, checkedId ->
            val selectedRb = group.findViewById<RadioButton>(checkedId)
            val selectedText = selectedRb.text.toString()

            if (selectedText == getString(R.string.repeat_custom)) {
                dialog.dismiss()
                showKustomDayDialog() // Tetap panggil dialog kustom hari Anda
            } else {
                prefs.setUlangi(selectedText)
                tvSekali.text = selectedText
                showToast(getString(R.string.toast_pengulangan, selectedText))
                dialog.dismiss()
            }
        }

        btnBatal.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showKustomDayDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_kustom_hari, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val days = resources.getStringArray(R.array.days_of_week)
        val checkBoxes = arrayOf(
            dialogView.findViewById<CheckBox>(R.id.cb_minggu),
            dialogView.findViewById<CheckBox>(R.id.cb_senin),
            dialogView.findViewById<CheckBox>(R.id.cb_selasa),
            dialogView.findViewById<CheckBox>(R.id.cb_rabu),
            dialogView.findViewById<CheckBox>(R.id.cb_kamis),
            dialogView.findViewById<CheckBox>(R.id.cb_jumat),
            dialogView.findViewById<CheckBox>(R.id.cb_sabtu)
        )

        // Load data lama ke CheckBox
        val savedDays = prefs.getHariKustom()
        selectedHariKustom.clear()
        selectedHariKustom.addAll(savedDays)

        checkBoxes.forEachIndexed { index, checkBox ->
            checkBox.isChecked = selectedHariKustom.contains(index)
        }

        dialogView.findViewById<Button>(R.id.btn_batal_hari).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_ok_hari).setOnClickListener {
            // Ambil pilihan baru dari CheckBox
            selectedHariKustom.clear()
            checkBoxes.forEachIndexed { index, checkBox ->
                if (checkBox.isChecked) selectedHariKustom.add(index)
            }

            if (selectedHariKustom.isEmpty()) {
                prefs.setUlangi(getString(R.string.repeat_once))
                prefs.setHariKustom(emptyList())
                tvSekali.text = getString(R.string.repeat_once)
                showToast(getString(R.string.toast_pengulangan_sekali))
            } else {
                selectedHariKustom.sort()
                val dayNames = selectedHariKustom.map { days[it] }.joinToString(", ")
                prefs.setUlangi(getString(R.string.repeat_custom))
                prefs.setHariKustom(selectedHariKustom)
                tvSekali.text = dayNames
                showToast(getString(R.string.toast_pengulangan, dayNames))
            }
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showLabelDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_label_alarm, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etLabel = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_label_input)
        val btnBatal = dialogView.findViewById<Button>(R.id.btn_batal_label)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btn_simpan_label)

        // Set teks awal dari preferences
        etLabel.setText(prefs.getLabelAlarm())
        etLabel.setSelection(etLabel.text?.length ?: 0)
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            val label = etLabel.text.toString().trim()
            if (label.isNotEmpty()) {
                prefs.setLabelAlarm(label)
                showToast(getString(R.string.toast_label_disimpan, label))
            } else {
                prefs.setLabelAlarm("")
                showToast(getString(R.string.toast_label_dihapus))
            }
            // Pastikan Anda memperbarui UI di Activity jika ada TextView label
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showTimePicker(isMulai: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title_time)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.custom_time_picker)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_time)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok_time)

        // Atur judul dan mode 24 jam
        tvTitle.text = if (isMulai) getString(R.string.dialog_waktu_mulai) else getString(R.string.dialog_waktu_selesai)
        timePicker.setIs24HourView(true)

        // Ambil waktu saat ini dari prefs untuk posisi awal spinner
        val currentTime = if (isMulai) prefs.getWaktuMulai() else prefs.getWaktuSelesai()
        val parts = currentTime.split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.hour = hour
            timePicker.minute = minute
        } else {
            @Suppress("DEPRECATION")
            timePicker.currentHour = hour
            @Suppress("DEPRECATION")
            timePicker.currentMinute = minute
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnOk.setOnClickListener {
            val selectedHour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
            val selectedMinute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute

            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)

            if (isMulai) {
                prefs.setWaktuMulai(timeString)
                tvMulaiTime.text = timeString
            } else {
                prefs.setWaktuSelesai(timeString)
                tvSelesaiTime.text = timeString
            }

            showToast(getString(R.string.toast_waktu_disimpan, timeString))
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
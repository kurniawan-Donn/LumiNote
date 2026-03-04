package com.example.luminote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArsipActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var btnArsipAction: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var rvArsip: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var arsipAdapter: ArsipAdapter
    private lateinit var arsipPreferences: ArsipPreferences
    private lateinit var catatanPreferences: CatatanPreferences
    private lateinit var tugasPreferences: TugasPreferences

    private var allArsipList = listOf<ArsipItem>()
    private var filteredList = listOf<ArsipItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arsip)

        initViews()
        initPreferences()
        setupRecyclerView()
        setupListeners()
        loadArsipData()
    }

    override fun onResume() {
        super.onResume()
        loadArsipData()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        btnArsipAction = findViewById(R.id.btnStar)
        etSearch = findViewById(R.id.etSearch)
        rvArsip = findViewById(R.id.rvPenting)
        layoutEmpty = findViewById(R.id.layoutEmpty)
    }

    private fun initPreferences() {
        arsipPreferences = ArsipPreferences(this)
        catatanPreferences = CatatanPreferences(this)
        tugasPreferences = TugasPreferences(this)
    }

    private fun setupRecyclerView() {
        arsipAdapter = ArsipAdapter(
            arsipList = emptyList(),
            onPulihkanClick = { item ->
                showPulihkanDialog(item)
            },
            onHapusClick = { item ->
                showHapusDialog(item)
            }
        )

        rvArsip.apply {
            layoutManager = LinearLayoutManager(this@ArsipActivity)
            adapter = arsipAdapter
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        btnArsipAction.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_feature_under_repair), Toast.LENGTH_SHORT).show()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadArsipData() {
        val arsipList = mutableListOf<ArsipItem>()

        val arsipCatatanIds = arsipPreferences.getArsipCatatan()
        val allCatatan = catatanPreferences.getCatatanList()
        val catatanArsip = allCatatan.filter { it.id in arsipCatatanIds }

        catatanArsip.forEach { catatan ->
            arsipList.add(ArsipItem.fromCatatan(catatan))
        }

        val arsipTugasIds = arsipPreferences.getArsipTugas()
        val allTugas = tugasPreferences.getAllTugas()
        val tugasArsip = allTugas.filter { it.id in arsipTugasIds }

        tugasArsip.forEach { tugas ->
            arsipList.add(ArsipItem.fromTugas(tugas))
        }

        allArsipList = arsipList.sortedByDescending { it.timestamp }
        filteredList = allArsipList

        updateUI()
    }

    private fun filterData(query: String) {
        filteredList = if (query.isEmpty()) {
            allArsipList
        } else {
            allArsipList.filter { it.matchesQuery(query) }
        }
        updateUI()
    }

    private fun updateUI() {
        if (filteredList.isEmpty()) {
            rvArsip.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvArsip.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            arsipAdapter.updateData(filteredList)
        }
    }
    private fun getTranslatedType(tipe: String): String {
        return if (tipe == "Catatan") getString(R.string.type_catatan) else getString(R.string.type_tugas)
    }

    private fun showPulihkanDialog(item: ArsipItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pulihkan_arsip, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = getString(R.string.title_restore_item_dialog, getTranslatedType(item.tipe))
        tvMessage.text = getString(R.string.msg_restore_item_dialog, item.judul)

        btnConfirm.setOnClickListener {
            pulihkanItem(item)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHapusDialog(item: ArsipItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hapus_permanen, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDeleteTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDeleteMessage)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmDelete)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelDelete)

        // Set Data Dinamis
        tvTitle.text = getString(R.string.title_delete_permanent_dialog, item.tipe)
        tvMessage.text = getString(R.string.msg_delete_permanent_dialog, item.judul)

        btnConfirm.setOnClickListener {
            hapusItem(item)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun pulihkanItem(item: ArsipItem) {
        val translatedType = getTranslatedType(item.tipe)
        when (item.tipe) {
            "Catatan" -> arsipPreferences.pulihkanCatatan(item.id)
            "Tugas" -> arsipPreferences.pulihkanTugas(item.id)
        }
        Toast.makeText(this, getString(R.string.toast_item_restored, translatedType), Toast.LENGTH_SHORT).show()
        loadArsipData()
    }

    private fun hapusItem(item: ArsipItem) {
        val translatedType = getTranslatedType(item.tipe)
        when (item.tipe) {
            "Catatan" -> {
                arsipPreferences.pulihkanCatatan(item.id)
                catatanPreferences.deleteCatatan(item.id)
            }
            "Tugas" -> {
                arsipPreferences.pulihkanTugas(item.id)
                tugasPreferences.deleteTugas(item.id)
            }
        }
        Toast.makeText(this, getString(R.string.toast_item_deleted_perm, translatedType), Toast.LENGTH_SHORT).show()
        loadArsipData()
    }
}
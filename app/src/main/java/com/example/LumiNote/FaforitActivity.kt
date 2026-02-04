package com.example.LumiNote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FaforitActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var etSearch: EditText
    private lateinit var rvPenting: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var faforitAdapter: FaforitAdapter
    private lateinit var faforitPreferences: FaforitPreferences
    private lateinit var catatanPreferences: CatatanPreferences
    private lateinit var tugasPreferences: TugasPreferences

    private var allFaforitList = listOf<FaforitItem>()
    private var filteredList = listOf<FaforitItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faforit)

        initViews()
        initPreferences()
        setupRecyclerView()
        setupListeners()
        loadFaforitData()
    }

    override fun onResume() {
        super.onResume()
        // Reload data setiap kali activity di-resume
        loadFaforitData()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        etSearch = findViewById(R.id.etSearch)
        rvPenting = findViewById(R.id.rvPenting)
        layoutEmpty = findViewById(R.id.layoutEmpty)
    }

    private fun initPreferences() {
        faforitPreferences = FaforitPreferences(this)
        catatanPreferences = CatatanPreferences(this)
        tugasPreferences = TugasPreferences(this)
    }

    private fun setupRecyclerView() {
        faforitAdapter = FaforitAdapter(emptyList()) { item ->
            removeFavorit(item)
        }

        rvPenting.apply {
            layoutManager = LinearLayoutManager(this@FaforitActivity)
            adapter = faforitAdapter
        }
    }

    private fun setupListeners() {
        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadFaforitData() {
        val faforitList = mutableListOf<FaforitItem>()

        // Load Catatan Favorit
        val favoritCatatanIds = faforitPreferences.getFavoritCatatan()

        // Get all catatan (gunakan method yang benar)
        try {
            val allCatatan = catatanPreferences.getCatatanList()
            val catatanFavorit = allCatatan.filter { it.id in favoritCatatanIds }

            catatanFavorit.forEach { catatan ->
                faforitList.add(FaforitItem.fromCatatan(catatan))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Load Tugas Favorit
        val favoritTugasIds = faforitPreferences.getFavoritTugas()

        // Get all tugas (gunakan method yang benar)
        try {
            val allTugas = tugasPreferences.getAllTugas()
            val tugasFavorit = allTugas.filter { it.id in favoritTugasIds }

            tugasFavorit.forEach { tugas ->
                faforitList.add(FaforitItem.fromTugas(tugas))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort by timestamp (newest first)
        allFaforitList = faforitList.sortedByDescending { it.timestamp }
        filteredList = allFaforitList

        updateUI()
    }

    private fun filterData(query: String) {
        filteredList = if (query.isEmpty()) {
            allFaforitList
        } else {
            allFaforitList.filter { it.matchesQuery(query) }
        }
        updateUI()
    }

    private fun updateUI() {
        if (filteredList.isEmpty()) {
            rvPenting.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvPenting.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            faforitAdapter.updateData(filteredList)
        }
    }

    private fun removeFavorit(item: FaforitItem) {
        val translatedType = if (item.tipe == "Catatan") {
            getString(R.string.type_catatan)
        } else {
            getString(R.string.type_tugas)
        }
        when (item.tipe) {
            "Catatan" -> {
                faforitPreferences.toggleCatatanFavorit(item.id)
            }
            "Tugas" -> {
                faforitPreferences.toggleTugasFavorit(item.id)
            }
        }
        Toast.makeText(
            this,
            getString(R.string.toast_favorite_removed_item, translatedType),
            Toast.LENGTH_SHORT
        ).show()
        loadFaforitData()
    }
}
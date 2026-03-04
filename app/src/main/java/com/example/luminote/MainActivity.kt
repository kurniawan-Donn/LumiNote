package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var mainHeader: TextView
    private lateinit var btnToProfil: ImageButton
    private lateinit var sessionManager: SessionManager

    // 🔥 TAMBAHAN: Track dark mode state
    private var currentDarkMode: Boolean = false
    private var currentLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)

        super.onCreate(savedInstanceState)

        android.util.Log.d("MainActivity", "🏠 MAIN onCreate - Language: ${LanguageHelper.getLanguage(this)}, Theme Applied")

        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        // 🔥 Simpan state dark mode saat pertama kali dibuat
        currentDarkMode = ThemeHelper.isDarkMode(this)
        currentLanguage = LanguageHelper.getLanguage(this)

        // Inisialisasi views
        bottomNav = findViewById(R.id.bottom_nav_view)
        fab = findViewById(R.id.fab_add)
        mainHeader = findViewById(R.id.main_header)
        btnToProfil = findViewById(R.id.btnToProfil)

        setupNavigation()
        setupFab()
        setupBottomNavListener()
        setupBackPressHandler()
        setupProfileButton()

        // Log dark mode status
        android.util.Log.d("MainActivity", "Dark Mode Active: ${ThemeHelper.isDarkMode(this)}")
    }

    // 🔥 SOLUSI UTAMA: Refresh theme saat kembali dari ProfilActivity
    override fun onResume() {
        super.onResume()

        val newDarkMode = ThemeHelper.isDarkMode(this)
        val newLanguage = LanguageHelper.getLanguage(this)

        android.util.Log.d("MainActivity", "🔄 onResume - Current: $currentDarkMode, New: $newDarkMode")

        // Jika dark mode berubah, recreate activity
        if (currentDarkMode != newDarkMode || currentLanguage != newLanguage) {
            android.util.Log.d("MainActivity", "🔄 Perubahan terdeteksi! Recreating MainActivity...")
            currentDarkMode = newDarkMode
            currentLanguage = newLanguage
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.catatan_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catatan -> {
                    navController.navigate(R.id.navigation_catatan)
                    mainHeader.text = getString(R.string.daftar_catatan)
                    true
                }
                R.id.navigation_tugas -> {
                    navController.navigate(R.id.navigation_tugas)
                    mainHeader.text = getString(R.string.daftar_tugas)
                    true
                }
                R.id.navigation_plus -> {
                    showPilihTipeDialog()
                    false
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        fab.setOnClickListener {
            showPilihTipeDialog()
        }
    }

    private fun setupBottomNavListener() {
        bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catatan -> mainHeader.text = getString(R.string.daftar_catatan)
                R.id.navigation_tugas -> mainHeader.text = getString(R.string.daftar_tugas)
            }
        }
    }

    private fun showPilihTipeDialog() {
        val bottomSheet = BottomSheetDialog()
        bottomSheet.show(supportFragmentManager, "PilihTipeBottomSheet")
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.catatan_fragment) as NavHostFragment
                val navController = navHostFragment.navController

                if (navController.currentDestination?.id == R.id.navigation_catatan ||
                    navController.currentDestination?.id == R.id.navigation_tugas) {
                    finish()
                } else {
                    navController.navigateUp()
                }
            }
        })
    }

    private fun setupProfileButton() {
        btnToProfil.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)
        }
    }
}
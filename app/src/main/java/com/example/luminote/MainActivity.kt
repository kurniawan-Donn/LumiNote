package com.example.luminote

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var mainHeader: TextView
    private lateinit var btnToProfil: ImageButton

    // ✅ Tombol kalender floating
    private lateinit var cardBtnKalender: CardView
    private lateinit var btnKalender: ImageButton

    private lateinit var sessionManager: SessionManager
    private var currentDarkMode: Boolean = false
    private var currentLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager    = SessionManager(this)
        currentDarkMode   = ThemeHelper.isDarkMode(this)
        currentLanguage   = LanguageHelper.getLanguage(this)

        bottomNav      = findViewById(R.id.bottom_nav_view)
        fab            = findViewById(R.id.fab_add)
        mainHeader     = findViewById(R.id.main_header)
        btnToProfil    = findViewById(R.id.btnToProfil)
        cardBtnKalender = findViewById(R.id.card_btn_kalender)
        btnKalender    = findViewById(R.id.btn_kalender)

        setupNavigation()
        setupFab()
        setupBottomNavListener()
        setupBackPressHandler()
        setupProfileButton()
        setupKalenderButton()
    }

    override fun onResume() {
        super.onResume()
        val newDarkMode = ThemeHelper.isDarkMode(this)
        val newLanguage = LanguageHelper.getLanguage(this)
        if (currentDarkMode != newDarkMode || currentLanguage != newLanguage) {
            currentDarkMode = newDarkMode
            currentLanguage = newLanguage
            recreate()
        }
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
                    // Kalender button: warna normal
                    setKalenderActive(false)
                    true
                }
                R.id.navigation_tugas -> {
                    navController.navigate(R.id.navigation_tugas)
                    mainHeader.text = getString(R.string.daftar_tugas)
                    setKalenderActive(false)
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

    private fun setupKalenderButton() {
        cardBtnKalender.setOnClickListener { navigateToKalender() }
        btnKalender.setOnClickListener    { navigateToKalender() }
    }

    private fun navigateToKalender() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.catatan_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.navigate(R.id.navigation_kalender)
        mainHeader.text = getString(R.string.title_kalender)

        // Hilangkan active state dari bottom nav
        bottomNav.menu.findItem(R.id.navigation_catatan)?.isChecked = false
        bottomNav.menu.findItem(R.id.navigation_tugas)?.isChecked   = false

        // Highlight tombol kalender
        setKalenderActive(true)
    }

    /** Ubah visual tombol kalender saat aktif/tidak aktif */
    private fun setKalenderActive(active: Boolean) {
        val alpha = if (active) 1.0f else 0.7f
        cardBtnKalender.alpha = alpha
        cardBtnKalender.cardElevation = if (active) 16f else 10f
    }

    private fun setupFab() {
        fab.setOnClickListener { showPilihTipeDialog() }
    }

    private fun setupBottomNavListener() {
        bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catatan -> mainHeader.text = getString(R.string.daftar_catatan)
                R.id.navigation_tugas   -> mainHeader.text = getString(R.string.daftar_tugas)
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

                val dest = navController.currentDestination?.id
                if (dest == R.id.navigation_catatan ||
                    dest == R.id.navigation_tugas    ||
                    dest == R.id.navigation_kalender) {
                    finish()
                } else {
                    navController.navigateUp()
                }
            }
        })
    }

    private fun setupProfileButton() {
        btnToProfil.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }
    }
}
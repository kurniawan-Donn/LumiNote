package com.example.LumiNote

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    // ========================================
    // ⏱️ PENGATURAN DURASI ANIMASI SPLASH
    // ========================================
    companion object {
        // === DURASI ANIMASI INDIVIDUAL ===
        private const val GRADIENT_FADE_DURATION = 500L        // Durasi fade in background gradient
        private const val CIRCLE_SCALE_DURATION = 600L         // Durasi scaling circle background
        private const val LOGO_ANIMATION_DURATION = 700L       // Durasi bounce logo
        private const val APP_NAME_ANIMATION_DURATION = 500L   // Durasi slide in app name
        private const val TAGLINE_ANIMATION_DURATION = 500L    // Durasi fade in tagline
        private const val DOTS_FADE_DURATION = 300L            // Durasi fade in loading dots
        private const val VERSION_FADE_DURATION = 300L         // Durasi fade in version

        // === DELAY ANTAR ANIMASI ===
        private const val CIRCLE_START_DELAY = 300L            // Delay sebelum circle muncul
        private const val LOGO_START_DELAY = 600L              // Delay sebelum logo muncul
        private const val APP_NAME_START_DELAY = 1000L         // Delay sebelum app name muncul
        private const val TAGLINE_START_DELAY = 1300L          // Delay sebelum tagline muncul
        private const val DOTS_START_DELAY = 1500L             // Delay sebelum loading dots muncul
        private const val VERSION_START_DELAY = 1700L          // Delay sebelum version muncul

        // === ANIMASI LOADING DOTS ===
        private const val DOT_BOUNCE_DURATION = 400L           // Durasi bouncing per dot
        private const val DOT1_DELAY = 0L                      // Delay dot pertama
        private const val DOT2_DELAY = 150L                    // Delay dot kedua
        private const val DOT3_DELAY = 300L                    // Delay dot ketiga

        // === TIMING NAVIGASI ===
        private const val DELAY_BEFORE_NAVIGATION = 2000L       // Delay setelah animasi selesai
        private const val FADE_OUT_DURATION = 300L             // Durasi fade out sebelum pindah screen

        // === TOTAL WAKTU SPLASH ===
        // Total = Animasi terlama + DELAY_BEFORE_NAVIGATION + FADE_OUT_DURATION
        // Saat ini: ~1700ms (animasi) + 800ms + 300ms = 2800ms (~3 detik)
        // Untuk mengubah total waktu splash, sesuaikan DELAY_BEFORE_NAVIGATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - prevent back
            }
        })

        // Jalankan animasi setelah layout siap
        window.decorView.post {
            startSplashAnimation()
        }
    }

    private fun startSplashAnimation() {
        val gradientBg = findViewById<View>(R.id.gradientBackground)
        val circleBg = findViewById<View>(R.id.circleBackground)
        val logo = findViewById<View>(R.id.ivLogo)
        val appName = findViewById<View>(R.id.tvAppName)
        val tagline = findViewById<View>(R.id.tvTagline)
        val loadingDots = findViewById<View>(R.id.loadingDotsContainer)
        val version = findViewById<View>(R.id.tvVersion)
        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)

        // === Fase 1: Background Gradient Fade In ===
        val gradientFadeIn = ObjectAnimator.ofFloat(gradientBg, "alpha", 0f, 1f).apply {
            duration = GRADIENT_FADE_DURATION
            interpolator = AccelerateDecelerateInterpolator()
        }

        // === Fase 2: Circle Scale & Fade In ===
        val circleScale = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(circleBg, "scaleX", 0.3f, 1f),
                ObjectAnimator.ofFloat(circleBg, "scaleY", 0.3f, 1f),
                ObjectAnimator.ofFloat(circleBg, "alpha", 0f, 1f)
            )
            duration = CIRCLE_SCALE_DURATION
            startDelay = CIRCLE_START_DELAY
            interpolator = OvershootInterpolator(1.2f)
        }

        // === Fase 3: Logo Scale & Fade In dengan Bounce ===
        val logoAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logo, "scaleX", 0.5f, 1.15f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleY", 0.5f, 1.15f, 1f),
                ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
            )
            duration = LOGO_ANIMATION_DURATION
            startDelay = LOGO_START_DELAY
            interpolator = OvershootInterpolator(1.5f)
        }


        // === Fase 4: App Name Slide In dari Bawah ===
        val appNameAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(appName, "translationY", 50f, 0f),
                ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f)
            )
            duration = APP_NAME_ANIMATION_DURATION
            startDelay = APP_NAME_START_DELAY
            interpolator = AccelerateDecelerateInterpolator()
        }

        // === Fase 5: Tagline Fade In ===
        val taglineAnimation = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).apply {
            duration = TAGLINE_ANIMATION_DURATION
            startDelay = TAGLINE_START_DELAY
        }

        // === Fase 6: Loading Dots Animation ===
        val dotsAnimation = ObjectAnimator.ofFloat(loadingDots, "alpha", 0f, 1f).apply {
            duration = DOTS_FADE_DURATION
            startDelay = DOTS_START_DELAY
        }

        dotsAnimation.doOnEnd {
            animateDots(dot1, DOT1_DELAY)
            animateDots(dot2, DOT2_DELAY)
            animateDots(dot3, DOT3_DELAY)
        }


        // === Fase 7: Version Info ===
        val versionAnimation = ObjectAnimator.ofFloat(version, "alpha", 0f, 1f).apply {
            duration = VERSION_FADE_DURATION
            startDelay = VERSION_START_DELAY
        }

        // Jalankan semua animasi
        val masterAnimator = AnimatorSet().apply {
            playTogether(
                gradientFadeIn,
                circleScale,
                logoAnimation,
                appNameAnimation,
                taglineAnimation,
                dotsAnimation,
                versionAnimation
            )
        }

        masterAnimator.doOnEnd {
            // Delay sebelum navigasi
            window.decorView.postDelayed({
                checkSessionAndNavigate()
            }, DELAY_BEFORE_NAVIGATION)
        }

        masterAnimator.start()
    }

    private fun animateDots(dot: View, startDelay: Long) {
        val bounceUp = ObjectAnimator.ofFloat(dot, "translationY", 0f, -20f).apply {
            duration = DOT_BOUNCE_DURATION
            interpolator = AccelerateDecelerateInterpolator()
        }

        val bounceDown = ObjectAnimator.ofFloat(dot, "translationY", -20f, 0f).apply {
            duration = DOT_BOUNCE_DURATION
            interpolator = AccelerateDecelerateInterpolator()
        }

        val bounceAnimator = AnimatorSet().apply {
            playSequentially(bounceUp, bounceDown)
            this.startDelay = startDelay
        }

        bounceAnimator.doOnEnd {
            // Loop animation hanya jika activity masih aktif
            if (!isFinishing && !isDestroyed) {
                animateDots(dot, 0)
            }
        }

        bounceAnimator.start()
    }

    private fun checkSessionAndNavigate() {
        // Fade out animation sebelum pindah
        val rootView = findViewById<View>(android.R.id.content)
        ObjectAnimator.ofFloat(rootView, "alpha", 1f, 0f).apply {
            duration = FADE_OUT_DURATION
            doOnEnd {
                // Cek session seperti kode original Anda
                val isLoggedIn = sessionManager.isLoggedIn()

                val intent = if (isLoggedIn) {
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            start()
        }
    }
}
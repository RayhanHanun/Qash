package com.example.qash_finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate // Import wajib untuk Dark Mode
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.ui.HistoryFragment
import com.example.qash_finalproject.ui.HomeFragment
import com.example.qash_finalproject.ui.InboxFragment
import com.example.qash_finalproject.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabScan: FloatingActionButton
    private lateinit var sessionManager: SessionManager

    // Variabel untuk menahan Splash Screen
    private var isSessionChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // --- 1. LOGIKA PENERAPAN DARK MODE (Dijalankan Paling Awal) ---
        val sessionTemp = SessionManager(this)
        if (sessionTemp.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // -------------------------------------------------------------

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // --- 2. TAHAN SPLASH SCREEN ---
        // Layar loading akan terus tampil sampai isSessionChecked = true
        splashScreen.setKeepOnScreenCondition {
            !isSessionChecked
        }

        // --- 3. JALANKAN PENGECEKAN SESI & DATABASE ---
        checkLoginAndDatabase()

        setContentView(R.layout.activity_main)

        // Init View
        bottomNav = findViewById(R.id.bottom_navigation)
        fabScan = findViewById(R.id.fab_scan)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Disable tombol tengah (placeholder untuk FAB)
        bottomNav.menu.findItem(R.id.nav_scan).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_history -> { loadFragment(HistoryFragment()); true }
                R.id.nav_inbox -> { loadFragment(InboxFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }

        fabScan.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun checkLoginAndDatabase() {
        // A. Cek Session Manager dulu
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // B. Cek Database (Apakah User ID ini benar-benar ada?)
        val userId = sessionManager.getUserId()
        val db = QashDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.qashDao().getUserSync(userId)

            withContext(Dispatchers.Main) {
                if (user == null) {
                    // Jika User hilang (misal database di-reset) -> Logout Paksa
                    sessionManager.logout()
                    goToLogin()
                } else {
                    // User Valid -> Izinkan masuk, hilangkan splash screen
                    isSessionChecked = true
                }
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
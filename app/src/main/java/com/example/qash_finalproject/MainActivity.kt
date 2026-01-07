package com.example.qash_finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.qash_finalproject.ui.HistoryFragment
import com.example.qash_finalproject.ui.HomeFragment
import com.example.qash_finalproject.ui.InboxFragment
import com.example.qash_finalproject.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabScan: FloatingActionButton
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Redirect to Login if not logged in
        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // 1. Inisialisasi View
        bottomNav = findViewById(R.id.bottom_navigation)
        fabScan = findViewById(R.id.fab_scan)

        // 2. Load Fragment Pertama (Home) saat aplikasi dibuka
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 3. Matikan klik pada menu tengah (QRIS Placeholder)
        bottomNav.menu.findItem(R.id.nav_scan).isEnabled = false

        // 4. Listener untuk Menu Bawah
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_inbox -> {
                    loadFragment(InboxFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        fabScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

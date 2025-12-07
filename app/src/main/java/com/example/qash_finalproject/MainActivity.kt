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

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabScan: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi View
        bottomNav = findViewById(R.id.bottom_navigation)
        fabScan = findViewById(R.id.fab_scan)

        // 2. Load Fragment Pertama (Home) saat aplikasi dibuka
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 3. Matikan klik pada menu tengah (QRIS Placeholder)
        // Agar tombol QRIS di Navbar tidak bisa diklik (karena fungsinya digantikan oleh FAB)
        bottomNav.menu.findItem(R.id.nav_scan).isEnabled = false

        // 4. Listener untuk Menu Bawah
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // 1. BERANDA -> HomeFragment
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                // 2. RIWAYAT -> HistoryFragment (Transfer dihapus, ganti Riwayat)
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }

                // 3. QRIS (Tengah) -> Tidak melakukan apa-apa di sini karena ditangani FAB
                // R.id.nav_scan -> false

                // 4. INBOX -> InboxFragment (BARU)
                R.id.nav_inbox -> {
                    loadFragment(InboxFragment())
                    true
                }

                // 5. SAYA -> ProfileFragment
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // 5. Listener untuk Tombol Tengah (FAB QRIS)
        // Saat tombol bulat besar ditekan, buka kamera
        fabScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi helper untuk mengganti Fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
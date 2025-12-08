package com.example.qash_finalproject.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.qash_finalproject.R
import com.example.qash_finalproject.TopUpActivity
import com.example.qash_finalproject.TransferActivity
import com.example.qash_finalproject.WithdrawActivity
import com.example.qash_finalproject.data.QashDatabase
// --- IMPORT ACTIVITY MENU GRID (PENTING) ---
import com.example.qash_finalproject.grid.EmoneyActivity
import com.example.qash_finalproject.grid.ListrikActivity
import com.example.qash_finalproject.grid.PdamActivity
import com.example.qash_finalproject.grid.PulsaActivity
// -------------------------------------------
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var viewModel: QashViewModel
    private lateinit var tvBalance: TextView
    private lateinit var tvGreeting: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi View
        tvBalance = view.findViewById(R.id.tv_balance)
        tvGreeting = view.findViewById(R.id.tv_greeting)

        // 2. Setup ViewModel & Database
        val application = requireNotNull(this.activity).application
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Cek User Default
        viewModel.checkInitialization("Rayhan")

        // Observasi Saldo Real-time
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvBalance.text = formatRupiah.format(user.balance).replace(",00", "")
            }
        }

        // 3. Setup Tombol Aksi Utama (Atas)
        setupMainButtons(view)

        // 4. Setup Grid Menu (Tengah)
        setupGridMenu(view)

        // 5. Setup Carousel Promo (Bawah)
        setupCarousel(view)
    }

    private fun setupMainButtons(view: View) {
        val btnTopUp = view.findViewById<LinearLayout>(R.id.btn_topup)
        val btnTransfer = view.findViewById<LinearLayout>(R.id.btn_transfer)
        val btnScan = view.findViewById<LinearLayout>(R.id.btn_scan)

        btnTopUp?.setOnClickListener {
            startActivity(Intent(activity, TopUpActivity::class.java))
        }

        btnTransfer?.setOnClickListener {
            startActivity(Intent(activity, TransferActivity::class.java))
        }

        btnScan?.setOnClickListener {
            startActivity(Intent(activity, WithdrawActivity::class.java))
        }
    }

    private fun setupGridMenu(view: View) {
        val gridLayout = view.findViewById<GridLayout>(R.id.grid_menu)

        if (gridLayout != null) {
            for (i in 0 until gridLayout.childCount) {
                val itemContainer = gridLayout.getChildAt(i) as? LinearLayout

                itemContainer?.setOnClickListener {
                    // Ambil teks dari TextView ke-2 (index 1) dalam item grid
                    val tvMenu = itemContainer.getChildAt(1) as? TextView
                    val menuNameRaw = tvMenu?.text.toString()
                    val menuName = menuNameRaw.replace("\n", " ").trim()

                    // Logika Navigasi Menu
                    when {
                        // 1. Menu Pulsa & Data
                        menuName.contains("Pulsa & Data", ignoreCase = true) -> {
                            startActivity(Intent(activity, PulsaActivity::class.java))
                        }
                        // 2. Menu Listrik PLN
                        menuName.contains("Listrik", ignoreCase = true) || menuName.contains("PLN", ignoreCase = true) -> {
                            startActivity(Intent(activity, ListrikActivity::class.java))
                        }
                        // 3. Menu Air PDAM
                        menuName.contains("PDAM", ignoreCase = true) -> {
                            startActivity(Intent(activity, PdamActivity::class.java))
                        }
                        // 4. Menu E-Money (BARU)
                        menuName.contains("e-Money", ignoreCase = true) -> {
                            startActivity(Intent(activity, EmoneyActivity::class.java))
                        }
                        // Di HomeFragment.kt, dalam when(menuName)
                        menuName.contains("Internet", ignoreCase = true) || menuName.contains("TV", ignoreCase = true) -> {
                            startActivity(Intent(activity, com.example.qash_finalproject.grid.InternetActivity::class.java))
                        }

                        // Menu Lainnya (Placeholder)
                        menuName.contains("BPJS", ignoreCase = true) -> showToast("Fitur BPJS dalam pengembangan.")
                        menuName.contains("Game", ignoreCase = true) -> showToast("Voucher Game coming soon!")
                        menuName.contains("Lainnya", ignoreCase = true) -> showToast("Lihat semua layanan...")

                        else -> showToast("Menu $menuName belum tersedia")
                    }
                }
            }
        }
    }

    private fun setupCarousel(view: View) {
        val vpPromo = view.findViewById<ViewPager2>(R.id.vp_promo)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_indicator)

        if (vpPromo != null) {
            val promoImages = listOf(
                R.drawable.promo1,
                R.drawable.promo2,
                R.drawable.promo3
            )

            val promoAdapter = PromoAdapter(promoImages)
            vpPromo.adapter = promoAdapter

            // Efek Zoom-Out Keren
            vpPromo.clipToPadding = false
            vpPromo.clipChildren = false
            vpPromo.offscreenPageLimit = 3
            vpPromo.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(30))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
            vpPromo.setPageTransformer(compositePageTransformer)

            // Hubungkan dengan Dot Indicator
            if (tabLayout != null) {
                TabLayoutMediator(tabLayout, vpPromo) { _, _ -> }.attach()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
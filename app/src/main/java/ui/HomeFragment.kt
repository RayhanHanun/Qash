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
// --- IMPORT SEMUA ACTIVITY MENU GRID ---
import com.example.qash_finalproject.grid.EmoneyActivity
import com.example.qash_finalproject.grid.InternetActivity
import com.example.qash_finalproject.grid.ListrikActivity
import com.example.qash_finalproject.grid.PbbActivity
import com.example.qash_finalproject.grid.PdamActivity
import com.example.qash_finalproject.grid.PulsaActivity
// --- IMPORT ACTIVITY PROMO ---
import com.example.qash_finalproject.PromoActivity
// ---------------------------------------
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

        // 1. Inisialisasi View & ViewModel
        initViewModel(view)

        // 2. Setup Header (Tombol Promo ada di sini)
        setupHeaderActions(view)

        // 3. Setup Tombol Aksi Utama (TopUp, Transfer, Scan)
        setupMainButtons(view)

        // 4. Setup Grid Menu (Pulsa, PBB, Internet, dll)
        setupGridMenu(view)

        // 5. Setup Carousel Banner
        setupCarousel(view)

        val btnPoints = view.findViewById<View>(R.id.btn_qash_points) // Pastikan ID ini sudah ditambahkan di XML
        btnPoints?.setOnClickListener {
            startActivity(Intent(activity, com.example.qash_finalproject.PointsActivity::class.java))
        }
    }

    private fun initViewModel(view: View) {
        tvBalance = view.findViewById(R.id.tv_balance)
        tvGreeting = view.findViewById(R.id.tv_greeting)

        val application = requireNotNull(this.activity).application
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Cek User Default & Observasi Saldo
        viewModel.checkInitialization("Rayhan")
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvBalance.text = formatRupiah.format(user.balance).replace(",00", "")
            }
        }
    }

    // --- LOGIKA TOMBOL PROMO (DI HEADER ATAS) ---
    private fun setupHeaderActions(view: View) {
        // ID ini harus sesuai dengan yang ada di fragment_home.xml (btn_top_promo)
        val btnTopPromo = view.findViewById<LinearLayout>(R.id.btn_top_promo)

        btnTopPromo?.setOnClickListener {
            startActivity(Intent(activity, PromoActivity::class.java))
        }
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
                    // Ambil teks menu dari TextView (anak ke-2 di dalam linear layout item)
                    val tvMenu = itemContainer.getChildAt(1) as? TextView
                    val menuNameRaw = tvMenu?.text.toString()
                    val menuName = menuNameRaw.replace("\n", " ").trim()

                    // Navigasi berdasarkan nama menu
                    when {
                        // Pulsa
                        menuName.contains("Pulsa", ignoreCase = true) -> {
                            startActivity(Intent(activity, PulsaActivity::class.java))
                        }
                        // Listrik
                        menuName.contains("Listrik", ignoreCase = true) || menuName.contains("PLN", ignoreCase = true) -> {
                            startActivity(Intent(activity, ListrikActivity::class.java))
                        }
                        // PDAM
                        menuName.contains("Air", ignoreCase = true) || menuName.contains("PDAM", ignoreCase = true) -> {
                            startActivity(Intent(activity, PdamActivity::class.java))
                        }
                        // E-Money
                        menuName.contains("Money", ignoreCase = true) || menuName.contains("Dompet", ignoreCase = true) -> {
                            startActivity(Intent(activity, EmoneyActivity::class.java))
                        }
                        // Internet & TV Kabel
                        menuName.contains("Internet", ignoreCase = true) || menuName.contains("TV", ignoreCase = true) -> {
                            startActivity(Intent(activity, InternetActivity::class.java))
                        }
                        // PBB (Pajak)
                        menuName.contains("PBB", ignoreCase = true) || menuName.contains("Pajak", ignoreCase = true) -> {
                            startActivity(Intent(activity, PbbActivity::class.java))
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

            // Efek Zoom-Out
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

            // Indikator (Titik-titik)
            if (tabLayout != null) {
                TabLayoutMediator(tabLayout, vpPromo) { _, _ -> }.attach()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
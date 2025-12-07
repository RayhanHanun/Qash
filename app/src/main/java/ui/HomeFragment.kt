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
import androidx.viewpager2.widget.ViewPager2
import com.example.qash_finalproject.R
import com.example.qash_finalproject.ScanActivity
import com.example.qash_finalproject.TopUpActivity
import com.example.qash_finalproject.TransferActivity
import com.example.qash_finalproject.WithdrawActivity
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale

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

        // 2. Inisialisasi Tombol Menu Utama
        val btnTopUp = view.findViewById<LinearLayout>(R.id.btn_topup)
        val btnTransfer = view.findViewById<LinearLayout>(R.id.btn_transfer)
        val btnScan = view.findViewById<LinearLayout>(R.id.btn_scan) // Ini untuk Tarik Tunai / Scan

        // 3. Setup ViewModel & Database (Untuk Saldo Real-time)
        val application = requireNotNull(this.activity).application
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Cek User Default (Buat user 'Rayhan' jika belum ada)
        viewModel.checkInitialization("Rayhan")

        // Observasi Data Saldo
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvBalance.text = formatRupiah.format(user.balance)
                // tvGreeting.text = "Qash • ${user.name}" // Opsional: Update nama di header
            }
        }

        // 4. Listener Tombol Utama
        btnTopUp?.setOnClickListener {
            startActivity(Intent(activity, TopUpActivity::class.java))
        }

        btnTransfer?.setOnClickListener {
            startActivity(Intent(activity, TransferActivity::class.java))
        }

        btnScan?.setOnClickListener {
            // Membuka halaman Tarik Tunai (WithdrawActivity)
            startActivity(Intent(activity, WithdrawActivity::class.java))
        }

        // 5. Setup Grid Menu (Pulsa, Listrik, dll)
        // Logika agar setiap ikon di Grid bisa diklik dan membuka PaymentActivity
        val gridLayout = view.findViewById<GridLayout>(R.id.grid_menu)

        if (gridLayout != null) {
            for (i in 0 until gridLayout.childCount) {
                val itemContainer = gridLayout.getChildAt(i) as? LinearLayout

                itemContainer?.setOnClickListener {
                    // Ambil teks dari menu yang diklik (misal: "Listrik PLN")
                    val tvMenu = itemContainer.getChildAt(1) as? TextView
                    val menuNameRaw = tvMenu?.text.toString()
                    val cleanMenuName = menuNameRaw.replace("\n", " ") // Hapus enter jika ada

                }
            }
        }

        // 6. Setup Carousel Promo & Indikator Titik
        setupCarousel(view)
    }

    private fun setupCarousel(view: View) {
        val vpPromo = view.findViewById<ViewPager2>(R.id.vp_promo)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_indicator)

        if (vpPromo != null) {
            // Ganti R.drawable.ic_launcher_background dengan gambar promo asli Anda
            // Contoh: R.drawable.promo1, R.drawable.promo2
            val promoImages = listOf(
                R.drawable.promo1,
                R.drawable.promo2,
                R.drawable.promo3
            )

            val promoAdapter = PromoAdapter(promoImages)
            vpPromo.adapter = promoAdapter

            // Efek Zoom-out yang cantik saat digeser
            vpPromo.setPageTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }

            // Hubungkan ViewPager dengan TabLayout (Titik-titik)
            if (tabLayout != null) {
                TabLayoutMediator(tabLayout, vpPromo) { _, _ ->
                    // Kosongkan karena kita hanya butuh titik, bukan teks judul
                }.attach()
            }
        }
    }
}
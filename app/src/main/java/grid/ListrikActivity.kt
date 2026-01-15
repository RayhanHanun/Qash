package com.example.qash_finalproject.grid

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.ui.SessionManager
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class ListrikActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    // Variabel Logic
    private var selectedAmount: Long = 0
    private var isTagihanTab = false
    private var isBillShown = false // Penanda apakah tagihan sudah dicek/muncul
    private var activeCard: MaterialCardView? = null

    private val tokenNominals = listOf(20000L, 50000L, 100000L, 200000L, 500000L, 1000000L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listrik)

        // 1. Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // 2. Setup ViewModel & Session
        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        // 3. Inisialisasi View
        val etIdPel = findViewById<TextInputEditText>(R.id.et_pln_number)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        // Layout Container
        val gridToken = findViewById<GridLayout>(R.id.grid_token)
        val layoutTagihanContainer = findViewById<View>(R.id.layout_rincian)

        // Kartu Detail Rincian (PASTIKAN ID INI SAMA DENGAN XML)
        // Jika di XML ID-nya 'layout_detail_tagihan', ganti di sini.
        val cardHasilCek = findViewById<View>(R.id.layout_rincian)

        val btnAction = findViewById<Button>(R.id.btn_action)
        val tvTotal = findViewById<TextView>(R.id.tv_total_payment)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount) // Teks harga di dalam kartu rincian

        // --- PENTING: Sembunyikan Rincian Tagihan saat pertama buka ---
        cardHasilCek.visibility = View.GONE

        // 4. Setup Grid Token (Looping Nominal)
        tokenNominals.forEach { amount ->
            val card = createTokenCard(amount)
            card.setOnClickListener {
                handleTokenSelection(card, amount, tvTotal, btnAction)
            }
            gridToken.addView(card)
        }

        // 5. Logic Pindah Tab (Token vs Tagihan)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isTagihanTab = tab?.position == 1

                if (isTagihanTab) {
                    // --- Masuk Tab Tagihan (Pascabayar) ---
                    gridToken.visibility = View.GONE
                    layoutTagihanContainer.visibility = View.VISIBLE

                    // RESET STATUS: Sembunyikan lagi rincian jika user bolak-balik tab
                    isBillShown = false
                    cardHasilCek.visibility = View.GONE

                    // Reset Tombol
                    btnAction.text = "Cek Tagihan"
                    btnAction.isEnabled = true
                    tvTotal.text = "-"

                } else {
                    // --- Masuk Tab Token (Prabayar) ---
                    gridToken.visibility = View.VISIBLE
                    layoutTagihanContainer.visibility = View.GONE

                    btnAction.text = "Beli Token"
                    btnAction.isEnabled = false // Harus pilih nominal dulu
                    resetSelection(btnAction, tvTotal)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 6. Logic Tombol Action (Cek / Bayar)
        btnAction.setOnClickListener {
            val id = etIdPel.text.toString()
            if (id.length < 9) {
                etIdPel.error = "ID Pelanggan tidak valid (Min 9 digit)"
                return@setOnClickListener
            }

            if (isTagihanTab) {
                // --- LOGIC TAGIHAN ---
                if (!isBillShown) {
                    // STEP 1: Jika belum muncul tagihan -> HITUNG DULU

                    // Simulasi hitung tagihan random
                    val randomBill = (150000..850000).random().toLong()
                    selectedAmount = (randomBill / 1000) * 1000 // Bulatkan ke ribuan terdekat

                    val formattedPrice = formatRupiah(selectedAmount)

                    // Update Tampilan
                    tvTotal.text = formattedPrice
                    tvBillAmount?.text = formattedPrice // Isi teks di dalam kartu rincian

                    // MUNCULKAN KARTU RINCIAN SEKARANG
                    cardHasilCek.visibility = View.VISIBLE

                    // Ubah fungsi tombol jadi Bayar
                    btnAction.text = "Bayar Tagihan"
                    isBillShown = true

                } else {
                    // STEP 2: Jika tagihan sudah muncul -> PROSES BAYAR
                    processPayment(selectedAmount, "Tagihan Listrik ($id)")
                }
            } else {
                // --- LOGIC TOKEN ---
                if (selectedAmount > 0) {
                    processPayment(selectedAmount, "Token Listrik ${formatRupiah(selectedAmount)} ($id)")
                }
            }
        }

        // Observer Error Message
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Helper Functions ---

    private fun createTokenCard(amount: Long): MaterialCardView {
        val card = MaterialCardView(this)
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(16, 16, 16, 16)
        }
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 4f
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        val textView = TextView(this)
        textView.text = formatRupiah(amount)
        textView.textSize = 16f
        textView.gravity = android.view.Gravity.CENTER
        textView.setPadding(16, 32, 16, 32)
        textView.setTextColor(ContextCompat.getColor(this, R.color.black))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

        card.addView(textView)
        return card
    }

    private fun handleTokenSelection(card: MaterialCardView, amount: Long, tvTotal: TextView, btnAction: Button) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)

        // Reset kartu sebelumnya
        activeCard?.strokeWidth = 0

        if (activeCard == card) {
            // Deselect (klik ulang kartu yg sama)
            activeCard = null
            selectedAmount = 0
            btnAction.isEnabled = false
            tvTotal.text = "-"
        } else {
            // Select baru
            card.strokeWidth = 6
            card.strokeColor = blueColor
            activeCard = card
            selectedAmount = amount
            btnAction.isEnabled = true
            tvTotal.text = formatRupiah(amount)
        }
    }

    private fun resetSelection(btnAction: Button, tvTotal: TextView) {
        activeCard?.strokeWidth = 0
        activeCard = null
        selectedAmount = 0
        btnAction.isEnabled = false
        tvTotal.text = "-"
    }

    private fun processPayment(amount: Long, desc: String) {
        viewModel.addTransaction(
            type = "KELUAR",
            amount = amount,
            description = desc,
            category = "Listrik"
        ) {
            runOnUiThread {
                Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_LONG).show()
                finish() // Tutup activity dan kembali ke Home
            }
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "").replace("Rp", "Rp ")
    }
}
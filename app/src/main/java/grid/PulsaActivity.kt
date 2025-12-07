package com.example.qash_finalproject.grid

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

// Model data sederhana untuk Paket Data
data class DataPackage(val name: String, val desc: String, val price: Long)

class PulsaActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    // State Variables
    private var selectedAmount: Long = 0
    private var selectedNote: String = "" // Untuk membedakan di history (Pulsa/Data)
    private var activeCard: MaterialCardView? = null

    // Data Pulsa
    private val nominals = listOf(5000L, 10000L, 25000L, 50000L, 100000L, 200000L)

    // Data Paket Data (Dummy)
    private val dataPackages = listOf(
        DataPackage("Internet 2.5 GB", "Masa aktif 3 Hari", 15000),
        DataPackage("Internet 7 GB", "Masa aktif 7 Hari", 35000),
        DataPackage("Freedom 14 GB", "Masa aktif 30 Hari", 65000),
        DataPackage("Unlimited YouTube", "FUP 10GB / 30 Hari", 80000),
        DataPackage("Combo Sakti 25 GB", "Internet + Telpon 30 Hari", 105000)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pulsa)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Init Views
        val etPhone = findViewById<TextInputEditText>(R.id.et_phone_number)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val gridPulsa = findViewById<GridLayout>(R.id.grid_pulsa)
        val gridData = findViewById<GridLayout>(R.id.grid_data)
        val tvLabel = findViewById<TextView>(R.id.tv_label_pilihan)

        // Bottom Bar
        val tvSelectedPrice = findViewById<TextView>(R.id.tv_selected_price)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_pulsa)

        // 1. GENERATE GRID PULSA
        nominals.forEach { amount ->
            val cardView = createPulsaCard(amount)
            cardView.setOnClickListener {
                handleCardSelection(cardView, amount, "Pulsa", btnConfirm, tvSelectedPrice)
            }
            gridPulsa.addView(cardView)
        }

        // 2. GENERATE GRID DATA
        dataPackages.forEach { pkg ->
            val cardView = createDataCard(pkg)
            cardView.setOnClickListener {
                handleCardSelection(cardView, pkg.price, pkg.name, btnConfirm, tvSelectedPrice)
            }
            gridData.addView(cardView)
        }

        // 3. LOGIKA TAB SWITCH
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Reset Pilihan saat ganti tab
                resetSelection(btnConfirm, tvSelectedPrice)

                if (tab?.position == 0) {
                    // TAB PULSA
                    gridPulsa.visibility = View.VISIBLE
                    gridData.visibility = View.GONE
                    tvLabel.text = "Pilih Nominal"
                } else {
                    // TAB DATA
                    gridPulsa.visibility = View.GONE
                    gridData.visibility = View.VISIBLE
                    tvLabel.text = "Pilih Paket Data"
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 4. LOGIKA TOMBOL BELI
        btnConfirm.setOnClickListener {
            val phone = etPhone.text.toString()

            if (phone.length < 10) {
                etPhone.error = "Nomor tidak valid"
                return@setOnClickListener
            }
            // Kirim note (Pulsa/Nama Paket) ke fungsi transaksi
            processTransaction(selectedAmount, phone, selectedNote)
        }
    }

    // Fungsi Reset Pilihan (Dipanggil saat ganti tab)
    private fun resetSelection(btnConfirm: Button, tvPrice: TextView) {
        activeCard?.strokeWidth = 0 // Hilangkan outline kartu sebelumnya
        activeCard = null
        selectedAmount = 0
        btnConfirm.isEnabled = false
        tvPrice.text = "-"
    }

    private fun handleCardSelection(
        clickedCard: MaterialCardView,
        amount: Long,
        note: String,
        btnConfirm: Button,
        tvPrice: TextView
    ) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        if (activeCard == clickedCard) {
            // BATAL PILIH
            clickedCard.strokeWidth = 0
            activeCard = null
            selectedAmount = 0
            btnConfirm.isEnabled = false
            tvPrice.text = "-"
        } else {
            // PILIH BARU
            activeCard?.strokeWidth = 0 // Matikan yang lama
            clickedCard.strokeWidth = 6 // Nyalakan yang baru
            clickedCard.strokeColor = blueColor

            activeCard = clickedCard
            selectedAmount = amount
            selectedNote = note // Simpan jenis pembelian

            btnConfirm.isEnabled = true
            tvPrice.text = formatRp.format(amount).replace(",00", "")
        }
    }

    // -- CARD DESIGN UNTUK PULSA (Kecil, Grid 2 Kolom) --
    private fun createPulsaCard(amount: Long): MaterialCardView {
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

        // Ripple
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        card.foreground = ContextCompat.getDrawable(this, outValue.resourceId)

        val textView = TextView(this)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        textView.text = formatRp.format(amount).replace(",00", "")
        textView.textSize = 18f
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.setPadding(32, 48, 32, 48)
        textView.setTextColor(ContextCompat.getColor(this, R.color.qash_primary))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

        card.addView(textView)
        return card
    }

    // -- CARD DESIGN UNTUK DATA (Lebar, Ada Deskripsi) --
    private fun createDataCard(pkg: DataPackage): MaterialCardView {
        val card = MaterialCardView(this)
        val params = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.MATCH_PARENT // Full width 1 kolom
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(16, 16, 16, 16)
        }
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 4f
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        // Ripple
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        card.foreground = ContextCompat.getDrawable(this, outValue.resourceId)

        // Layout Vertical di dalam Card (Judul di atas, Harga di bawah)
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val tvName = TextView(this).apply {
            text = pkg.name
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        val tvDesc = TextView(this).apply {
            text = pkg.desc
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            setPadding(0, 8, 0, 0)
        }

        val tvPrice = TextView(this).apply {
            val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            text = formatRp.format(pkg.price).replace(",00", "")
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.qash_primary))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.END // Harga di kanan bawah container
            setPadding(0, 20, 0, 0)
        }

        container.addView(tvName)
        container.addView(tvDesc)
        container.addView(tvPrice)
        card.addView(container)

        return card
    }

    private fun processTransaction(amount: Long, phone: String, noteType: String) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= amount) {
                    // Note akan jadi "Beli Pulsa 0812.." atau "Beli Internet 2.5GB 0812.."
                    val finalNote = if (noteType == "Pulsa") "Beli Pulsa $phone" else "Beli $noteType ($phone)"

                    viewModel.addTransaction("KELUAR", amount, finalNote)
                    Toast.makeText(this, "Pembelian Berhasil!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                }
                viewModel.user.removeObservers(this)
            }
        }
    }
}
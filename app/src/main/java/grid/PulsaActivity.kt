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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R // <--- PENTING: Agar R.id tidak merah
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.ui.SessionManager
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

// Data Class sederhana untuk paket data
data class DataPackage(val name: String, val desc: String, val price: Long)

class PulsaActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var selectedAmount: Long = 0
    private var selectedNote: String = ""
    private var activeCard: MaterialCardView? = null

    private val nominals = listOf(5000L, 10000L, 25000L, 50000L, 100000L, 200000L)
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

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]

        // Set User ID yang sedang login
        viewModel.setUserId(sessionManager.getUserId())

        val etPhone = findViewById<TextInputEditText>(R.id.et_phone_number)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val gridPulsa = findViewById<GridLayout>(R.id.grid_pulsa)
        val gridData = findViewById<GridLayout>(R.id.grid_data) // Pastikan ID ini ada di XML
        val tvLabel = findViewById<TextView>(R.id.tv_label_pilihan)
        val tvSelectedPrice = findViewById<TextView>(R.id.tv_selected_price)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_pulsa)

        // Generate Menu Pulsa
        nominals.forEach { amount ->
            val cardView = createPulsaCard(amount)
            cardView.setOnClickListener { handleCardSelection(cardView, amount, "Pulsa", btnConfirm, tvSelectedPrice) }
            gridPulsa.addView(cardView)
        }

        // Generate Menu Paket Data
        dataPackages.forEach { pkg ->
            val cardView = createDataCard(pkg)
            cardView.setOnClickListener { handleCardSelection(cardView, pkg.price, pkg.name, btnConfirm, tvSelectedPrice) }
            gridData.addView(cardView)
        }

        // Logic Tab (Pulsa vs Data)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                resetSelection(btnConfirm, tvSelectedPrice)
                if (tab?.position == 0) {
                    gridPulsa.visibility = View.VISIBLE
                    gridData.visibility = View.GONE
                    tvLabel.text = "Pilih Nominal"
                } else {
                    gridPulsa.visibility = View.GONE
                    gridData.visibility = View.VISIBLE
                    tvLabel.text = "Pilih Paket Data"
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnConfirm.setOnClickListener {
            val phone = etPhone.text.toString()
            if (phone.length < 10) {
                etPhone.error = "Nomor tidak valid"
                return@setOnClickListener
            }
            // Panggil fungsi transaksi
            processTransaction(selectedAmount, phone, selectedNote)
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetSelection(btnConfirm: Button, tvPrice: TextView) {
        activeCard?.strokeWidth = 0
        activeCard = null
        selectedAmount = 0
        btnConfirm.isEnabled = false
        tvPrice.text = "-"
    }

    private fun handleCardSelection(clickedCard: MaterialCardView, amount: Long, note: String, btnConfirm: Button, tvPrice: TextView) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        if (activeCard == clickedCard) {
            clickedCard.strokeWidth = 0
            activeCard = null
            selectedAmount = 0
            btnConfirm.isEnabled = false
            tvPrice.text = "-"
        } else {
            activeCard?.strokeWidth = 0
            clickedCard.strokeWidth = 6
            clickedCard.strokeColor = blueColor
            activeCard = clickedCard
            selectedAmount = amount
            selectedNote = note
            btnConfirm.isEnabled = true
            tvPrice.text = formatRp.format(amount).replace(",00", "")
        }
    }

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

    private fun createDataCard(pkg: DataPackage): MaterialCardView {
        val card = MaterialCardView(this)
        val params = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.MATCH_PARENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(16, 16, 16, 16)
        }
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 4f
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        card.foreground = ContextCompat.getDrawable(this, outValue.resourceId)

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
            gravity = Gravity.END
            setPadding(0, 20, 0, 0)
        }

        container.addView(tvName)
        container.addView(tvDesc)
        container.addView(tvPrice)
        card.addView(container)
        return card
    }

    private fun processTransaction(amount: Long, phone: String, noteType: String) {
        val finalNote = if (noteType == "Pulsa") "Beli Pulsa $phone" else "Beli $noteType ($phone)"

        // --- PEMANGGILAN YANG SUDAH SESUAI DENGAN VIEWMODEL ---
        viewModel.addTransaction(
            type = "KELUAR",
            amount = amount,
            description = finalNote,
            category = "Pulsa" // <--- Ini wajib ada agar cocok dengan QashViewModel
        ) {
            runOnUiThread {
                Toast.makeText(this, "Pembelian Berhasil!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
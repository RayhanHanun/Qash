package com.example.qash_finalproject.grid

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class EmoneyActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    // State
    private var selectedProvider: String = ""
    private var activeProviderCard: MaterialCardView? = null

    private var selectedAmount: Long = 0
    private var activeNominalCard: MaterialCardView? = null

    // Data Provider
    private val providers = listOf("GoPay", "OVO", "Dana", "ShopeePay", "LinkAja", "e-Toll")
    // Data Nominal
    private val nominals = listOf(10000L, 20000L, 25000L, 50000L, 100000L, 200000L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emoney)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Init Views
        val layoutProvider = findViewById<LinearLayout>(R.id.layout_provider_container)
        val gridNominal = findViewById<GridLayout>(R.id.grid_nominal)
        val etNumber = findViewById<TextInputEditText>(R.id.et_customer_number)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_topup)
        val tvPrice = findViewById<TextView>(R.id.tv_selected_price)

        // 1. Generate Provider Cards (Horizontal)
        providers.forEach { name ->
            val card = createProviderCard(name)
            card.setOnClickListener {
                handleProviderSelection(card, name)
                updateButtonState(btnConfirm, etNumber)
            }
            layoutProvider.addView(card)
        }

        // 2. Generate Nominal Cards (Grid)
        nominals.forEach { amount ->
            val card = createNominalCard(amount)
            card.setOnClickListener {
                handleNominalSelection(card, amount, tvPrice)
                updateButtonState(btnConfirm, etNumber)
            }
            gridNominal.addView(card)
        }

        // 3. Tombol Confirm
        btnConfirm.setOnClickListener {
            val number = etNumber.text.toString()
            if (number.length < 8) {
                etNumber.error = "Nomor tidak valid"
                return@setOnClickListener
            }
            processTransaction(selectedAmount, number, selectedProvider)
        }
    }

    // --- LOGIKA SELEKSI PROVIDER ---
    private fun handleProviderSelection(card: MaterialCardView, name: String) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)

        // Reset card lama
        activeProviderCard?.strokeWidth = 0
        activeProviderCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        // Set card baru
        activeProviderCard = card
        selectedProvider = name

        card.strokeWidth = 6
        card.strokeColor = blueColor
        // Opsional: ganti warna background biar makin keliatan aktif
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_app))
    }

    // --- LOGIKA SELEKSI NOMINAL ---
    private fun handleNominalSelection(card: MaterialCardView, amount: Long, tvPrice: TextView) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // Toggle Logic (Bisa batal pilih)
        if (activeNominalCard == card) {
            // Deselect
            card.strokeWidth = 0
            activeNominalCard = null
            selectedAmount = 0
            tvPrice.text = "-"
        } else {
            // Select New
            activeNominalCard?.strokeWidth = 0
            activeNominalCard = card
            selectedAmount = amount

            card.strokeWidth = 6
            card.strokeColor = blueColor
            tvPrice.text = formatRp.format(amount).replace(",00", "")
        }
    }

    // Cek apakah semua syarat terpenuhi (Provider dipilih + Nominal dipilih)
    private fun updateButtonState(btn: Button, etNumber: TextInputEditText) {
        btn.isEnabled = (selectedProvider.isNotEmpty() && selectedAmount > 0)
    }

    // --- UI HELPER: CREATE PROVIDER CARD ---
    private fun createProviderCard(name: String): MaterialCardView {
        val card = MaterialCardView(this)
        val params = LinearLayout.LayoutParams(
            300, // Lebar fixed agar seragam
            150  // Tinggi fixed
        ).apply {
            setMargins(8, 8, 8, 8)
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
        textView.text = name
        textView.textSize = 16f
        textView.gravity = Gravity.CENTER
        textView.setTextColor(ContextCompat.getColor(this, R.color.qash_primary))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

        card.addView(textView)
        return card
    }

    // --- UI HELPER: CREATE NOMINAL CARD (Sama kayak Pulsa) ---
    private fun createNominalCard(amount: Long): MaterialCardView {
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

    private fun processTransaction(amount: Long, number: String, provider: String) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= amount) {
                    viewModel.addTransaction("KELUAR", amount, "Top Up $provider $number")
                    Toast.makeText(this, "Top Up Berhasil!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                }
                viewModel.user.removeObservers(this)
            }
        }
    }
}
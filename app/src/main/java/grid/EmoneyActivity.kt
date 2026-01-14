package com.example.qash_finalproject.grid

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class EmoneyActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    private var selectedProvider: String = ""
    private var selectedAmount: Long = 0
    private var activeNominalCard: MaterialCardView? = null

    private val providers = listOf("GoPay", "OVO", "Dana", "ShopeePay", "LinkAja")
    private val nominals = listOf(20000L, 50000L, 100000L, 200000L, 500000L, 1000000L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emoney)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        val spinner = findViewById<Spinner>(R.id.spinner_provider)
        val etNumber = findViewById<TextInputEditText>(R.id.et_customer_number)
        val gridNominal = findViewById<GridLayout>(R.id.grid_nominal)
        val btnTopUp = findViewById<Button>(R.id.btn_confirm_topup)
        val tvTotal = findViewById<TextView>(R.id.tv_selected_price)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProvider = providers[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        nominals.forEach { amount ->
            val card = createNominalCard(amount)
            card.setOnClickListener {
                selectNominal(card, amount, btnTopUp, tvTotal)
            }
            gridNominal.addView(card)
        }

        btnTopUp.setOnClickListener {
            val number = etNumber.text.toString()
            if (number.isEmpty()) {
                etNumber.error = "Nomor tidak boleh kosong"
                return@setOnClickListener
            }
            if (selectedAmount == 0L) {
                Toast.makeText(this, "Pilih nominal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            processTransaction(selectedAmount, number, selectedProvider)
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectNominal(card: MaterialCardView, amount: Long, btn: Button, tvTotal: TextView) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)

        activeNominalCard?.strokeWidth = 0
        activeNominalCard = card
        activeNominalCard?.strokeColor = blueColor
        activeNominalCard?.strokeWidth = 6

        selectedAmount = amount
        btn.isEnabled = true

        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvTotal.text = formatRp.format(amount).replace(",00", "")
    }

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
        viewModel.addTransaction(
            type = "KELUAR",
            amount = amount,
            description = "Top Up $provider - $number",
            category = "E-Money"
        ) {
            runOnUiThread {
                Toast.makeText(this, "Top Up Berhasil!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
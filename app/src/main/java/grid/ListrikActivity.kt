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
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.data.QashDatabase
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
    private var selectedAmount: Long = 0
    private var isTagihanTab = false
    private var activeCard: MaterialCardView? = null

    private val tokenNominals = listOf(20000L, 50000L, 100000L, 200000L, 500000L, 1000000L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listrik)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        // PERBAIKAN ID: et_pln_number (bukan et_id_pelanggan)
        val etIdPel = findViewById<TextInputEditText>(R.id.et_pln_number)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val gridToken = findViewById<GridLayout>(R.id.grid_token)
        val layoutTagihan = findViewById<View>(R.id.layout_rincian)
        val layoutToken = findViewById<View>(R.id.layout_token_container)
        val btnAction = findViewById<Button>(R.id.btn_action)
        val tvTotal = findViewById<TextView>(R.id.tv_total_payment)

        // Setup Grid
        tokenNominals.forEach { amount ->
            val card = createTokenCard(amount)
            card.setOnClickListener {
                handleTokenSelection(card, amount, tvTotal, btnAction)
            }
            gridToken.addView(card)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isTagihanTab = tab?.position == 1
                if (isTagihanTab) {
                    layoutToken.visibility = View.GONE
                    layoutTagihan.visibility = View.VISIBLE
                    btnAction.text = "Cek Tagihan"
                    btnAction.isEnabled = true
                    resetSelection(btnAction, tvTotal)
                } else {
                    layoutToken.visibility = View.VISIBLE
                    layoutTagihan.visibility = View.GONE
                    btnAction.text = "Beli Token"
                    btnAction.isEnabled = false
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnAction.setOnClickListener {
            val id = etIdPel.text.toString()
            if (id.length < 9) {
                etIdPel.error = "ID Pelanggan tidak valid"
                return@setOnClickListener
            }

            if (isTagihanTab) {
                if (btnAction.text == "Cek Tagihan") {
                    selectedAmount = (150000..500000).random().toLong()
                    tvTotal.text = formatRupiah(selectedAmount)
                    btnAction.text = "Bayar Tagihan"
                } else {
                    processPayment(selectedAmount, "Bayar Listrik ($id)")
                }
            } else {
                processPayment(selectedAmount, "Token Listrik $selectedAmount ($id)")
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

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
        if (activeCard == card) {
            card.strokeWidth = 0
            activeCard = null
            selectedAmount = 0
            btnAction.isEnabled = false
            tvTotal.text = "-"
        } else {
            activeCard?.strokeWidth = 0
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
        btnAction.isEnabled = true
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
                finish()
            }
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "").replace("Rp", "Rp")
    }
}
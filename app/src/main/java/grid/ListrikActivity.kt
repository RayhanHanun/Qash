package com.example.qash_finalproject.grid

import android.os.Bundle
import android.util.TypedValue
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
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class ListrikActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private var selectedAmount: Long = 0
    private var isTagihanTab = false
    private var activeCard: MaterialCardView? = null

    private val tokenNominals = listOf(20000L, 50000L, 100000L, 200000L, 500000L, 1000000L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listrik)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val layoutToken = findViewById<View>(R.id.layout_token_container)
        val layoutRincian = findViewById<View>(R.id.layout_rincian)
        val gridToken = findViewById<GridLayout>(R.id.grid_token)
        val etPln = findViewById<TextInputEditText>(R.id.et_pln_number)
        val tvTotalPayment = findViewById<TextView>(R.id.tv_total_payment)
        val btnAction = findViewById<Button>(R.id.btn_action)
        val tvLayanan = findViewById<TextView>(R.id.tv_layanan)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)

        // Generate Token Cards (Like Pulsa)
        tokenNominals.forEach { amount ->
            val cardView = createTokenCard(amount)
            cardView.setOnClickListener {
                handleCardSelection(cardView, amount, tvLayanan, tvTotalPayment, btnAction)
            }
            gridToken.addView(cardView)
        }

        // Logic Tab Switch
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                resetSelection(btnAction, tvTotalPayment)
                if (tab?.position == 0) {
                    isTagihanTab = false
                    layoutToken.visibility = View.VISIBLE
                    layoutRincian.visibility = View.GONE
                    btnAction.text = "Beli"
                } else {
                    isTagihanTab = true
                    layoutToken.visibility = View.GONE
                    layoutRincian.visibility = View.GONE
                    btnAction.text = "Cek Tagihan"
                    btnAction.isEnabled = true
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnAction.setOnClickListener {
            val idPln = etPln.text.toString()
            if (idPln.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isTagihanTab) {
                if (btnAction.text == "Cek Tagihan") {
                    layoutRincian.visibility = View.VISIBLE
                    tvLayanan.text = "Listrik Pasca Bayar"
                    selectedAmount = 345000L
                    tvBillAmount.text = formatRupiah(selectedAmount)
                    tvTotalPayment.text = formatRupiah(selectedAmount)
                    btnAction.text = "Bayar Sekarang"
                } else {
                    processPayment(selectedAmount, "Tagihan Listrik $idPln")
                }
            } else {
                processPayment(selectedAmount, "Token PLN $idPln")
            }
        }
    }

    private fun createTokenCard(amount: Long): MaterialCardView {
        val card = MaterialCardView(this)
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(12, 12, 12, 12)
        }
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 2f
        card.strokeWidth = 0
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        card.foreground = ContextCompat.getDrawable(this, outValue.resourceId)

        val textView = TextView(this)
        textView.text = formatRupiah(amount)
        textView.textSize = 16f
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.setPadding(24, 40, 24, 40)
        textView.setTextColor(ContextCompat.getColor(this, R.color.qash_primary))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

        card.addView(textView)
        return card
    }

    private fun handleCardSelection(
        clickedCard: MaterialCardView,
        amount: Long,
        tvLayanan: TextView,
        tvTotal: TextView,
        btnAction: Button
    ) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)
        if (activeCard == clickedCard) {
            clickedCard.strokeWidth = 0
            activeCard = null
            selectedAmount = 0
            btnAction.isEnabled = false
            tvTotal.text = "-"
        } else {
            activeCard?.strokeWidth = 0
            clickedCard.strokeWidth = 6
            clickedCard.strokeColor = blueColor
            activeCard = clickedCard
            selectedAmount = amount
            btnAction.isEnabled = true
            tvTotal.text = formatRupiah(amount)
            tvLayanan.text = "Token Listrik " + formatRupiah(amount)
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
        viewModel.user.observe(this) { user ->
            if (user != null && user.balance >= amount) {
                viewModel.addTransaction("KELUAR", amount, desc)
                Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Saldo kurang!", Toast.LENGTH_SHORT).show()
            }
            viewModel.user.removeObservers(this)
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "").replace("Rp", "Rp")
    }
}
package com.example.qash_finalproject.grid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.MainActivity
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.ui.SessionManager
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class PbbActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    private var isBillShown = false
    private var billAmount: Long = 0
    private var selectedRegion: String = ""
    private var selectedYear: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pbb)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        val etNop = findViewById<TextInputEditText>(R.id.et_nop)
        val autoCompleteRegion = findViewById<AutoCompleteTextView>(R.id.act_region) // ID: act_region
        val autoCompleteYear = findViewById<AutoCompleteTextView>(R.id.act_year)     // ID: act_year
        // PERBAIKAN ID: btn_action_pbb
        val btnAction = findViewById<Button>(R.id.btn_action_pbb)

        val cardDetail = findViewById<View>(R.id.card_bill_detail)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)

        val tvDetailRegion = findViewById<TextView>(R.id.tv_detail_region)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        // PERBAIKAN ID: tv_total_bottom
        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)

        val regions = listOf("DKI Jakarta", "Kota Bandung", "Kab. Bogor", "Kota Surabaya", "Kota Medan")
        val years = listOf("2024", "2023", "2022")

        autoCompleteRegion.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regions))
        autoCompleteYear.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, years))

        btnAction.setOnClickListener {
            val nop = etNop.text.toString()
            selectedRegion = autoCompleteRegion.text.toString()
            selectedYear = autoCompleteYear.text.toString()

            if (nop.length < 10) {
                etNop.error = "NOP harus valid"
                return@setOnClickListener
            }
            if (selectedRegion.isEmpty()) {
                autoCompleteRegion.error = "Pilih Wilayah"
                return@setOnClickListener
            }

            if (!isBillShown) {
                billAmount = (150000..900000).random().toLong()
                billAmount = (billAmount / 1000) * 1000

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val textHarga = formatRp.format(billAmount).replace(",00", "")

                tvDetailRegion.text = selectedRegion
                tvBillAmount.text = textHarga
                tvTotalBottom.text = textHarga
                cardDetail.visibility = View.VISIBLE

                btnAction.text = "Bayar Sekarang"
                isBillShown = true
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            } else {
                processPayment(nop)
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPayment(nop: String) {
        viewModel.addTransaction(
            type = "KELUAR",
            amount = billAmount,
            description = "Bayar PBB $selectedYear ($nop)",
            category = "PBB"
        ) {
            runOnUiThread {
                Toast.makeText(this, "Pembayaran PBB Berhasil!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
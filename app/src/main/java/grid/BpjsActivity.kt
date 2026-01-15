package com.example.qash_finalproject.grid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
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

class BpjsActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var billAmount: Long = 0
    private var isBillShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bpjs)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        val etNumber = findViewById<TextInputEditText>(R.id.et_bpjs_number)
        val spinner = findViewById<Spinner>(R.id.spinner_months) // ID: spinner_months
        // PERBAIKAN ID: btn_action
        val btnAction = findViewById<Button>(R.id.btn_action)

        val cardDetail = findViewById<View>(R.id.card_bill_detail)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)

        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        // PERBAIKAN ID: tv_total_bottom
        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)
        val tvPeriode = findViewById<TextView>(R.id.tv_periode)

        val months = listOf("1 Bulan", "3 Bulan", "6 Bulan", "1 Tahun")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        spinner.adapter = adapter

        btnAction.setOnClickListener {
            val number = etNumber.text.toString()
            if (number.length < 10) {
                etNumber.error = "Nomor BPJS Salah"
                return@setOnClickListener
            }

            if (!isBillShown) {
                val selectedMonthPos = spinner.selectedItemPosition
                val multiplier = when (selectedMonthPos) {
                    0 -> 1
                    1 -> 3
                    2 -> 6
                    3 -> 12
                    else -> 1
                }

                billAmount = 150000L * multiplier

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val textHarga = formatRp.format(billAmount).replace(",00", "")

                tvBillAmount.text = textHarga
                tvTotalBottom.text = textHarga
                tvPeriode.text = spinner.selectedItem.toString()

                cardDetail.visibility = View.VISIBLE
                btnAction.text = "Bayar Sekarang"
                isBillShown = true
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            } else {
                viewModel.addTransaction(
                    type = "KELUAR",
                    amount = billAmount,
                    description = "Bayar BPJS - $number",
                    category = "BPJS"
                ) {
                    runOnUiThread {
                        Toast.makeText(this, "Pembayaran BPJS Berhasil!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
package com.example.qash_finalproject.grid

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class PdamActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var selectedAmount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdam)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        val spinner = findViewById<Spinner>(R.id.spinner_wilayah)
        // PERBAIKAN ID: et_pdam_id
        val etIdPel = findViewById<TextInputEditText>(R.id.et_pdam_id)
        // PERBAIKAN ID: btn_action
        val btnAction = findViewById<Button>(R.id.btn_action)
        val layoutRincian = findViewById<View>(R.id.layout_rincian)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        val tvTotalPayment = findViewById<TextView>(R.id.tv_total_payment)
        val tvAreaDetail = findViewById<TextView>(R.id.tv_area_detail)

        val areas = listOf("PDAM Tirtawening (Bandung)", "PAM Jaya (Jakarta)", "PDAM Surya Sembada (Surabaya)", "PDAM Tirtanadi (Medan)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, areas)
        spinner.adapter = adapter

        btnAction.setOnClickListener {
            val id = etIdPel.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (btnAction.text == "Cek Tagihan") {
                selectedAmount = (50000..150000).random().toLong()
                tvBillAmount.text = formatRupiah(selectedAmount)
                tvTotalPayment.text = formatRupiah(selectedAmount)
                tvAreaDetail.text = spinner.selectedItem.toString()

                layoutRincian.visibility = View.VISIBLE
                btnAction.text = "Bayar Sekarang"
            } else {
                val area = spinner.selectedItem.toString()
                viewModel.addTransaction(
                    type = "KELUAR",
                    amount = selectedAmount,
                    description = "Bayar Air $area ($id)",
                    category = "PDAM"
                ) {
                    runOnUiThread {
                        Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "")
    }
}
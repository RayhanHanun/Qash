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
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class PdamActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private var selectedAmount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdam)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        val spinner = findViewById<Spinner>(R.id.spinner_wilayah)
        val areas = resources.getStringArray(R.array.pdam_regions)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, areas)
        spinner.adapter = adapter

        val etId = findViewById<TextInputEditText>(R.id.et_pdam_id)
        val btnAction = findViewById<Button>(R.id.btn_action)
        val layoutRincian = findViewById<View>(R.id.layout_rincian)
        val tvTotalPayment = findViewById<TextView>(R.id.tv_total_payment)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        val tvAreaDetail = findViewById<TextView>(R.id.tv_area_detail)

        btnAction.setOnClickListener {
            val id = etId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (btnAction.text == "Cek Tagihan") {
                // Simulasi Cek Tagihan
                selectedAmount = (50000..150000).random().toLong()
                tvBillAmount.text = formatRupiah(selectedAmount)
                tvTotalPayment.text = formatRupiah(selectedAmount)
                tvAreaDetail.text = spinner.selectedItem.toString()
                
                layoutRincian.visibility = View.VISIBLE
                btnAction.text = "Bayar Sekarang"
            } else {
                // Proses Pembayaran
                viewModel.user.observe(this) { user ->
                    if (user != null) {
                        if (user.balance >= selectedAmount) {
                            val area = spinner.selectedItem.toString()
                            viewModel.addTransaction("KELUAR", selectedAmount, "Bayar Air $area ($id)")
                            Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.user.removeObservers(this)
                    }
                }
            }
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "").replace("Rp", "Rp ")
    }
}
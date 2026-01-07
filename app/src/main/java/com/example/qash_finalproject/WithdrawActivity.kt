package com.example.qash_finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class WithdrawActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var selectedAmount: Long = 0
    private var currentBalance: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        // 1. Setup ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        // 2. Ambil Saldo Terkini (untuk validasi)
        viewModel.user.observe(this) { user ->
            if (user != null) currentBalance = user.balance
        }

        val tvSelectedAmount: TextView = findViewById(R.id.tv_selected_amount)
        val spinnerMerchant: Spinner = findViewById(R.id.spinner_merchant)
        val btnConfirm: Button = findViewById(R.id.btn_confirm)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        // 3. Logic Tombol Nominal
        val btn50k: Button = findViewById(R.id.btn_50k)
        val btn100k: Button = findViewById(R.id.btn_100k)
        val btn250k: Button = findViewById(R.id.btn_250k)
        val btn500k: Button = findViewById(R.id.btn_500k)

        fun setAmount(amount: Long) {
            selectedAmount = amount
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvSelectedAmount.text = formatRupiah.format(amount).replace(",00", "")
        }

        btn50k.setOnClickListener { setAmount(50000) }
        btn100k.setOnClickListener { setAmount(100000) }
        btn250k.setOnClickListener { setAmount(250000) }
        btn500k.setOnClickListener { setAmount(500000) }

        // 4. Aksi Konfirmasi
        btnConfirm.setOnClickListener {
            if (selectedAmount == 0L) {
                Toast.makeText(this, "Pilih nominal dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAmount > currentBalance) {
                Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val merchant = spinnerMerchant.selectedItem.toString()

            // Simpan Transaksi (KELUAR)
            viewModel.addTransaction("KELUAR", selectedAmount, "Tarik Tunai di $merchant") {
                runOnUiThread {
                    Toast.makeText(this, "Kode Penarikan Berhasil Dibuat!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        btnBack.setOnClickListener {
            finish()
        }
    }
}

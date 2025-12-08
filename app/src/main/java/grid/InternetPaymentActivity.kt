package com.example.qash_finalproject.grid

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class InternetPaymentActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private var billAmount: Long = 0
    private var providerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_payment)

        // 1. Ambil Data dari Intent (Dikirim dari InternetActivity)
        providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Internet"
        val providerLogo = intent.getIntExtra("PROVIDER_LOGO", R.drawable.ic_internet)

        // 2. Setup Toolbar & Header
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val tvProvider = findViewById<TextView>(R.id.tv_provider_name)
        val imgProvider = findViewById<ImageView>(R.id.img_provider_logo)
        tvProvider.text = providerName
        imgProvider.setImageResource(providerLogo)

        // 3. Setup ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // 4. Inisialisasi View
        val etId = findViewById<EditText>(R.id.et_customer_id)
        val btnCheck = findViewById<Button>(R.id.btn_check_bill)
        val cardBill = findViewById<CardView>(R.id.card_bill_detail)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)

        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)
        val btnPay = findViewById<Button>(R.id.btn_pay_now)

        // LOGIKA TOMBOL CEK TAGIHAN
        btnCheck.setOnClickListener {
            val id = etId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan", Toast.LENGTH_SHORT).show()
            } else {
                // Simulasi Loading (bisa ditambah ProgressBar kalau mau)

                // Simulasi Tagihan Random (Antara 250rb - 500rb)
                billAmount = (250000..500000).random().toLong()
                // Bulatkan ke ribuan terdekat biar rapi
                billAmount = (billAmount / 1000) * 1000

                // Format Rupiah
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val textHarga = formatRp.format(billAmount).replace(",00", "")

                // Tampilkan Card Detail
                cardBill.visibility = View.VISIBLE
                tvBillAmount.text = textHarga

                // Update Bottom Bar
                tvTotalBottom.text = textHarga
                btnPay.isEnabled = true
                btnPay.text = "Bayar"
            }
        }

        // LOGIKA TOMBOL BAYAR
        btnPay.setOnClickListener {
            val id = etId.text.toString()
            processTransaction(billAmount, providerName, id)
        }
    }

    private fun processTransaction(amount: Long, provider: String, custId: String) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= amount) {
                    viewModel.addTransaction("KELUAR", amount, "Bayar $provider ($custId)")
                    Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                    finish() // Kembali ke menu sebelumnya
                } else {
                    Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                }
                viewModel.user.removeObservers(this)
            }
        }
    }
}
package com.example.qash_finalproject.grid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.MainActivity
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class InternetPaymentActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private var billAmount: Long = 0
    private var isBillShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_payment)

        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Internet"
        val providerLogo = intent.getIntExtra("PROVIDER_LOGO", R.drawable.ic_internet)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup Header
        findViewById<TextView>(R.id.tv_provider_name).text = providerName
        findViewById<ImageView>(R.id.img_provider_logo).setImageResource(providerLogo)

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Views
        val etId = findViewById<EditText>(R.id.et_customer_id)
        val btnAction = findViewById<Button>(R.id.btn_action)
        val cardDetail = findViewById<View>(R.id.card_bill_detail)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)
        val tvBillProvider = findViewById<TextView>(R.id.tv_bill_provider_name)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)

        // Reset State jika ID diubah
        etId.setOnFocusChangeListener { _, _ ->
            if (isBillShown) {
                isBillShown = false
                cardDetail.visibility = View.GONE
                btnAction.text = "Cek Tagihan"
                tvTotalBottom.text = "-"
            }
        }

        // LOGIKA TOMBOL MULTIFUNGSI
        btnAction.setOnClickListener {
            val id = etId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isBillShown) {
                // --- MODE 1: CEK TAGIHAN ---
                // Simulasi Data
                billAmount = (250000..500000).random().toLong()
                billAmount = (billAmount / 1000) * 1000

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val textHarga = formatRp.format(billAmount).replace(",00", "")

                // Tampilkan Detail
                tvBillProvider.text = providerName
                tvBillAmount.text = textHarga
                tvTotalBottom.text = textHarga
                cardDetail.visibility = View.VISIBLE

                // Ubah Tombol jadi Bayar
                btnAction.text = "Bayar Sekarang"
                isBillShown = true

                // Scroll ke bawah otomatis
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            } else {
                // --- MODE 2: BAYAR SEKARANG ---
                processPayment(billAmount, providerName, id)
            }
        }
    }

    private fun processPayment(amount: Long, provider: String, custId: String) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= amount) {
                    viewModel.addTransaction("KELUAR", amount, "Bayar Internet $provider ($custId)")
                    Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                }
                viewModel.user.removeObservers(this)
            }
        }
    }
}
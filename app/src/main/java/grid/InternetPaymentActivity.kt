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
import com.example.qash_finalproject.SessionManager // Pastikan ini ter-import
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class InternetPaymentActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var billAmount: Long = 0
    private var isBillShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_payment)

        // Ambil Data dari Intent (yang dikirim dari InternetActivity)
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Internet"
        val providerLogo = intent.getIntExtra("PROVIDER_LOGO", R.drawable.ic_internet)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup Header UI
        findViewById<TextView>(R.id.tv_provider_name).text = providerName
        findViewById<ImageView>(R.id.img_provider_logo).setImageResource(providerLogo)

        // --- 1. SETUP SESSION & VIEWMODEL ---
        sessionManager = SessionManager(this)

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // --- 2. SET USER ID (Wajib agar transaksi jalan) ---
        viewModel.setUserId(sessionManager.getUserId())

        // Init Views
        val etId = findViewById<EditText>(R.id.et_customer_id)
        val btnAction = findViewById<Button>(R.id.btn_action)
        val cardDetail = findViewById<View>(R.id.card_bill_detail)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)
        val tvBillProvider = findViewById<TextView>(R.id.tv_bill_provider_name)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)

        // Reset jika user mengetik ulang ID
        etId.setOnFocusChangeListener { _, _ ->
            if (isBillShown) {
                isBillShown = false
                cardDetail.visibility = View.GONE
                btnAction.text = "Cek Tagihan"
                tvTotalBottom.text = "-"
            }
        }

        btnAction.setOnClickListener {
            val id = etId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isBillShown) {
                // --- MODE 1: CEK TAGIHAN ---
                billAmount = (250000..500000).random().toLong() // Simulasi random harga
                billAmount = (billAmount / 1000) * 1000

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val textHarga = formatRp.format(billAmount).replace(",00", "")

                tvBillProvider.text = providerName
                tvBillAmount.text = textHarga
                tvTotalBottom.text = textHarga
                cardDetail.visibility = View.VISIBLE

                btnAction.text = "Bayar Sekarang"
                isBillShown = true
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            } else {
                // --- MODE 2: BAYAR SEKARANG ---
                processPayment(billAmount, providerName, id)
            }
        }

        // Menangkap error dari ViewModel (misal Saldo kurang)
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processPayment(amount: Long, provider: String, custId: String) {
        viewModel.addTransaction("KELUAR", amount, "Bayar Internet $provider ($custId)") {
            // Callback jika sukses
            runOnUiThread {
                Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()

                // Kembali ke Home agar saldo ter-refresh
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
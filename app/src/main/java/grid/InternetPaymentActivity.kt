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
import com.example.qash_finalproject.ui.SessionManager
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

        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Internet"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = providerName
        toolbar.setNavigationOnClickListener { finish() }

        val logo = findViewById<ImageView>(R.id.img_provider_logo)
        logo.setImageResource(R.drawable.ic_internet)

        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        val etCustId = findViewById<EditText>(R.id.et_customer_id)
        val btnAction = findViewById<Button>(R.id.btn_action)
        val cardDetail = findViewById<View>(R.id.card_bill_detail)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)
        val tvBillProvider = findViewById<TextView>(R.id.tv_bill_provider_name)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)

        btnAction.setOnClickListener {
            val id = etCustId.text.toString()
            if (id.isEmpty()) {
                etCustId.error = "Masukkan ID Pelanggan"
                return@setOnClickListener
            }

            if (!isBillShown) {
                billAmount = (200000..750000).random().toLong()
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
                processPayment(billAmount, providerName, id)
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPayment(amount: Long, provider: String, custId: String) {
        viewModel.addTransaction(
            type = "KELUAR",
            amount = amount,
            description = "Bayar Internet $provider ($custId)",
            category = "Internet"
        ) {
            runOnUiThread {
                Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
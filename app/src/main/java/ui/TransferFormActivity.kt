package com.example.qash_finalproject.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.MainActivity
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class TransferFormActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var currentBalance: Long = 0
    private var transferType: String = "SESAMA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_form)

        sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        transferType = intent.getStringExtra("TRANSFER_TYPE") ?: "SESAMA"

        val tvTitle: TextView = findViewById(R.id.tv_title)
        val lblTujuan: TextView = findViewById(R.id.lbl_tujuan)
        val etReceiver: EditText = findViewById(R.id.et_receiver)

        if (transferType == "BANK") {
            tvTitle.text = "Ke Rekening Bank"
            lblTujuan.text = "Nomor Rekening"
        } else {
            tvTitle.text = "Ke Sesama Qash"
            lblTujuan.text = "Nomor Ponsel / ID Qash"
        }

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val tvBalance = findViewById<TextView>(R.id.tv_balance)
        val etAmount = findViewById<EditText>(R.id.et_amount)
        val etNote = findViewById<EditText>(R.id.et_note)
        val btnContinue = findViewById<Button>(R.id.btn_continue)

        btnBack.setOnClickListener { finish() }

        viewModel.user.observe(this) { user ->
            if (user != null) {
                currentBalance = user.balance
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvBalance.text = "Saldo Aktif: ${formatRp.format(user.balance).replace(",00", "")}"
            }
        }

        // Error Message Observer
        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // TextWatcher untuk Validasi Input (Opsional, agar tombol mati/nyala)
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val amount = etAmount.text.toString().toLongOrNull() ?: 0
                val receiver = etReceiver.text.toString()
                btnContinue.isEnabled = amount > 0 && receiver.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etAmount.addTextChangedListener(textWatcher)
        etReceiver.addTextChangedListener(textWatcher)

        btnContinue.setOnClickListener {
            val amount = etAmount.text.toString().toLongOrNull() ?: 0
            val receiver = etReceiver.text.toString()
            val note = etNote.text.toString()

            if (amount > currentBalance) {
                Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount < 10000) {
                Toast.makeText(this, "Minimal transfer Rp 10.000", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val desc = if (transferType == "BANK") "Transfer Bank ke $receiver" else "Kirim ke $receiver"
            val finalNote = if (note.isNotEmpty()) "$desc ($note)" else desc

            viewModel.addTransaction(
                type = "KELUAR",
                amount = amount,
                description = finalNote,
                category = "Transfer"
            ) {
                runOnUiThread {
                    Toast.makeText(this, "Transfer Berhasil!", Toast.LENGTH_LONG).show()

                    // Kembali ke Home & Clear Stack
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
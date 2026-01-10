package com.example.qash_finalproject

import android.content.Intent // Pastikan Intent di-import
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
            etReceiver.hint = "Masukkan nomor rekening"
            etReceiver.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        } else {
            tvTitle.text = "Ke Sesama Qash"
            lblTujuan.text = "Nomor Ponsel / Nama"
            etReceiver.hint = "Masukkan nomor ponsel"
        }

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        val tvCurrentBalance: TextView = findViewById(R.id.tv_current_balance)
        viewModel.user.observe(this) { user ->
            if (user != null) {
                currentBalance = user.balance
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvCurrentBalance.text = "Saldo ${formatRupiah.format(currentBalance)}"
            }
        }

        val etAmount: EditText = findViewById(R.id.et_amount)
        val etNote: EditText = findViewById(R.id.et_note)
        val btnContinue: Button = findViewById(R.id.btn_continue)
        val btnBack: ImageView = findViewById(R.id.btn_back)

        btnBack.setOnClickListener { finish() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amount = etAmount.text.toString().toLongOrNull() ?: 0
                val receiver = etReceiver.text.toString()

                if (amount > 0 && receiver.isNotEmpty()) {
                    btnContinue.isEnabled = true
                    btnContinue.backgroundTintList = getColorStateList(R.color.qash_primary)
                } else {
                    btnContinue.isEnabled = false
                    btnContinue.backgroundTintList = getColorStateList(android.R.color.darker_gray)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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

            viewModel.addTransaction("KELUAR", amount, finalNote) {
                runOnUiThread {
                    Toast.makeText(this, "Transfer Berhasil!", Toast.LENGTH_LONG).show()

                    // --- PERUBAHAN UTAMA DI SINI ---
                    // Kembali ke MainActivity dan hapus tumpukan activity sebelumnya
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    // -------------------------------
                }
            }
        }
    }
}
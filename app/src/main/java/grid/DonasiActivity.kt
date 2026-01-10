package com.example.qash_finalproject.grid

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.MainActivity
import com.example.qash_finalproject.R
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class DonasiActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donasi)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup ViewModel
        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        // Init Views
        val spinner = findViewById<Spinner>(R.id.spinner_lembaga)
        val etAmount = findViewById<TextInputEditText>(R.id.et_amount)
        val etNote = findViewById<TextInputEditText>(R.id.et_note)
        val btnDonate = findViewById<Button>(R.id.btn_donate)

        // Data Lembaga
        val foundations = arrayOf("Kitabisa.com", "Dompet Dhuafa", "ACT (Aksi Cepat Tanggap)", "Baznas", "Rumah Yatim", "Qash Peduli")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, foundations)
        spinner.adapter = adapter

        btnDonate.setOnClickListener {
            val amountStr = etAmount.text.toString()
            val noteStr = etNote.text.toString()
            val lembaga = spinner.selectedItem.toString()

            if (amountStr.isEmpty()) {
                etAmount.error = "Masukkan nominal"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0
            if (amount < 1000) {
                Toast.makeText(this, "Minimal donasi Rp 1.000", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pesan Transaksi
            val finalNote = if (noteStr.isNotEmpty()) "Donasi $lembaga: $noteStr" else "Donasi ke $lembaga"

            // Proses Transaksi
            viewModel.addTransaction("KELUAR", amount, finalNote) {
                runOnUiThread {
                    Toast.makeText(this, "Terima kasih atas donasi Anda!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Error Handler
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
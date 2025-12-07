package com.example.qash_finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class TopUpActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)

        // 1. Setup ViewModel (Penghubung ke Database)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // 2. Inisialisasi Komponen UI
        val etAmount: TextInputEditText = findViewById(R.id.et_amount)
        val spinnerMethod: Spinner = findViewById(R.id.spinner_method)
        val btnSave: Button = findViewById(R.id.btn_save)

        // 3. Aksi saat tombol diklik
        btnSave.setOnClickListener {
            val amountString = etAmount.text.toString()
            val selectedMethod = spinnerMethod.selectedItem.toString()

            // Validasi Input
            if (amountString.isEmpty()) {
                etAmount.error = "Nominal tidak boleh kosong"
                return@setOnClickListener
            }

            val amount = amountString.toLong()

            if (amount < 10000) {
                etAmount.error = "Minimal Top Up Rp 10.000"
                return@setOnClickListener
            }

            // 4. SIMPAN KE DATABASE
            // Tipe: MASUK, Keterangan: Top Up via [Nama Bank]
            viewModel.addTransaction("MASUK", amount, "Top Up via $selectedMethod")

            // 5. Beri Feedback dan Tutup
            Toast.makeText(this, "Top Up Rp $amount Berhasil!", Toast.LENGTH_LONG).show()
            finish() // Kembali ke halaman Home
        }
    }
}
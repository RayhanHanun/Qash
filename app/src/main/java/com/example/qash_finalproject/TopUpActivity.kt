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

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // PENTING: Observasi User agar data ter-load sebelum transaksi
        viewModel.user.observe(this) {
            // Data user siap digunakan
        }

        val etAmount: TextInputEditText = findViewById(R.id.et_amount)
        val spinnerMethod: Spinner = findViewById(R.id.spinner_method)
        val btnSave: Button = findViewById(R.id.btn_save)

        btnSave.setOnClickListener {
            val amountString = etAmount.text.toString()
            val selectedMethod = spinnerMethod.selectedItem.toString()

            if (amountString.isEmpty()) {
                etAmount.error = "Nominal tidak boleh kosong"
                return@setOnClickListener
            }

            val amount = amountString.toLong()

            if (amount < 10000) {
                etAmount.error = "Minimal Top Up Rp 10.000"
                return@setOnClickListener
            }

            // PENTING: Gunakan Callback { finish() }
            // Aplikasi hanya akan menutup layar SETELAH saldo berhasil tersimpan
            viewModel.addTransaction("MASUK", amount, "Top Up via $selectedMethod") {
                runOnUiThread {
                    Toast.makeText(this, "Top Up Rp $amount Berhasil!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}
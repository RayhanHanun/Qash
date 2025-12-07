package com.example.qash_finalproject.grid

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory

class PdamActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdam)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Setup Spinner Wilayah
        val spinner = findViewById<Spinner>(R.id.spinner_wilayah)
        val areas = listOf("PDAM Jakarta (Aetra)", "PDAM Jakarta (Palyja)", "PDAM Bogor", "PDAM Depok", "PDAM Bekasi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, areas)
        spinner.adapter = adapter

        val etId = findViewById<EditText>(R.id.et_pdam_id)
        val btnCheck = findViewById<Button>(R.id.btn_check_bill)

        btnCheck.setOnClickListener {
            val id = etId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID Pelanggan", Toast.LENGTH_SHORT).show()
            } else {
                // Simulasi Tagihan Random
                val randomBill = (50000..150000).random().toLong()

                // Langsung bayar (simulasi)
                viewModel.user.observe(this) { user ->
                    if (user != null && user.balance >= randomBill) {
                        val area = spinner.selectedItem.toString()
                        viewModel.addTransaction("KELUAR", randomBill, "Bayar Air $area ($id)")
                        Toast.makeText(this, "Tagihan Rp $randomBill Lunas!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Saldo tidak cukup untuk bayar tagihan Rp $randomBill", Toast.LENGTH_LONG).show()
                    }
                    viewModel.user.removeObservers(this)
                }
            }
        }
    }
}
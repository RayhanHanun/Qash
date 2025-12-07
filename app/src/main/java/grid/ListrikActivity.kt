package com.example.qash_finalproject.grid

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.tabs.TabLayout

class ListrikActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listrik)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val layoutToken = findViewById<LinearLayout>(R.id.layout_token_options)
        val etPln = findViewById<EditText>(R.id.et_pln_number)

        // Logic Tab Switch
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    layoutToken.visibility = View.VISIBLE
                    etPln.hint = "Masukkan No. Meter / ID"
                } else {
                    layoutToken.visibility = View.GONE
                    etPln.hint = "Masukkan ID Pelanggan"
                    // Untuk tagihan, biasanya ada tombol "Cek Tagihan" (disederhanakan disini)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Logic Beli Token
        findViewById<Button>(R.id.btn_buy_20k).setOnClickListener { processBuy(20000, etPln.text.toString()) }
        findViewById<Button>(R.id.btn_buy_50k).setOnClickListener { processBuy(50000, etPln.text.toString()) }
    }

    private fun processBuy(amount: Long, idPln: String) {
        if (idPln.isEmpty()) {
            Toast.makeText(this, "Masukkan ID Pelanggan dulu", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.user.observe(this) { user ->
            if (user != null && user.balance >= amount) {
                viewModel.addTransaction("KELUAR", amount, "Token PLN $idPln")
                Toast.makeText(this, "Token Berhasil Dibeli!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Saldo kurang!", Toast.LENGTH_SHORT).show()
            }
            viewModel.user.removeObservers(this)
        }
    }
}
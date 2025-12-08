package com.example.qash_finalproject.grid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
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
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

class PbbActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    // Data State
    private var isBillShown = false // Apakah tagihan sedang ditampilkan?
    private var billAmount: Long = 0
    private var selectedRegion: String = ""
    private var selectedYear: String = ""

    // Data Wilayah
    private val regions = listOf(
        "PBB DKI JAKARTA", "PBB KOTA DEPOK", "PBB KOTA BOGOR", "PBB KAB. BOGOR",
        "PBB KOTA BEKASI", "PBB KOTA TANGERANG", "PBB KOTA BANDUNG", "PBB KOTA SURABAYA",
        "PBB KOTA SEMARANG", "PBB KOTA YOGYAKARTA", "PBB KAB. SLEMAN"
    )

    // Data Tahun
    private val years = listOf("2025", "2024", "2023", "2022", "2021")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pbb)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Views
        val actRegion = findViewById<AutoCompleteTextView>(R.id.act_region)
        val actYear = findViewById<AutoCompleteTextView>(R.id.act_year)
        val etNop = findViewById<TextInputEditText>(R.id.et_nop)
        val cardDetail = findViewById<android.view.View>(R.id.card_bill_detail)
        val btnAction = findViewById<Button>(R.id.btn_action_pbb)
        val tvTotalBottom = findViewById<TextView>(R.id.tv_total_bottom)
        val tvBillAmount = findViewById<TextView>(R.id.tv_bill_amount)
        val tvDetailRegion = findViewById<TextView>(R.id.tv_detail_region)
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)

        // 1. SETUP DROPDOWN (Region & Year)
        val adapterRegion = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regions)
        actRegion.setAdapter(adapterRegion)

        val adapterYear = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, years)
        actYear.setAdapter(adapterYear)

        // Listener agar tombol kembali ke "Cek Tagihan" jika user mengubah input
        val resetStateListener = View.OnFocusChangeListener { _, _ ->
            if (isBillShown) {
                isBillShown = false
                cardDetail.visibility = View.GONE
                btnAction.text = "Cek Tagihan"
                tvTotalBottom.text = "-"
            }
        }
        etNop.onFocusChangeListener = resetStateListener
        actRegion.setOnItemClickListener { _, _, _, _ -> resetStateListener.onFocusChange(actRegion, true) }

        // 2. LOGIKA TOMBOL AKSI (Multifungsi: Cek / Bayar)
        btnAction.setOnClickListener {
            selectedRegion = actRegion.text.toString()
            selectedYear = actYear.text.toString()
            val nop = etNop.text.toString()

            if (selectedRegion.isEmpty() || selectedYear.isEmpty()) {
                Toast.makeText(this, "Lengkapi data wilayah dan tahun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nop.length < 10) {
                etNop.error = "NOP minimal 10 digit"
                return@setOnClickListener
            }

            if (!isBillShown) {
                // --- MODE 1: CEK TAGIHAN ---
                // Simulasi Loading & Get Data
                billAmount = (150000..900000).random().toLong()
                billAmount = (billAmount / 1000) * 1000 // Bulatkan

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val textHarga = formatRp.format(billAmount).replace(",00", "")

                // Tampilkan Detail
                tvDetailRegion.text = selectedRegion
                tvBillAmount.text = textHarga
                tvTotalBottom.text = textHarga
                cardDetail.visibility = View.VISIBLE

                // Ubah Tombol jadi Bayar
                btnAction.text = "Bayar Sekarang"
                isBillShown = true

                // Scroll ke bawah agar rincian terlihat
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            } else {
                // --- MODE 2: BAYAR SEKARANG ---
                processPayment(nop)
            }
        }
    }

    private fun processPayment(nop: String) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= billAmount) {
                    viewModel.addTransaction("KELUAR", billAmount, "Bayar PBB $selectedYear ($nop)")
                    Toast.makeText(this, "Pembayaran PBB Berhasil!", Toast.LENGTH_LONG).show()

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
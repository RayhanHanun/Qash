package com.example.qash_finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // 1. Ambil Data "Jenis Menu" dari Intent Home
        val menuName = intent.getStringExtra("MENU_NAME") ?: "Layanan"

        // 2. Inisialisasi View
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvInputLabel = findViewById<TextView>(R.id.tv_input_label)
        val etInputId = findViewById<EditText>(R.id.et_input_id)
        val btnAction = findViewById<Button>(R.id.btn_action)

        // 3. Set Tampilan Berdasarkan Menu
        tvTitle.text = menuName

        // Logika sederhana untuk mengubah Label Input
        when (menuName) {
            "Pulsa & Data" -> {
                tvInputLabel.text = "Nomor Handphone"
                etInputId.hint = "08xx xxxx xxxx"
                btnAction.text = "Lihat Pilihan Pulsa"
            }
            "Listrik PLN" -> {
                tvInputLabel.text = "ID Pelanggan / No. Meter"
                etInputId.hint = "Contoh: 54728..."
                btnAction.text = "Cek Tagihan"
            }
            "Air PDAM" -> {
                tvInputLabel.text = "Nomor Sambungan"
                etInputId.hint = "Masukkan nomor pelanggan"
                btnAction.text = "Cek Tagihan"
            }
            "Internet" -> {
                tvInputLabel.text = "Nomor Pelanggan"
                etInputId.hint = "ID Pelanggan (Indihome/Biznet)"
                btnAction.text = "Cek Tagihan"
            }
            "BPJS" -> {
                tvInputLabel.text = "Nomor VA Keluarga"
                etInputId.hint = "88888xxxxxxx"
                btnAction.text = "Cek Iuran"
            }
            else -> {
                // Default
                tvInputLabel.text = "Nomor ID / Pelanggan"
                etInputId.hint = "Masukkan nomor"
                btnAction.text = "Lanjutkan"
            }
        }

        // 4. Tombol Kembali
        btnBack.setOnClickListener { finish() }

        // 5. Tombol Aksi (Simulasi)
        btnAction.setOnClickListener {
            val input = etInputId.text.toString()
            if (input.isEmpty()) {
                Toast.makeText(this, "Mohon isi nomor dulu", Toast.LENGTH_SHORT).show()
            } else {
                // Di sini nanti bisa lanjut ke pembayaran beneran
                // Untuk sekarang, kita kasih Toast realisits
                Toast.makeText(this, "Mengecek tagihan untuk $input...", Toast.LENGTH_LONG).show()
            }
        }
    }
}
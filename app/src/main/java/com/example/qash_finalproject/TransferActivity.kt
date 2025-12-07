package com.example.qash_finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        // 1. Inisialisasi View (Menggunakan ID yang benar)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val cardSesama = findViewById<CardView>(R.id.card_sesama)
        val cardBank = findViewById<CardView>(R.id.card_bank)

        // 2. Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // 3. Pilihan: Ke Sesama Qash
        cardSesama.setOnClickListener {
            // Membuka halaman Form Transfer
            val intent = Intent(this, TransferFormActivity::class.java)
            // Kirim data tipe agar form tahu ini transfer ke sesama
            intent.putExtra("TRANSFER_TYPE", "SESAMA")
            startActivity(intent)
        }

        // 4. Pilihan: Ke Rekening Bank
        cardBank.setOnClickListener {
            // Membuka halaman Form Transfer juga
            val intent = Intent(this, TransferFormActivity::class.java)
            // Tapi kirim data tipe BANK agar form menyesuaikan labelnya
            intent.putExtra("TRANSFER_TYPE", "BANK")
            startActivity(intent)
        }
    }
}
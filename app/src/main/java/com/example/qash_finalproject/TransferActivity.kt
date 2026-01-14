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

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val cardSesama = findViewById<CardView>(R.id.card_sesama)
        val cardBank = findViewById<CardView>(R.id.card_bank)

        btnBack.setOnClickListener {
            finish()
        }

        // Ke Form Transfer (Sesama)
        cardSesama.setOnClickListener {
            val intent = Intent(this, TransferFormActivity::class.java)
            intent.putExtra("TRANSFER_TYPE", "SESAMA")
            startActivity(intent)
        }

        // Ke Form Transfer (Bank)
        cardBank.setOnClickListener {
            val intent = Intent(this, TransferFormActivity::class.java)
            intent.putExtra("TRANSFER_TYPE", "BANK")
            startActivity(intent)
        }
    }
}
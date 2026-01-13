package com.example.qash_finalproject.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.qash_finalproject.R

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val cardEmail = findViewById<CardView>(R.id.card_email)
        cardEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@qash.id")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi Qash")
            startActivity(Intent.createChooser(intent, "Kirim Email"))
        }

        val cardWa = findViewById<CardView>(R.id.card_wa)
        cardWa.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=6281234567890"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }
}
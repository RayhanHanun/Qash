package com.example.qash_finalproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R

// Model Data
data class PromoModel(
    val title: String,
    val description: String,
    val imageRes: Int
)

class PromoActivity : AppCompatActivity() {

    private val promoList = listOf(
        PromoModel("Diskon Pulsa 20%", "Berlaku untuk semua operator. Min trx 50rb.", R.drawable.promo1),
        PromoModel("Cashback Token Listrik", "Dapatkan cashback 5% maks 10rb.", R.drawable.promo2),
        PromoModel("Gratis Biaya Admin", "Bayar tagihan air PDAM tanpa biaya admin.", R.drawable.promo3),
        PromoModel("Internet Super Cepat", "Diskon 15rb bayar IndiHome/Biznet.", R.drawable.promo1),
        PromoModel("Promo Pengguna Baru", "Khusus pengguna baru Qash. Cashback 50%.", R.drawable.promo2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promo) // Pastikan ini mengarah ke layout yang benar

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvPromo = findViewById<RecyclerView>(R.id.rv_promo)
        rvPromo.layoutManager = LinearLayoutManager(this)
        rvPromo.adapter = PromoAdapter(promoList)
    }

    inner class PromoAdapter(private val list: List<PromoModel>) :
        RecyclerView.Adapter<PromoAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgPromo: ImageView = view.findViewById(R.id.img_promo)
            val tvTitle: TextView = view.findViewById(R.id.tv_promo_title)
            val tvDesc: TextView = view.findViewById(R.id.tv_promo_desc)

            fun bind(item: PromoModel) {
                imgPromo.setImageResource(item.imageRes)
                tvTitle.text = item.title
                tvDesc.text = item.description
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_promo, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size
    }
}
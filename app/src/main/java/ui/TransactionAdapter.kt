package com.example.qash_finalproject.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(private var transactionList: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNote: TextView = itemView.findViewById(R.id.tv_note)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val imgIcon: ImageView = itemView.findViewById(R.id.img_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        // 1. Set Judul & Tanggal
        holder.tvNote.text = transaction.note

        val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
        holder.tvDate.text = dateFormat.format(Date(transaction.date))

        // 2. Format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = formatRupiah.format(transaction.amount)

        // 3. Logika Warna & Ikon (MASUK vs KELUAR)
        if (transaction.type == "MASUK") {
            holder.tvAmount.text = "+ $formattedAmount"
            holder.tvAmount.setTextColor(Color.parseColor("#00C853")) // Hijau
            holder.imgIcon.setImageResource(R.drawable.ic_add_white) // Ganti ikon panah masuk jika ada
            holder.imgIcon.setColorFilter(Color.parseColor("#00C853"))
        } else {
            holder.tvAmount.text = "- $formattedAmount"
            holder.tvAmount.setTextColor(Color.parseColor("#D50000")) // Merah
            holder.imgIcon.setImageResource(R.drawable.ic_transfer) // Ganti ikon panah keluar
            holder.imgIcon.setColorFilter(Color.parseColor("#D50000"))
        }
    }

    override fun getItemCount(): Int = transactionList.size

    // Fungsi untuk update data dari LiveData
    fun setData(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged()
    }
}
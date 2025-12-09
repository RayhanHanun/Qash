package com.example.qash_finalproject.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // WAJIB ADA
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Perhatikan: Hapus '(private var list...)' di konstruktor. Biarkan kosong '()'
class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    // 1. DiffUtil: Untuk membandingkan data lama & baru secara otomatis
    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    // 2. ViewHolder
    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNote: TextView = itemView.findViewById(R.id.tv_note) // Pastikan ID ini benar di XML
        val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val imgIcon: ImageView = itemView.findViewById(R.id.img_icon)

        fun bind(transaction: Transaction) {
            // Set Text
            tvNote.text = transaction.note

            // Format Tanggal
            val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
            tvDate.text = dateFormat.format(Date(transaction.date))

            // Format Rupiah
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedAmount = formatRupiah.format(transaction.amount)

            // Logika Warna (Hijau/Merah)
            if (transaction.type == "MASUK") {
                tvAmount.text = "+ $formattedAmount"
                tvAmount.setTextColor(Color.parseColor("#00C853")) // Hijau
                imgIcon.setImageResource(R.drawable.ic_add_white) // Pastikan icon ini ada
                imgIcon.setColorFilter(Color.parseColor("#00C853"))
            } else {
                tvAmount.text = "- $formattedAmount"
                tvAmount.setTextColor(Color.parseColor("#D50000")) // Merah
                imgIcon.setImageResource(R.drawable.ic_transfer) // Pastikan icon ini ada
                imgIcon.setColorFilter(Color.parseColor("#D50000"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        // PENTING: Gunakan getItem(position) bawaan ListAdapter
        val transaction = getItem(position)
        holder.bind(transaction)
    }
}
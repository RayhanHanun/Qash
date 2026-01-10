package com.example.qash_finalproject.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.Transaction
import com.example.qash_finalproject.databinding.ItemTransactionBinding // Otomatis muncul setelah enable ViewBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    // --- PERBAIKAN POIN 6: ViewBinding di ViewHolder ---
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            // Set Text pakai binding, bukan findViewById
            binding.tvNote.text = transaction.note

            val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
            binding.tvDate.text = dateFormat.format(Date(transaction.date))

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedAmount = formatRupiah.format(transaction.amount)

            if (transaction.type == "MASUK") {
                binding.tvAmount.text = "+ $formattedAmount"
                binding.tvAmount.setTextColor(Color.parseColor("#00C853"))
                binding.imgIcon.setImageResource(R.drawable.ic_add_white)
                binding.imgIcon.setColorFilter(Color.parseColor("#00C853"))
            } else {
                binding.tvAmount.text = "- $formattedAmount"
                binding.tvAmount.setTextColor(Color.parseColor("#D50000"))
                binding.imgIcon.setImageResource(R.drawable.ic_transfer)
                binding.imgIcon.setColorFilter(Color.parseColor("#D50000"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        // Inflate pakai Binding
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }
}
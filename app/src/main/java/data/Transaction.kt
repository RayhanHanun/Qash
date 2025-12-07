package com.example.qash_finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID otomatis nambah (1, 2, 3...)

    val type: String, // "IN" (Masuk) atau "OUT" (Keluar)
    val amount: Long, // Jumlah uang
    val note: String, // Keterangan (misal: "Beli Bakso")
    val date: Long = System.currentTimeMillis() // Waktu transaksi (otomatis saat ini)
)
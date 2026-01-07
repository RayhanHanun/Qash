package com.example.qash_finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int, // Menghubungkan transaksi dengan User
    val type: String, // "IN" (Masuk) atau "OUT" (Keluar)
    val amount: Long,
    val note: String,
    val date: Long = System.currentTimeMillis()
)

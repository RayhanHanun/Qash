package com.example.qash_finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val type: String,
    val amount: Long,
    val note: String,
    val categoryName: String = "Lainnya",

    val date: Long = System.currentTimeMillis()
)
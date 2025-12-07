package com.example.qash_finalproject.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface QashDao {

    // --- USER / SALDO ---
    @Insert
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_table WHERE id = 1")
    fun getUser(): LiveData<User> // Untuk UI (HomeFragment) agar update otomatis

    // FUNGSI BARU: Ambil data User secara langsung (Synchronous) untuk Logic
    @Query("SELECT * FROM user_table WHERE id = 1")
    suspend fun getUserSync(): User?

    // --- TRANSAKSI ---
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transaction_table WHERE note LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    fun searchTransactions(searchQuery: String): LiveData<List<Transaction>>

    @Query("DELETE FROM transaction_table")
    suspend fun clearAllTransactions()
}
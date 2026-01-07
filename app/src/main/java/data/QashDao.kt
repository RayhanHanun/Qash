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
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_table WHERE id = :userId")
    fun getUserById(userId: Int): LiveData<User?>

    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun getUserSync(userId: Int): User?

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // --- TRANSAKSI ---
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transaction_table WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: Int): LiveData<List<Transaction>>

    @Query("SELECT * FROM transaction_table WHERE userId = :userId AND note LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    fun searchTransactions(userId: Int, searchQuery: String): LiveData<List<Transaction>>

    @Query("DELETE FROM transaction_table")
    suspend fun clearAllTransactions()
}

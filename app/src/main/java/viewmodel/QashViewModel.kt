package com.example.qash_finalproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.qash_finalproject.data.QashDao
import com.example.qash_finalproject.data.Transaction
import com.example.qash_finalproject.data.User
import kotlinx.coroutines.launch

class QashViewModel(private val dao: QashDao) : ViewModel() {

    // Data Live untuk UI
    val user: LiveData<User> = dao.getUser()
    val allTransactions: LiveData<List<Transaction>> = dao.getAllTransactions()

    // Fungsi Cek User Awal
    fun checkInitialization(defaultName: String) {
        viewModelScope.launch {
            try {
                // Coba buat user baru dengan saldo 0
                dao.insertUser(User(id = 1, name = defaultName, balance = 0))
            } catch (e: Exception) {
                // Jika user sudah ada (error conflict), abaikan saja
            }
        }
    }

    // Fungsi Tambah Transaksi (Top Up / Transfer)
    fun addTransaction(type: String, amount: Long, note: String) {
        viewModelScope.launch {
            // 1. Catat di Tabel Transaksi
            val trx = Transaction(type = type, amount = amount, note = note)
            dao.insertTransaction(trx)

            // 2. Update Saldo User
            // Menggunakan getUserSync() agar data pasti terambil meskipun di Activity lain
            val currentUser = dao.getUserSync()

            if (currentUser != null) {
                val currentBalance = currentUser.balance

                // Hitung Saldo Baru
                val newBalance = if (type == "MASUK") {
                    currentBalance + amount
                } else {
                    currentBalance - amount
                }

                // Simpan Perubahan Saldo
                val updatedUser = currentUser.copy(balance = newBalance)
                dao.updateUser(updatedUser)
            }
        }
    }
}

// Factory untuk ViewModel (Boilerplate)
class QashViewModelFactory(private val dao: QashDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QashViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
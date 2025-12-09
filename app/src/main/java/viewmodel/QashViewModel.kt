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
                dao.insertUser(User(id = 1, name = defaultName, balance = 0))
            } catch (e: Exception) {
                // Abaikan jika user sudah ada
            }
        }
    }

    // Fungsi Tambah Transaksi
    fun addTransaction(type: String, amount: Long, description: String) {
        viewModelScope.launch {
            val currentUser = user.value
            if (currentUser != null) {
                var newBalance = currentUser.balance
                var newPoints = currentUser.points

                if (type == "MASUK") {
                    newBalance += amount
                } else {
                    // TIPE "KELUAR"
                    if (newBalance >= amount) {
                        newBalance -= amount
                        // LOGIKA POIN: 1000 Rupiah = 1 Poin
                        val pointsEarned = amount / 1000
                        newPoints += pointsEarned
                    } else {
                        return@launch
                    }
                }

                // Update User (GANTI 'repository' JADI 'dao')
                val updatedUser = currentUser.copy(balance = newBalance, points = newPoints)
                dao.updateUser(updatedUser) // <--- PERBAIKAN DISINI

                // Simpan Riwayat (GANTI 'repository' JADI 'dao')
                val newTransaction = Transaction(
                    type = type,
                    amount = amount,
                    description = description,
                    date = System.currentTimeMillis()
                )
                dao.insertTransaction(newTransaction) // <--- PERBAIKAN DISINI
            }
        }
    }
}

// --- PINDAHKAN FACTORY KE LUAR CLASS QashViewModel (DI BAWAH SINI) ---
class QashViewModelFactory(private val dao: QashDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QashViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
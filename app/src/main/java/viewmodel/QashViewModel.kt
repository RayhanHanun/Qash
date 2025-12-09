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

    val user: LiveData<User> = dao.getUser()
    val allTransactions: LiveData<List<Transaction>> = dao.getAllTransactions()

    fun checkInitialization(defaultName: String) {
        viewModelScope.launch {
            try {
                dao.insertUser(User(id = 1, name = defaultName, balance = 0))
            } catch (e: Exception) {
            }
        }
    }

    // UPDATE: Tambahkan parameter 'onComplete' di akhir
    fun addTransaction(type: String, amount: Long, description: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val currentUser = user.value
            if (currentUser != null) {
                var newBalance = currentUser.balance
                var newPoints = currentUser.points

                if (type == "MASUK") {
                    newBalance += amount
                } else {
                    if (newBalance >= amount) {
                        newBalance -= amount
                        val pointsEarned = amount / 1000
                        newPoints += pointsEarned
                    } else {
                        return@launch
                    }
                }

                // Update Saldo
                val updatedUser = currentUser.copy(balance = newBalance, points = newPoints)
                dao.updateUser(updatedUser)

                // Simpan Transaksi
                val newTransaction = Transaction(
                    type = type,
                    amount = amount,
                    note = description, // Pastikan ini 'note', bukan 'description'
                    date = System.currentTimeMillis()
                )
                dao.insertTransaction(newTransaction)

                // PENTING: Panggil onComplete setelah selesai simpan
                onComplete()
            }
        }
    }
}

class QashViewModelFactory(private val dao: QashDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QashViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.qash_finalproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.qash_finalproject.data.QashDao
import com.example.qash_finalproject.data.Transaction
import com.example.qash_finalproject.data.User
import kotlinx.coroutines.launch

class QashViewModel(private val dao: QashDao) : ViewModel() {

    private val _userId = MutableLiveData<Int>()

    // Untuk Feedback Error (Toast)
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val user: LiveData<User?> = _userId.switchMap { id ->
        dao.getUserById(id)
    }

    val allTransactions: LiveData<List<Transaction>> = _userId.switchMap { id ->
        dao.getAllTransactions(id)
    }

    fun setUserId(id: Int) {
        _userId.value = id
    }

    fun updateProfile(newName: String, newPhone: String, newImage: String?) {
        viewModelScope.launch {
            try { // --- PERBAIKAN POIN 3: Try-Catch ---
                val id = _userId.value ?: return@launch
                val currentUser = dao.getUserSync(id)
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        name = newName,
                        phone = newPhone,
                        profileImage = newImage
                    )
                    dao.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal update profil: ${e.message}"
            }
        }
    }

    fun addTransaction(type: String, amount: Long, description: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try { // --- PERBAIKAN POIN 3: Try-Catch ---
                val id = _userId.value ?: return@launch
                val currentUser = dao.getUserSync(id)

                if (currentUser != null) {
                    var newBalance = currentUser.balance

                    if (type == "MASUK") {
                        newBalance += amount
                    } else {
                        if (newBalance >= amount) {
                            newBalance -= amount
                        } else {
                            _errorMessage.value = "Saldo tidak mencukupi!" // Feedback Error
                            return@launch
                        }
                    }

                    val updatedUser = currentUser.copy(balance = newBalance)
                    dao.updateUser(updatedUser)

                    val newTransaction = Transaction(
                        userId = id,
                        type = type,
                        amount = amount,
                        note = description,
                        date = System.currentTimeMillis()
                    )
                    dao.insertTransaction(newTransaction)

                    onComplete()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal transaksi: ${e.message}"
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

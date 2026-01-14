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
import kotlinx.coroutines.Dispatchers

class QashViewModel(private val dao: QashDao) : ViewModel() {

    private val _userId = MutableLiveData<Int>()

    // Untuk Feedback Error (Toast)
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData User yang auto-update jika ada perubahan di Database
    val user: LiveData<User?> = _userId.switchMap { id ->
        dao.getUserById(id)
    }

    // LiveData List Transaksi
    val allTransactions: LiveData<List<Transaction>> = _userId.switchMap { id ->
        dao.getAllTransactions(id)
    }

    fun setUserId(id: Int) {
        _userId.value = id
    }

    // Fungsi Update Profil (Nama, HP, Foto)
    fun updateProfile(newName: String, newPhone: String, newImage: String?) {
        viewModelScope.launch {
            try {
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

    // --- FUNGSI TRANSAKSI UTAMA (Sudah Support Kategori) ---
    fun addTransaction(
        type: String,
        amount: Long,
        description: String,
        category: String, // <--- Parameter Baru: Kategori Transaksi
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val id = _userId.value ?: return@launch
                val currentUser = dao.getUserSync(id)

                if (currentUser != null) {
                    var newBalance = currentUser.balance

                    // 1. Logic Cek & Update Saldo
                    if (type == "MASUK") {
                        newBalance += amount
                    } else {
                        if (newBalance >= amount) {
                            newBalance -= amount
                        } else {
                            _errorMessage.value = "Saldo tidak mencukupi!" // Kirim error ke UI
                            return@launch
                        }
                    }

                    // Simpan Saldo Baru ke Database User
                    val updatedUser = currentUser.copy(balance = newBalance)
                    dao.updateUser(updatedUser)

                    // 2. Simpan Riwayat Transaksi (Dengan Kategori)
                    val newTransaction = Transaction(
                        userId = id,
                        type = type,
                        amount = amount,
                        note = description,
                        categoryName = category, // <--- Disimpan ke Database
                        date = System.currentTimeMillis()
                    )
                    dao.insertTransaction(newTransaction)

                    // Beritahu UI bahwa sukses
                    onComplete()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal transaksi: ${e.message}"
            }
        }
    }

    // Fungsi Hapus Akun Permanen (Fitur Delete Data)
    fun deleteAccount(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Hapus data user dari database
            dao.deleteUser(user)

            // Kembali ke Main Thread untuk update UI (Logout/Pindah Layar)
            launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}

// Factory untuk membuat ViewModel (Karena butuh parameter DAO)
class QashViewModelFactory(private val dao: QashDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QashViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
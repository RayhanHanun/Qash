package com.example.qash_finalproject.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.qash_finalproject.data.QashDao
import com.example.qash_finalproject.data.Transaction
import com.example.qash_finalproject.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class QashViewModelTest {

    // Rule agar LiveData berjalan secara synchronous (langsung, tidak di background thread)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Dispatcher khusus untuk testing Coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var dao: QashDao

    private lateinit var viewModel: QashViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher) // Set Main thread ke test dispatcher
        viewModel = QashViewModel(dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset Main thread setelah test selesai
    }

    @Test
    fun `addTransaction Income adds balance correctly`() = runTest {
        // GIVEN (Siapkan Data)
        val userId = 1
        val initialBalance = 100_000L
        val amount = 50_000L
        val user = User(id = userId, name = "Test User", email = "test@qash.com", phone = "08123", password = "pass", balance = initialBalance)

        // Mocking: Ketika DAO diminta user, kembalikan user palsu di atas
        whenever(dao.getUserSync(userId)).thenReturn(user)
        viewModel.setUserId(userId)

        // WHEN (Jalankan Fungsi)
        viewModel.addTransaction("MASUK", amount, "Top Up", "Saldo")

        // Majukan waktu coroutine agar selesai dieksekusi
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN (Verifikasi Hasil)
        val userCaptor = argumentCaptor<User>()

        // Pastikan fungsi updateUser dipanggil
        verify(dao).updateUser(userCaptor.capture())

        // Pastikan saldo bertambah (100.000 + 50.000 = 150.000)
        assertEquals(150_000L, userCaptor.firstValue.balance)

        // Pastikan transaksi tercatat
        verify(dao).insertTransaction(any())
    }

    @Test
    fun `addTransaction Expense subtracts balance correctly`() = runTest {
        // GIVEN
        val userId = 1
        val initialBalance = 100_000L
        val amount = 20_000L // Pengeluaran
        val user = User(id = userId, name = "Test User", email = "test@qash.com", phone = "08123", password = "pass", balance = initialBalance)

        whenever(dao.getUserSync(userId)).thenReturn(user)
        viewModel.setUserId(userId)

        // WHEN
        viewModel.addTransaction("KELUAR", amount, "Beli Makan", "Makan")
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        val userCaptor = argumentCaptor<User>()
        verify(dao).updateUser(userCaptor.capture())

        // Pastikan saldo berkurang (100.000 - 20.000 = 80.000)
        assertEquals(80_000L, userCaptor.firstValue.balance)
    }

    @Test
    fun `addTransaction Expense fails if insufficient balance`() = runTest {
        // GIVEN
        val userId = 1
        val initialBalance = 10_000L // Saldo cuma 10rb
        val amount = 50_000L // Mau beli barang 50rb
        val user = User(id = userId, name = "Test User", email = "test@qash.com", phone = "08123", password = "pass", balance = initialBalance)

        whenever(dao.getUserSync(userId)).thenReturn(user)
        viewModel.setUserId(userId)

        // WHEN
        viewModel.addTransaction("KELUAR", amount, "Belanja Mahal", "Belanja")
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        // Pastikan DAO updateUser TIDAK PERNAH dipanggil (Saldo aman)
        verify(dao, never()).updateUser(any())

        // Pastikan pesan error terisi
        assertEquals("Saldo tidak mencukupi!", viewModel.errorMessage.value)
    }
}
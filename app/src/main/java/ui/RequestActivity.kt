package com.example.qash_finalproject.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory

class RequestActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        // 1. Init Session & ViewModel
        sessionManager = SessionManager(this)
        val dao = QashDatabase.getDatabase(this).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]

        // 2. Set User ID yang sedang login
        viewModel.setUserId(sessionManager.getUserId())

        // 3. Init View
        val tvName = findViewById<TextView>(R.id.tv_req_name)
        val tvPhone = findViewById<TextView>(R.id.tv_req_phone)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // 4. Observe Data User (Agar Dinamis)
        viewModel.user.observe(this) { user ->
            if (user != null) {
                tvName.text = user.name.uppercase() // Nama User (Huruf Besar)
                tvPhone.text = user.phone           // Nomor HP User
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
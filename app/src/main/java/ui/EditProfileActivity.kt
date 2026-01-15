package com.example.qash_finalproject.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory

class EditProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    private lateinit var ivProfile: ImageView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivProfile.setImageURI(it)
            ivProfile.setPadding(0, 0, 0, 0)
            ivProfile.clearColorFilter()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        val dao = QashDatabase.getDatabase(this).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        ivProfile = findViewById(R.id.iv_profile_picture)
        val etName = findViewById<EditText>(R.id.et_name)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnChangePhoto = findViewById<androidx.cardview.widget.CardView>(R.id.btn_change_photo)

        // Load current data for the logged-in user
        viewModel.user.observe(this) { user ->
            if (user != null) {
                etName.setText(user.name)
                etPhone.setText(user.phone)
                user.profileImage?.let {
                    val uri = Uri.parse(it)
                    ivProfile.setImageURI(uri)
                    ivProfile.setPadding(0, 0, 0, 0)
                    ivProfile.clearColorFilter()
                    selectedImageUri = uri
                }
            }
        }

        btnChangePhoto.setOnClickListener {
            getContent.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                viewModel.updateProfile(name, phone, selectedImageUri?.toString())
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Nama dan Nomor Telepon tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}

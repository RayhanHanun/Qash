package com.example.qash_finalproject.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.LoginActivity
import com.example.qash_finalproject.R
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
// Import yang Benar untuk Tema Kamu:
import com.google.android.material.switchmaterial.SwitchMaterial

class ProfileFragment : Fragment() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        val dao = QashDatabase.getDatabase(requireContext()).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        // Init Views
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val tvPhone = view.findViewById<TextView>(R.id.tv_phone)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_main)
        val ivEdit = view.findViewById<ImageView>(R.id.iv_edit_profile)
        val layoutEditProfile = view.findViewById<LinearLayout>(R.id.layout_edit_profile)
        val layoutSavedBank = view.findViewById<LinearLayout>(R.id.layout_saved_bank)
        val layoutTerms = view.findViewById<LinearLayout>(R.id.layout_terms)
        val layoutHelp = view.findViewById<LinearLayout>(R.id.layout_help)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        // Init Switch (Gunakan SwitchMaterial)
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)

        // Observe User Data
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvName.text = user.name

                // Gunakan 'phone' sesuai User.kt
                tvPhone.text = user.phone

                user.profileImage?.let {
                    ivProfile.setImageURI(Uri.parse(it))
                    ivProfile.setPadding(0, 0, 0, 0)
                    ivProfile.clearColorFilter()
                } ?: run {
                    ivProfile.setImageResource(R.drawable.ic_nav_profile)
                    ivProfile.setPadding(12, 12, 12, 12)
                    ivProfile.setColorFilter(resources.getColor(R.color.qash_primary))
                }
            }
        }

        // --- LOGIKA DARK MODE ---
        if (switchDarkMode != null) {
            switchDarkMode.isChecked = sessionManager.isDarkMode()
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                sessionManager.setDarkMode(isChecked)
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        // Navigate to EditProfileActivity
        val goToEditProfile = View.OnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
        ivEdit.setOnClickListener(goToEditProfile)
        layoutEditProfile.setOnClickListener(goToEditProfile)

        // Menu Lainnya
        layoutSavedBank.setOnClickListener {
            Toast.makeText(context, "Fitur Rekening Bank akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        layoutTerms.setOnClickListener {
            Toast.makeText(context, "Syarat & Ketentuan Qash App", Toast.LENGTH_SHORT).show()
        }

        layoutHelp.setOnClickListener {
            Toast.makeText(context, "Hubungi Customer Service: support@qash.id", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                sessionManager.logout()
                Toast.makeText(context, "Berhasil keluar", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
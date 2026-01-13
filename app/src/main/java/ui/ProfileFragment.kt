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
import com.example.qash_finalproject.data.User
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial

class ProfileFragment : Fragment() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null // Simpan data user untuk keperluan hapus akun

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. SETUP SESSION & DATABASE ---
        sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        val dao = QashDatabase.getDatabase(requireContext()).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        // --- 2. INISIALISASI VIEW ---
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val tvPhone = view.findViewById<TextView>(R.id.tv_phone)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_main)

        // Tombol Edit
        val ivEdit = view.findViewById<ImageView>(R.id.iv_edit_profile)
        val layoutEditProfile = view.findViewById<LinearLayout>(R.id.layout_edit_profile)

        // Menu Lainnya
        val layoutTerms = view.findViewById<LinearLayout>(R.id.layout_terms)
        val layoutHelp = view.findViewById<LinearLayout>(R.id.layout_help)

        // Switch Dark Mode
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)

        // Tombol Aksi
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btn_delete_account)

        // --- 3. OBSERVE DATA USER ---
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user // Simpan ke variabel global
                tvName.text = user.name
                tvPhone.text = user.phone

                // Load Gambar Profil
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

        // --- 4. LOGIKA DARK MODE ---
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

        // --- 5. NAVIGASI MENU ---

        // Ke Edit Profil
        val goToEditProfile = View.OnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        ivEdit.setOnClickListener(goToEditProfile)
        layoutEditProfile.setOnClickListener(goToEditProfile)


        // Ke Syarat & Ketentuan
        layoutTerms.setOnClickListener {
            startActivity(Intent(context, TermsActivity::class.java))
        }

        // Ke Pusat Bantuan
        layoutHelp.setOnClickListener {
            startActivity(Intent(context, HelpActivity::class.java))
        }

        // --- 6. LOGOUT & DELETE ACCOUNT ---

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    // --- HELPER FUNCTIONS ---

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Akun Permanen")
            .setMessage("PERINGATAN: Tindakan ini tidak dapat dibatalkan. Semua data transaksi dan saldo Anda akan hilang selamanya. Yakin ingin menghapus akun?")
            .setPositiveButton("YA, HAPUS") { _, _ ->
                // Panggil ViewModel untuk hapus user
                currentUser?.let { user ->
                    viewModel.deleteAccount(user) {
                        Toast.makeText(context, "Akun berhasil dihapus permanen.", Toast.LENGTH_LONG).show()
                        performLogout() // Logout setelah dihapus
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        sessionManager.logout()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}
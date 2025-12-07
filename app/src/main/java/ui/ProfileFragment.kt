package com.example.qash_finalproject.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.qash_finalproject.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Set Nama (Bisa diambil dari Database/ViewModel jika mau dinamis)
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        tvName.text = "Rayhan" // Sementara hardcode, sesuaikan dengan nama user

        // 2. Logic Logout
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi Qash?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                // Aksi Logout (Misal tutup activity atau kembali ke login)
                Toast.makeText(context, "Anda telah keluar.", Toast.LENGTH_SHORT).show()
                activity?.finish() // Menutup aplikasi
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
package com.example.qash_finalproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R

class InboxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvInbox = view.findViewById<RecyclerView>(R.id.rv_inbox)

        // Data Dummy (Contoh Pesan)
        val dummyData = listOf(
            InboxModel("Keamanan Akun", "Ada login baru di perangkat Xiaomi Redmi Note 10 pada 12:30 WIB.", "Baru saja", false),
            InboxModel("Promo Gajian!", "Cashback 50% max Rp 20.000 untuk pembayaran tagihan listrik.", "Kemarin", false),
            InboxModel("Transfer Berhasil", "Transfer ke Budi Santoso sebesar Rp 50.000 berhasil.", "2 Hari lalu", true),
            InboxModel("Top Up Sukses", "Saldo Rp 100.000 telah masuk ke akunmu.", "3 Hari lalu", true),
            InboxModel("Maintenance Sistem", "Kami akan melakukan pemeliharaan sistem pada jam 02:00 - 04:00 WIB.", "1 Minggu lalu", true),
            InboxModel("Selamat Datang!", "Terima kasih telah bergabung dengan Qash. Nikmati kemudahan transaksi.", "1 Bulan lalu", true)
        )

        // Setup RecyclerView
        rvInbox.layoutManager = LinearLayoutManager(context)
        rvInbox.adapter = InboxAdapter(dummyData)
    }
}
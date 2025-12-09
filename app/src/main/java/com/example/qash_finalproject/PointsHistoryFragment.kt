package com.example.qash_finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.ui.TransactionAdapter
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory // Pastikan ini ter-import

class PointsHistoryFragment : Fragment() {

    private lateinit var viewModel: QashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_points_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHistory = view.findViewById<RecyclerView>(R.id.rv_history_points)

        // Pastikan TransactionAdapter() sudah dibuat sebelumnya
        val adapter = TransactionAdapter()
        rvHistory.layoutManager = LinearLayoutManager(context)
        rvHistory.adapter = adapter

        // Setup ViewModel
        val dao = QashDatabase.getDatabase(requireContext()).qashDao()
        val factory = QashViewModelFactory(dao)

        // Gunakan requireActivity() agar data sinkron dengan Activity induknya
        viewModel = ViewModelProvider(requireActivity(), factory)[QashViewModel::class.java]

        // Tampilkan Riwayat
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val pointTransactions = transactions.filter { it.type == "KELUAR" }
            adapter.submitList(pointTransactions)
        }
    }
}
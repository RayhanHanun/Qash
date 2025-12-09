package com.example.qash_finalproject.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory

class HistoryFragment : Fragment() {

    private lateinit var viewModel: QashViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var layoutEmpty: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup ViewModel
        val application = requireNotNull(this.activity).application
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // 2. Setup RecyclerView
        val rvHistory = view.findViewById<RecyclerView>(R.id.rv_history)
        layoutEmpty = view.findViewById(R.id.layout_empty)

        // PERBAIKAN 1: Konstruktor ListAdapter tidak butuh parameter
        adapter = TransactionAdapter()
        rvHistory.adapter = adapter
        rvHistory.layoutManager = LinearLayoutManager(context)

        // 3. Observasi Data Real-time
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                rvHistory.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            } else {
                rvHistory.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                // PERBAIKAN 2: Gunakan submitList(), bukan setData()
                adapter.submitList(transactions)
            }
        }

        // 4. Fitur Search
        val etSearch = view.findViewById<EditText>(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Logika Filter Sederhana
    private fun filterList(query: String) {
        val fullList = viewModel.allTransactions.value ?: return
        val filteredList = fullList.filter {
            it.note.contains(query, ignoreCase = true)
        }

        // PERBAIKAN 3: Gunakan submitList()
        adapter.submitList(filteredList)
    }
}
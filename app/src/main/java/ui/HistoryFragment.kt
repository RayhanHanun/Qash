package com.example.qash_finalproject.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory

class HistoryFragment : Fragment() {

    private lateinit var viewModel: QashViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar // Tambahan
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        // 1. Setup ViewModel
        val application = requireNotNull(this.activity).application
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]
        viewModel.setUserId(userId)

        // 2. Setup RecyclerView & Views
        val rvHistory = view.findViewById<RecyclerView>(R.id.rv_history)
        layoutEmpty = view.findViewById(R.id.layout_empty)
        progressBar = view.findViewById(R.id.progressBar) // Inisialisasi ProgressBar

        adapter = TransactionAdapter()
        rvHistory.adapter = adapter
        rvHistory.layoutManager = LinearLayoutManager(context)

        // 3. Observasi Data Real-time
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            // --- PERBAIKAN POIN 4: Hide Loading saat data masuk ---
            progressBar.visibility = View.GONE

            if (transactions == null || transactions.isEmpty()) {
                rvHistory.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            } else {
                rvHistory.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE
                adapter.submitList(transactions)
            }
        }

        // --- PERBAIKAN POIN 5: Feedback Error (Toast) ---
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

    private fun filterList(query: String) {
        val fullList = viewModel.allTransactions.value ?: return
        val filteredList = fullList.filter {
            it.note.contains(query, ignoreCase = true)
        }
        adapter.submitList(filteredList)
    }
}
package com.example.qash_finalproject.grid

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

// Model Data Provider
data class InternetProvider(val name: String, val imageResId: Int)

class InternetActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel
    private lateinit var adapter: ProviderListAdapter

    // Data Provider (Sebaiknya ganti imageResId dengan logo asli masing-masing jika ada)
    private val allProviders = listOf(
        InternetProvider("Biznet Home", R.drawable.ic_internet),
        InternetProvider("IndiHome", R.drawable.ic_internet),
        InternetProvider("First Media", R.drawable.ic_internet),
        InternetProvider("MyRepublic", R.drawable.ic_internet),
        InternetProvider("CBN Fiber", R.drawable.ic_internet),
        InternetProvider("MNC Play", R.drawable.ic_internet),
        InternetProvider("Oxygen.id", R.drawable.ic_internet),
        InternetProvider("XL Home", R.drawable.ic_internet),
        InternetProvider("Transvision", R.drawable.ic_internet)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // ViewModel
        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Setup RecyclerView
        val rvProviders = findViewById<RecyclerView>(R.id.rv_providers)
        rvProviders.layoutManager = LinearLayoutManager(this)

        // Init Adapter
        adapter = ProviderListAdapter(allProviders) { selectedProvider ->
            // Aksi saat item diklik: Munculkan Form Input (Bottom Sheet)
            showInputBottomSheet(selectedProvider)
        }
        rvProviders.adapter = adapter

        // Setup Search
        val etSearch = findViewById<EditText>(R.id.et_search_provider)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterList(query: String) {
        val filtered = allProviders.filter {
            it.name.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }

    // --- BOTTOM SHEET FORM (Formulir Pembayaran) ---
    private fun showInputBottomSheet(provider: InternetProvider) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_payment, null)

        // Bind View di dalam Bottom Sheet
        val tvTitle = view.findViewById<TextView>(R.id.tv_bs_title)
        val imgLogo = view.findViewById<ImageView>(R.id.img_bs_logo)
        val etId = view.findViewById<EditText>(R.id.et_customer_id)
        val btnPay = view.findViewById<Button>(R.id.btn_pay_now)

        tvTitle.text = provider.name
        imgLogo.setImageResource(provider.imageResId)

        btnPay.setOnClickListener {
            val id = etId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan Nomor Pelanggan", Toast.LENGTH_SHORT).show()
            } else {
                // Simulasi Bayar
                val randomBill = (150000..500000).random().toLong() // Tagihan acak
                processTransaction(randomBill, provider.name, id, dialog)
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun processTransaction(amount: Long, providerName: String, custId: String, dialog: BottomSheetDialog) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= amount) {
                    viewModel.addTransaction("KELUAR", amount, "Bayar Internet $providerName ($custId)")
                    Toast.makeText(this, "Tagihan Rp $amount Lunas!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    finish()
                } else {
                    Toast.makeText(this, "Saldo tidak cukup untuk bayar tagihan Rp $amount", Toast.LENGTH_LONG).show()
                }
                viewModel.user.removeObservers(this)
            }
        }
    }

    // --- ADAPTER CLASS ---
    inner class ProviderListAdapter(
        private var list: List<InternetProvider>,
        private val onItemClick: (InternetProvider) -> Unit
    ) : RecyclerView.Adapter<ProviderListAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val imgLogo: ImageView = view.findViewById(R.id.img_logo)

            fun bind(item: InternetProvider) {
                tvName.text = item.name
                imgLogo.setImageResource(item.imageResId)
                itemView.setOnClickListener { onItemClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_internet_provider, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        fun updateList(newList: List<InternetProvider>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
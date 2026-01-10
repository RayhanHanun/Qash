package com.example.qash_finalproject.grid

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R

// Model Data Provider (Hanya ada di file ini atau dipisah ke file model sendiri)
data class InternetProvider(val name: String, val imageResId: Int)

class InternetActivity : AppCompatActivity() {

    private lateinit var adapter: ProviderListAdapter

    // Data Provider
    private val allProviders = listOf(
        InternetProvider("Biznet Home", R.drawable.ic_internet),
        InternetProvider("IndiHome", R.drawable.ic_internet),
        InternetProvider("First Media", R.drawable.ic_internet),
        InternetProvider("MyRepublic", R.drawable.ic_internet),
        InternetProvider("CBN Fiber", R.drawable.ic_internet),
        InternetProvider("MNC Play", R.drawable.ic_internet),
        InternetProvider("Oxygen.id", R.drawable.ic_internet),
        InternetProvider("XL Home", R.drawable.ic_internet),
        InternetProvider("Transvision", R.drawable.ic_internet),
        InternetProvider("Megavision", R.drawable.ic_internet)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvProviders = findViewById<RecyclerView>(R.id.rv_providers)
        rvProviders.layoutManager = LinearLayoutManager(this)

        adapter = ProviderListAdapter(allProviders) { selectedProvider ->
            // --- PINDAH KE HALAMAN BAYAR (InternetPaymentActivity) ---
            val intent = Intent(this, InternetPaymentActivity::class.java)
            intent.putExtra("PROVIDER_NAME", selectedProvider.name)
            intent.putExtra("PROVIDER_LOGO", selectedProvider.imageResId)
            startActivity(intent)
        }
        rvProviders.adapter = adapter

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
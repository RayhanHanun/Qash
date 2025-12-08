package com.example.qash_finalproject.grid

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.qash_finalproject.R
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

// 1. Data Model untuk Provider (Nama + Gambar)
data class EmoneyProvider(val name: String, val imageResId: Int)

class EmoneyActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    // State
    private var selectedProvider: String = ""
    private var selectedAmount: Long = 0
    private var activeNominalCard: MaterialCardView? = null

    // 2. Data Provider dengan Gambar
    // PENTING: Ganti R.drawable.ic_emoney dengan gambar kartu asli kamu (misal: R.drawable.card_mandiri)
    private val providers = listOf(
        EmoneyProvider("Mandiri e-Money", R.drawable.mandiri),
        EmoneyProvider("BCA Flazz", R.drawable.bca),
        EmoneyProvider("BNI TapCash", R.drawable.bni),
    )

    private val nominals = listOf(10000L, 20000L, 25000L, 50000L, 100000L, 200000L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emoney)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val dao = QashDatabase.getDatabase(application).qashDao()
        val viewModelFactory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[QashViewModel::class.java]

        // Init Views
        val spinnerProvider = findViewById<Spinner>(R.id.spinner_provider)
        val gridNominal = findViewById<GridLayout>(R.id.grid_nominal)
        val etNumber = findViewById<TextInputEditText>(R.id.et_customer_number)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_topup)
        val tvPrice = findViewById<TextView>(R.id.tv_selected_price)

        // 3. SETUP CUSTOM ADAPTER UNTUK SPINNER
        val adapter = ProviderAdapter(this, providers)
        spinnerProvider.adapter = adapter

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ambil objek EmoneyProvider yang dipilih
                val selectedItem = providers[position]
                selectedProvider = selectedItem.name
                updateButtonState(btnConfirm)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Generate Nominal Cards
        nominals.forEach { amount ->
            val card = createNominalCard(amount)
            card.setOnClickListener {
                handleNominalSelection(card, amount, tvPrice)
                updateButtonState(btnConfirm)
            }
            gridNominal.addView(card)
        }

        // Tombol Confirm
        btnConfirm.setOnClickListener {
            val number = etNumber.text.toString()
            if (number.length < 8) {
                etNumber.error = "Nomor kartu tidak valid"
                return@setOnClickListener
            }
            processTransaction(selectedAmount, number, selectedProvider)
        }
    }

    // --- CUSTOM ADAPTER CLASS (SUDAH DIPERBAIKI) ---
    inner class ProviderAdapter(context: Context, private val items: List<EmoneyProvider>) :
        ArrayAdapter<EmoneyProvider>(context, 0, items) {

        // Ini tampilan saat Spinner TERTUTUP (Selected Item)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // isDropdown = false (Munculkan Panah)
            return createItemView(position, convertView, parent, isDropdown = false)
        }

        // Ini tampilan saat Spinner TERBUKA (List Pilihan)
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            // isDropdown = true (Sembunyikan Panah)
            return createItemView(position, convertView, parent, isDropdown = true)
        }

        // Fungsi helper dengan parameter tambahan 'isDropdown'
        private fun createItemView(position: Int, convertView: View?, parent: ViewGroup, isDropdown: Boolean): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.item_provider_spinner, parent, false
            )

            val item = items[position]
            val imgLogo = view.findViewById<ImageView>(R.id.img_provider_logo)
            val tvName = view.findViewById<TextView>(R.id.tv_provider_name)
            val imgArrow = view.findViewById<ImageView>(R.id.img_arrow) // Ambil ID Panah

            imgLogo.setImageResource(item.imageResId)
            tvName.text = item.name

            // --- LOGIKA SEMBUNYIKAN PANAH ---
            if (isDropdown) {
                // Jika sedang membuka list, panah DIHILANGKAN
                imgArrow.visibility = View.GONE
            } else {
                // Jika tampilan utama (terpilih), panah DITAMPILKAN
                imgArrow.visibility = View.VISIBLE
            }

            return view
        }
    }

    // --- LOGIKA LAINNYA (SAMA SEPERTI SEBELUMNYA) ---
    private fun handleNominalSelection(card: MaterialCardView, amount: Long, tvPrice: TextView) {
        val blueColor = ContextCompat.getColor(this, R.color.qash_primary)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        if (activeNominalCard == card) {
            card.strokeWidth = 0
            activeNominalCard = null
            selectedAmount = 0
            tvPrice.text = "-"
        } else {
            activeNominalCard?.strokeWidth = 0
            activeNominalCard = card
            selectedAmount = amount

            card.strokeWidth = 6
            card.strokeColor = blueColor
            tvPrice.text = formatRp.format(amount).replace(",00", "")
        }
    }

    private fun updateButtonState(btn: Button) {
        btn.isEnabled = (selectedAmount > 0)
    }

    private fun createNominalCard(amount: Long): MaterialCardView {
        val card = MaterialCardView(this)
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(16, 16, 16, 16)
        }
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 4f
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        card.foreground = ContextCompat.getDrawable(this, outValue.resourceId)

        val textView = TextView(this)
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        textView.text = formatRp.format(amount).replace(",00", "")
        textView.textSize = 18f
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.setPadding(32, 48, 32, 48)
        textView.setTextColor(ContextCompat.getColor(this, R.color.qash_primary))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

        card.addView(textView)
        return card
    }

    private fun processTransaction(amount: Long, number: String, provider: String) {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                if (user.balance >= amount) {
                    viewModel.addTransaction("KELUAR", amount, "Top Up $provider - $number")
                    Toast.makeText(this, "Top Up Berhasil!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Saldo tidak cukup!", Toast.LENGTH_SHORT).show()
                }
                viewModel.user.removeObservers(this)
            }
        }
    }
}
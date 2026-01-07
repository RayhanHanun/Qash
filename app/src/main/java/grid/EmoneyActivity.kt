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

// Model Data
data class EmoneyProvider(val name: String, val imageResId: Int)

class EmoneyActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    // State
    private var selectedProvider: String = ""
    private var selectedAmount: Long = 0
    private var activeNominalCard: MaterialCardView? = null

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

        val spinnerProvider = findViewById<Spinner>(R.id.spinner_provider)
        val gridNominal = findViewById<GridLayout>(R.id.grid_nominal)
        val etNumber = findViewById<TextInputEditText>(R.id.et_customer_number)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_topup)
        val tvPrice = findViewById<TextView>(R.id.tv_selected_price)

        // --- REVISI: LOAD DATA DARI XML & MAPPING GAMBAR ---
        val stringArray = resources.getStringArray(R.array.emoney_products)
        val providers = stringArray.map { name ->
            val image = when {
                name.contains("Mandiri", true) -> R.drawable.mandiri
                name.contains("BCA", true) -> R.drawable.bca
                name.contains("BNI", true) -> R.drawable.bni
                else -> R.drawable.ic_emoney // Default icon jika tidak ada logo khusus
            }
            EmoneyProvider(name, image)
        }
        // ----------------------------------------------------

        // Setup Adapter
        val adapter = ProviderAdapter(this, providers)
        spinnerProvider.adapter = adapter

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = providers[position]
                selectedProvider = selectedItem.name
                updateButtonState(btnConfirm)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Generate Grid
        nominals.forEach { amount ->
            val card = createNominalCard(amount)
            card.setOnClickListener {
                handleNominalSelection(card, amount, tvPrice)
                updateButtonState(btnConfirm)
            }
            gridNominal.addView(card)
        }

        btnConfirm.setOnClickListener {
            val number = etNumber.text.toString()
            if (number.length < 8) {
                etNumber.error = "Nomor kartu tidak valid"
                return@setOnClickListener
            }
            processTransaction(selectedAmount, number, selectedProvider)
        }
    }

    // --- Adapter Custom Tetap Dipertahankan (Biar UI Bagus) ---
    inner class ProviderAdapter(context: Context, private val items: List<EmoneyProvider>) :
        ArrayAdapter<EmoneyProvider>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent, isDropdown = false)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent, isDropdown = true)
        }

        private fun createItemView(position: Int, convertView: View?, parent: ViewGroup, isDropdown: Boolean): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.item_provider_spinner, parent, false
            )
            val item = items[position]
            val imgLogo = view.findViewById<ImageView>(R.id.img_provider_logo)
            val tvName = view.findViewById<TextView>(R.id.tv_provider_name)
            val imgArrow = view.findViewById<ImageView>(R.id.img_arrow)

            imgLogo.setImageResource(item.imageResId)
            tvName.text = item.name

            if (isDropdown) imgArrow.visibility = View.GONE else imgArrow.visibility = View.VISIBLE
            return view
        }
    }

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
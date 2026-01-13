package com.example.qash_finalproject.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.qash_finalproject.PromoActivity // Pastikan import ini ada
import com.example.qash_finalproject.R
import com.example.qash_finalproject.RequestActivity
import com.example.qash_finalproject.SessionManager
import com.example.qash_finalproject.TopUpActivity
import com.example.qash_finalproject.TransferActivity
import com.example.qash_finalproject.WithdrawActivity
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.grid.BpjsActivity
import com.example.qash_finalproject.grid.DonasiActivity
import com.example.qash_finalproject.grid.EmoneyActivity
import com.example.qash_finalproject.grid.InternetActivity
import com.example.qash_finalproject.grid.ListrikActivity
import com.example.qash_finalproject.grid.PbbActivity
import com.example.qash_finalproject.grid.PdamActivity
import com.example.qash_finalproject.grid.PulsaActivity
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.example.qash_finalproject.viewmodel.QashViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var viewModel: QashViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init ViewModel & Session
        sessionManager = SessionManager(requireContext())
        val dao = QashDatabase.getDatabase(requireContext()).qashDao()
        val factory = QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]
        viewModel.setUserId(sessionManager.getUserId())

        // --- SETUP SALDO USER ---
        val tvGreeting = view.findViewById<TextView>(R.id.tv_greeting)
        val tvBalance = view.findViewById<TextView>(R.id.tv_balance)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvGreeting.text = "Qash • ${user.name}"
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvBalance.text = formatRp.format(user.balance).replace(",00", "")
            }
        }

        // --- SETUP TOMBOL PROMO (INI YANG KEMARIN KURANG) ---
        view.findViewById<View>(R.id.btn_top_promo).setOnClickListener {
            startActivity(Intent(context, PromoActivity::class.java))
        }

        // --- SETUP MENU UTAMA (TopUp, Transfer, dll) ---
        view.findViewById<View>(R.id.btn_topup).setOnClickListener {
            startActivity(Intent(context, TopUpActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_transfer).setOnClickListener {
            startActivity(Intent(context, TransferActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_request).setOnClickListener {
            startActivity(Intent(context, RequestActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_tarik).setOnClickListener {
            startActivity(Intent(context, WithdrawActivity::class.java))
        }

        // --- SETUP MENU GRID (Pulsa, BPJS, dll) ---
        // 1. Pulsa & Data (Index 0)
        getChildAt(view, 0).setOnClickListener {
            startActivity(Intent(context, PulsaActivity::class.java))
        }
        // 2. E-Money (Index 1)
        getChildAt(view, 1).setOnClickListener {
            startActivity(Intent(context, EmoneyActivity::class.java))
        }
        // 3. Internet (Index 2)
        getChildAt(view, 2).setOnClickListener {
            startActivity(Intent(context, InternetActivity::class.java))
        }
        // 4. PBB (Index 3)
        getChildAt(view, 3).setOnClickListener {
            startActivity(Intent(context, PbbActivity::class.java))
        }
        // 5. Listrik (Index 4)
        getChildAt(view, 4).setOnClickListener {
            startActivity(Intent(context, ListrikActivity::class.java))
        }
        // 6. PDAM (Index 5)
        getChildAt(view, 5).setOnClickListener {
            startActivity(Intent(context, PdamActivity::class.java))
        }
        // 7. BPJS
        view.findViewById<View>(R.id.btn_menu_bpjs).setOnClickListener {
            startActivity(Intent(context, BpjsActivity::class.java))
        }
        // 8. Donasi
        view.findViewById<View>(R.id.btn_menu_donasi).setOnClickListener {
            startActivity(Intent(context, DonasiActivity::class.java))
        }

        // --- SETUP PROMO SLIDER ---
        setupPromoSlider(view)
    }

    private fun getChildAt(view: View, index: Int): View {
        val grid = view.findViewById<GridLayout>(R.id.grid_menu)
        return grid.getChildAt(index)
    }

    private fun setupPromoSlider(view: View) {
        val viewPager = view.findViewById<ViewPager2>(R.id.vp_promo)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_indicator)

        val promoImages = listOf(
            R.drawable.promo1,
            R.drawable.promo2,
            R.drawable.promo3
        )

        val adapter = PromoAdapter(promoImages)
        viewPager.adapter = adapter

        // Efek Carousel
        viewPager.offscreenPageLimit = 3
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPager.setPageTransformer(transformer)

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }
}
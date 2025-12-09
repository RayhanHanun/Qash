package com.example.qash_finalproject

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.qash_finalproject.data.QashDatabase
import com.example.qash_finalproject.points.PointsHistoryFragment
import com.example.qash_finalproject.points.PointsRedeemFragment
import com.example.qash_finalproject.viewmodel.QashViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale

class PointsActivity : AppCompatActivity() {

    private lateinit var viewModel: QashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        // 1. Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // 2. Setup Header Poin (Real-time update)
        val tvPoints = findViewById<TextView>(R.id.tv_total_points)
        val dao = QashDatabase.getDatabase(application).qashDao()
        val factory = QashViewModel.QashViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[QashViewModel::class.java]

        viewModel.user.observe(this) { user ->
            if (user != null) {
                val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
                tvPoints.text = formatter.format(user.points)
            }
        }

        // 3. Setup Tabs & ViewPager
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        val adapter = PointsPagerAdapter(this)
        viewPager.adapter = adapter

        // Hubungkan Tab dengan ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Penukaran" else "Riwayat"
        }.attach()
    }

    // Inner Class Adapter untuk Paging
    inner class PointsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) PointsRedeemFragment() else PointsHistoryFragment()
        }
    }
}
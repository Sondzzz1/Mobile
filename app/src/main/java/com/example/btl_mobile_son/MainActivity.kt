package com.example.btl_mobile_son

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.btl_mobile_son.data.SampleDataHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load sample data lần đầu
        lifecycleScope.launch {
            try {
                SampleDataHelper(this@MainActivity).loadSampleDataIfNeeded()
                
                // Cập nhật trạng thái hợp đồng hết hạn
                withContext(Dispatchers.IO) {
                    val dbManager = com.example.btl_mobile_son.data.db.DatabaseManager.getInstance(this@MainActivity)
                    dbManager.hopDongDao.capNhatHopDongHetHan()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Error loading data", e)
            }
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        // Display user info in nav header
        val sessionManager = SessionManager(this)
        val headerView = navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
        val tvUserRole = headerView.findViewById<android.widget.TextView>(R.id.tvUserRole)
        
        tvUserName.text = sessionManager.getFullName()?.uppercase() ?: "NGƯỜI DÙNG"
        tvUserRole.text = if (sessionManager.isAdmin()) "QUẢN TRỊ VIÊN" else "NHÂN VIÊN"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> { loadFragment(DashboardFragment()); true }
                R.id.navigation_invoice -> { loadFragment(InvoiceListFragment()); true }
                R.id.navigation_tenant -> { loadFragment(TenantListFragment()); true }
                R.id.navigation_message -> { loadFragment(ContractListFragment()); true }
                R.id.navigation_issue -> { loadFragment(DepositListFragment()); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            navView.setCheckedItem(R.id.nav_home)
            bottomNav.selectedItemId = R.id.navigation_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> loadFragment(HouseListFragment())
            R.id.nav_tenant -> loadFragment(TenantListFragment())
            R.id.nav_contract -> loadFragment(ContractListFragment())
            R.id.nav_service -> loadFragment(ServiceListFragment())
            R.id.nav_room_service -> loadFragment(RoomServiceManagementFragment())
            R.id.nav_utility -> loadFragment(UtilityListFragment())
            R.id.nav_stats -> loadFragment(StatisticsFragment())
            R.id.nav_report -> loadFragment(ReportFragment())
            R.id.nav_finance -> loadFragment(IncomeListFragment())
            R.id.nav_issue -> loadFragment(IssueListFragment())
            R.id.nav_permission -> {
                // Logout
                val sessionManager = SessionManager(this)
                sessionManager.logout()
                val intent = android.content.Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

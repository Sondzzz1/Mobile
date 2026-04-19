package com.example.btl_mobile_son

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        if (sessionManager.isTenant()) {
            startActivity(android.content.Intent(this, TenantMainActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)

        // Xử lý nút Back bằng OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Nếu Drawer không mở, thực hiện hành động back mặc định
                    isEnabled = false // Tạm tắt callback để tránh lặp vô hạn
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true // Bật lại sau đó
                }
            }
        })

        // Khởi tạo Database ở background ngay lập tức
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbManager = DatabaseManager.getInstance(this@MainActivity)
                dbManager.getDatabase() // Mồi database mở sẵn ở background
                dbManager.hopDongDao.capNhatHopDongHetHan()
                android.util.Log.d("MainActivity", "Database initialized in background")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Background DB Init Error", e)
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
        
        // Đảm bảo click vào icon Menu sẽ mở ngăn kéo Drawer
        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navView.setNavigationItemSelectedListener(this)

        val headerView = navView.getHeaderView(0)
        headerView.findViewById<android.widget.TextView>(R.id.tvUserName).text = 
            sessionManager.getFullName()?.uppercase() ?: "QUẢN TRỊ VIÊN"
        headerView.findViewById<android.widget.TextView>(R.id.tvUserRole).text = "ADMIN"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> { loadFragment(DashboardFragment()); true }
                R.id.navigation_invoice -> { loadFragment(InvoiceListFragment()); true }
                R.id.navigation_tenant -> { loadFragment(TenantListFragment()); true }
                R.id.navigation_message -> { loadFragment(ContractListFragment()); true }
                R.id.navigation_issue -> { loadFragment(IssueListFragment()); true }
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
            R.id.nav_finance -> loadFragment(TransactionListFragment())
            R.id.nav_issue -> loadFragment(IssueListFragment())
            R.id.nav_permission -> {
                sessionManager.logout()
                startActivity(android.content.Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

package com.example.btl_mobile_son

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class TenantMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val bottomNav = findViewById<BottomNavigationView>(R.id.tenant_bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tenant_nav_home -> { loadFragment(TenantDashboardFragment()); true }
                R.id.tenant_nav_invoice -> { loadFragment(TenantInvoiceListFragment()); true }
                R.id.tenant_nav_issue -> { loadFragment(TenantIssueReportFragment()); true }
                R.id.tenant_nav_profile -> { loadFragment(TenantProfileFragment()); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(TenantDashboardFragment())
            bottomNav.selectedItemId = R.id.tenant_nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tenant_fragment_container, fragment)
            .commit()
    }
}

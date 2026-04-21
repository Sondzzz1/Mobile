package com.example.btl_mobile_son

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManHinhChinhKhachThueActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Kiểm tra session - nếu chưa đăng nhập hoặc không phải tenant, chuyển về DangNhapActivity
            val sessionManager = QuanLyPhien(this)
            if (!sessionManager.isLoggedIn()) {
                val intent = android.content.Intent(this, DangNhapActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
            
            // Nếu là admin, chuyển sang ManHinhChinhAdminActivity
            if (sessionManager.isAdmin()) {
                val intent = android.content.Intent(this, ManHinhChinhAdminActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
            
            // Chỉ tenant mới vào ManHinhChinhKhachThueActivity
            if (!sessionManager.isTenant()) {
                sessionManager.logout()
                val intent = android.content.Intent(this, DangNhapActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
            
            setContentView(R.layout.activity_tenant_main)

            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)

            val bottomNav = findViewById<BottomNavigationView>(R.id.tenant_bottom_navigation)
            bottomNav.setOnItemSelectedListener { item ->
                try {
                    when (item.itemId) {
                        R.id.tenant_nav_home -> { loadFragment(DashboardKhachThueFragment()); true }
                        R.id.tenant_nav_invoice -> { loadFragment(DanhSachHoaDonKhachThueFragment()); true }
                        R.id.tenant_nav_issue -> { loadFragment(BaoCaoSuCoKhachThueFragment()); true }
                        R.id.tenant_nav_profile -> { loadFragment(HoSoKhachThueFragment()); true }
                        else -> false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ManHinhChinhKhachThueActiv", "Error loading fragment", e)
                    android.widget.Toast.makeText(this, "Lỗi: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    false
                }
            }

            if (savedInstanceState == null) {
                loadFragment(DashboardKhachThueFragment())
                bottomNav.selectedItemId = R.id.tenant_nav_home
            }
        } catch (e: Exception) {
            android.util.Log.e("ManHinhChinhKhachThueActiv", "Error in onCreate", e)
            android.widget.Toast.makeText(this, "Lỗi khởi tạo: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            
            // Fallback to login
            val intent = android.content.Intent(this, DangNhapActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tenant_fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            android.util.Log.e("ManHinhChinhKhachThueActiv", "Error in loadFragment", e)
            android.widget.Toast.makeText(this, "Lỗi tải fragment: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

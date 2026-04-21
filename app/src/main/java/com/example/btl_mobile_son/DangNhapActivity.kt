package com.example.btl_mobile_son

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DangNhapActivity : AppCompatActivity() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var sessionManager: QuanLyPhien

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = QuanLyPhien(this)
        
        // Nếu đã đăng nhập, chuyển thẳng vào ManHinhChinhAdminActivity hoặc ManHinhChinhKhachThueActivity
        if (sessionManager.isLoggedIn()) {
            when {
                sessionManager.isAdmin() -> {
                    startMainActivity()
                    return
                }
                sessionManager.isTenant() -> {
                    startTenantActivity()
                    return
                }
                else -> {
                    // Trạng thái không hợp lệ, đăng xuất và tiếp tục hiện màn hình login
                    sessionManager.logout()
                }
            }
        }
        
        setContentView(R.layout.activity_login)
        
        // Khởi tạo DatabaseManager ở background nếu cần thiết, 
        // nhưng ở đây getInstance chỉ lấy instance, việc mở DB đã được làm lazy.
        dbManager = DatabaseManager.getInstance(this)
        
        // Kích hoạt khởi tạo DB ở background ngay từ bây giờ để sẵn sàng khi vào Dashboard
        dbManager.triggerInitialization()
        
        // Tạo tài khoản admin mặc định nếu chưa có
        createDefaultAdminIfNeeded()
        
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            login(username, password)
        }

        // Register button
        tvRegister.setOnClickListener {
            val intent = Intent(this, DangKyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createDefaultAdminIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Kiểm tra xem đã có admin chưa
                val adminExists = try {
                    dbManager.nguoiDungDao.dangNhap("admin", "admin123") != null
                } catch (e: Exception) {
                    false
                }
                
                // Nếu chưa có admin, tạo tài khoản admin
                if (!adminExists) {
                    dbManager.nguoiDungDao.them(
                        com.example.btl_mobile_son.data.model.NguoiDung(
                            tenDangNhap = "admin",
                            matKhau = "admin123",
                            hoTen = "Quản trị viên",
                            vaiTro = "admin",
                            soDienThoai = "0900000000",
                            email = "admin@nhatro.com"
                        )
                    )
                    android.util.Log.d("DangNhapActivity", "Default admin account created")
                }
            } catch (e: Exception) {
                android.util.Log.e("DangNhapActivity", "Error creating admin", e)
            }
        }
    }

    private fun login(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("DangNhapActivity", "Attempting login for: $username")
                
                // Try admin/staff login first
                val nguoiDung = dbManager.nguoiDungDao.dangNhap(username, password)
                android.util.Log.d("DangNhapActivity", "Admin login result: ${nguoiDung != null}")
                
                if (nguoiDung != null) {
                    withContext(Dispatchers.Main) {
                        sessionManager.createLoginSession(nguoiDung)
                        Toast.makeText(this@DangNhapActivity, "Đăng nhập Admin thành công", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    }
                } else {
                    // Try tenant login
                    val khachThue = dbManager.khachThueDao.dangNhap(username, password)
                    android.util.Log.d("DangNhapActivity", "Tenant login result: ${khachThue != null}")
                    
                    // Debug: List all tenants
                    val allTenants = dbManager.khachThueDao.layTatCa()
                    android.util.Log.d("DangNhapActivity", "Total tenants in DB: ${allTenants.size}")
                    allTenants.forEach { tenant ->
                        android.util.Log.d("DangNhapActivity", "Tenant: ${tenant.tenDangNhap} / ${tenant.hoTen}")
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (khachThue != null) {
                            sessionManager.createTenantLoginSession(khachThue.maKhach, khachThue.hoTen, username)
                            Toast.makeText(this@DangNhapActivity, "Đăng nhập Người dùng thành công", Toast.LENGTH_SHORT).show()
                            startTenantActivity()
                        } else {
                            Toast.makeText(this@DangNhapActivity, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DangNhapActivity", "Login error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DangNhapActivity, "Lỗi đăng nhập: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, ManHinhChinhAdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startTenantActivity() {
        val intent = Intent(this, ManHinhChinhKhachThueActivity::class.java)
        startActivity(intent)
        finish()
    }
}

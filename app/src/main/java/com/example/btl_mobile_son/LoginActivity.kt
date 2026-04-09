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

class LoginActivity : AppCompatActivity() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Nếu đã đăng nhập, chuyển thẳng vào MainActivity hoặc TenantMainActivity
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                startMainActivity()
            } else if (sessionManager.isTenant()) {
                startTenantActivity()
            }
            return
        }
        
        setContentView(R.layout.activity_login)
        
        dbManager = DatabaseManager.getInstance(this)
        
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
            val intent = Intent(this, RegisterActivity::class.java)
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
                    android.util.Log.d("LoginActivity", "Default admin account created")
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Error creating admin", e)
            }
        }
    }

    private fun login(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("LoginActivity", "Attempting login for: $username")
                
                // Try admin/staff login first
                val nguoiDung = dbManager.nguoiDungDao.dangNhap(username, password)
                android.util.Log.d("LoginActivity", "Admin login result: ${nguoiDung != null}")
                
                if (nguoiDung != null) {
                    withContext(Dispatchers.Main) {
                        sessionManager.createLoginSession(nguoiDung)
                        Toast.makeText(this@LoginActivity, "Đăng nhập Admin thành công", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    }
                } else {
                    // Try tenant login
                    val khachThue = dbManager.khachThueDao.dangNhap(username, password)
                    android.util.Log.d("LoginActivity", "Tenant login result: ${khachThue != null}")
                    
                    // Debug: List all tenants
                    val allTenants = dbManager.khachThueDao.layTatCa()
                    android.util.Log.d("LoginActivity", "Total tenants in DB: ${allTenants.size}")
                    allTenants.forEach { tenant ->
                        android.util.Log.d("LoginActivity", "Tenant: ${tenant.tenDangNhap} / ${tenant.hoTen}")
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (khachThue != null) {
                            sessionManager.createTenantLoginSession(khachThue.maKhach, khachThue.hoTen, username)
                            Toast.makeText(this@LoginActivity, "Đăng nhập Người dùng thành công", Toast.LENGTH_SHORT).show()
                            startTenantActivity()
                        } else {
                            Toast.makeText(this@LoginActivity, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Login error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Lỗi đăng nhập: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startTenantActivity() {
        val intent = Intent(this, TenantMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

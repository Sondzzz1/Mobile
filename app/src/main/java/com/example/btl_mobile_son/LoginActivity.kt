package com.example.btl_mobile_son

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
        
        // Nếu đã đăng nhập, chuyển thẳng vào MainActivity
        if (sessionManager.isLoggedIn()) {
            startMainActivity()
            return
        }
        
        setContentView(R.layout.activity_login)
        
        dbManager = DatabaseManager.getInstance(this)
        
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try admin/staff login first
                val nguoiDung = dbManager.nguoiDungDao.dangNhap(username, password)
                
                if (nguoiDung != null) {
                    withContext(Dispatchers.Main) {
                        sessionManager.createLoginSession(nguoiDung)
                        Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    }
                } else {
                    // Try tenant login
                    val khachThue = dbManager.khachThueDao.dangNhap(username, password)
                    
                    withContext(Dispatchers.Main) {
                        if (khachThue != null) {
                            sessionManager.createTenantLoginSession(khachThue.maKhach, khachThue.hoTen, username)
                            Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                            startTenantActivity()
                        } else {
                            Toast.makeText(this@LoginActivity, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
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

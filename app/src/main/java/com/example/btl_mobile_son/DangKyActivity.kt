package com.example.btl_mobile_son

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.KhachThue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DangKyActivity : AppCompatActivity() {

    private lateinit var dbManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbManager = DatabaseManager.getInstance(this)

        val etFullName = findViewById<EditText>(R.id.etRegisterFullName)
        val etUsername = findViewById<EditText>(R.id.etRegisterUsername)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val etPhone = findViewById<EditText>(R.id.etRegisterPhone)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etCMND = findViewById<EditText>(R.id.etRegisterCMND)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val cmnd = etCMND.text.toString().trim()

            // Validation
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isEmpty() || username.length < 3) {
                Toast.makeText(this, "Tên đăng nhập phải có ít nhất 3 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isEmpty() || phone.length < 10) {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cmnd.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập CMND/CCCD", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(fullName, username, password, phone, email, cmnd)
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun register(
        fullName: String,
        username: String,
        password: String,
        phone: String,
        email: String,
        cmnd: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("DangKyActivity", "Attempting to register: $username")
                
                // Check if username already exists
                val existingTenant = dbManager.khachThueDao.layTatCa()
                    .find { it.tenDangNhap == username }

                if (existingTenant != null) {
                    android.util.Log.d("DangKyActivity", "Username already exists: $username")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@DangKyActivity,
                            "Tên đăng nhập đã tồn tại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Create new tenant account
                val khachThue = KhachThue(
                    hoTen = fullName,
                    soDienThoai = phone,
                    email = email,
                    soCmnd = cmnd,
                    tenDangNhap = username,
                    matKhau = password,
                    ngayTao = System.currentTimeMillis()
                )

                val result = dbManager.khachThueDao.them(khachThue)
                android.util.Log.d("DangKyActivity", "Registration result ID: $result")
                
                // Verify the tenant was saved
                val savedTenant = dbManager.khachThueDao.dangNhap(username, password)
                android.util.Log.d("DangKyActivity", "Verification: ${savedTenant != null}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DangKyActivity,
                        "✓ Đăng ký thành công! Vui lòng đăng nhập",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                android.util.Log.e("DangKyActivity", "Registration error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DangKyActivity,
                        "✗ Lỗi đăng ký: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

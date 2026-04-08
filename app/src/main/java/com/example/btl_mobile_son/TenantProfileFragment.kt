package com.example.btl_mobile_son

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.KhachThue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantProfileFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var sessionManager: SessionManager
    private var maKhach: Long = 0
    private var khachThue: KhachThue? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tenant_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dbManager = DatabaseManager.getInstance(requireContext())
            sessionManager = SessionManager(requireContext())
            maKhach = sessionManager.getUserId().toLong()

            view.findViewById<Button>(R.id.btnUpdateProfile)?.setOnClickListener {
                updateProfile()
            }

            view.findViewById<Button>(R.id.btnChangePassword)?.setOnClickListener {
                changePassword()
            }

            view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
                logout()
            }

            loadProfile()
        } catch (e: Exception) {
            android.util.Log.e("TenantProfile", "Error in onViewCreated", e)
            android.widget.Toast.makeText(context, "Lỗi khởi tạo: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                khachThue = withContext(Dispatchers.IO) {
                    try {
                        dbManager.khachThueDao.layTheoMa(maKhach)
                    } catch (e: Exception) {
                        android.util.Log.e("TenantProfile", "Error loading profile", e)
                        null
                    }
                }

                khachThue?.let { kt ->
                    view?.findViewById<TextView>(R.id.tvProfileName)?.text = kt.hoTen
                    view?.findViewById<EditText>(R.id.etProfilePhone)?.setText(kt.soDienThoai)
                    view?.findViewById<EditText>(R.id.etProfileEmail)?.setText(kt.email)
                    view?.findViewById<TextView>(R.id.tvProfileCMND)?.text = "CMND/CCCD: ${kt.soCmnd}"
                } ?: run {
                    android.widget.Toast.makeText(context, "Không tìm thấy thông tin người dùng", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("TenantProfile", "Error in loadProfile", e)
                android.widget.Toast.makeText(context, "Lỗi tải thông tin: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        val phone = view?.findViewById<EditText>(R.id.etProfilePhone)?.text.toString().trim()
        val email = view?.findViewById<EditText>(R.id.etProfileEmail)?.text.toString().trim()

        if (phone.isEmpty()) {
            android.widget.Toast.makeText(context, "Vui lòng nhập số điện thoại", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    khachThue?.let { kt ->
                        val updated = kt.copy(soDienThoai = phone, email = email)
                        dbManager.khachThueDao.capNhat(updated)
                    }
                }

                android.widget.Toast.makeText(context, "✓ Cập nhật thành công", android.widget.Toast.LENGTH_SHORT).show()
                loadProfile()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "✗ Lỗi: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changePassword() {
        val oldPassword = view?.findViewById<EditText>(R.id.etOldPassword)?.text.toString().trim()
        val newPassword = view?.findViewById<EditText>(R.id.etNewPassword)?.text.toString().trim()
        val confirmPassword = view?.findViewById<EditText>(R.id.etConfirmPassword)?.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            android.widget.Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            android.widget.Toast.makeText(context, "Mật khẩu mới không khớp", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (khachThue?.matKhau != oldPassword) {
            android.widget.Toast.makeText(context, "Mật khẩu cũ không đúng", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dbManager.khachThueDao.capNhatMatKhau(maKhach, newPassword)
                }

                android.widget.Toast.makeText(context, "✓ Đổi mật khẩu thành công", android.widget.Toast.LENGTH_SHORT).show()
                view?.findViewById<EditText>(R.id.etOldPassword)?.setText("")
                view?.findViewById<EditText>(R.id.etNewPassword)?.setText("")
                view?.findViewById<EditText>(R.id.etConfirmPassword)?.setText("")
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "✗ Lỗi: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        sessionManager.logout()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}

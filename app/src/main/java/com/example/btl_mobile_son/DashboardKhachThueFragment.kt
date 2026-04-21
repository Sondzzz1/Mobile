package com.example.btl_mobile_son

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class DashboardKhachThueFragment : Fragment() {

    private lateinit var sessionManager: QuanLyPhien

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DashboardKhachThue", "onCreateView called")
        return try {
            inflater.inflate(R.layout.fragment_tenant_dashboard, container, false)
        } catch (e: Exception) {
            Log.e("DashboardKhachThue", "Error inflating layout", e)
            Toast.makeText(context, "Lỗi tải giao diện: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("DashboardKhachThue", "onViewCreated started")
        
        try {
            // Initialize session manager
            sessionManager = QuanLyPhien(requireContext())
            Log.d("DashboardKhachThue", "QuanLyPhien initialized")
            
            // Get user info from session
            val fullName = sessionManager.getFullName() ?: "Khách thuê"
            val userId = sessionManager.getUserId()
            
            Log.d("DashboardKhachThue", "User: $fullName (ID: $userId)")
            
            // Set static values for now - no database calls
            view.findViewById<TextView>(R.id.tvTenantName)?.apply {
                text = fullName
                Log.d("DashboardKhachThue", "Set tenant name: $fullName")
            }
            
            view.findViewById<TextView>(R.id.tvRoomInfo)?.apply {
                text = "Chưa có thông tin phòng"
                Log.d("DashboardKhachThue", "Set room info")
            }
            
            view.findViewById<TextView>(R.id.tvContractDate)?.apply {
                text = "Chưa có hợp đồng"
                Log.d("DashboardKhachThue", "Set contract date")
            }
            
            view.findViewById<TextView>(R.id.tvDepositAmount)?.apply {
                text = ""
                Log.d("DashboardKhachThue", "Set deposit amount")
            }
            
            view.findViewById<TextView>(R.id.tvRentAmount)?.apply {
                text = "0đ"
                Log.d("DashboardKhachThue", "Set rent amount")
            }
            
            view.findViewById<TextView>(R.id.tvRentStatus)?.apply {
                text = "Chưa có hóa đơn"
                Log.d("DashboardKhachThue", "Set rent status")
            }
            
            Log.d("DashboardKhachThue", "All views initialized successfully")
            Toast.makeText(context, "Chào mừng $fullName", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("DashboardKhachThue", "Error in onViewCreated", e)
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

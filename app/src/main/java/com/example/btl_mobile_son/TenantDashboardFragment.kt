package com.example.btl_mobile_son

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class TenantDashboardFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TenantDashboard", "onCreateView called")
        return try {
            inflater.inflate(R.layout.fragment_tenant_dashboard, container, false)
        } catch (e: Exception) {
            Log.e("TenantDashboard", "Error inflating layout", e)
            Toast.makeText(context, "Lỗi tải giao diện: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("TenantDashboard", "onViewCreated started")
        
        try {
            // Initialize session manager
            sessionManager = SessionManager(requireContext())
            Log.d("TenantDashboard", "SessionManager initialized")
            
            // Get user info from session
            val fullName = sessionManager.getFullName() ?: "Khách thuê"
            val userId = sessionManager.getUserId()
            
            Log.d("TenantDashboard", "User: $fullName (ID: $userId)")
            
            // Set static values for now - no database calls
            view.findViewById<TextView>(R.id.tvTenantName)?.apply {
                text = fullName
                Log.d("TenantDashboard", "Set tenant name: $fullName")
            }
            
            view.findViewById<TextView>(R.id.tvRoomInfo)?.apply {
                text = "Chưa có thông tin phòng"
                Log.d("TenantDashboard", "Set room info")
            }
            
            view.findViewById<TextView>(R.id.tvContractDate)?.apply {
                text = "Chưa có hợp đồng"
                Log.d("TenantDashboard", "Set contract date")
            }
            
            view.findViewById<TextView>(R.id.tvDepositAmount)?.apply {
                text = ""
                Log.d("TenantDashboard", "Set deposit amount")
            }
            
            view.findViewById<TextView>(R.id.tvRentAmount)?.apply {
                text = "0đ"
                Log.d("TenantDashboard", "Set rent amount")
            }
            
            view.findViewById<TextView>(R.id.tvRentStatus)?.apply {
                text = "Chưa có hóa đơn"
                Log.d("TenantDashboard", "Set rent status")
            }
            
            Log.d("TenantDashboard", "All views initialized successfully")
            Toast.makeText(context, "Chào mừng $fullName", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("TenantDashboard", "Error in onViewCreated", e)
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

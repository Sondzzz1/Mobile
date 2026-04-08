package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.utils.CurrencyHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TenantDashboardFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var sessionManager: SessionManager
    private var maKhach: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tenant_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            android.util.Log.d("TenantDashboard", "onViewCreated started")
            
            // Set default values first - no database access
            view.findViewById<TextView>(R.id.tvTenantName)?.text = "Khách thuê"
            view.findViewById<TextView>(R.id.tvRoomInfo)?.text = "Chưa có phòng"
            view.findViewById<TextView>(R.id.tvContractDate)?.text = "Chưa có hợp đồng"
            view.findViewById<TextView>(R.id.tvDepositAmount)?.text = ""
            view.findViewById<TextView>(R.id.tvRentAmount)?.text = "0đ"
            view.findViewById<TextView>(R.id.tvRentStatus)?.text = "Chưa có hóa đơn"
            
            android.util.Log.d("TenantDashboard", "Default values set")

            // Initialize managers
            dbManager = DatabaseManager.getInstance(requireContext())
            sessionManager = SessionManager(requireContext())
            maKhach = sessionManager.getUserId().toLong()
            
            android.util.Log.d("TenantDashboard", "Managers initialized, maKhach=$maKhach")

            // Update name from session
            view.findViewById<TextView>(R.id.tvTenantName)?.text = 
                sessionManager.getFullName() ?: "Khách thuê"
            
            android.util.Log.d("TenantDashboard", "Name updated")

            // Load data in background - don't block UI
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                loadDashboardData()
            }, 500)
            
            android.util.Log.d("TenantDashboard", "onViewCreated completed")
        } catch (e: Exception) {
            android.util.Log.e("TenantDashboard", "Error in onViewCreated", e)
            android.widget.Toast.makeText(context, "Lỗi khởi tạo: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    try {
                        // Get active contract
                        val hopDong = dbManager.hopDongDao.layHopDongHienTaiCuaKhach(maKhach)
                        
                        val roomInfo = if (hopDong != null) {
                            val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
                            val nhaTro = phong?.let { dbManager.nhaTroDao.layTheoMa(it.maNha) }
                            Pair(phong?.tenPhong ?: "", nhaTro?.tenNha ?: "")
                        } else {
                            Pair("", "")
                        }

                        // Get latest invoice
                        val hoaDon = hopDong?.let { 
                            dbManager.hoaDonDao.layHoaDonTheoHopDongVaThang(it.maHopDong, 
                                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
                        }

                        Triple(hopDong, roomInfo, hoaDon)
                    } catch (e: Exception) {
                        android.util.Log.e("TenantDashboard", "Error loading data", e)
                        Triple(null, Pair("", ""), null)
                    }
                }

                val (hopDong, roomInfo, hoaDon) = data
                val (tenPhong, tenNhaTro) = roomInfo

                // Safely update UI
                view?.findViewById<TextView>(R.id.tvTenantName)?.text = 
                    sessionManager.getFullName() ?: "Khách thuê"
                    
                view?.findViewById<TextView>(R.id.tvRoomInfo)?.text = if (tenPhong.isNotEmpty()) {
                    "$tenNhaTro - $tenPhong"
                } else {
                    "Chưa có phòng"
                }

                if (hopDong != null) {
                    try {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        view?.findViewById<TextView>(R.id.tvContractDate)?.text = 
                            "Ngày thuê: ${dateFormat.format(Date(hopDong.ngayBatDau))}"
                        view?.findViewById<TextView>(R.id.tvDepositAmount)?.text = 
                            "Tiền cọc: ${CurrencyHelper.format(hopDong.tienDatCoc.toDouble())}"
                    } catch (e: Exception) {
                        android.util.Log.e("TenantDashboard", "Error formatting contract", e)
                        view?.findViewById<TextView>(R.id.tvContractDate)?.text = "Chưa có hợp đồng"
                        view?.findViewById<TextView>(R.id.tvDepositAmount)?.text = ""
                    }
                } else {
                    view?.findViewById<TextView>(R.id.tvContractDate)?.text = "Chưa có hợp đồng"
                    view?.findViewById<TextView>(R.id.tvDepositAmount)?.text = ""
                }

                if (hoaDon != null) {
                    try {
                        view?.findViewById<TextView>(R.id.tvRentAmount)?.text = 
                            CurrencyHelper.format(hoaDon.tongTien.toDouble())
                        view?.findViewById<TextView>(R.id.tvRentStatus)?.text = 
                            if (hoaDon.trangThai == "da_thanh_toan") "✓ Đã đóng" else "✗ Chưa đóng"
                        view?.findViewById<TextView>(R.id.tvRentStatus)?.setTextColor(
                            if (hoaDon.trangThai == "da_thanh_toan") 
                                android.graphics.Color.parseColor("#4CAF50")
                            else 
                                android.graphics.Color.parseColor("#F44336")
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("TenantDashboard", "Error formatting invoice", e)
                        view?.findViewById<TextView>(R.id.tvRentAmount)?.text = "0đ"
                        view?.findViewById<TextView>(R.id.tvRentStatus)?.text = "Chưa có hóa đơn"
                    }
                } else {
                    view?.findViewById<TextView>(R.id.tvRentAmount)?.text = "0đ"
                    view?.findViewById<TextView>(R.id.tvRentStatus)?.text = "Chưa có hóa đơn"
                }

            } catch (e: Exception) {
                android.util.Log.e("TenantDashboard", "Error in loadDashboardData", e)
                android.widget.Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

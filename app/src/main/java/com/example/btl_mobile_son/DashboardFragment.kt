package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class DashboardFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("DashboardFragment", "onViewCreated started")
        
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
            android.util.Log.d("DashboardFragment", "DatabaseManager initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("DashboardFragment", "Error initializing DatabaseManager", e)
            e.printStackTrace()
            return
        }

        // Nút tạo dịch vụ
        view.findViewById<View>(R.id.btnCreateService)?.setOnClickListener {
            navigate(CreateServiceFragment())
        }

        // Grid quản lý - Hàng 1
        view.findViewById<View>(R.id.btnHouse)?.setOnClickListener {
            navigate(HouseListFragment())
        }

        view.findViewById<View>(R.id.btnRoom)?.setOnClickListener {
            navigate(RoomListFragment())
        }

        view.findViewById<View>(R.id.btnService)?.setOnClickListener {
            navigate(ServiceListFragment())
        }

        view.findViewById<View>(R.id.btnContract)?.setOnClickListener {
            navigate(ContractListFragment())
        }

        // Grid quản lý - Hàng 2
        view.findViewById<View>(R.id.btnUtility)?.setOnClickListener {
            navigate(UtilityListFragment())
        }

        view.findViewById<View>(R.id.btnDeposit)?.setOnClickListener {
            navigate(TransactionListFragment())
        }

        view.findViewById<View>(R.id.btnTenant)?.setOnClickListener {
            navigate(TenantListFragment())
        }

        view.findViewById<View>(R.id.btnInvoice)?.setOnClickListener {
            navigate(InvoiceListFragment())
        }

        // Grid quản lý - Hàng 3
        view.findViewById<View>(R.id.btnIncome)?.setOnClickListener {
            navigate(TransactionListFragment())
        }

        view.findViewById<View>(R.id.btnExpense)?.setOnClickListener {
            navigate(TransactionListFragment())
        }

        view.findViewById<View>(R.id.btnReservation)?.setOnClickListener {
            navigate(TransactionListFragment())
        }

        view.findViewById<View>(R.id.btnReport)?.setOnClickListener {
            navigate(ReportFragment())
        }

        // Header buttons
        view.findViewById<View>(R.id.btnMenu)?.setOnClickListener {
            Toast.makeText(context, "Menu", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnNotification)?.setOnClickListener {
            Toast.makeText(context, "Thông báo", Toast.LENGTH_SHORT).show()
        }

        // Load thống kê
        loadStatistics(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadStatistics(it) }
    }

    private fun loadStatistics(view: View) {
        val tvRoomStats = view.findViewById<TextView>(R.id.tvRoomStats)
        val tvTenantCount = view.findViewById<TextView>(R.id.tvTenantCount)
        val tvUnpaidInvoice = view.findViewById<TextView>(R.id.tvUnpaidInvoice)
        val tvMonthRevenue = view.findViewById<TextView>(R.id.tvMonthRevenue)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Thống kê phòng
                val tatCaPhong = dbManager.phongDao.layTatCa()
                val phongDaThue = tatCaPhong.count { it.trangThai == "da_thue" }
                val tongPhong = tatCaPhong.size

                // Thống kê khách thuê
                val soKhach = dbManager.khachThueDao.layTatCa().size

                // Hóa đơn chưa thanh toán
                val hoaDonChuaTT = dbManager.hoaDonDao.layTatCa().count { it.trangThai != "da_thanh_toan" }

                // Doanh thu tháng này
                val calendar = Calendar.getInstance()
                val thangHienTai = calendar.get(Calendar.MONTH) + 1
                val namHienTai = calendar.get(Calendar.YEAR)
                
                val doanhThuThang = dbManager.giaoDichDao.layTatCa()
                    .filter { giaoDich ->
                        if (giaoDich.loai != "thu") return@filter false
                        val calGD = Calendar.getInstance()
                        calGD.timeInMillis = giaoDich.ngayGiaoDich
                        calGD.get(Calendar.MONTH) + 1 == thangHienTai && calGD.get(Calendar.YEAR) == namHienTai
                    }
                    .sumOf { it.soTien }

                // Kiểm tra cảnh báo
                checkWarnings()

                withContext(Dispatchers.Main) {
                    tvRoomStats?.text = "$phongDaThue/$tongPhong"
                    tvTenantCount?.text = "$soKhach"
                    tvUnpaidInvoice?.text = "$hoaDonChuaTT"
                    tvMonthRevenue?.text = "${String.format("%,.0f", doanhThuThang.toDouble())}đ"
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardFragment", "Error loading statistics", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvRoomStats?.text = "0/0"
                    tvTenantCount?.text = "0"
                    tvUnpaidInvoice?.text = "0"
                    tvMonthRevenue?.text = "0đ"
                }
            }
        }
    }

    private suspend fun checkWarnings() {
        try {
            val warnings = mutableListOf<String>()
            
            // Kiểm tra hợp đồng sắp hết hạn (30 ngày)
            val hopDongSapHetHan = dbManager.hopDongDao.layTatCa().filter { hopDong ->
                if (hopDong.trangThai != "dang_thue") return@filter false
                val ngayHetHan = hopDong.ngayKetThuc
                val ngayHienTai = System.currentTimeMillis()
                val soNgayConLai = (ngayHetHan - ngayHienTai) / (1000 * 60 * 60 * 24)
                soNgayConLai in 1..30
            }
            if (hopDongSapHetHan.isNotEmpty()) {
                warnings.add("⚠ ${hopDongSapHetHan.size} hợp đồng sắp hết hạn")
            }

            // Kiểm tra hóa đơn quá hạn
            val hoaDonQuaHan = dbManager.hoaDonDao.layTatCa().count { 
                it.trangThai == "qua_han" 
            }
            if (hoaDonQuaHan > 0) {
                warnings.add("⚠ $hoaDonQuaHan hóa đơn quá hạn chưa thanh toán")
            }

            // Kiểm tra sự cố chưa xử lý
            val suCoChuaXuLy = dbManager.suCoDao.layTatCa().count { 
                it.trangThai == "chua_xu_ly" 
            }
            if (suCoChuaXuLy > 0) {
                warnings.add("⚠ $suCoChuaXuLy sự cố chưa xử lý")
            }

            // Hiển thị cảnh báo
            if (warnings.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, 
                        warnings.joinToString("\n"), 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardFragment", "Error checking warnings", e)
        }
    }

    private fun navigate(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

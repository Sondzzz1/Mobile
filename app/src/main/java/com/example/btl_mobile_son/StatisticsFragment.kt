package com.example.btl_mobile_son

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        loadStatistics(view)
    }

    private fun loadStatistics(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = Calendar.getInstance()
                val thangHienTai = calendar.get(Calendar.MONTH) + 1
                val namHienTai = calendar.get(Calendar.YEAR)

                // 1. Thống kê phòng
                val tatCaPhong = dbManager.phongDao.layTatCa()
                val phongDaThue = tatCaPhong.count { it.trangThai == "da_thue" }
                val phongTrong = tatCaPhong.size - phongDaThue
                val tyLeLayDay = if (tatCaPhong.isNotEmpty()) (phongDaThue * 100 / tatCaPhong.size) else 0

                // 2. Thống kê thu chi tháng này
                val giaoDichThangNay = dbManager.giaoDichDao.layTatCa().filter { gd ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = gd.ngayGiaoDich
                    cal.get(Calendar.MONTH) + 1 == thangHienTai && cal.get(Calendar.YEAR) == namHienTai
                }
                
                val tongThu = giaoDichThangNay.filter { it.loai == "thu" }.sumOf { it.soTien }
                val tongChi = giaoDichThangNay.filter { it.loai == "chi" }.sumOf { it.soTien }
                val loiNhuan = tongThu - tongChi

                // 3. Thống kê hóa đơn
                val tatCaHoaDon = dbManager.hoaDonDao.layTatCa()
                val hoaDonDaTT = tatCaHoaDon.count { it.trangThai == "da_thanh_toan" }
                val hoaDonChuaTT = tatCaHoaDon.count { it.trangThai == "chua_thanh_toan" }
                val tyLeThanhToan = if (tatCaHoaDon.isNotEmpty()) (hoaDonDaTT * 100 / tatCaHoaDon.size) else 0

                // 4. Thống kê hợp đồng
                val tatCaHopDong = dbManager.hopDongDao.layTatCa()
                val hopDongDangThue = tatCaHopDong.count { it.trangThai == "dang_thue" }
                val hopDongHetHan = tatCaHopDong.count { it.trangThai == "het_han" }

                withContext(Dispatchers.Main) {
                    // Hiển thị thống kê phòng
                    view.findViewById<TextView>(R.id.tvRoomOccupied)?.text = "$phongDaThue phòng"
                    view.findViewById<TextView>(R.id.tvRoomEmpty)?.text = "$phongTrong phòng"
                    view.findViewById<TextView>(R.id.tvRoomPercent)?.text = "$tyLeLayDay%"
                    view.findViewById<ProgressBar>(R.id.progressRoom)?.progress = tyLeLayDay

                    // Hiển thị thu chi
                    view.findViewById<TextView>(R.id.tvIncome)?.text = "${formatter.format(tongThu)}đ"
                    view.findViewById<TextView>(R.id.tvExpense)?.text = "${formatter.format(tongChi)}đ"
                    view.findViewById<TextView>(R.id.tvProfit)?.text = "${formatter.format(loiNhuan)}đ"
                    view.findViewById<TextView>(R.id.tvProfit)?.setTextColor(
                        if (loiNhuan >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                    )
                    
                    // Progress bar thu chi
                    val maxThuChi = maxOf(tongThu, tongChi, 1L)
                    view.findViewById<ProgressBar>(R.id.progressIncome)?.progress = ((tongThu * 100) / maxThuChi).toInt()
                    view.findViewById<ProgressBar>(R.id.progressExpense)?.progress = ((tongChi * 100) / maxThuChi).toInt()

                    // Hiển thị hóa đơn
                    view.findViewById<TextView>(R.id.tvInvoicePaid)?.text = "$hoaDonDaTT hóa đơn"
                    view.findViewById<TextView>(R.id.tvInvoiceUnpaid)?.text = "$hoaDonChuaTT hóa đơn"
                    view.findViewById<TextView>(R.id.tvInvoicePercent)?.text = "$tyLeThanhToan%"
                    view.findViewById<ProgressBar>(R.id.progressInvoice)?.progress = tyLeThanhToan

                    // Hiển thị hợp đồng
                    view.findViewById<TextView>(R.id.tvContractActive)?.text = "$hopDongDangThue hợp đồng"
                    view.findViewById<TextView>(R.id.tvContractExpired)?.text = "$hopDongHetHan hợp đồng"
                    val tyLeHopDong = if (tatCaHopDong.isNotEmpty()) (hopDongDangThue * 100 / tatCaHopDong.size) else 0
                    view.findViewById<ProgressBar>(R.id.progressContract)?.progress = tyLeHopDong
                }
            } catch (e: Exception) {
                android.util.Log.e("StatisticsFragment", "Error loading statistics", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải thống kê", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

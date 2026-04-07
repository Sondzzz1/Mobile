package com.example.btl_mobile_son

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.HoaDonAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class InvoiceListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: HoaDonAdapter
    private var danhSachDayDu = listOf<com.example.btl_mobile_son.data.model.HoaDon>()
    private var statusFilter = "all"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invoice_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("InvoiceListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val rvInvoiceList = view.findViewById<RecyclerView>(R.id.rvInvoiceList)
        adapter = HoaDonAdapter(
            danhSach = emptyList(),
            onItemClick = { hoaDon ->
                hienThiChiTietHoaDon(hoaDon, view)
            },
            onItemLongClick = { hoaDon ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa hóa đơn")
                    .setMessage("Xóa hóa đơn tháng ${hoaDon.thang}/${hoaDon.nam}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.hoaDonDao.xoa(hoaDon.maHoaDon)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                taiDuLieu(view)
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null).show()
            }
        )
        rvInvoiceList.layoutManager = LinearLayoutManager(requireContext())
        rvInvoiceList.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnCreateInvoice).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateInvoiceFragment())
                .addToBackStack(null).commit()
        }

        // Status filter spinner
        val spinnerStatus = view.findViewById<Spinner>(R.id.spinnerStatusFilter)
        val statusOptions = arrayOf("Tất cả", "Chưa TT", "Đã TT", "Quá hạn")
        spinnerStatus.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, statusOptions)
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statusFilter = when (position) {
                    1 -> "chua_thanh_toan"
                    2 -> "da_thanh_toan"
                    3 -> "qua_han"
                    else -> "all"
                }
                applyFilters(view)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Search
        view.findViewById<EditText>(R.id.etSearchInvoice)?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilters(view)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        taiDuLieu(view)
    }

    private fun applyFilters(view: View?) {
        val searchView = view?.findViewById<EditText>(R.id.etSearchInvoice)
        val tuKhoa = searchView?.text?.toString()?.trim()?.lowercase() ?: ""
        
        CoroutineScope(Dispatchers.IO).launch {
            var filtered = danhSachDayDu
            
            // Filter by status
            if (statusFilter != "all") {
                filtered = filtered.filter { it.trangThai == statusFilter }
            }
            
            // Filter by search keyword
            if (tuKhoa.isNotEmpty()) {
                filtered = filtered.filter { hoaDon ->
                    val hopDong = dbManager.hopDongDao.layTheoMa(hoaDon.maHopDong)
                    val khach = hopDong?.let { dbManager.khachThueDao.layTheoMa(it.maKhach) }
                    val phong = hopDong?.let { dbManager.phongDao.layTheoMa(it.maPhong) }
                    
                    hoaDon.maHoaDon.toString().contains(tuKhoa) ||
                    khach?.hoTen?.lowercase()?.contains(tuKhoa) == true ||
                    phong?.tenPhong?.lowercase()?.contains(tuKhoa) == true ||
                    "${hoaDon.thang}/${hoaDon.nam}".contains(tuKhoa)
                }
            }
            
            withContext(Dispatchers.Main) {
                adapter.capNhatDanhSach(filtered)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { taiDuLieu(it) }
    }

    private fun taiDuLieu(view: View) {
        val cal = Calendar.getInstance()
        val thang = cal.get(Calendar.MONTH) + 1
        val nam = cal.get(Calendar.YEAR)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val danhSach = dbManager.hoaDonDao.layTatCa()
                val tongChuaTT = dbManager.hoaDonDao.tinhTongChuaThanhToan()
                withContext(Dispatchers.Main) {
                    danhSachDayDu = danhSach
                    adapter.capNhatDanhSach(danhSach)
                    view.findViewById<TextView>(R.id.tvTotalInvoices)?.text = "Tổng tiền (${danhSach.size} hóa đơn)"
                    view.findViewById<TextView>(R.id.tvTotalAmount)?.text = "${String.format("%,.0f", tongChuaTT.toDouble())} đ"
                }
            } catch (e: Exception) {
                android.util.Log.e("InvoiceListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun hienThiChiTietHoaDon(hoaDon: com.example.btl_mobile_son.data.model.HoaDon, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            // Lấy thông tin chi tiết
            val hopDong = dbManager.hopDongDao.layTheoMa(hoaDon.maHopDong)
            val khach = hopDong?.let { dbManager.khachThueDao.layTheoMa(it.maKhach) }
            val phong = hopDong?.let { dbManager.phongDao.layTheoMa(it.maPhong) }
            val nha = phong?.let { dbManager.nhaTroDao.layTheoMa(it.maNha) }
            
            // Lấy chi tiết điện nước
            val chiSoDienNuoc = dbManager.chiSoDienNuocDao.layTheoThangNam(hoaDon.thang, hoaDon.nam)
                .filter { it.maPhong == phong?.maPhong }
            
            withContext(Dispatchers.Main) {
                val statusText = when (hoaDon.trangThai) {
                    "da_thanh_toan" -> "✓ Đã thanh toán"
                    "thanh_toan_mot_phan" -> "⚠ Thanh toán một phần"
                    "qua_han" -> "✗ Quá hạn"
                    else -> "○ Chưa thanh toán"
                }
                
                // Tạo nội dung chi tiết
                val chiTiet = StringBuilder()
                chiTiet.append("━━━━━━━━━━━━━━━━━━━━━━\n")
                chiTiet.append("📍 Nhà: ${nha?.tenNha ?: "--"}\n")
                chiTiet.append("🚪 Phòng: ${phong?.tenPhong ?: "--"}\n")
                chiTiet.append("👤 Khách: ${khach?.hoTen ?: "--"}\n")
                chiTiet.append("📅 Kỳ: Tháng ${hoaDon.thang}/${hoaDon.nam}\n")
                chiTiet.append("━━━━━━━━━━━━━━━━━━━━━━\n\n")
                
                chiTiet.append("CHI TIẾT:\n\n")
                chiTiet.append("🏠 Tiền phòng:\n")
                chiTiet.append("   ${String.format("%,d", hoaDon.tienPhong)} đ\n\n")
                
                if (hoaDon.tongTienDichVu > 0) {
                    chiTiet.append("⚡ Dịch vụ:\n")
                    for (chiSo in chiSoDienNuoc) {
                        val loaiText = if (chiSo.loai == "dien") "Điện" else "Nước"
                        val soTieuThu = if (chiSo.soTieuThu > 0) chiSo.soTieuThu else (chiSo.chiSoMoi - chiSo.chiSoCu)
                        val tien = soTieuThu * chiSo.donGia
                        chiTiet.append("   • $loaiText: ${chiSo.chiSoCu} → ${chiSo.chiSoMoi} (${soTieuThu})\n")
                        chiTiet.append("     ${String.format("%,d", soTieuThu)} x ${String.format("%,d", chiSo.donGia)} = ${String.format("%,d", tien)} đ\n")
                    }
                    chiTiet.append("   Tổng dịch vụ: ${String.format("%,d", hoaDon.tongTienDichVu)} đ\n\n")
                }
                
                if (hoaDon.giamGia > 0) {
                    chiTiet.append("🎁 Giảm giá:\n")
                    chiTiet.append("   -${String.format("%,d", hoaDon.giamGia)} đ\n\n")
                }
                
                chiTiet.append("━━━━━━━━━━━━━━━━━━━━━━\n")
                chiTiet.append("💰 TỔNG CỘNG:\n")
                chiTiet.append("   ${String.format("%,d", hoaDon.tongTien)} đ\n")
                chiTiet.append("━━━━━━━━━━━━━━━━━━━━━━\n\n")
                chiTiet.append("Trạng thái: $statusText")
                
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("HÓA ĐƠN #${hoaDon.maHoaDon}")
                    .setMessage(chiTiet.toString())
                
                if (hoaDon.trangThai != "da_thanh_toan") {
                    dialog.setPositiveButton("💳 Thanh toán") { _, _ ->
                        thanhToanHoaDon(hoaDon, view)
                    }
                    dialog.setNegativeButton("Đóng", null)
                } else {
                    dialog.setPositiveButton("Đóng", null)
                }
                
                dialog.show()
            }
        }
    }
    
    private fun thanhToanHoaDon(hoaDon: com.example.btl_mobile_son.data.model.HoaDon, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            // Đánh dấu hóa đơn đã thanh toán
            dbManager.hoaDonDao.danhDauDaThanhToan(hoaDon.maHoaDon)
            
            // Tạo giao dịch thu tương ứng
            val hopDong = dbManager.hopDongDao.layTheoMa(hoaDon.maHopDong)
            val khach = hopDong?.let { dbManager.khachThueDao.layTheoMa(it.maKhach) }
            val phong = hopDong?.let { dbManager.phongDao.layTheoMa(it.maPhong) }
            
            dbManager.giaoDichDao.them(
                com.example.btl_mobile_son.data.model.GiaoDich(
                    loai = "thu",
                    maPhong = hopDong?.maPhong,
                    soTien = hoaDon.tongTien,
                    danhMuc = "Tiền thuê phòng",
                    ngayGiaoDich = System.currentTimeMillis(),
                    noiDung = "Thu tiền phòng ${phong?.tenPhong ?: ""} tháng ${hoaDon.thang}/${hoaDon.nam}",
                    tenNguoi = khach?.hoTen ?: "",
                    phuongThucThanhToan = "tien_mat"
                )
            )
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "✓ Đã thanh toán ${String.format("%,d", hoaDon.tongTien)} đ\nĐã tạo giao dịch thu",
                    Toast.LENGTH_LONG
                ).show()
                taiDuLieu(view)
            }
        }
    }
}

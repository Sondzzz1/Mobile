package com.example.btl_mobile_son

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.HoaDon
import com.example.btl_mobile_son.data.model.HopDong
import com.example.btl_mobile_son.data.model.NhaTro
import com.example.btl_mobile_son.data.model.Phong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class CreateInvoiceFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var danhSachNha = listOf<NhaTro>()
    private var danhSachPhong = listOf<Phong>()
    private var danhSachHopDong = listOf<HopDong>()
    private var thangChon: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var namChon: Int = Calendar.getInstance().get(Calendar.YEAR)

    // Dữ liệu tính toán
    private var tienPhong = 0L
    private var tienDichVu = 0L
    private var tenKhach = "--"
    private var tenPhong = "--"
    private var tenNha = "--"

    private fun formatMoney(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${formatter.format(amount)}đ"
    }
    
    private suspend fun tinhTienDichVu(maPhong: Long, thang: Int, nam: Int): Long {
        var tongTienDichVu = 0L
        
        // 1. Tính tiền điện nước (từ bảng chi_so_dien_nuoc)
        val chiSoDienNuoc = dbManager.chiSoDienNuocDao.layTheoThangNam(thang, nam)
            .filter { it.maPhong == maPhong }

        android.util.Log.d("CreateInvoice", "Tìm thấy ${chiSoDienNuoc.size} chỉ số điện nước cho phòng $maPhong tháng $thang/$nam")
        
        for (chiSo in chiSoDienNuoc) {
            // Tính lại soTieuThu nếu = 0 (dữ liệu cũ)
            val soTieuThu = if (chiSo.soTieuThu > 0) chiSo.soTieuThu else (chiSo.chiSoMoi - chiSo.chiSoCu)
            val tien = soTieuThu * chiSo.donGia
            tongTienDichVu += tien
            android.util.Log.d("CreateInvoice", "  ${chiSo.loai}: ${chiSo.chiSoCu} → ${chiSo.chiSoMoi} (${soTieuThu}) x ${chiSo.donGia} = ${tien}đ")
        }
        
        // 2. Tính tiền các dịch vụ khác (từ bảng phong_dich_vu)
        val phongDichVu = dbManager.phongDichVuDao.layTheoPhong(maPhong)
        android.util.Log.d("CreateInvoice", "Tìm thấy ${phongDichVu.size} dịch vụ khác cho phòng $maPhong")
        
        for (pdv in phongDichVu) {
            val dichVu = dbManager.dichVuDao.layTheoMa(pdv.maDichVu)
            if (dichVu != null && dichVu.isActive) {
                // Sử dụng đơn giá riêng nếu có, không thì dùng đơn giá mặc định
                val donGia = pdv.donGiaRieng?.toLong() ?: dichVu.donGia
                
                // Tính tiền theo cách tính của dịch vụ
                val tien = when (dichVu.cachTinh) {
                    "theo_phong", "theo_thang" -> donGia // Tính 1 lần/tháng
                    "theo_nguoi" -> {
                        // Đếm số người trong phòng (từ hợp đồng hiện tại)
                        val hopDong = dbManager.hopDongDao.layHopDongDangThue(maPhong)
                        val soNguoi = hopDong?.let { 
                            dbManager.hopDongThanhVienDao.layNguoiDangOTheoHopDong(it.maHopDong).size 
                        } ?: 1
                        donGia * soNguoi
                    }
                    "mot_lan" -> 0L // Không tính vào hóa đơn hàng tháng
                    else -> donGia
                }
                
                tongTienDichVu += tien
                android.util.Log.d("CreateInvoice", "  ${dichVu.tenDichVu}: ${donGia}đ (${dichVu.cachTinh}) = ${tien}đ")
            }
        }
        
        android.util.Log.d("CreateInvoice", "Tổng tiền dịch vụ: ${tongTienDichVu}đ")
        return tongTienDichVu
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_invoice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("CreateInvoiceFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
        val spinnerContract = view.findViewById<Spinner>(R.id.spinnerContract)
        val etMonth = view.findViewById<EditText>(R.id.etMonth)
        val btnPreview = view.findViewById<Button>(R.id.btnPreview)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val layoutPreview = view.findViewById<LinearLayout>(R.id.layoutPreview)
        val cbDiscount = view.findViewById<CheckBox>(R.id.cbDiscount)
        val layoutDiscount = view.findViewById<LinearLayout>(R.id.layoutDiscount)
        val etDiscount = view.findViewById<EditText>(R.id.etDiscount)

        // Preview views
        val tvPreviewRoom = view.findViewById<TextView>(R.id.tvPreviewRoom)
        val tvPreviewTenant = view.findViewById<TextView>(R.id.tvPreviewTenant)
        val tvPreviewPeriod = view.findViewById<TextView>(R.id.tvPreviewPeriod)
        val tvPreviewRoomPrice = view.findViewById<TextView>(R.id.tvPreviewRoomPrice)
        val tvPreviewServicePrice = view.findViewById<TextView>(R.id.tvPreviewServicePrice)
        val tvPreviewDiscount = view.findViewById<TextView>(R.id.tvPreviewDiscount)
        val tvPreviewTotal = view.findViewById<TextView>(R.id.tvPreviewTotal)

        // Set tháng hiện tại
        etMonth.setText("$thangChon/$namChon")

        // Chọn tháng
        etMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, _ ->
                    thangChon = month + 1
                    namChon = year
                    etMonth.setText("$thangChon/$namChon")
                },
                namChon,
                thangChon - 1,
                1
            ).show()
        }

        // Load danh sách nhà
        CoroutineScope(Dispatchers.IO).launch {
            danhSachNha = dbManager.nhaTroDao.layTatCa()
            
            withContext(Dispatchers.Main) {
                if (danhSachNha.isEmpty()) {
                    Toast.makeText(context, "Chưa có nhà trọ nào!", Toast.LENGTH_SHORT).show()
                    return@withContext
                }

                spinnerHouse.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    danhSachNha.map { it.tenNha }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                // Listener chọn nhà → load phòng
                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        val nha = danhSachNha[pos]
                        
                        CoroutineScope(Dispatchers.IO).launch {
                            danhSachPhong = dbManager.phongDao.layTheoNha(nha.maNha)
                            
                            withContext(Dispatchers.Main) {
                                if (danhSachPhong.isEmpty()) {
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        listOf("Không có phòng")
                                    )
                                } else {
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        danhSachPhong.map { it.tenPhong }
                                    ).apply {
                                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    }
                                }
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        // Listener chọn phòng → load hợp đồng
        spinnerRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (danhSachPhong.isEmpty()) return
                
                val phong = danhSachPhong[pos]
                
                CoroutineScope(Dispatchers.IO).launch {
                    danhSachHopDong = dbManager.hopDongDao.layTatCa()
                        .filter { it.maPhong == phong.maPhong && it.trangThai == "dang_thue" }
                    
                    withContext(Dispatchers.Main) {
                        if (danhSachHopDong.isEmpty()) {
                            spinnerContract.adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                listOf("Phòng chưa có hợp đồng đang thuê")
                            )
                        } else {
                            spinnerContract.adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                danhSachHopDong.map { "HĐ #${it.maHopDong} - ${formatMoney(it.giaThueThang)}/tháng" }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Load danh sách hợp đồng đang thuê (VĐ7: Chỉ chọn hợp đồng)
        /*
        CoroutineScope(Dispatchers.IO).launch {
            danhSachHopDong = dbManager.hopDongDao.layTatCa()
                .filter { it.trangThai == "dang_thue" }

            withContext(Dispatchers.Main) {
                if (danhSachHopDong.isEmpty()) {
                    spinnerContract.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        listOf("Không có hợp đồng đang thuê")
                    )
                } else {
                    spinnerContract.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        danhSachHopDong.map { "HĐ #${it.maHopDong} - ${formatMoney(it.giaThueThang)}/tháng" }
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    
                    // Listener cho hợp đồng - tự động hiển thị thông tin phòng và nhà
                    spinnerContract.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                            val hopDong = danhSachHopDong[pos]
                            
                            CoroutineScope(Dispatchers.IO).launch {
                                val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
                                val nha = phong?.let { dbManager.nhaTroDao.layTheoMa(it.maNha) }
                                val khach = dbManager.khachThueDao.layTheoMa(hopDong.maKhach)
                                
                                withContext(Dispatchers.Main) {
                                    tenPhong = phong?.tenPhong ?: "--"
                                    tenNha = nha?.tenNha ?: "--"
                                    tenKhach = khach?.hoTen ?: "--"
                                }
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
        }
        */

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Xử lý checkbox giảm giá
        cbDiscount.setOnCheckedChangeListener { _, isChecked ->
            layoutDiscount.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                etDiscount.setText("")
            }
        }

        // Nút xem trước
        btnPreview.setOnClickListener {
            if (danhSachHopDong.isEmpty()) {
                Toast.makeText(context, "Không có hợp đồng đang thuê!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hopDong = danhSachHopDong[spinnerContract.selectedItemPosition]

            CoroutineScope(Dispatchers.IO).launch {
                // Lấy thông tin phòng
                val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
                
                // Lấy thông tin khách thuê
                val khachThue = dbManager.khachThueDao.layTheoMa(hopDong.maKhach)

                // Tính tiền điện nước
                val tongTienDichVu = tinhTienDichVu(phong?.maPhong ?: 0, thangChon, namChon)

                val giamGia = etDiscount.text.toString().toLongOrNull() ?: 0L
                val tongTien = hopDong.giaThueThang + tongTienDichVu - giamGia

                withContext(Dispatchers.Main) {
                    tvPreviewRoom.text = phong?.tenPhong ?: "--"
                    tvPreviewTenant.text = khachThue?.hoTen ?: "--"
                    tvPreviewPeriod.text = "$thangChon/$namChon"
                    tvPreviewRoomPrice.text = formatMoney(hopDong.giaThueThang)
                    tvPreviewServicePrice.text = formatMoney(tongTienDichVu)
                    tvPreviewDiscount.text = formatMoney(giamGia)
                    tvPreviewTotal.text = formatMoney(tongTien)

                    layoutPreview.visibility = View.VISIBLE
                    btnSubmit.visibility = View.VISIBLE
                }
            }
        }

        // VĐ7, VĐ8: Nút lưu hóa đơn
        btnSubmit.setOnClickListener {
            if (danhSachHopDong.isEmpty()) {
                Toast.makeText(context, "⚠️ Không có hợp đồng đang thuê!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hopDong = danhSachHopDong[spinnerContract.selectedItemPosition]

            CoroutineScope(Dispatchers.IO).launch {
                // VĐ8: Kiểm tra trùng hóa đơn (maHopDong + thang + nam)
                val daTonTai = dbManager.hoaDonDao.kiemTraTrungHoaDon(
                    hopDong.maHopDong,
                    thangChon,
                    namChon
                )
                
                if (daTonTai) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "⚠️ Đã có hóa đơn cho hợp đồng #${hopDong.maHopDong} trong tháng $thangChon/$namChon!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Lấy thông tin phòng
                val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
                
                // Tính tiền điện nước
                val tongTienDichVu = tinhTienDichVu(phong?.maPhong ?: 0, thangChon, namChon)

                val giamGia = etDiscount.text.toString().toLongOrNull() ?: 0L
                val tongTien = hopDong.giaThueThang + tongTienDichVu - giamGia

                val hoaDon = HoaDon(
                    maHopDong = hopDong.maHopDong,
                    thang = thangChon,
                    nam = namChon,
                    tienPhong = hopDong.giaThueThang,
                    tongTienDichVu = tongTienDichVu,
                    giamGia = giamGia,
                    tongTien = tongTien,
                    tienDaThanhToan = 0L,
                    trangThai = "chua_thanh_toan"
                )
                
                dbManager.hoaDonDao.them(hoaDon)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "✓ Đã tạo hóa đơn tháng $thangChon/$namChon\n" +
                        "Phòng: ${phong?.tenPhong ?: "--"}\n" +
                        "Tiền phòng: ${formatMoney(hopDong.giaThueThang)}\n" +
                        "Điện nước: ${formatMoney(tongTienDichVu)}\n" +
                        "Tổng: ${formatMoney(tongTien)}",
                        Toast.LENGTH_LONG
                    ).show()
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}

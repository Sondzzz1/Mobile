package com.example.btl_mobile_son

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.KhachThue
import com.example.btl_mobile_son.data.model.NhaTro
import com.example.btl_mobile_son.data.model.Phong
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateTenantFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maKhach: Long = -1L
    private var danhSachNha = listOf<NhaTro>()
    private var danhSachPhong = listOf<Phong>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_tenant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("CreateTenantFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }
        maKhach = arguments?.getLong("maKhach", -1L) ?: -1L

        // Nếu đang chỉnh sửa khách thuê, ẩn spinners và chỉ cho sửa thông tin cá nhân
        if (maKhach > 0) {
            view.findViewById<Spinner>(R.id.spinnerHouse)?.visibility = View.GONE
            view.findViewById<Spinner>(R.id.spinnerRoom)?.visibility = View.GONE
        }

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etIdCard = view.findViewById<EditText>(R.id.etIdCard)
        val etDob = view.findViewById<EditText>(R.id.etDob)
        val etIssueDate = view.findViewById<EditText>(R.id.etIssueDate)
        val etIssuePlace = view.findViewById<EditText>(R.id.etIssuePlace)
        val etWorkplace = view.findViewById<EditText>(R.id.etWorkplace)
        val etProvince = view.findViewById<EditText>(R.id.etProvince)
        val etDistrict = view.findViewById<EditText>(R.id.etDistrict)
        val etWard = view.findViewById<EditText>(R.id.etWard)
        val etDetailedAddress = view.findViewById<EditText>(R.id.etDetailedAddress)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        
        var ngaySinh: Long? = null
        var ngayCap: Long? = null
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Date picker cho ngày sinh
        etDob.setOnClickListener {
            val cal = Calendar.getInstance()
            if (ngaySinh != null) cal.timeInMillis = ngaySinh!!
            android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                ngaySinh = cal.timeInMillis
                etDob.setText(sdf.format(Date(ngaySinh!!)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        // Date picker cho ngày cấp
        etIssueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            if (ngayCap != null) cal.timeInMillis = ngayCap!!
            android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                ngayCap = cal.timeInMillis
                etIssueDate.setText(sdf.format(Date(ngayCap!!)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Load danh sách nhà
        CoroutineScope(Dispatchers.IO).launch {
            try {
                danhSachNha = dbManager.nhaTroDao.layTatCa()
            } catch (e: Exception) {
                android.util.Log.e("CreateTenantFragment", "Error loading houses", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải danh sách nhà trọ: ${e.message}", Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                }
                return@launch
            }
            
            withContext(Dispatchers.Main) {
                if (danhSachNha.isEmpty()) {
                    Toast.makeText(context, "Chưa có nhà trọ nào. Vui lòng tạo nhà trọ trước!", Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                    return@withContext
                }

                try {
                    spinnerHouse.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        danhSachNha.map { it.tenNha }
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CreateTenantFragment", "Error setting house adapter", e)
                    Toast.makeText(context, "Lỗi hiển thị danh sách nhà: ${e.message}", Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                    return@withContext
                }

                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val allPhong = dbManager.phongDao.layTheoNha(danhSachNha[pos].maNha)
                            
                            // Lấy số người đang ở trong mỗi phòng via HopDongThanhVien
                            val phongVoiSoNguoi = allPhong.map { phong ->
                                val hopDong = dbManager.hopDongDao.layHopDongDangThue(phong.maPhong)
                                val soNguoiDangO = hopDong?.let { dbManager.hopDongThanhVienDao.demNguoiDangO(it.maHopDong) } ?: 0
                                Pair(phong, soNguoiDangO)
                            }
                            
                            // Lọc phòng: không hiển thị phòng đã đủ người hoặc đã đặt cọc (khi thêm mới)
                            danhSachPhong = if (maKhach > 0) {
                                // Khi edit: hiển thị tất cả phòng
                                allPhong
                            } else {
                                // Khi thêm mới: chỉ hiển thị phòng chưa đủ người và chưa đặt cọc
                                phongVoiSoNguoi
                                    .filter { (phong, soNguoi) ->
                                        phong.trangThai != "dat_coc" && soNguoi < phong.soNguoiToiDa
                                    }
                                    .map { it.first }
                            }
                            
                            withContext(Dispatchers.Main) {
                                try {
                                    if (danhSachPhong.isEmpty()) {
                                        spinnerRoom.adapter = ArrayAdapter(
                                            requireContext(),
                                            android.R.layout.simple_spinner_item,
                                            listOf("Không có phòng trống")
                                        )
                                    } else {
                                        // Hiển thị phòng với thông tin số người
                                        val roomLabels = danhSachPhong.map { phong ->
                                            val soNguoiDangO = phongVoiSoNguoi
                                                .find { it.first.maPhong == phong.maPhong }?.second ?: 0
                                            val status = when {
                                                phong.trangThai == "dat_coc" -> " (Đã đặt cọc)"
                                                phong.trangThai == "da_thue" && soNguoiDangO >= phong.soNguoiToiDa -> " (Đã đủ ${phong.soNguoiToiDa} người)"
                                                phong.trangThai == "da_thue" -> " (${soNguoiDangO}/${phong.soNguoiToiDa} người)"
                                                else -> " (Trống)"
                                            }
                                            "${phong.tenPhong} - ${phong.giaCoBan.toLong()}đ$status"
                                        }
                                        
                                        spinnerRoom.adapter = ArrayAdapter(
                                            requireContext(),
                                            android.R.layout.simple_spinner_item,
                                            roomLabels
                                        ).apply {
                                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("CreateTenantFragment", "Error setting room adapter", e)
                                    Toast.makeText(context, "Lỗi hiển thị danh sách phòng: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            } catch (e: Exception) {
                                android.util.Log.e("CreateTenantFragment", "Error loading rooms", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Lỗi tải danh sách phòng: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Nếu là chỉnh sửa
                if (maKhach > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val khach = dbManager.khachThueDao.layTheoMa(maKhach)
                        withContext(Dispatchers.Main) {
                            khach?.let {
                                etPhone.setText(it.soDienThoai)
                                etFullName.setText(it.hoTen)
                                etEmail.setText(it.email)
                                etIdCard.setText(it.soCmnd)
                                etWorkplace.setText(it.noiLamViec)
                                etIssuePlace.setText(it.noiCap)
                                etProvince.setText(it.tinhThanh)
                                etDistrict.setText(it.quanHuyen)
                                etWard.setText(it.xaPhuong)
                                etDetailedAddress.setText(it.diaChiChiTiet)
                                etNote.setText(it.ghiChu)
                                
                                if (it.ngaySinh != null) {
                                    ngaySinh = it.ngaySinh
                                    etDob.setText(sdf.format(Date(it.ngaySinh)))
                                }
                                if (it.ngayCap != null) {
                                    ngayCap = it.ngayCap
                                    etIssueDate.setText(sdf.format(Date(it.ngayCap)))
                                }
                            }
                        }
                    }
                }
            }
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val hoTen = etFullName.text.toString().trim()
            val sdt = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val cmnd = etIdCard.text.toString().trim()

            // Validation
            if (danhSachNha.isEmpty()) {
                Toast.makeText(context, "Chưa có nhà trọ nào!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (danhSachPhong.isEmpty()) {
                Toast.makeText(context, "Chưa có phòng nào trong nhà này!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationHelper.isNotEmpty(hoTen)) {
                Toast.makeText(context, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sdt.isNotEmpty() && !ValidationHelper.isValidPhoneNumber(sdt)) {
                Toast.makeText(context, ValidationHelper.getPhoneErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && !ValidationHelper.isValidEmail(email)) {
                Toast.makeText(context, ValidationHelper.getEmailErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cmnd.isNotEmpty() && !ValidationHelper.isValidCMND(cmnd)) {
                Toast.makeText(context, ValidationHelper.getCMNDErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phong = danhSachPhong[spinnerRoom.selectedItemPosition]
            
            // Kiểm tra số người đang ở trong phòng
            CoroutineScope(Dispatchers.IO).launch {
                // Kiểm tra phòng đã đặt cọc chưa
                if (phong.trangThai == "dat_coc") {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "⚠️ Phòng ${phong.tenPhong} đã được đặt cọc!\n" +
                            "Không thể thêm người thuê.\n" +
                            "Vui lòng chọn phòng khác.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                
                val hopDong = dbManager.hopDongDao.layHopDongDangThue(phong.maPhong)
                val soNguoiDangO = hopDong?.let { dbManager.hopDongThanhVienDao.demNguoiDangO(it.maHopDong) } ?: 0
                
                // Kiểm tra phòng đã đủ người chưa (khi thêm mới)
                if (maKhach <= 0 && soNguoiDangO >= phong.soNguoiToiDa) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "⚠️ Phòng ${phong.tenPhong} đã đủ người!\n" +
                            "Hiện có: ${soNguoiDangO}/${phong.soNguoiToiDa} người\n" +
                            "Vui lòng chọn phòng khác.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                
                withContext(Dispatchers.Main) {
                    // Nếu là chỉnh sửa
                    if (maKhach > 0) {
                        // KHÔNG CHO đổi phòng qua chức năng "Sửa khách thuê"
                        // Chỉ cho sửa thông tin cá nhân
                        val khach = KhachThue(
                            maKhach = maKhach,
                            hoTen = hoTen,
                            soDienThoai = sdt,
                            email = email,
                            soCmnd = cmnd,
                            ngaySinh = ngaySinh,
                            ngayCap = ngayCap,
                            noiCap = etIssuePlace.text.toString().trim(),
                            noiLamViec = etWorkplace.text.toString().trim(),
                            tinhThanh = etProvince.text.toString().trim(),
                            quanHuyen = etDistrict.text.toString().trim(),
                            xaPhuong = etWard.text.toString().trim(),
                            diaChiChiTiet = etDetailedAddress.text.toString().trim(),
                            ghiChu = etNote.text.toString().trim()
                        )
                        
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.khachThueDao.capNhat(khach)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "✓ Đã cập nhật thông tin khách thuê\n" +
                                    "ℹ️ Lưu ý: Không thể đổi phòng qua chức năng này.\n" +
                                    "Để chuyển phòng, vui lòng kết thúc hợp đồng cũ và tạo hợp đồng mới.",
                                    Toast.LENGTH_LONG
                                ).show()
                                requireActivity().onBackPressed()
                            }
                        }
                        return@withContext
                    }
                    
                    // Nếu là thêm mới
                    val khach = KhachThue(
                        maKhach = 0,
                        hoTen = hoTen,
                        soDienThoai = sdt,
                        email = email,
                        soCmnd = cmnd,
                        ngaySinh = ngaySinh,
                        ngayCap = ngayCap,
                        noiCap = etIssuePlace.text.toString().trim(),
                        noiLamViec = etWorkplace.text.toString().trim(),
                        tinhThanh = etProvince.text.toString().trim(),
                        quanHuyen = etDistrict.text.toString().trim(),
                        xaPhuong = etWard.text.toString().trim(),
                        diaChiChiTiet = etDetailedAddress.text.toString().trim(),
                        ghiChu = etNote.text.toString().trim()
                    )
                    
                    // Kiểm tra phòng trống hay đã có người
                    if (phong.trangThai == "trong" && soNguoiDangO == 0) {
                        // Phòng trống - BẮT BUỘC tạo hợp đồng
                        showCreateContractDialog(khach, phong)
                    } else if (soNguoiDangO < phong.soNguoiToiDa) {
                        // Phòng đã có người nhưng chưa đầy - thêm vào HopDongThanhVien
                        showAddRoommateDialog(khach, phong, soNguoiDangO)
                    } else {
                        // Phòng đã đầy
                        Toast.makeText(
                            context, 
                            "⚠️ Phòng đã đầy (${soNguoiDangO}/${phong.soNguoiToiDa} người)", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun showAddRoommateDialog(khach: KhachThue, phong: Phong, soNguoiDangO: Int) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Thêm người ở ghép")
            .setMessage(
                "Phòng ${phong.tenPhong} đang có $soNguoiDangO người.\n" +
                "Tối đa: ${phong.soNguoiToiDa} người.\n\n" +
                "Bạn có muốn thêm ${khach.hoTen} vào phòng này không?\n\n" +
                "⚠️ Lưu ý:\n" +
                "• Người ở ghép sẽ được thêm vào hợp đồng hiện tại\n" +
                "• Vai trò: Thành viên (không phải người đại diện)\n" +
                "• 1 phòng chỉ có 1 hợp đồng đang hiệu lực"
            )
            .setPositiveButton("Thêm ở ghép") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    // 1. Lưu khách thuê
                    val maKhachMoi = dbManager.khachThueDao.them(khach)
                    
                    // 2. Lấy hợp đồng đang active của phòng
                    val hopDongHienTai = dbManager.hopDongDao.layHopDongDangThue(phong.maPhong)
                    
                    if (hopDongHienTai == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "⚠️ Lỗi: Phòng chưa có hợp đồng đang hiệu lực!\n" +
                                "Vui lòng tạo hợp đồng trước.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                    
                    // 3. Thêm vào HopDongThanhVien với vai trò "thanh_vien"
                    val thanhVien = com.example.btl_mobile_son.data.model.HopDongThanhVien(
                        maHopDong = hopDongHienTai.maHopDong,
                        maKhach = maKhachMoi,
                        vaiTro = "thanh_vien",
                        ngayVaoO = System.currentTimeMillis(),
                        trangThai = "dang_o",
                        ghiChu = "Người ở ghép"
                    )
                    dbManager.hopDongThanhVienDao.them(thanhVien)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "✓ Đã thêm ${khach.hoTen} vào phòng ${phong.tenPhong}\n" +
                            "✓ Vai trò: Thành viên ở ghép\n" +
                            "✓ Hợp đồng: #${hopDongHienTai.maHopDong}",
                            Toast.LENGTH_LONG
                        ).show()
                        requireActivity().onBackPressed()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showCreateContractDialog(khach: KhachThue, phong: Phong, isRoommate: Boolean = false) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_contract, null)
        
        // Các view trong dialog
        val tvTenantName = dialogView.findViewById<TextView>(R.id.tvTenantName)
        val tvTenantPhone = dialogView.findViewById<TextView>(R.id.tvTenantPhone)
        val tvTenantIdCard = dialogView.findViewById<TextView>(R.id.tvTenantIdCard)
        val tvRoomName = dialogView.findViewById<TextView>(R.id.tvRoomName)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val tvEndDate = dialogView.findViewById<TextView>(R.id.tvEndDate)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val etDeposit = dialogView.findViewById<EditText>(R.id.etDeposit)
        val etContractNote = dialogView.findViewById<EditText>(R.id.etContractNote)
        val tvRoommateWarning = dialogView.findViewById<TextView>(R.id.tvRoommateWarning)
        
        // Hiển thị thông tin khách thuê
        tvTenantName.text = "Họ tên: ${khach.hoTen}"
        tvTenantPhone.text = "SĐT: ${khach.soDienThoai}"
        tvTenantIdCard.text = if (khach.soCmnd.isNotEmpty()) "CMND/CCCD: ${khach.soCmnd}" else "CMND/CCCD: Chưa có"
        tvRoomName.text = "Phòng: ${phong.tenPhong}"
        
        // Ẩn cảnh báo roommate
        tvRoommateWarning.visibility = View.GONE
        
        // Set giá phòng mặc định
        etPrice.setText(phong.giaCoBan.toLong().toString())
        
        var ngayBatDau: Long = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etStartDate.setText(sdf.format(Date(ngayBatDau)))
        
        // Hàm tính ngày kết thúc
        fun updateEndDate() {
            val duration = etDuration.text.toString().toIntOrNull() ?: 12
            val cal = Calendar.getInstance()
            cal.timeInMillis = ngayBatDau
            cal.add(Calendar.MONTH, duration)
            tvEndDate.text = sdf.format(Date(cal.timeInMillis))
        }
        
        updateEndDate()
        
        // Date picker cho ngày bắt đầu
        etStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = ngayBatDau
            android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                ngayBatDau = cal.timeInMillis
                etStartDate.setText(sdf.format(Date(ngayBatDau)))
                updateEndDate()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        // Cập nhật ngày kết thúc khi thay đổi thời hạn
        etDuration.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateEndDate()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Tạo dialog
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Tạo hợp đồng", null)
            .setNegativeButton("Hủy", null)
            .setCancelable(false)  // Không cho đóng bằng cách bấm ngoài
            .create()
        
        dialog.setOnShowListener {
            val btnPositive = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            val btnNegative = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
            
            btnPositive.setOnClickListener {
                val duration = etDuration.text.toString().toIntOrNull() ?: 12
                val deposit = etDeposit.text.toString().toLongOrNull() ?: 0L
                val price = etPrice.text.toString().toLongOrNull()
                val note = etContractNote.text.toString().trim()
                
                // Validation
                if (price == null || price <= 0) {
                    Toast.makeText(context, "Vui lòng nhập giá thuê hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (duration <= 0) {
                    Toast.makeText(context, "Thời hạn phải lớn hơn 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val cal = Calendar.getInstance()
                cal.timeInMillis = ngayBatDau
                cal.add(Calendar.MONTH, duration)
                val ngayKetThuc = cal.timeInMillis
                
                CoroutineScope(Dispatchers.IO).launch {
                    // 1. Thêm khách thuê
                    val maKhachMoi = dbManager.khachThueDao.them(khach)
                    
                    // 2. Tạo hợp đồng
                    val hopDong = com.example.btl_mobile_son.data.model.HopDong(
                        maPhong = phong.maPhong,
                        maKhach = maKhachMoi,
                        ngayBatDau = ngayBatDau,
                        ngayKetThuc = ngayKetThuc,
                        giaThueThang = price,
                        tienDatCoc = deposit,
                        trangThai = "dang_thue"
                    )
                    val maHopDongMoi = dbManager.hopDongDao.them(hopDong)
                    
                    // 3. Thêm vào HopDongThanhVien với vai trò "dai_dien"
                    val thanhVien = com.example.btl_mobile_son.data.model.HopDongThanhVien(
                        maHopDong = maHopDongMoi,
                        maKhach = maKhachMoi,
                        vaiTro = "dai_dien",
                        ngayVaoO = ngayBatDau,
                        trangThai = "dang_o",
                        ghiChu = "Người đại diện hợp đồng"
                    )
                    dbManager.hopDongThanhVienDao.them(thanhVien)
                    
                    // 4. Cập nhật trạng thái phòng thành đã thuê
                    dbManager.phongDao.capNhat(phong.copy(trangThai = "da_thue"))
                    
                    // 5. Nếu phòng có đặt cọc, cập nhật trạng thái đặt cọc
                    val danhSachDatCoc = dbManager.datCocDao.layTatCa()
                        .filter { it.maPhong == phong.maPhong && it.trangThai == "hieu_luc" }
                    danhSachDatCoc.forEach { datCoc ->
                        dbManager.datCocDao.capNhat(
                            datCoc.copy(trangThai = "da_chuyen_hop_dong")
                        )
                    }
                    
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        Toast.makeText(
                            context,
                            "✓ Đã thêm khách thuê: ${khach.hoTen}\n" +
                            "✓ Đã tạo hợp đồng #$maHopDongMoi\n" +
                            "✓ Phòng ${phong.tenPhong} đã cho thuê\n" +
                            "Thời hạn: $duration tháng | Giá: ${price.toLong()}đ/tháng",
                            Toast.LENGTH_LONG
                        ).show()
                        requireActivity().onBackPressed()
                    }
                }
            }
            
            btnNegative.setOnClickListener {
                // Hủy - KHÔNG lưu gì cả
                dialog.dismiss()
                Toast.makeText(
                    context,
                    "Đã hủy. Không lưu thông tin khách thuê.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        dialog.show()
    }
}

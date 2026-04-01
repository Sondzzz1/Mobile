package com.example.btl_mobile_son

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.HopDong
import com.example.btl_mobile_son.data.model.NhaTro
import com.example.btl_mobile_son.data.model.Phong
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CreateContractFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maHopDongEdit: Long = -1L
    private var ngayBatDau: Long = 0L
    private var ngayKetThuc: Long = 0L
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var danhSachPhong: List<Phong> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_contract, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("CreateContractFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }
        maHopDongEdit = arguments?.getLong("maHopDong", -1L) ?: -1L

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
        val etStartDate = view.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = view.findViewById<EditText>(R.id.etEndDate)
        val etRoomPrice = view.findViewById<EditText>(R.id.etRoomPrice)
        val etDeposit = view.findViewById<EditText>(R.id.etDeposit)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etFullName = view.findViewById<EditText>(R.id.etFullName)

        // Load danh sách nhà vào spinner
        CoroutineScope(Dispatchers.IO).launch {
            val danhSachNha = dbManager.nhaTroDao.layTatCa()
            withContext(Dispatchers.Main) {
                val tenNha = danhSachNha.map { it.tenNha }
                spinnerHouse.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tenNha)

                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        val maNha = danhSachNha[pos].maNha
                        CoroutineScope(Dispatchers.IO).launch {
                            danhSachPhong = dbManager.phongDao.layTheoNha(maNha)
                            withContext(Dispatchers.Main) {
                                val tenPhong = danhSachPhong.map { it.tenPhong }
                                spinnerRoom.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tenPhong)
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Load dữ liệu khi edit
                if (maHopDongEdit > 0) {
                    loadEditData(danhSachNha, spinnerHouse, spinnerRoom, etStartDate, etEndDate, etRoomPrice, etDeposit, etPhone, etFullName)
                }
            }
        }

        // Date pickers
        etStartDate.setOnClickListener { chonNgay { ts -> ngayBatDau = ts; etStartDate.setText(sdf.format(Date(ts))) } }
        etEndDate.setOnClickListener { chonNgay { ts -> ngayKetThuc = ts; etEndDate.setText(sdf.format(Date(ts))) } }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Nút lưu
        val allButtons = getAllButtons(view)
        allButtons.lastOrNull()?.setOnClickListener {
            val sdt = etPhone.text.toString().trim()
            val hoTen = etFullName.text.toString().trim()
            val giaStr = etRoomPrice.text.toString().trim()
            val datCocStr = etDeposit.text.toString().trim()

            // Validation
            if (!ValidationHelper.isNotEmpty(hoTen)) {
                Toast.makeText(context, "Nhập tên khách thuê", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!ValidationHelper.isValidPhoneNumber(sdt)) {
                Toast.makeText(context, ValidationHelper.getPhoneErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ngayBatDau == 0L) {
                Toast.makeText(context, "Chọn ngày bắt đầu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ngayKetThuc == 0L) {
                Toast.makeText(context, "Chọn ngày kết thúc", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!ValidationHelper.isValidDateRange(ngayBatDau, ngayKetThuc)) {
                Toast.makeText(context, ValidationHelper.getDateRangeErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (giaStr.isEmpty()) {
                Toast.makeText(context, "Nhập giá thuê", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val giaThue = giaStr.toLongOrNull() ?: 0L
            if (!ValidationHelper.isValidAmount(giaThue.toDouble())) {
                Toast.makeText(context, ValidationHelper.getAmountErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (danhSachPhong.isEmpty()) {
                Toast.makeText(context, "Chọn phòng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val maPhong = danhSachPhong[spinnerRoom.selectedItemPosition].maPhong

            CoroutineScope(Dispatchers.IO).launch {
                // Kiểm tra phòng đã có hợp đồng đang thuê chưa
                val hopDongHienTai = dbManager.hopDongDao.layHopDongDangThue(maPhong)
                
                if (maHopDongEdit > 0) {
                    // Khi sửa: loại trừ chính bản ghi đang sửa
                    if (hopDongHienTai != null && hopDongHienTai.maHopDong != maHopDongEdit) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "⚠️ Phòng đã có hợp đồng đang thuê khác!\n" +
                                "Hợp đồng #${hopDongHienTai.maHopDong}\n\n" +
                                "Quy tắc: 1 phòng chỉ có 1 hợp đồng đang hiệu lực.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                } else {
                    // Khi tạo mới: không được có hợp đồng nào đang thuê
                    if (hopDongHienTai != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "⚠️ Phòng đã có hợp đồng đang thuê!\n" +
                                "Hợp đồng #${hopDongHienTai.maHopDong}\n\n" +
                                "Quy tắc: 1 phòng chỉ có 1 hợp đồng đang hiệu lực.\n" +
                                "Muốn thêm người vào phòng này, vui lòng dùng chức năng 'Thêm người ở ghép'.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                }

                // Tạo hoặc tìm khách thuê (ưu tiên CMND)
                var maKhach = -1L
                
                // Tìm theo CMND trước (chính xác nhất)
                if (sdt.isNotEmpty()) {
                    val khachTheoCmnd = dbManager.khachThueDao.layTatCa()
                        .find { it.soCmnd.isNotEmpty() && it.soCmnd == sdt }
                    
                    if (khachTheoCmnd != null) {
                        maKhach = khachTheoCmnd.maKhach
                    }
                }
                
                // Nếu không tìm thấy theo CMND, tìm theo SĐT
                if (maKhach == -1L) {
                    val ds = dbManager.khachThueDao.timKiem(sdt)
                    maKhach = if (ds.isNotEmpty()) ds[0].maKhach
                    else dbManager.khachThueDao.them(
                        com.example.btl_mobile_son.data.model.KhachThue(hoTen = hoTen, soDienThoai = sdt)
                    )
                }

                val hopDong = HopDong(
                    maHopDong = if (maHopDongEdit > 0) maHopDongEdit else 0,
                    maPhong = maPhong,
                    maKhach = maKhach,
                    ngayBatDau = ngayBatDau,
                    ngayKetThuc = ngayKetThuc,
                    giaThueThang = giaThue,
                    tienDatCoc = datCocStr.toLongOrNull() ?: 0L,
                    trangThai = "dang_thue"
                )
                
                if (maHopDongEdit > 0) {
                    dbManager.hopDongDao.capNhat(hopDong)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✓ Đã cập nhật hợp đồng", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                } else {
                    val maHopDongMoi = dbManager.hopDongDao.them(hopDong)
                    
                    // Thêm vào HopDongThanhVien với vai trò "dai_dien"
                    val thanhVien = com.example.btl_mobile_son.data.model.HopDongThanhVien(
                        maHopDong = maHopDongMoi,
                        maKhach = maKhach,
                        vaiTro = "dai_dien",
                        ngayVaoO = ngayBatDau,
                        trangThai = "dang_o",
                        ghiChu = "Người đại diện hợp đồng"
                    )
                    dbManager.hopDongThanhVienDao.them(thanhVien)
                    
                    // Cập nhật trạng thái phòng thành đã thuê
                    val phong = dbManager.phongDao.layTheoMa(maPhong)
                    phong?.let { dbManager.phongDao.capNhat(it.copy(trangThai = "da_thue")) }
                    
                    // Nếu phòng có đặt cọc, cập nhật trạng thái đặt cọc
                    val danhSachDatCoc = dbManager.datCocDao.layTatCa()
                        .filter { it.maPhong == maPhong && it.trangThai == "hieu_luc" }
                    danhSachDatCoc.forEach { datCoc ->
                        dbManager.datCocDao.capNhat(
                            datCoc.copy(trangThai = "da_chuyen_hop_dong")
                        )
                    }
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "✓ Đã tạo hợp đồng #$maHopDongMoi\n" +
                            "✓ Phòng đã cho thuê",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
    }

    private fun chonNgay(onChon: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d)
            onChon(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadEditData(
        danhSachNha: List<NhaTro>,
        spinnerHouse: Spinner,
        spinnerRoom: Spinner,
        etStartDate: EditText,
        etEndDate: EditText,
        etRoomPrice: EditText,
        etDeposit: EditText,
        etPhone: EditText,
        etFullName: EditText
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val hopDong = dbManager.hopDongDao.layTheoMa(maHopDongEdit) ?: return@launch
            val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong) ?: return@launch
            val khach = dbManager.khachThueDao.layTheoMa(hopDong.maKhach)

            withContext(Dispatchers.Main) {
                // Set ngày
                ngayBatDau = hopDong.ngayBatDau
                ngayKetThuc = hopDong.ngayKetThuc
                etStartDate.setText(sdf.format(Date(ngayBatDau)))
                etEndDate.setText(sdf.format(Date(ngayKetThuc)))

                // Set giá và đặt cọc
                etRoomPrice.setText(hopDong.giaThueThang.toLong().toString())
                etDeposit.setText(hopDong.tienDatCoc.toLong().toString())

                // Set thông tin khách
                khach?.let {
                    etFullName.setText(it.hoTen)
                    etPhone.setText(it.soDienThoai)
                }

                // Set spinner nhà
                val viTriNha = danhSachNha.indexOfFirst { it.maNha == phong.maNha }
                if (viTriNha >= 0) {
                    spinnerHouse.setSelection(viTriNha)
                    // Load phòng và set spinner phòng
                    CoroutineScope(Dispatchers.IO).launch {
                        danhSachPhong = dbManager.phongDao.layTheoNha(phong.maNha)
                        withContext(Dispatchers.Main) {
                            val tenPhong = danhSachPhong.map { it.tenPhong }
                            spinnerRoom.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tenPhong)
                            val viTriPhong = danhSachPhong.indexOfFirst { it.maPhong == phong.maPhong }
                            if (viTriPhong >= 0) {
                                spinnerRoom.setSelection(viTriPhong)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAllButtons(view: View): List<Button> {
        val result = mutableListOf<Button>()
        if (view is ViewGroup) for (i in 0 until view.childCount) result.addAll(getAllButtons(view.getChildAt(i)))
        else if (view is Button) result.add(view)
        return result
    }
}

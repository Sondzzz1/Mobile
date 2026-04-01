package com.example.btl_mobile_son

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.DatCoc
import com.example.btl_mobile_son.data.model.Phong
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CreateDepositFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maDatCocEdit: Long = -1L
    private var ngayDuKienVao: Long = 0L
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var danhSachPhong: List<Phong> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_deposit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())
        maDatCocEdit = arguments?.getLong("maDatCoc", -1L) ?: -1L

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
        val etTenKhach = view.findViewById<EditText>(R.id.etTenKhach)
        val etSdt = view.findViewById<EditText>(R.id.etSdt)
        val etCmnd = view.findViewById<EditText>(R.id.etCmnd)
        val etTienCoc = view.findViewById<EditText>(R.id.etTienCoc)
        val etGiaPhong = view.findViewById<EditText>(R.id.etGiaPhong)
        val etNgayVao = view.findViewById<EditText>(R.id.etNgayVao)
        val etGhiChu = view.findViewById<EditText>(R.id.etGhiChu)
        val btnLuu = view.findViewById<Button>(R.id.btnLuu)

        etNgayVao.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                ngayDuKienVao = cal.timeInMillis
                etNgayVao.setText(sdf.format(Date(ngayDuKienVao)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Load nhà vào spinner
        CoroutineScope(Dispatchers.IO).launch {
            val danhSachNha = dbManager.nhaTroDao.layTatCa()
            withContext(Dispatchers.Main) {
                spinnerHouse.adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, danhSachNha.map { it.tenNha })

                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        CoroutineScope(Dispatchers.IO).launch {
                            // Nếu đang edit, hiển thị tất cả phòng
                            // Nếu thêm mới, chỉ hiển thị phòng trống
                            danhSachPhong = if (maDatCocEdit > 0) {
                                dbManager.phongDao.layTheoNha(danhSachNha[pos].maNha)
                            } else {
                                dbManager.phongDao.layTheoNha(danhSachNha[pos].maNha)
                                    .filter { it.trangThai == "trong" }
                            }
                            
                            withContext(Dispatchers.Main) {
                                if (danhSachPhong.isEmpty()) {
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_dropdown_item,
                                        listOf("Không có phòng trống")
                                    )
                                } else {
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_dropdown_item,
                                        danhSachPhong.map { 
                                            val status = when(it.trangThai) {
                                                "trong" -> "Trống"
                                                "dat_coc" -> "Đã đặt cọc"
                                                "da_thue" -> "Đã thuê"
                                                else -> ""
                                            }
                                            "${it.tenPhong} - ${it.giaCoBan.toLong()}đ ${if (status.isNotEmpty()) "($status)" else ""}"
                                        }
                                    )
                                }
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Nếu đang edit, load dữ liệu cũ
                if (maDatCocEdit > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val dc = dbManager.datCocDao.layTheoMa(maDatCocEdit)
                        withContext(Dispatchers.Main) {
                            dc?.let {
                                etTenKhach.setText(it.tenKhach)
                                etSdt.setText(it.soDienThoai)
                                etCmnd.setText(it.soCmnd)
                                etTienCoc.setText(it.tienDatCoc.toLong().toString())
                                etGiaPhong.setText(it.giaPhong.toLong().toString())
                                etGhiChu.setText(it.ghiChu)
                                if (it.ngayDuKienVao > 0) {
                                    ngayDuKienVao = it.ngayDuKienVao
                                    etNgayVao.setText(sdf.format(Date(it.ngayDuKienVao)))
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

        btnLuu.setOnClickListener {
            val tenKhach = etTenKhach.text.toString().trim()
            val sdt = etSdt.text.toString().trim()
            val cmnd = etCmnd.text.toString().trim()
            val tienStr = etTienCoc.text.toString().trim()
            val giaPhongStr = etGiaPhong.text.toString().trim()

            // Validation
            if (!ValidationHelper.isNotEmpty(tenKhach)) {
                Toast.makeText(context, "Nhập tên khách", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sdt.isNotEmpty() && !ValidationHelper.isValidPhoneNumber(sdt)) {
                Toast.makeText(context, ValidationHelper.getPhoneErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cmnd.isNotEmpty() && !ValidationHelper.isValidCMND(cmnd)) {
                Toast.makeText(context, ValidationHelper.getCMNDErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tienStr.isEmpty()) {
                Toast.makeText(context, "Nhập tiền đặt cọc", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tienCoc = tienStr.toDoubleOrNull() ?: 0.0
            if (!ValidationHelper.isValidAmount(tienCoc)) {
                Toast.makeText(context, ValidationHelper.getAmountErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (danhSachPhong.isEmpty()) {
                Toast.makeText(context, "Không có phòng trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phongDuocChon = danhSachPhong[spinnerRoom.selectedItemPosition]
            val maPhong = phongDuocChon.maPhong

            CoroutineScope(Dispatchers.IO).launch {
                // Kiểm tra lại trạng thái phòng trước khi lưu (nếu là thêm mới)
                if (maDatCocEdit <= 0) {
                    val phong = dbManager.phongDao.layTheoMa(maPhong)
                    if (phong == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Không tìm thấy phòng", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    
                    // Kiểm tra phòng đã có người chưa
                    if (phong.trangThai == "da_thue") {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "⚠️ Phòng ${phong.tenPhong} đã có người thuê!\nKhông thể đặt cọc.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                    
                    if (phong.trangThai == "dat_coc") {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "⚠️ Phòng ${phong.tenPhong} đã được đặt cọc!\nVui lòng chọn phòng khác.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                }
                
                val datCoc = DatCoc(
                    maDatCoc = if (maDatCocEdit > 0) maDatCocEdit else 0,
                    maPhong = maPhong,
                    tenKhach = tenKhach,
                    soDienThoai = sdt,
                    soCmnd = cmnd,
                    email = "",
                    tienDatCoc = tienCoc,
                    giaPhong = giaPhongStr.toDoubleOrNull() ?: 0.0,
                    ngayDuKienVao = ngayDuKienVao,
                    ghiChu = etGhiChu.text.toString().trim()
                )

                if (maDatCocEdit > 0) {
                    // Cập nhật đặt cọc
                    dbManager.datCocDao.capNhat(datCoc)
                } else {
                    // Thêm mới đặt cọc
                    dbManager.datCocDao.them(datCoc)
                    
                    // Cập nhật trạng thái phòng thành "dat_coc"
                    val phong = dbManager.phongDao.layTheoMa(maPhong)
                    phong?.let {
                        dbManager.phongDao.capNhat(it.copy(trangThai = "dat_coc"))
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        if (maDatCocEdit > 0) "✓ Đã cập nhật đặt cọc"
                        else "✓ Đã lưu đặt cọc\n✓ Phòng ${phongDuocChon.tenPhong} đã được đặt cọc",
                        Toast.LENGTH_LONG
                    ).show()
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}

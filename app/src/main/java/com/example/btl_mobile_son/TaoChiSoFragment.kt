package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.ChiSoDienNuoc
import com.example.btl_mobile_son.data.model.Phong
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class TaoChiSoFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maChiSoEdit: Long = -1L
    private var danhSachPhong: List<Phong> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_utility, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("TaoChiSoFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }
        maChiSoEdit = arguments?.getLong("maChiSo", -1L) ?: -1L

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
        val spinnerLoai = view.findViewById<Spinner>(R.id.spinnerLoai)
        val etThang = view.findViewById<EditText>(R.id.etThang)
        val etNam = view.findViewById<EditText>(R.id.etNam)
        val etChiSoCu = view.findViewById<EditText>(R.id.etChiSoCu)
        val etChiSoMoi = view.findViewById<EditText>(R.id.etChiSoMoi)
        val etDonGia = view.findViewById<EditText>(R.id.etDonGia)
        val etGhiChu = view.findViewById<EditText>(R.id.etGhiChu)
        val btnLuu = view.findViewById<Button>(R.id.btnLuu)

        val loaiList = listOf("Điện" to "dien", "Nước" to "nuoc")
        spinnerLoai.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, loaiList.map { it.first })

        // Mặc định tháng/năm hiện tại
        val cal = Calendar.getInstance()
        etThang.setText((cal.get(Calendar.MONTH) + 1).toString())
        etNam.setText(cal.get(Calendar.YEAR).toString())

        CoroutineScope(Dispatchers.IO).launch {
            val danhSachNha = dbManager.nhaTroDao.layTatCa()
            withContext(Dispatchers.Main) {
                spinnerHouse.adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, danhSachNha.map { it.tenNha })

                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        CoroutineScope(Dispatchers.IO).launch {
                            danhSachPhong = dbManager.phongDao.layTheoNha(danhSachNha[pos].maNha)
                            withContext(Dispatchers.Main) {
                                spinnerRoom.adapter = ArrayAdapter(requireContext(),
                                    android.R.layout.simple_spinner_dropdown_item,
                                    danhSachPhong.map { it.tenPhong })
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Listener cho phòng - tự động load chỉ số cũ từ tháng trước
                spinnerRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        if (danhSachPhong.isNotEmpty() && maChiSoEdit <= 0) {
                            val maPhong = danhSachPhong[pos].maPhong
                            val loai = loaiList[spinnerLoai.selectedItemPosition].second
                            CoroutineScope(Dispatchers.IO).launch {
                                val thang = etThang.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.MONTH) + 1
                                val nam = etNam.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                                
                                val chiSoThangTruoc = dbManager.chiSoDienNuocDao.layChiSoThangTruoc(
                                    maPhong, loai, thang, nam
                                )
                                
                                withContext(Dispatchers.Main) {
                                    chiSoThangTruoc?.let {
                                        etChiSoCu.setText(it.chiSoMoi.toString())
                                        etDonGia.setText(it.donGia.toLong().toString())
                                    }
                                }
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Listener cho loại dịch vụ - cập nhật chỉ số cũ khi đổi loại
                spinnerLoai.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        if (danhSachPhong.isNotEmpty() && maChiSoEdit <= 0) {
                            val maPhong = danhSachPhong[spinnerRoom.selectedItemPosition].maPhong
                            val loai = loaiList[pos].second
                            CoroutineScope(Dispatchers.IO).launch {
                                val thang = etThang.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.MONTH) + 1
                                val nam = etNam.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                                
                                val chiSoThangTruoc = dbManager.chiSoDienNuocDao.layChiSoThangTruoc(
                                    maPhong, loai, thang, nam
                                )
                                
                                withContext(Dispatchers.Main) {
                                    chiSoThangTruoc?.let {
                                        etChiSoCu.setText(it.chiSoMoi.toString())
                                        etDonGia.setText(it.donGia.toLong().toString())
                                    }
                                }
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                if (maChiSoEdit > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val cs = dbManager.chiSoDienNuocDao.layTheoMa(maChiSoEdit)
                        withContext(Dispatchers.Main) {
                            cs?.let {
                                etThang.setText(it.thang.toString())
                                etNam.setText(it.nam.toString())
                                etChiSoCu.setText(it.chiSoCu.toString())
                                etChiSoMoi.setText(it.chiSoMoi.toString())
                                etDonGia.setText(it.donGia.toLong().toString())
                                etGhiChu.setText(it.ghiChu)
                                val loaiIdx = loaiList.indexOfFirst { l -> l.second == it.loai }
                                if (loaiIdx >= 0) spinnerLoai.setSelection(loaiIdx)
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
            val thangStr = etThang.text.toString().trim()
            val namStr = etNam.text.toString().trim()
            val chiSoCuStr = etChiSoCu.text.toString().trim()
            val chiSoMoiStr = etChiSoMoi.text.toString().trim()
            val donGiaStr = etDonGia.text.toString().trim()

            // Validation
            if (thangStr.isEmpty() || namStr.isEmpty()) {
                Toast.makeText(context, "Nhập tháng và năm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val thang = thangStr.toIntOrNull() ?: 0
            if (thang < 1 || thang > 12) {
                Toast.makeText(context, "Tháng phải từ 1 đến 12", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (chiSoMoiStr.isEmpty()) {
                Toast.makeText(context, "Nhập chỉ số mới", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val chiSoCu = chiSoCuStr.toLongOrNull() ?: 0L
            val chiSoMoi = chiSoMoiStr.toLongOrNull() ?: 0L
            if (chiSoMoi < chiSoCu) {
                Toast.makeText(context, "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (donGiaStr.isEmpty()) {
                Toast.makeText(context, "Nhập đơn giá", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val donGia = donGiaStr.toLongOrNull() ?: 0L
            if (!ValidationHelper.isValidAmount(donGia.toDouble())) {
                Toast.makeText(context, ValidationHelper.getAmountErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (danhSachPhong.isEmpty()) {
                Toast.makeText(context, "Chưa có phòng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val maPhong = danhSachPhong[spinnerRoom.selectedItemPosition].maPhong
            val loai = loaiList[spinnerLoai.selectedItemPosition].second
            val nam = namStr.toIntOrNull() ?: 2024
            
            val chiSo = ChiSoDienNuoc(
                maChiSo = if (maChiSoEdit > 0) maChiSoEdit else 0,
                maPhong = maPhong,
                loai = loai,
                thang = thang,
                nam = nam,
                chiSoCu = chiSoCu,
                chiSoMoi = chiSoMoi,
                soTieuThu = chiSoMoi - chiSoCu,
                donGia = donGia,
                ghiChu = etGhiChu.text.toString().trim()
            )

            CoroutineScope(Dispatchers.IO).launch {
                // Kiểm tra trùng lặp
                val daTonTai = dbManager.chiSoDienNuocDao.kiemTraTrungChiSo(
                    maPhong, loai, thang, nam, if (maChiSoEdit > 0) maChiSoEdit else -1
                )
                
                if (daTonTai) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "⚠️ Phòng này đã có chỉ số ${if(loai=="dien") "điện" else "nước"} tháng $thang/$nam!\nKhông thể lưu trùng.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                
                luuChiSo(chiSo, maPhong, loai, thang, nam)
            }
        }
    }
    
    private suspend fun luuChiSo(chiSo: ChiSoDienNuoc, maPhong: Long, loai: String, thang: Int, nam: Int) {
        if (maChiSoEdit > 0) dbManager.chiSoDienNuocDao.capNhat(chiSo)
        else dbManager.chiSoDienNuocDao.them(chiSo)
        
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "✓ Đã lưu chỉ số ${if(loai=="dien") "điện" else "nước"}", Toast.LENGTH_SHORT).show()
            
            // Chỉ gợi ý tạo hóa đơn khi đã có đủ cả điện và nước
            CoroutineScope(Dispatchers.IO).launch {
                val chiSoDienNuoc = dbManager.chiSoDienNuocDao.layTheoThangNam(thang, nam)
                    .filter { it.maPhong == maPhong }
                
                val coDien = chiSoDienNuoc.any { it.loai == "dien" }
                val coNuoc = chiSoDienNuoc.any { it.loai == "nuoc" }
                
                withContext(Dispatchers.Main) {
                    if (coDien && coNuoc) {
                        // Đã có đủ cả điện và nước -> gợi ý tạo hóa đơn
                        AlertDialog.Builder(requireContext())
                            .setTitle("✓ Đã đủ dữ liệu")
                            .setMessage("Phòng này đã có đủ chỉ số điện và nước tháng $thang/$nam.\n\nTạo hóa đơn ngay?")
                            .setPositiveButton("Tạo hóa đơn") { _, _ ->
                                val fragment = TaoHoaDonFragment().apply {
                                    arguments = Bundle().apply {
                                        putLong("maPhong", maPhong)
                                        putInt("thang", thang)
                                        putInt("nam", nam)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            .setNegativeButton("Để sau") { _, _ ->
                                requireActivity().onBackPressed()
                            }
                            .show()
                    } else {
                        // Chưa đủ dữ liệu -> thông báo cần nhập thêm
                        val thieu = if (!coDien && !coNuoc) "điện và nước"
                                    else if (!coDien) "điện"
                                    else "nước"
                        
                        AlertDialog.Builder(requireContext())
                            .setTitle("⚠️ Chưa đủ dữ liệu")
                            .setMessage("Phòng này chưa có chỉ số $thieu tháng $thang/$nam.\n\nNên nhập đủ cả điện và nước trước khi tạo hóa đơn.")
                            .setPositiveButton("OK") { _, _ ->
                                requireActivity().onBackPressed()
                            }
                            .show()
                    }
                }
            }
        }
    }
}

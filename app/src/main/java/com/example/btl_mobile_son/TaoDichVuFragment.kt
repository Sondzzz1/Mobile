package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.DichVu
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaoDichVuFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maDichVuEdit: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("TaoDichVuFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }
        maDichVuEdit = arguments?.getLong("maDichVu", -1L) ?: -1L

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val etTenDichVu = view.findViewById<EditText>(R.id.etTenDichVu)
        val etDonVi = view.findViewById<EditText>(R.id.etDonVi)
        val etDonGia = view.findViewById<EditText>(R.id.etDonGia)
        val spinnerLoai = view.findViewById<Spinner>(R.id.spinnerLoai)
        val spinnerCachTinh = view.findViewById<Spinner>(R.id.spinnerCachTinh)
        val btnLuu = view.findViewById<Button>(R.id.btnLuu)

        val loaiDichVu = listOf("Điện" to "dien", "Nước" to "nuoc", "Khác" to "khac")
        spinnerLoai.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, loaiDichVu.map { it.first })

        // Thêm spinner cách tính
        val cachTinhList = listOf(
            "Theo phòng" to "theo_phong",
            "Theo người" to "theo_nguoi",
            "Theo tháng" to "theo_thang",
            "Một lần" to "mot_lan"
        )
        spinnerCachTinh.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, cachTinhList.map { it.first })

        CoroutineScope(Dispatchers.IO).launch {
            val danhSachNha = dbManager.nhaTroDao.layTatCa()
            withContext(Dispatchers.Main) {
                spinnerHouse.adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, danhSachNha.map { it.tenNha })

                if (maDichVuEdit > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val dv = dbManager.dichVuDao.layTheoMa(maDichVuEdit)
                        withContext(Dispatchers.Main) {
                            dv?.let {
                                etTenDichVu.setText(it.tenDichVu)
                                etDonVi.setText(it.donVi)
                                etDonGia.setText(it.donGia.toLong().toString())
                                val nhaIdx = danhSachNha.indexOfFirst { n -> n.maNha == it.maNha }
                                if (nhaIdx >= 0) spinnerHouse.setSelection(nhaIdx)
                                val loaiIdx = loaiDichVu.indexOfFirst { l -> l.second == it.loaiDichVu }
                                if (loaiIdx >= 0) spinnerLoai.setSelection(loaiIdx)
                                // Load cách tính
                                val cachTinhIdx = cachTinhList.indexOfFirst { c -> c.second == it.cachTinh }
                                if (cachTinhIdx >= 0) spinnerCachTinh.setSelection(cachTinhIdx)
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
            val ten = etTenDichVu.text.toString().trim()
            val donGiaStr = etDonGia.text.toString().trim()

            // Validation
            if (!ValidationHelper.isNotEmpty(ten)) {
                Toast.makeText(context, "Nhập tên dịch vụ", Toast.LENGTH_SHORT).show()
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

            CoroutineScope(Dispatchers.IO).launch {
                val danhSachNha = dbManager.nhaTroDao.layTatCa()
                if (danhSachNha.isEmpty()) {
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Chưa có nhà trọ", Toast.LENGTH_SHORT).show() }
                    return@launch
                }
                val maNha = danhSachNha[spinnerHouse.selectedItemPosition].maNha
                val donVi = etDonVi.text.toString().trim().ifEmpty { "lần" }
                val cachTinh = cachTinhList[spinnerCachTinh.selectedItemPosition].second
                
                // Kiểm tra trùng dịch vụ (tên + giá + đơn vị + cách tính)
                val daTonTai = dbManager.dichVuDao.layTatCa()
                    .any { 
                        it.maNha == maNha && 
                        it.tenDichVu == ten && 
                        it.donGia == donGia && 
                        it.donVi == donVi &&
                        it.cachTinh == cachTinh &&
                        it.maDichVu != maDichVuEdit 
                    }
                
                if (daTonTai) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context, 
                            "⚠️ Đã tồn tại dịch vụ '$ten' với giá ${donGia.toLong()}đ, đơn vị '$donVi', cách tính '${cachTinhList.find { it.second == cachTinh }?.first}'", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                
                val dichVu = DichVu(
                    maDichVu = if (maDichVuEdit > 0) maDichVuEdit else 0,
                    maNha = maNha,
                    tenDichVu = ten,
                    donVi = donVi,
                    donGia = donGia,
                    loaiDichVu = loaiDichVu[spinnerLoai.selectedItemPosition].second,
                    cachTinh = cachTinh
                )
                if (maDichVuEdit > 0) dbManager.dichVuDao.capNhat(dichVu)
                else dbManager.dichVuDao.them(dichVu)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✓ Đã lưu dịch vụ", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}

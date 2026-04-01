package com.example.btl_mobile_son

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.GiaoDich
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CreateIncomeFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maGiaoDichEdit: Long = -1L
    private var ngayGiaoDich: Long = System.currentTimeMillis()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_income, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())
        maGiaoDichEdit = arguments?.getLong("maGiaoDich", -1L) ?: -1L

        val etSoTien = view.findViewById<EditText>(R.id.etSoTien)
        val etNoiDung = view.findViewById<EditText>(R.id.etNoiDung)
        val etTenNguoi = view.findViewById<EditText>(R.id.etTenNguoi)
        val etNgay = view.findViewById<EditText>(R.id.etNgay)
        val etGhiChu = view.findViewById<EditText>(R.id.etGhiChu)
        val spinnerDanhMuc = view.findViewById<Spinner>(R.id.spinnerDanhMuc)
        val spinnerPhuongThuc = view.findViewById<Spinner>(R.id.spinnerPhuongThuc)
        val btnLuu = view.findViewById<Button>(R.id.btnLuu)

        // Danh mục thu
        val danhMucThu = listOf("Tiền thuê phòng", "Tiền điện", "Tiền nước", "Dịch vụ", "Đặt cọc", "Khác")
        spinnerDanhMuc.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, danhMucThu)

        val phuongThuc = listOf("Tiền mặt", "Chuyển khoản", "Thẻ")
        spinnerPhuongThuc.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, phuongThuc)

        // Ngày mặc định hôm nay
        etNgay.setText(sdf.format(Date(ngayGiaoDich)))
        etNgay.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                ngayGiaoDich = cal.timeInMillis
                etNgay.setText(sdf.format(Date(ngayGiaoDich)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Load dữ liệu nếu đang edit
        if (maGiaoDichEdit > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val gd = dbManager.giaoDichDao.layTheoMa(maGiaoDichEdit)
                withContext(Dispatchers.Main) {
                    gd?.let {
                        etSoTien.setText(it.soTien.toLong().toString())
                        etNoiDung.setText(it.noiDung)
                        etTenNguoi.setText(it.tenNguoi)
                        etGhiChu.setText(it.ghiChu)
                        ngayGiaoDich = it.ngayGiaoDich
                        etNgay.setText(sdf.format(Date(it.ngayGiaoDich)))
                        val idx = danhMucThu.indexOfFirst { dm -> dm == it.danhMuc }
                        if (idx >= 0) spinnerDanhMuc.setSelection(idx)
                        val ptIdx = listOf("tien_mat", "chuyen_khoan", "the").indexOf(it.phuongThucThanhToan)
                        if (ptIdx >= 0) spinnerPhuongThuc.setSelection(ptIdx)
                    }
                }
            }
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        btnLuu.setOnClickListener {
            val soTienStr = etSoTien.text.toString().trim()
            
            if (soTienStr.isEmpty()) {
                Toast.makeText(context, "Nhập số tiền", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val soTien = soTienStr.toLongOrNull() ?: 0L
            if (!ValidationHelper.isValidAmount(soTien.toDouble())) {
                Toast.makeText(context, ValidationHelper.getAmountErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ptMap = listOf("tien_mat", "chuyen_khoan", "the")
            val giaoDich = GiaoDich(
                maGiaoDich = if (maGiaoDichEdit > 0) maGiaoDichEdit else 0,
                loai = "thu",
                soTien = soTien,
                danhMuc = danhMucThu[spinnerDanhMuc.selectedItemPosition],
                ngayGiaoDich = ngayGiaoDich,
                noiDung = etNoiDung.text.toString().trim(),
                tenNguoi = etTenNguoi.text.toString().trim(),
                phuongThucThanhToan = ptMap[spinnerPhuongThuc.selectedItemPosition],
                ghiChu = etGhiChu.text.toString().trim()
            )

            CoroutineScope(Dispatchers.IO).launch {
                if (maGiaoDichEdit > 0) dbManager.giaoDichDao.capNhat(giaoDich)
                else dbManager.giaoDichDao.them(giaoDich)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Đã lưu khoản thu", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}

package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.Phong
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateRoomFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maNha: Long = -1L
    private var maPhong: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())
        maNha = arguments?.getLong("maNha", -1L) ?: -1L
        maPhong = arguments?.getLong("maPhong", -1L) ?: -1L

        val etTenPhong = view.findViewById<EditText>(R.id.etRoomName)
        val etGia = view.findViewById<EditText>(R.id.etRoomPrice)
        val etDienTich = view.findViewById<EditText>(R.id.etRoomArea)
        val etSoNguoi = view.findViewById<EditText>(R.id.etMaxTenant)
        val etGhiChu = view.findViewById<EditText>(R.id.etNote)

        // Nếu là chỉnh sửa, load dữ liệu cũ
        if (maPhong > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val phong = dbManager.phongDao.layTheoMa(maPhong)
                withContext(Dispatchers.Main) {
                    phong?.let {
                        etTenPhong.setText(it.tenPhong)
                        etGia.setText(it.giaCoBan.toLong().toString())
                        etDienTich.setText(it.dienTichM2.toString())
                        etSoNguoi.setText(it.soNguoiToiDa.toString())
                        etGhiChu.setText(it.ghiChu)
                    }
                }
            }
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val tenPhong = etTenPhong.text.toString().trim()
            val giaStr = etGia.text.toString().trim()
            val dienTichStr = etDienTich.text.toString().trim()

            // Validation
            if (!ValidationHelper.isNotEmpty(tenPhong)) {
                Toast.makeText(context, "Vui lòng nhập tên phòng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (giaStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập giá phòng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val gia = giaStr.toLongOrNull() ?: 0L
            if (!ValidationHelper.isValidAmount(gia.toDouble())) {
                Toast.makeText(context, ValidationHelper.getAmountErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dienTichStr.isNotEmpty()) {
                val dienTich = dienTichStr.toFloatOrNull() ?: 0f
                if (dienTich <= 0) {
                    Toast.makeText(context, "Diện tích phải lớn hơn 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                // Kiểm tra trùng tên phòng trong cùng nhà
                if (dbManager.phongDao.kiemTraTrungTen(maNha, tenPhong, maPhong)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "⚠️ Tên phòng '$tenPhong' đã tồn tại trong nhà này!\n" +
                            "Vui lòng chọn tên khác để tránh nhầm lẫn.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                
                // Lấy trạng thái cũ nếu đang edit
                val trangThaiCu = if (maPhong > 0) {
                    dbManager.phongDao.layTheoMa(maPhong)?.trangThai ?: "trong"
                } else {
                    "trong"
                }

                val phong = Phong(
                    maPhong = if (maPhong > 0) maPhong else 0,
                    maNha = maNha,
                    tenPhong = tenPhong,
                    giaCoBan = gia,
                    dienTichM2 = dienTichStr.toFloatOrNull() ?: 0f,
                    soNguoiToiDa = etSoNguoi.text.toString().toIntOrNull() ?: 1,
                    trangThai = trangThaiCu,
                    ghiChu = etGhiChu.text.toString().trim()
                )

                if (maPhong > 0) {
                    dbManager.phongDao.capNhat(phong)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✓ Đã cập nhật phòng", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                } else {
                    dbManager.phongDao.them(phong)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✓ Đã tạo phòng mới", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
    }
}

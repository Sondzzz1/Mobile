package com.example.btl_mobile_son

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.SuCo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CreateIssueFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maSuCo: Int? = null
    private var ngayBaoGao = System.currentTimeMillis()
    private var ngayXuLy: Long? = null
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        fun newInstance(maSuCo: Int): CreateIssueFragment {
            val fragment = CreateIssueFragment()
            val args = Bundle()
            args.putInt("maSuCo", maSuCo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_issue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        maSuCo = arguments?.getInt("maSuCo")

        val spinnerPhong = view.findViewById<Spinner>(R.id.spinnerPhong)
        val spinnerLoaiSuCo = view.findViewById<Spinner>(R.id.spinnerLoaiSuCo)
        val spinnerTrangThai = view.findViewById<Spinner>(R.id.spinnerTrangThai)
        val etMoTa = view.findViewById<EditText>(R.id.etMoTa)
        val etNguoiBaoGao = view.findViewById<EditText>(R.id.etNguoiBaoGao)
        val etNguoiXuLy = view.findViewById<EditText>(R.id.etNguoiXuLy)
        val etNgayBaoGao = view.findViewById<EditText>(R.id.etNgayBaoGao)
        val etNgayXuLy = view.findViewById<EditText>(R.id.etNgayXuLy)
        val etChiPhi = view.findViewById<EditText>(R.id.etChiPhi)
        val etGhiChu = view.findViewById<EditText>(R.id.etGhiChu)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Load phòng
        CoroutineScope(Dispatchers.IO).launch {
            val danhSachPhong = dbManager.phongDao.layTatCa()
            val tenPhong = danhSachPhong.map { "${it.tenPhong}" }
            
            withContext(Dispatchers.Main) {
                spinnerPhong.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tenPhong)
            }
        }

        // Loại sự cố
        val loaiSuCo = arrayOf("Điện", "Nước", "Điều hòa", "Tủ lạnh", "Máy giặt", "Cửa", "Khóa", "Khác")
        spinnerLoaiSuCo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, loaiSuCo)

        // Trạng thái
        val trangThai = arrayOf("Chưa xử lý", "Đang xử lý", "Đã xử lý")
        spinnerTrangThai.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, trangThai)

        // Ngày báo cáo
        etNgayBaoGao.setText(sdf.format(Date(ngayBaoGao)))
        etNgayBaoGao.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = ngayBaoGao
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    ngayBaoGao = calendar.timeInMillis
                    etNgayBaoGao.setText(sdf.format(Date(ngayBaoGao)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Ngày xử lý
        etNgayXuLy.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    ngayXuLy = calendar.timeInMillis
                    etNgayXuLy.setText(sdf.format(Date(ngayXuLy!!)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Load data nếu edit
        if (maSuCo != null && maSuCo!! > 0) {
            btnDelete.visibility = View.VISIBLE
            loadEditData(view)
        } else {
            btnDelete.visibility = View.GONE
        }

        btnSave.setOnClickListener {
            saveData(view)
        }

        btnDelete.setOnClickListener {
            deleteData()
        }
    }

    private fun loadEditData(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            val suCo = dbManager.suCoDao.layTheoId(maSuCo!!)
            if (suCo != null) {
                val danhSachPhong = dbManager.phongDao.layTatCa()
                withContext(Dispatchers.Main) {
                    val viTriPhong = danhSachPhong.indexOfFirst { it.maPhong.toInt() == suCo.maPhong }
                    if (viTriPhong >= 0) view.findViewById<Spinner>(R.id.spinnerPhong).setSelection(viTriPhong)

                    val loaiSuCo = arrayOf("Điện", "Nước", "Điều hòa", "Tủ lạnh", "Máy giặt", "Cửa", "Khóa", "Khác")
                    val viTriLoai = loaiSuCo.indexOf(suCo.loaiSuCo)
                    if (viTriLoai >= 0) view.findViewById<Spinner>(R.id.spinnerLoaiSuCo).setSelection(viTriLoai)

                    val trangThaiMap = mapOf("chua_xu_ly" to 0, "dang_xu_ly" to 1, "da_xu_ly" to 2)
                    view.findViewById<Spinner>(R.id.spinnerTrangThai).setSelection(trangThaiMap[suCo.trangThai] ?: 0)

                    view.findViewById<EditText>(R.id.etMoTa).setText(suCo.moTa)
                    view.findViewById<EditText>(R.id.etNguoiBaoGao).setText(suCo.nguoiBaoGao)
                    view.findViewById<EditText>(R.id.etNguoiXuLy).setText(suCo.nguoiXuLy)
                    
                    ngayBaoGao = suCo.ngayBaoGao
                    view.findViewById<EditText>(R.id.etNgayBaoGao).setText(sdf.format(Date(ngayBaoGao)))
                    
                    if (suCo.ngayXuLy != null) {
                        ngayXuLy = suCo.ngayXuLy
                        view.findViewById<EditText>(R.id.etNgayXuLy).setText(sdf.format(Date(ngayXuLy!!)))
                    }
                    
                    view.findViewById<EditText>(R.id.etChiPhi).setText(suCo.chiPhi.toString())
                    view.findViewById<EditText>(R.id.etGhiChu).setText(suCo.ghiChu)
                }
            }
        }
    }

    private fun saveData(view: View) {
        val spinnerPhong = view.findViewById<Spinner>(R.id.spinnerPhong)
        val spinnerLoaiSuCo = view.findViewById<Spinner>(R.id.spinnerLoaiSuCo)
        val spinnerTrangThai = view.findViewById<Spinner>(R.id.spinnerTrangThai)
        val etMoTa = view.findViewById<EditText>(R.id.etMoTa)
        val etNguoiBaoGao = view.findViewById<EditText>(R.id.etNguoiBaoGao)
        val etNguoiXuLy = view.findViewById<EditText>(R.id.etNguoiXuLy)
        val etChiPhi = view.findViewById<EditText>(R.id.etChiPhi)
        val etGhiChu = view.findViewById<EditText>(R.id.etGhiChu)

        CoroutineScope(Dispatchers.IO).launch {
            val danhSachPhong = dbManager.phongDao.layTatCa()
            val maPhong = danhSachPhong[spinnerPhong.selectedItemPosition].maPhong.toInt()
            val loaiSuCo = spinnerLoaiSuCo.selectedItem.toString()
            
            val trangThaiMap = arrayOf("chua_xu_ly", "dang_xu_ly", "da_xu_ly")
            val trangThai = trangThaiMap[spinnerTrangThai.selectedItemPosition]
            
            val chiPhi = etChiPhi.text.toString().toDoubleOrNull() ?: 0.0

            val suCo = SuCo(
                maSuCo = maSuCo ?: 0,
                maPhong = maPhong,
                loaiSuCo = loaiSuCo,
                moTa = etMoTa.text.toString(),
                trangThai = trangThai,
                nguoiBaoGao = etNguoiBaoGao.text.toString(),
                nguoiXuLy = etNguoiXuLy.text.toString(),
                ngayBaoGao = ngayBaoGao,
                ngayXuLy = ngayXuLy,
                chiPhi = chiPhi,
                ghiChu = etGhiChu.text.toString()
            )

            val result = if (maSuCo == null || maSuCo!! <= 0) {
                dbManager.suCoDao.them(suCo).toInt()
            } else {
                dbManager.suCoDao.capNhat(suCo)
            }

            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                } else {
                    Toast.makeText(context, "Lưu thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteData() {
        if (maSuCo == null || maSuCo!! <= 0) return
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = dbManager.suCoDao.xoa(maSuCo!!)
            
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                } else {
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

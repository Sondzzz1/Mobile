package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.DichVuPhongItem
import com.example.btl_mobile_son.adapter.PhongDichVuAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuanLyDichVuPhongFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var danhSachNha = listOf<com.example.btl_mobile_son.data.model.NhaTro>()
    private var danhSachPhong = listOf<com.example.btl_mobile_son.data.model.Phong>()
    private var maNhaChon: Long = -1L
    private var maPhongChon: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_room_service_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
        val rvDichVu = view.findViewById<RecyclerView>(R.id.rvDichVuPhong)
        val layoutEmpty = view.findViewById<View>(R.id.layoutEmptyService)
        val tvHuongDan = view.findViewById<TextView>(R.id.tvHuongDan)

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
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
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                        maNhaChon = danhSachNha[position].maNha
                        
                        CoroutineScope(Dispatchers.IO).launch {
                            danhSachPhong = dbManager.phongDao.layTheoNha(maNhaChon)
                            
                            withContext(Dispatchers.Main) {
                                if (danhSachPhong.isEmpty()) {
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        listOf("Không có phòng")
                                    )
                                    rvDichVu.visibility = View.GONE
                                    layoutEmpty.visibility = View.VISIBLE
                                    tvHuongDan.text = "Nhà này chưa có phòng nào"
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

        // Listener chọn phòng → load dịch vụ
        spinnerRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (danhSachPhong.isEmpty()) return
                
                maPhongChon = danhSachPhong[position].maPhong
                loadDichVuPhong(rvDichVu, layoutEmpty, tvHuongDan)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadDichVuPhong(rvDichVu: RecyclerView, layoutEmpty: View, tvHuongDan: TextView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lấy danh sách dịch vụ của nhà
                val danhSachDichVu = dbManager.dichVuDao.layTatCa()
                    .filter { it.maNha == maNhaChon && it.isActive }
                
                // Lấy danh sách dịch vụ đã chọn cho phòng này
                val dichVuDaChon = dbManager.phongDichVuDao.layTheoPhong(maPhongChon)
                    .map { it.maDichVu }
                
                // Tạo danh sách item với trạng thái checked
                val items = danhSachDichVu.map { dv ->
                    DichVuPhongItem(
                        dichVu = dv,
                        isChecked = dichVuDaChon.contains(dv.maDichVu)
                    )
                }
                
                withContext(Dispatchers.Main) {
                    if (items.isEmpty()) {
                        rvDichVu.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                        tvHuongDan.text = "Nhà này chưa có dịch vụ nào.\nVào Quản lý dịch vụ để thêm."
                    } else {
                        rvDichVu.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE
                        
                        rvDichVu.layoutManager = LinearLayoutManager(requireContext())
                        rvDichVu.adapter = PhongDichVuAdapter(
                            danhSach = items,
                            onCheckChanged = { dichVu, isChecked ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (isChecked) {
                                        // Thêm dịch vụ vào phòng
                                        val phongDichVu = com.example.btl_mobile_son.data.model.PhongDichVu(
                                            maPhong = maPhongChon,
                                            maDichVu = dichVu.maDichVu,
                                            donGiaRieng = null,
                                            ghiChu = ""
                                        )
                                        dbManager.phongDichVuDao.them(phongDichVu)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "✓ Đã thêm ${dichVu.tenDichVu}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        // Xóa dịch vụ khỏi phòng
                                        val pdv = dbManager.phongDichVuDao.layTheoPhong(maPhongChon)
                                            .find { it.maDichVu == dichVu.maDichVu }
                                        pdv?.let {
                                            dbManager.phongDichVuDao.xoa(it.maPhongDichVu)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "✓ Đã xóa ${dichVu.tenDichVu}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("QuanLyDichVuPhongFragment", "Error loading services", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dịch vụ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

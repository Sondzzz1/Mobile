package com.example.btl_mobile_son

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.PhongAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.Phong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DanhSachPhongFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: PhongAdapter
    private var maNha: Long = -1L
    private var danhSachNha = listOf<com.example.btl_mobile_son.data.model.NhaTro>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_room_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("DanhSachPhongFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }
        maNha = arguments?.getLong("maNha", -1L) ?: -1L

        val rvRoomList = view.findViewById<RecyclerView>(R.id.rvRoomList)
        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        
        adapter = PhongAdapter(
            danhSach = emptyList(),
            onItemClick = { phong ->
                val fragment = ChiTietPhongFragment().apply {
                    arguments = Bundle().apply { putLong("maPhong", phong.maPhong) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onEditClick = { phong ->
                val fragment = TaoPhongFragment().apply {
                    arguments = Bundle().apply {
                        putLong("maNha", phong.maNha)
                        putLong("maPhong", phong.maPhong)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onDeleteClick = { phong ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa phòng")
                    .setMessage("Bạn có chắc muốn xóa \"${phong.tenPhong}\"?\n\nCảnh báo: Tất cả hợp đồng và dữ liệu liên quan sẽ bị xóa!")
                    .setPositiveButton("Xóa") { _, _ ->
                        xoaPhong(phong, view)
                    }
                    .setNegativeButton("Hủy", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        )
        rvRoomList.layoutManager = LinearLayoutManager(requireContext())
        rvRoomList.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Load danh sách nhà và setup spinner
        CoroutineScope(Dispatchers.IO).launch {
            danhSachNha = dbManager.nhaTroDao.layTatCa()
            
            withContext(Dispatchers.Main) {
                if (danhSachNha.isEmpty()) {
                    Toast.makeText(context, "Chưa có nhà trọ nào!", Toast.LENGTH_SHORT).show()
                    return@withContext
                }
                
                // Tạo danh sách hiển thị với "Tất cả nhà" ở đầu
                val tenNhaList = mutableListOf("Tất cả nhà")
                tenNhaList.addAll(danhSachNha.map { it.tenNha })
                
                spinnerHouse.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    tenNhaList
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                
                // Set vị trí ban đầu dựa vào maNha từ arguments
                if (maNha > 0) {
                    val index = danhSachNha.indexOfFirst { it.maNha == maNha }
                    if (index >= 0) {
                        spinnerHouse.setSelection(index + 1) // +1 vì có "Tất cả nhà" ở đầu
                    }
                } else {
                    spinnerHouse.setSelection(0) // Mặc định "Tất cả nhà"
                }
                
                // Listener khi chọn nhà
                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                        if (position == 0) {
                            // Chọn "Tất cả nhà"
                            maNha = -1L
                        } else {
                            // Chọn nhà cụ thể
                            maNha = danhSachNha[position - 1].maNha
                        }
                        taiDuLieu(view)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        view.findViewById<Button>(R.id.btnAddRoom).setOnClickListener {
            if (maNha <= 0) {
                // Nếu không có maNha, chuyển đến danh sách nhà để chọn
                Toast.makeText(context, "Vui lòng chọn nhà trước khi thêm phòng", Toast.LENGTH_LONG).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DanhSachNhaTroFragment())
                    .addToBackStack(null).commit()
            } else {
                val fragment = TaoPhongFragment().apply {
                    arguments = Bundle().apply { putLong("maNha", maNha) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            }
        }

        // Không gọi taiDuLieu ở đây vì spinner listener sẽ tự động gọi
    }

    override fun onResume() {
        super.onResume()
        view?.let { taiDuLieu(it) }
    }

    private fun taiDuLieu(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val danhSach = if (maNha > 0) {
                    dbManager.phongDao.layTheoNha(maNha)
                } else {
                    dbManager.phongDao.layTatCa()
                }
                
                // Lấy tên nhà nếu có maNha
                val tenNha = if (maNha > 0) {
                    dbManager.nhaTroDao.layTheoMa(maNha)?.tenNha ?: "Nhà trọ"
                } else {
                    "Tất cả nhà"
                }
                
                withContext(Dispatchers.Main) {
                    adapter.capNhatDanhSach(danhSach)
                    view.findViewById<TextView>(R.id.tvRoomCount)?.text = "$tenNha - ${danhSach.size} phòng"
                }
            } catch (e: Exception) {
                android.util.Log.e("DanhSachPhongFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun xoaPhong(phong: Phong, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = dbManager.phongDao.xoa(phong.maPhong)
                withContext(Dispatchers.Main) {
                    if (result > 0) {
                        Toast.makeText(context, "Đã xóa \"${phong.tenPhong}\" và dữ liệu liên quan", Toast.LENGTH_LONG).show()
                        taiDuLieu(view)
                    } else {
                        Toast.makeText(context, "Không thể xóa phòng", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

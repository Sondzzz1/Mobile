package com.example.btl_mobile_son

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.NhaTroAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.NhaTro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HouseListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: NhaTroAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_house_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())

        // Setup RecyclerView
        val rvHouseList = view.findViewById<RecyclerView>(R.id.rvHouseList)
        adapter = NhaTroAdapter(
            onItemClick = { nha ->
                // Mở danh sách phòng của nhà này
                val fragment = RoomListFragment().apply {
                    arguments = Bundle().apply { putLong("maNha", nha.maNha) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onEditClick = { nha ->
                // Mở fragment edit
                val fragment = CreateHouseFragment().apply {
                    arguments = Bundle().apply { putLong("maNha", nha.maNha) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onDeleteClick = { nha ->
                // Kiểm tra trước khi xóa
                CoroutineScope(Dispatchers.IO).launch {
                    val coPhong = dbManager.nhaTroDao.coThePhatSinhDuLieu(nha.maNha)
                    
                    withContext(Dispatchers.Main) {
                        if (coPhong) {
                            // Không cho xóa nếu đã có phòng
                            AlertDialog.Builder(requireContext())
                                .setTitle("Không thể xóa")
                                .setMessage(
                                    "Nhà trọ \"${nha.tenNha}\" đã có phòng và dữ liệu liên quan.\n\n" +
                                    "⚠️ Không thể xóa để đảm bảo tính toàn vẹn dữ liệu.\n\n" +
                                    "Nếu muốn ngừng sử dụng nhà này:\n" +
                                    "• Kết thúc tất cả hợp đồng\n" +
                                    "• Xóa tất cả phòng\n" +
                                    "• Sau đó mới có thể xóa nhà"
                                )
                                .setPositiveButton("Đóng", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                        } else {
                            // Cho phép xóa nếu chưa có phòng
                            AlertDialog.Builder(requireContext())
                                .setTitle("Xóa nhà trọ")
                                .setMessage(
                                    "Bạn có chắc muốn xóa \"${nha.tenNha}\"?\n\n" +
                                    "Nhà này chưa có phòng nào."
                                )
                                .setPositiveButton("Xóa") { _, _ ->
                                    xoaNhaTro(nha, view)
                                }
                                .setNegativeButton("Hủy", null)
                                .show()
                        }
                    }
                }
            }
        )
        rvHouseList.layoutManager = LinearLayoutManager(requireContext())
        rvHouseList.adapter = adapter

        // Nút back
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Nút tạo nhà mới
        view.findViewById<Button>(R.id.btnCreateHouse).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateHouseFragment())
                .addToBackStack(null).commit()
        }

        taiDuLieu(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { taiDuLieu(it) }
    }

    private fun taiDuLieu(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            val danhSach = dbManager.nhaTroDao.layTatCa()
            withContext(Dispatchers.Main) {
                adapter.capNhatDanhSach(danhSach)
                view.findViewById<TextView>(R.id.tvManagingCount)?.text =
                    "Bạn đang quản lý ${danhSach.size} nhà"
            }
        }
    }

    private fun xoaNhaTro(nha: NhaTro, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = dbManager.nhaTroDao.xoa(nha.maNha)
                
                withContext(Dispatchers.Main) {
                    if (result > 0) {
                        Toast.makeText(
                            context,
                            "✓ Đã xóa \"${nha.tenNha}\"",
                            Toast.LENGTH_SHORT
                        ).show()
                        taiDuLieu(view)
                    } else {
                        Toast.makeText(
                            context,
                            "⚠️ Không thể xóa nhà trọ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "⚠️ Lỗi: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

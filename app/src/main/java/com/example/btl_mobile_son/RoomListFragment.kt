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
import com.example.btl_mobile_son.adapter.PhongAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.Phong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: PhongAdapter
    private var maNha: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_room_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())
        maNha = arguments?.getLong("maNha", -1L) ?: -1L

        val rvRoomList = view.findViewById<RecyclerView>(R.id.rvRoomList)
        adapter = PhongAdapter(
            onItemClick = { phong ->
                val fragment = RoomDetailFragment().apply {
                    arguments = Bundle().apply { putLong("maPhong", phong.maPhong) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onEditClick = { phong ->
                val fragment = CreateRoomFragment().apply {
                    arguments = Bundle().apply {
                        putLong("maNha", maNha)
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

        view.findViewById<Button>(R.id.btnAddRoom).setOnClickListener {
            val fragment = CreateRoomFragment().apply {
                arguments = Bundle().apply { putLong("maNha", maNha) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
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
            val danhSach = if (maNha > 0) dbManager.phongDao.layTheoNha(maNha)
                           else dbManager.phongDao.layTatCa()
            withContext(Dispatchers.Main) {
                adapter.capNhatDanhSach(danhSach)
                view.findViewById<TextView>(R.id.tvRoomCount)?.text = "${danhSach.size} phòng"
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

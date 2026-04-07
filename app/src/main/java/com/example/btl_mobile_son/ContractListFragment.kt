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
import com.example.btl_mobile_son.adapter.HopDongAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContractListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: HopDongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contract_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("ContractListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val rvContractList = view.findViewById<RecyclerView>(R.id.rvContractList)
        adapter = HopDongAdapter(
            danhSach = emptyList(),
            onItemClick = { hopDong ->
                val fragment = CreateContractFragment().apply {
                    arguments = Bundle().apply { putLong("maHopDong", hopDong.maHopDong) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onItemLongClick = { hopDong ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa hợp đồng")
                    .setMessage("Xóa hợp đồng #${hopDong.maHopDong}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            // Cập nhật trạng thái những người trong hợp đồng này sang "da_chuyen_di"
                            // Sử dụng hopDongThanhVienDao vì khachThueDao không còn layTheoPhong
                            val dsThanhVien = dbManager.hopDongThanhVienDao.layTheoHopDong(hopDong.maHopDong)
                            for (tv in dsThanhVien) {
                                dbManager.hopDongThanhVienDao.capNhat(tv.copy(trangThai = "da_chuyen_di", ngayRoiDi = System.currentTimeMillis()))
                            }

                            dbManager.hopDongDao.xoa(hopDong.maHopDong)
                            
                            val hienTai = dbManager.hopDongDao.layHopDongDangThue(hopDong.maPhong)
                            if (hienTai == null) {
                                val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
                                phong?.let {
                                    dbManager.phongDao.capNhat(it.copy(trangThai = "trong"))
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã xóa hợp đồng và cập nhật trạng thái", Toast.LENGTH_SHORT).show()
                                taiDuLieu(view)
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null).show()
            }
        )
        rvContractList.layoutManager = LinearLayoutManager(requireContext())
        rvContractList.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnAddContract).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateContractFragment())
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
            try {
                val danhSachHopDong = dbManager.hopDongDao.layTatCa()
                
                // Load thông tin đầy đủ cho mỗi hợp đồng
                val danhSachHienThi = danhSachHopDong.map { hopDong ->
                    val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
                    val khach = dbManager.khachThueDao.layTheoMa(hopDong.maKhach)
                    val nha = phong?.let { dbManager.nhaTroDao.layTheoMa(it.maNha) }
                    
                    com.example.btl_mobile_son.adapter.HopDongDisplay(
                        hopDong = hopDong,
                        tenPhong = phong?.tenPhong ?: "Phòng #${hopDong.maPhong}",
                        tenKhach = khach?.hoTen ?: "Khách #${hopDong.maKhach}",
                        tenNha = nha?.tenNha ?: "Nhà trọ"
                    )
                }
                
                withContext(Dispatchers.Main) {
                    adapter.capNhatDanhSach(danhSachHienThi)
                    view.findViewById<TextView>(R.id.tvContractCount)?.text = "${danhSachHienThi.size}"
                }
            } catch (e: Exception) {
                android.util.Log.e("ContractListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

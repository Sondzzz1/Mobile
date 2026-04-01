package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.DatCocAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class DepositListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: DatCocAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_deposit_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("DepositListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        // Layout dùng rvDepositList và tvTotalDeposit
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDepositList)
        val tvTotalDeposit = view.findViewById<TextView>(R.id.tvTotalDeposit)

        adapter = DatCocAdapter(emptyList(),
            onItemClick = { datCoc ->
                val fragment = CreateDepositFragment().apply {
                    arguments = Bundle().apply { putLong("maDatCoc", datCoc.maDatCoc) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onItemLongClick = { datCoc ->
                // Hiển thị dialog với nhiều lựa chọn
                val options = arrayOf(
                    "Hủy đặt cọc (khách hủy)",
                    "Mất cọc (vi phạm)",
                    "Hoàn cọc",
                    "Sửa thông tin"
                )
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Quản lý đặt cọc: ${datCoc.tenKhach}")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> huyDatCoc(datCoc, "da_huy", "Khách hủy", recyclerView, tvTotalDeposit)
                            1 -> huyDatCoc(datCoc, "mat_coc", "Mất cọc", recyclerView, tvTotalDeposit)
                            2 -> huyDatCoc(datCoc, "da_hoan", "Hoàn cọc", recyclerView, tvTotalDeposit)
                            3 -> {
                                // Sửa thông tin
                                val fragment = CreateDepositFragment().apply {
                                    arguments = Bundle().apply { putLong("maDatCoc", datCoc.maDatCoc) }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null).commit()
                            }
                        }
                    }
                    .setNegativeButton("Đóng", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnAddDeposit).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateDepositFragment())
                .addToBackStack(null).commit()
        }

        taiDuLieu(recyclerView, tvTotalDeposit)
    }

    override fun onResume() {
        super.onResume()
        val rv = view?.findViewById<RecyclerView>(R.id.rvDepositList) ?: return
        val tvTotal = view?.findViewById<TextView>(R.id.tvTotalDeposit) ?: return
        taiDuLieu(rv, tvTotal)
    }

    private fun taiDuLieu(recyclerView: RecyclerView, tvTotal: TextView?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ds = dbManager.datCocDao.layTatCa()
                val tong = dbManager.datCocDao.tinhTongDatCoc()
                withContext(Dispatchers.Main) {
                    adapter.capNhatDanhSach(ds)
                    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                    tvTotal?.text = "${fmt.format(tong)} d"
                }
            } catch (e: Exception) {
                android.util.Log.e("DepositListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Hủy đặt cọc với lý do cụ thể
     * KHÔNG xóa bản ghi, chỉ cập nhật trạng thái để giữ lịch sử
     */
    private fun huyDatCoc(
        datCoc: com.example.btl_mobile_son.data.model.DatCoc,
        trangThaiMoi: String,
        lyDo: String,
        recyclerView: RecyclerView,
        tvTotal: TextView?
    ) {
        val thongBao = when (trangThaiMoi) {
            "da_huy" -> "Bạn có chắc muốn hủy đặt cọc?\n\nKhách: ${datCoc.tenKhach}\nSố tiền: ${datCoc.tienDatCoc.toLong()}đ\n\nPhòng sẽ trở về trạng thái trống."
            "mat_coc" -> "Xác nhận mất cọc?\n\nKhách: ${datCoc.tenKhach}\nSố tiền: ${datCoc.tienDatCoc.toLong()}đ\n\n⚠️ Khách sẽ mất tiền đặt cọc do vi phạm."
            "da_hoan" -> "Xác nhận hoàn cọc?\n\nKhách: ${datCoc.tenKhach}\nSố tiền: ${datCoc.tienDatCoc.toLong()}đ\n\n✓ Tiền sẽ được hoàn lại cho khách."
            else -> "Xác nhận thao tác?"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(lyDo)
            .setMessage(thongBao)
            .setPositiveButton("Xác nhận") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Lấy thông tin phòng
                    val phong = dbManager.phongDao.layTheoMa(datCoc.maPhong)
                    
                    // Cập nhật trạng thái đặt cọc (KHÔNG xóa)
                    dbManager.datCocDao.capNhat(
                        datCoc.copy(
                            trangThai = trangThaiMoi,
                            ghiChu = "${datCoc.ghiChu}\n[$lyDo: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date())}]"
                        )
                    )
                    
                    // Cập nhật trạng thái phòng về "trong" (trừ trường hợp đã chuyển hợp đồng)
                    if (trangThaiMoi != "da_chuyen_hop_dong") {
                        phong?.let {
                            if (it.trangThai == "dat_coc") {
                                dbManager.phongDao.capNhat(it.copy(trangThai = "trong"))
                            }
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        val icon = when (trangThaiMoi) {
                            "da_huy" -> "✓"
                            "mat_coc" -> "⚠️"
                            "da_hoan" -> "✓"
                            else -> "ℹ️"
                        }
                        
                        Toast.makeText(
                            context,
                            "$icon $lyDo thành công\n" +
                            "✓ Đã lưu lịch sử\n" +
                            "✓ Phòng ${phong?.tenPhong ?: ""} đã trở về trạng thái trống",
                            Toast.LENGTH_LONG
                        ).show()
                        taiDuLieu(recyclerView, tvTotal)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

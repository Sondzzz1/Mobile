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
import com.example.btl_mobile_son.adapter.HoaDonAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class InvoiceListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: HoaDonAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invoice_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())

        val rvInvoiceList = view.findViewById<RecyclerView>(R.id.rvInvoiceList)
        adapter = HoaDonAdapter(
            onItemClick = { hoaDon ->
                val statusText = when (hoaDon.trangThai) {
                    "da_thanh_toan" -> "Đã thanh toán"
                    "thanh_toan_mot_phan" -> "Thanh toán một phần"
                    "qua_han" -> "Quá hạn"
                    else -> "Chưa thanh toán"
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("Hóa đơn tháng ${hoaDon.thang}/${hoaDon.nam}")
                    .setMessage("Tổng tiền: ${String.format("%,.0f", hoaDon.tongTien)} đ\nTrạng thái: $statusText")
                    .setPositiveButton(if (hoaDon.trangThai == "da_thanh_toan") "Đóng" else "Đánh dấu đã TT") { _, _ ->
                        if (hoaDon.trangThai != "da_thanh_toan") {
                            CoroutineScope(Dispatchers.IO).launch {
                                // Đánh dấu hóa đơn đã thanh toán
                                dbManager.hoaDonDao.danhDauDaThanhToan(hoaDon.maHoaDon)
                                
                                // Tạo giao dịch thu tương ứng
                                val hopDong = dbManager.hopDongDao.layTheoMa(hoaDon.maHopDong)
                                val khach = hopDong?.let { dbManager.khachThueDao.layTheoMa(it.maKhach) }
                                val phong = hopDong?.let { dbManager.phongDao.layTheoMa(it.maPhong) }
                                
                                dbManager.giaoDichDao.them(
                                    com.example.btl_mobile_son.data.model.GiaoDich(
                                        loai = "thu",
                                        maPhong = hopDong?.maPhong,
                                        soTien = hoaDon.tongTien,
                                        danhMuc = "Tiền thuê phòng",
                                        ngayGiaoDich = System.currentTimeMillis(),
                                        noiDung = "Thu tiền phòng ${phong?.tenPhong ?: ""} tháng ${hoaDon.thang}/${hoaDon.nam}",
                                        tenNguoi = khach?.hoTen ?: "",
                                        phuongThucThanhToan = "tien_mat"
                                    )
                                )
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Đã cập nhật và tạo giao dịch thu", Toast.LENGTH_SHORT).show()
                                    taiDuLieu(view)
                                }
                            }
                        }
                    }
                    .setNegativeButton("Đóng", null).show()
            },
            onItemLongClick = { hoaDon ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa hóa đơn")
                    .setMessage("Xóa hóa đơn tháng ${hoaDon.thang}/${hoaDon.nam}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.hoaDonDao.xoa(hoaDon.maHoaDon)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                taiDuLieu(view)
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null).show()
            }
        )
        rvInvoiceList.layoutManager = LinearLayoutManager(requireContext())
        rvInvoiceList.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnCreateInvoice).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateInvoiceFragment())
                .addToBackStack(null).commit()
        }

        taiDuLieu(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { taiDuLieu(it) }
    }

    private fun taiDuLieu(view: View) {
        val cal = Calendar.getInstance()
        val thang = cal.get(Calendar.MONTH) + 1
        val nam = cal.get(Calendar.YEAR)
        CoroutineScope(Dispatchers.IO).launch {
            val danhSach = dbManager.hoaDonDao.layTatCa()
            val tongChuaTT = dbManager.hoaDonDao.tinhTongChuaThanhToan()
            withContext(Dispatchers.Main) {
                adapter.capNhatDanhSach(danhSach)
                view.findViewById<TextView>(R.id.tvTotalInvoices)?.text = "Tổng tiền (${danhSach.size} hóa đơn)"
                view.findViewById<TextView>(R.id.tvTotalAmount)?.text = "${String.format("%,.0f", tongChuaTT)} đ"
            }
        }
    }
}

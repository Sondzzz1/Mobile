package com.example.btl_mobile_son

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.HopDong
import com.example.btl_mobile_son.data.model.KhachThue
import com.example.btl_mobile_son.data.model.NhaTro
import com.example.btl_mobile_son.data.model.Phong
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomDetailFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maPhong: Long = -1L
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Dữ liệu cache
    private var phong: Phong? = null
    private var nha: NhaTro? = null
    private var hopDongHienTai: HopDong? = null
    private var danhSachKhachThue: List<KhachThue> = emptyList()
    private var lichSuHopDong: List<HopDong> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager.getInstance(requireContext())
        maPhong = arguments?.getLong("maPhong", -1L) ?: -1L

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val contentContainer = view.findViewById<FrameLayout>(R.id.contentContainer)

        // Tải dữ liệu rồi hiển thị tab đầu tiên
        taiDuLieu {
            hienThiTabThongTin(contentContainer)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> hienThiTabThongTin(contentContainer)
                    1 -> hienThiTabKhachThue(contentContainer)
                    2 -> hienThiTabHopDong(contentContainer)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun taiDuLieu(onDone: () -> Unit) {
        if (maPhong < 0) { onDone(); return }
        CoroutineScope(Dispatchers.IO).launch {
            phong = dbManager.phongDao.layTheoMa(maPhong)
            phong?.let { p ->
                nha = dbManager.nhaTroDao.layTheoMa(p.maNha)
            }
            hopDongHienTai = dbManager.hopDongDao.layHopDongDangThue(maPhong)
            
            // Lấy danh sách khách thuê đang ở trong phòng từ hợp đồng hiện tại
            val tvs = hopDongHienTai?.let { dbManager.hopDongThanhVienDao.layNguoiDangOTheoHopDong(it.maHopDong) } ?: emptyList()
            danhSachKhachThue = tvs.mapNotNull { tv -> dbManager.khachThueDao.layTheoMa(tv.maKhach) }
            
            lichSuHopDong = dbManager.hopDongDao.layTheoPhong(maPhong)
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    private fun hienThiTabThongTin(container: FrameLayout) {
        container.removeAllViews()
        val v = layoutInflater.inflate(R.layout.layout_room_info, container, false)

        phong?.let { p ->
            v.findViewById<TextView>(R.id.tvTenNha)?.text = nha?.tenNha ?: "--"
            v.findViewById<TextView>(R.id.tvTenPhong)?.text = p.tenPhong
            v.findViewById<TextView>(R.id.tvGiaCoBan)?.text = "${String.format("%,.0f", p.giaCoBan)} đ/tháng"
            v.findViewById<TextView>(R.id.tvDienTich)?.text = "${p.dienTichM2} m²"
            v.findViewById<TextView>(R.id.tvSoNguoi)?.text = "${p.soNguoiToiDa} người"
            val tvTrangThai = v.findViewById<TextView>(R.id.tvTrangThai)
            if (p.trangThai == "trong") {
                tvTrangThai?.text = "Còn trống"
                tvTrangThai?.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                tvTrangThai?.text = "Đã thuê"
                tvTrangThai?.setTextColor(Color.parseColor("#F44336"))
            }
        }

        container.addView(v)
    }

    private fun hienThiTabKhachThue(container: FrameLayout) {
        container.removeAllViews()
        val v = layoutInflater.inflate(R.layout.layout_room_tenant, container, false)

        val cardKhach = v.findViewById<View>(R.id.cardKhachThue)
        val layoutEmpty = v.findViewById<View>(R.id.layoutEmpty)
        val rvKhach = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvKhachThueInRoom)

        if (danhSachKhachThue.isNotEmpty()) {
            cardKhach.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            rvKhach?.visibility = View.VISIBLE
            rvKhach?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            rvKhach?.adapter = com.example.btl_mobile_son.adapter.KhachThueAdapter(
                danhSach = danhSachKhachThue,
                onItemClick = { khach ->
                    val fragment = CreateTenantFragment().apply {
                        arguments = Bundle().apply { putLong("maKhach", khach.maKhach) }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit()
                },
                onItemLongClick = {}
            )
        } else {
            cardKhach.visibility = View.GONE
            rvKhach?.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        }

        container.addView(v)
    }

    private fun hienThiTabHopDong(container: FrameLayout) {
        container.removeAllViews()
        val v = layoutInflater.inflate(R.layout.layout_room_contracts, container, false)

        val cardHD = v.findViewById<View>(R.id.cardHopDongHienTai)
        val layoutEmpty = v.findViewById<View>(R.id.layoutEmpty)
        val tvLichSuTitle = v.findViewById<TextView>(R.id.tvLichSuTitle)
        val rvLichSu = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLichSuHopDong)

        if (hopDongHienTai != null) {
            cardHD.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            val hd = hopDongHienTai!!
            v.findViewById<TextView>(R.id.tvNgayBatDau)?.text = sdf.format(Date(hd.ngayBatDau))
            v.findViewById<TextView>(R.id.tvNgayKetThuc)?.text = sdf.format(Date(hd.ngayKetThuc))
            v.findViewById<TextView>(R.id.tvGiaThue)?.text = "${String.format("%,.0f", hd.giaThueThang)} đ/tháng"
            v.findViewById<TextView>(R.id.tvTienCoc)?.text = "${String.format("%,.0f", hd.tienDatCoc)} đ"
            val tvTrangThai = v.findViewById<TextView>(R.id.tvTrangThaiHD)
            tvTrangThai?.text = when (hd.trangThai) {
                "dang_thue" -> "Đang thuê"
                "het_han" -> "Hết hạn"
                else -> "Đã hủy"
            }
        } else {
            cardHD.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        }

        // Lịch sử hợp đồng (các hợp đồng cũ)
        val lichSuCu = lichSuHopDong.filter { it.maHopDong != (hopDongHienTai?.maHopDong ?: -1L) }
        if (lichSuCu.isNotEmpty()) {
            tvLichSuTitle.visibility = View.VISIBLE
            rvLichSu.visibility = View.VISIBLE
            rvLichSu.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            rvLichSu.adapter = com.example.btl_mobile_son.adapter.HopDongAdapter(
                danhSach = lichSuCu,
                onItemClick = {},
                onItemLongClick = {}
            )
        }

        container.addView(v)
    }
}

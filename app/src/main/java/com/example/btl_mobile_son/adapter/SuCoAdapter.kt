package com.example.btl_mobile_son.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.SuCo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SuCoAdapter(
    private val danhSach: List<SuCo>,
    private val dbManager: DatabaseManager,
    private val onItemClick: (SuCo) -> Unit
) : RecyclerView.Adapter<SuCoAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPhong: TextView = view.findViewById(R.id.tvPhong)
        val tvLoaiSuCo: TextView = view.findViewById(R.id.tvLoaiSuCo)
        val tvMoTa: TextView = view.findViewById(R.id.tvMoTa)
        val tvNgayBaoGao: TextView = view.findViewById(R.id.tvNgayBaoGao)
        val tvTrangThai: TextView = view.findViewById(R.id.tvTrangThai)
        val tvChiPhi: TextView = view.findViewById(R.id.tvChiPhi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_su_co, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suCo = danhSach[position]
        
        // Load tên phòng
        CoroutineScope(Dispatchers.IO).launch {
            val phong = dbManager.phongDao.layTheoMa(suCo.maPhong.toLong())
            withContext(Dispatchers.Main) {
                holder.tvPhong.text = phong?.tenPhong ?: "Phòng #${suCo.maPhong}"
            }
        }
        
        holder.tvLoaiSuCo.text = suCo.loaiSuCo
        holder.tvMoTa.text = if (suCo.moTa.isNotEmpty()) suCo.moTa else "Không có mô tả"
        holder.tvNgayBaoGao.text = sdf.format(Date(suCo.ngayBaoGao))
        holder.tvChiPhi.text = "${formatter.format(suCo.chiPhi)}đ"
        
        // Trạng thái
        when (suCo.trangThai) {
            "chua_xu_ly" -> {
                holder.tvTrangThai.text = "Chưa xử lý"
                holder.tvTrangThai.setBackgroundColor(Color.parseColor("#F44336"))
            }
            "dang_xu_ly" -> {
                holder.tvTrangThai.text = "Đang xử lý"
                holder.tvTrangThai.setBackgroundColor(Color.parseColor("#FF9800"))
            }
            "da_xu_ly" -> {
                holder.tvTrangThai.text = "Đã xử lý"
                holder.tvTrangThai.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(suCo)
        }
    }

    override fun getItemCount() = danhSach.size
}

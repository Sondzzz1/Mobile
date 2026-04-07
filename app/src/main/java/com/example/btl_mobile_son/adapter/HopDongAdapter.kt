package com.example.btl_mobile_son.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.HopDong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class để chứa thông tin đầy đủ của hợp đồng
data class HopDongDisplay(
    val hopDong: HopDong,
    val tenPhong: String,
    val tenKhach: String,
    val tenNha: String
)

class HopDongAdapter(
    private var danhSach: List<HopDongDisplay> = emptyList(),
    private val onItemClick: (HopDong) -> Unit,
    private val onItemLongClick: (HopDong) -> Unit
) : RecyclerView.Adapter<HopDongAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenantName: TextView = view.findViewById(R.id.tvTenantName)
        val tvDateIn: TextView = view.findViewById(R.id.tvDateIn)
        val tvDateOut: TextView = view.findViewById(R.id.tvDateOut)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvRoomInfo: TextView = view.findViewById(R.id.tvRoomInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contract, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = danhSach[position]
        val hd = item.hopDong
        
        holder.tvTenantName.text = "${item.tenKhach} (HĐ #${hd.maHopDong})"
        holder.tvDateIn.text = "Từ: ${sdf.format(Date(hd.ngayBatDau))}"
        holder.tvDateOut.text = "Đến: ${sdf.format(Date(hd.ngayKetThuc))}"
        holder.tvRoomInfo.text = "${item.tenNha} - ${item.tenPhong} | ${String.format("%,.0f", hd.giaThueThang.toDouble())}đ/tháng"
        
        // Màu sắc theo trạng thái
        when (hd.trangThai) {
            "dang_thue" -> {
                holder.tvStatus.text = "ĐANG THUÊ"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.tvStatus.setTextColor(Color.WHITE)
            }
            "het_han" -> {
                holder.tvStatus.text = "HẾT HẠN"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
                holder.tvStatus.setTextColor(Color.WHITE)
            }
            else -> {
                holder.tvStatus.text = "ĐÃ HỦY"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"))
                holder.tvStatus.setTextColor(Color.WHITE)
            }
        }
        
        holder.itemView.setOnClickListener { onItemClick(hd) }
        holder.itemView.setOnLongClickListener { onItemLongClick(hd); true }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<HopDongDisplay>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

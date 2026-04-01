package com.example.btl_mobile_son.adapter

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

class HopDongAdapter(
    private var danhSach: List<HopDong> = emptyList(),
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
        val hd = danhSach[position]
        holder.tvTenantName.text = "Hợp đồng #${hd.maHopDong}"
        holder.tvDateIn.text = sdf.format(Date(hd.ngayBatDau))
        holder.tvDateOut.text = sdf.format(Date(hd.ngayKetThuc))
        holder.tvRoomInfo.text = "Phòng #${hd.maPhong} | ${String.format("%,.0f", hd.giaThueThang)} đ/tháng"
        holder.tvStatus.text = when (hd.trangThai) {
            "dang_thue" -> "Đang thuê"
            "het_han" -> "Hết hạn"
            else -> "Đã hủy"
        }
        holder.itemView.setOnClickListener { onItemClick(hd) }
        holder.itemView.setOnLongClickListener { onItemLongClick(hd); true }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<HopDong>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

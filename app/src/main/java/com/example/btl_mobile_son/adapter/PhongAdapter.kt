package com.example.btl_mobile_son.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.Phong

class PhongAdapter(
    private var danhSach: List<Phong> = emptyList(),
    private val onItemClick: (Phong) -> Unit,
    private val onEditClick: (Phong) -> Unit,
    private val onDeleteClick: (Phong) -> Unit
) : RecyclerView.Adapter<PhongAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenPhong: TextView = view.findViewById(R.id.tvRoomName)
        val tvGia: TextView = view.findViewById(R.id.tvPrice)
        val tvTrangThai: TextView = view.findViewById(R.id.tvRoomStatus)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phong = danhSach[position]
        holder.tvTenPhong.text = phong.tenPhong
        holder.tvGia.text = "${String.format("%,.0f", phong.giaCoBan.toDouble())} đ/tháng"
        
        // Màu sắc rõ ràng cho trạng thái phòng
        if (phong.trangThai == "trong") {
            holder.tvTrangThai.text = "CÒN TRỐNG"
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#4CAF50")) // Xanh lá
            holder.tvTrangThai.setTextColor(Color.WHITE)
        } else {
            holder.tvTrangThai.text = "ĐÃ THUÊ"
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#F44336")) // Đỏ
            holder.tvTrangThai.setTextColor(Color.WHITE)
        }
        
        holder.itemView.setOnClickListener { onItemClick(phong) }
        holder.btnEdit.setOnClickListener { onEditClick(phong) }
        holder.btnDelete.setOnClickListener { onDeleteClick(phong) }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<Phong>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

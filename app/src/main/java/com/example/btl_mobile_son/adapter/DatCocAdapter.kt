package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.DatCoc

class DatCocAdapter(
    private var danhSach: List<DatCoc> = emptyList(),
    private val onItemClick: (DatCoc) -> Unit,
    private val onItemLongClick: (DatCoc) -> Unit
) : RecyclerView.Adapter<DatCocAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvTenantInitials)
        val tvTenKhach: TextView = view.findViewById(R.id.tvTenantName)
        val tvPhong: TextView = view.findViewById(R.id.tvRoomInfo)
        val tvTien: TextView = view.findViewById(R.id.tvDepositAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deposit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dc = danhSach[position]
        holder.tvTenKhach.text = dc.tenKhach
        holder.tvPhong.text = "Phòng #${dc.maPhong} | ${dc.soDienThoai}"
        holder.tvTien.text = "${String.format("%,.0f", dc.tienDatCoc)} đ"
        val initials = dc.tenKhach.split(" ").takeLast(2).joinToString("") { it.first().toString() }
        holder.tvInitials.text = initials.uppercase()
        holder.itemView.setOnClickListener { onItemClick(dc) }
        holder.itemView.setOnLongClickListener { onItemLongClick(dc); true }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<DatCoc>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

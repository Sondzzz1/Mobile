package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.KhachThue

class KhachThueAdapter(
    private var danhSach: List<KhachThue> = emptyList(),
    private val onItemClick: (KhachThue) -> Unit,
    private val onItemLongClick: (KhachThue) -> Unit
) : RecyclerView.Adapter<KhachThueAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvTenantInitials)
        val tvHoTen: TextView = view.findViewById(R.id.tvTenantName)
        val tvSdt: TextView = view.findViewById(R.id.tvTenantPhone)
        val tvPhong: TextView = view.findViewById(R.id.tvTenantRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tenant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val khach = danhSach[position]
        holder.tvHoTen.text = khach.hoTen
        holder.tvSdt.text = khach.soDienThoai.ifEmpty { "Chưa có SĐT" }
        
        // Không hiển thị phòng và trạng thái vì đã bỏ khỏi model
        holder.tvPhong.text = "Khách thuê"
        
        // Lấy 2 chữ cái đầu làm avatar
        val initials = khach.hoTen.split(" ").takeLast(2).joinToString("") { it.first().toString() }
        holder.tvInitials.text = initials.uppercase()
        holder.itemView.setOnClickListener { onItemClick(khach) }
        holder.itemView.setOnLongClickListener { onItemLongClick(khach); true }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<KhachThue>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

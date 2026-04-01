package com.example.btl_mobile_son.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.HoaDon

class HoaDonAdapter(
    private var danhSach: List<HoaDon> = emptyList(),
    private val onItemClick: (HoaDon) -> Unit,
    private val onItemLongClick: (HoaDon) -> Unit
) : RecyclerView.Adapter<HoaDonAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenantName: TextView = view.findViewById(R.id.tvTenantName)
        val tvRoomInfo: TextView = view.findViewById(R.id.tvRoomInfo)
        val tvAmount: TextView = view.findViewById(R.id.tvInvoiceAmount)
        val tvStatus: TextView = view.findViewById(R.id.tvInvoiceStatus)
        val tvRoomCost: TextView = view.findViewById(R.id.tvRoomCost)
        val tvServiceCost: TextView = view.findViewById(R.id.tvServiceCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invoice, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hd = danhSach[position]
        holder.tvTenantName.text = "Hóa đơn tháng ${hd.thang}/${hd.nam}"
        holder.tvRoomInfo.text = "Hợp đồng #${hd.maHopDong}"
        holder.tvAmount.text = "${String.format("%,.0f", hd.tongTien)} đ"
        holder.tvRoomCost.text = "${String.format("%,.0f", hd.tienPhong)} đ"
        holder.tvServiceCost.text = "${String.format("%,.0f", hd.tongTienDichVu)} đ"
        if (hd.daThanhToan) {
            holder.tvStatus.text = "Đã thanh toán"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.text = "Chưa thanh toán"
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
        }
        holder.itemView.setOnClickListener { onItemClick(hd) }
        holder.itemView.setOnLongClickListener { onItemLongClick(hd); true }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<HoaDon>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.DichVu
import java.text.NumberFormat
import java.util.Locale

class DichVuAdapter(
    private val danhSach: List<DichVu>,
    private val onEdit: (DichVu) -> Unit,
    private val onDelete: (DichVu) -> Unit
) : RecyclerView.Adapter<DichVuAdapter.ViewHolder>() {

    private val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenDichVu: TextView = view.findViewById(R.id.tvTenDichVu)
        val tvDonGia: TextView = view.findViewById(R.id.tvDonGia)
        val tvDonVi: TextView = view.findViewById(R.id.tvDonVi)
        val tvLoai: TextView = view.findViewById(R.id.tvLoai)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dich_vu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dv = danhSach[position]
        // Hiển thị tên dịch vụ kèm đơn giá để phân biệt các dịch vụ cùng tên
        holder.tvTenDichVu.text = "${dv.tenDichVu} - ${fmt.format(dv.donGia.toLong())}đ"
        holder.tvDonGia.text = "${fmt.format(dv.donGia.toLong())}đ/${dv.donVi.ifEmpty { "lần" }}"
        holder.tvDonVi.text = dv.donVi.ifEmpty { "lần" }
        holder.tvLoai.text = when (dv.loaiDichVu) {
            "dien" -> "Điện"
            "nuoc" -> "Nước"
            else -> "Khác"
        }
        holder.btnEdit.setOnClickListener { onEdit(dv) }
        holder.btnDelete.setOnClickListener { onDelete(dv) }
    }

    override fun getItemCount() = danhSach.size
}

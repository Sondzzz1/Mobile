package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.GiaoDich
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GiaoDichAdapter(
    private val danhSach: List<GiaoDich>,
    private val onEdit: (GiaoDich) -> Unit,
    private val onDelete: (GiaoDich) -> Unit
) : RecyclerView.Adapter<GiaoDichAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNoiDung: TextView = view.findViewById(R.id.tvNoiDung)
        val tvSoTien: TextView = view.findViewById(R.id.tvSoTien)
        val tvNgay: TextView = view.findViewById(R.id.tvNgay)
        val tvDanhMuc: TextView = view.findViewById(R.id.tvDanhMuc)
        val tvTenNguoi: TextView = view.findViewById(R.id.tvTenNguoi)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_giao_dich, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gd = danhSach[position]
        holder.tvNoiDung.text = gd.noiDung.ifEmpty { gd.danhMuc }
        holder.tvSoTien.text = "${fmt.format(gd.soTien)} d"
        holder.tvNgay.text = if (gd.ngayGiaoDich > 0) sdf.format(Date(gd.ngayGiaoDich)) else "--"
        holder.tvDanhMuc.text = gd.danhMuc
        holder.tvTenNguoi.text = gd.tenNguoi
        holder.btnEdit.setOnClickListener { onEdit(gd) }
        holder.btnDelete.setOnClickListener { onDelete(gd) }
    }

    override fun getItemCount() = danhSach.size
}

package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.ChiSoDienNuoc
import java.text.NumberFormat
import java.util.Locale

// CẢI TIẾN 2: Data class để hiển thị đầy đủ thông tin
data class ChiSoDisplay(
    val chiSo: ChiSoDienNuoc,
    val tenPhong: String,
    val tenNha: String
)

class ChiSoAdapter(
    private val danhSach: List<ChiSoDisplay>,
    private val onEdit: (ChiSoDienNuoc) -> Unit,
    private val onDelete: (ChiSoDienNuoc) -> Unit
) : RecyclerView.Adapter<ChiSoAdapter.ViewHolder>() {

    private val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPhong: TextView = view.findViewById(R.id.tvPhong)
        val tvLoai: TextView = view.findViewById(R.id.tvLoai)
        val tvThangNam: TextView = view.findViewById(R.id.tvThangNam)
        val tvChiSo: TextView = view.findViewById(R.id.tvChiSo)
        val tvThanhTien: TextView = view.findViewById(R.id.tvThanhTien)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chi_so, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = danhSach[position]
        val cs = item.chiSo
        
        // CẢI TIẾN 2: Hiển thị tên phòng đầy đủ
        holder.tvPhong.text = "${item.tenPhong} - ${item.tenNha}"
        holder.tvLoai.text = if (cs.loai == "dien") "Điện" else "Nước"
        holder.tvThangNam.text = "Tháng ${cs.thang}/${cs.nam}"
        val tieu_thu = cs.chiSoMoi - cs.chiSoCu
        holder.tvChiSo.text = "${cs.chiSoCu.toInt()} → ${cs.chiSoMoi.toInt()} (${tieu_thu.toInt()})"
        val thanhTien = tieu_thu * cs.donGia
        holder.tvThanhTien.text = "${fmt.format(thanhTien.toLong())}đ"
        holder.btnEdit.setOnClickListener { onEdit(cs) }
        holder.btnDelete.setOnClickListener { onDelete(cs) }
    }

    override fun getItemCount() = danhSach.size
}

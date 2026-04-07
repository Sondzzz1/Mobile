package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.DichVu
import java.text.NumberFormat
import java.util.Locale

data class DichVuPhongItem(
    val dichVu: DichVu,
    var isChecked: Boolean
)

class PhongDichVuAdapter(
    private val danhSach: List<DichVuPhongItem>,
    private val onCheckChanged: (DichVu, Boolean) -> Unit
) : RecyclerView.Adapter<PhongDichVuAdapter.ViewHolder>() {

    private fun formatMoney(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${formatter.format(amount)}đ"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbDichVu: CheckBox = view.findViewById(R.id.cbDichVu)
        val tvTenDichVu: TextView = view.findViewById(R.id.tvTenDichVu)
        val tvThongTinDichVu: TextView = view.findViewById(R.id.tvThongTinDichVu)
        val tvDonGia: TextView = view.findViewById(R.id.tvDonGia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phong_dich_vu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = danhSach[position]
        val dv = item.dichVu
        
        holder.tvTenDichVu.text = dv.tenDichVu
        holder.tvDonGia.text = formatMoney(dv.donGia)
        
        val cachTinhText = when (dv.cachTinh) {
            "theo_phong" -> "theo phòng"
            "theo_nguoi" -> "theo người"
            "theo_thang" -> "theo tháng"
            "mot_lan" -> "một lần"
            else -> ""
        }
        holder.tvThongTinDichVu.text = "${formatMoney(dv.donGia)}/$cachTinhText"
        
        // Set checkbox state without triggering listener
        holder.cbDichVu.setOnCheckedChangeListener(null)
        holder.cbDichVu.isChecked = item.isChecked
        
        // Set listener
        holder.cbDichVu.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            onCheckChanged(dv, isChecked)
        }
        
        // Click on item also toggles checkbox
        holder.itemView.setOnClickListener {
            holder.cbDichVu.isChecked = !holder.cbDichVu.isChecked
        }
    }

    override fun getItemCount() = danhSach.size
}

package com.example.btl_mobile_son.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.R
import com.example.btl_mobile_son.data.model.NhaTro

class NhaTroAdapter(
    private var danhSach: List<NhaTro> = emptyList(),
    private val onItemClick: (NhaTro) -> Unit,
    private val onEditClick: (NhaTro) -> Unit,
    private val onDeleteClick: (NhaTro) -> Unit
) : RecyclerView.Adapter<NhaTroAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenNha: TextView = view.findViewById(R.id.tvHouseName)
        val tvDiaChi: TextView = view.findViewById(R.id.tvHouseAddress)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nha = danhSach[position]
        holder.tvTenNha.text = nha.tenNha
        holder.tvDiaChi.text = nha.diaChi.ifEmpty { "Chưa có địa chỉ" }
        holder.itemView.setOnClickListener { onItemClick(nha) }
        holder.btnEdit.setOnClickListener { onEditClick(nha) }
        holder.btnDelete.setOnClickListener { onDeleteClick(nha) }
    }

    override fun getItemCount() = danhSach.size

    fun capNhatDanhSach(list: List<NhaTro>) {
        danhSach = list
        notifyDataSetChanged()
    }
}

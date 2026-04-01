package com.example.btl_mobile_son.data.model

data class DatCoc(
    val maDatCoc: Long = 0,
    val maPhong: Long,
    val tenKhach: String,
    val soDienThoai: String = "",
    val soCmnd: String = "",
    val email: String = "",
    val tienDatCoc: Long,  // Đổi Double → Long (VNĐ)
    val giaPhong: Long = 0,  // Đổi Double → Long (VNĐ)
    val ngayDuKienVao: Long = 0,
    val trangThai: String = "hieu_luc", // "hieu_luc" | "da_chuyen_hop_dong" | "da_huy" | "mat_coc" | "da_hoan"
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)

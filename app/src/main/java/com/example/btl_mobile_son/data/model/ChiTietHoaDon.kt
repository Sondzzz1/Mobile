package com.example.btl_mobile_son.data.model

data class ChiTietHoaDon(
    val maChiTiet: Long = 0,
    val maHoaDon: Long,
    val tenDichVu: String,
    val soLuong: Double = 0.0,
    val donGia: Double = 0.0,
    val thanhTien: Double = 0.0
)

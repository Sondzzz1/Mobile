package com.example.btl_mobile_son.data.model

/**
 * VĐ1: Bảng liên kết giữa Phòng và Dịch vụ
 * Cho phép mỗi phòng có các dịch vụ riêng với giá riêng
 */
data class PhongDichVu(
    val maPhongDichVu: Long = 0,
    val maPhong: Long,
    val maDichVu: Long,
    val donGiaRieng: Double? = null,  // Nếu null thì dùng giá mặc định từ DichVu
    val ghiChu: String = ""
)

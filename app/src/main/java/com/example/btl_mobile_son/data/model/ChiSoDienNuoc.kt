package com.example.btl_mobile_son.data.model

data class ChiSoDienNuoc(
    val maChiSo: Long = 0,
    val maPhong: Long,
    val loai: String, // dien / nuoc
    val thang: Int,
    val nam: Int,
    val chiSoCu: Long = 0,  // Đổi Double → Long (chỉ số nguyên)
    val chiSoMoi: Long = 0,  // Đổi Double → Long (chỉ số nguyên)
    val soTieuThu: Long = 0,  // Đổi Double → Long (chiSoMoi - chiSoCu)
    val donGia: Long = 0,  // Đổi Double → Long (VNĐ/đơn vị)
    val ghiChu: String = ""
)

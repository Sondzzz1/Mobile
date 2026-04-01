package com.example.btl_mobile_son.data.model

data class Phong(
    val maPhong: Long = 0,
    val maNha: Long,
    val tenPhong: String,
    val dienTichM2: Float = 0f,
    val giaCoBan: Long = 0,  // Đổi Double → Long (VNĐ)
    val trangThai: String = "trong", // trong / da_thue
    val soNguoiToiDa: Int = 1,
    val ghiChu: String = ""
)

package com.example.btl_mobile_son.data.model

data class DichVu(
    val maDichVu: Long = 0,
    val maNha: Long,
    val tenDichVu: String,
    val donVi: String = "",
    val donGia: Long = 0,  // Đổi Double → Long (VNĐ)
    val cachTinh: String = "theo_phong", // "theo_phong" | "theo_nguoi" | "mot_lan" | "theo_thang"
    val loaiDichVu: String = "khac", // Chỉ còn "khac" - không dùng "dien"/"nuoc"
    val isActive: Boolean = true  // THÊM MỚI - Để ẩn/hiện dịch vụ
)

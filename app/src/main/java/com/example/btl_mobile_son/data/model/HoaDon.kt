package com.example.btl_mobile_son.data.model

data class HoaDon(
    val maHoaDon: Long = 0,
    val maHopDong: Long,
    val thang: Int,
    val nam: Int,
    val tienPhong: Long = 0,  // Đổi Double → Long (VNĐ)
    val tongTienDichVu: Long = 0,  // Đổi Double → Long (VNĐ)
    val giamGia: Long = 0,  // Đổi Double → Long (VNĐ)
    val tongTien: Long = 0,  // Đổi Double → Long (VNĐ)
    val tienDaThanhToan: Long = 0,  // THÊM MỚI - Số tiền đã thanh toán
    val trangThai: String = "chua_thanh_toan",  // THAY Boolean - "chua_thanh_toan" | "thanh_toan_mot_phan" | "da_thanh_toan" | "qua_han"
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)

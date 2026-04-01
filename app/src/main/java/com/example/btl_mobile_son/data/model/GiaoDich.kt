package com.example.btl_mobile_son.data.model

data class GiaoDich(
    val maGiaoDich: Long = 0,
    val loai: String, // thu / chi
    val maPhong: Long? = null,
    val maHoaDon: Long? = null,      // Liên kết với hóa đơn
    val maHopDong: Long? = null,     // Liên kết với hợp đồng
    val maDatCoc: Long? = null,      // Liên kết với đặt cọc
    val soTien: Long,  // Đổi Double → Long (VNĐ)
    val danhMuc: String = "",
    val ngayGiaoDich: Long = 0,
    val noiDung: String = "",
    val tenNguoi: String = "",
    val phuongThucThanhToan: String = "tien_mat",
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)

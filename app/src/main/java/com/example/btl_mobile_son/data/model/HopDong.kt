package com.example.btl_mobile_son.data.model

data class HopDong(
    val maHopDong: Long = 0,
    val maPhong: Long,
    val maKhach: Long,
    val ngayBatDau: Long,
    val ngayKetThuc: Long,
    val giaThueThang: Long,  // Đổi Double → Long (VNĐ)
    val tienDatCoc: Long = 0,  // Đổi Double → Long (VNĐ)
    val trangThai: String = "dang_thue" // dang_thue / het_han / da_huy
)

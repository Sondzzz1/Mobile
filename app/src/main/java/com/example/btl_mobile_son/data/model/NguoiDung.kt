package com.example.btl_mobile_son.data.model

data class NguoiDung(
    val maNguoiDung: Int = 0,
    val tenDangNhap: String,
    val matKhau: String,
    val hoTen: String,
    val vaiTro: String = "nhan_vien", // admin, nhan_vien
    val soDienThoai: String = "",
    val email: String = "",
    val trangThai: String = "hoat_dong", // hoat_dong, khoa
    val ngayTao: Long = System.currentTimeMillis()
)

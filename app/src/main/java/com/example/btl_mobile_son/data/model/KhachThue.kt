package com.example.btl_mobile_son.data.model

data class KhachThue(
    val maKhach: Long = 0,
    val hoTen: String,
    val soDienThoai: String = "",
    val email: String = "",
    val soCmnd: String = "",
    val ngaySinh: Long? = null,
    val ngayCap: Long? = null,
    val noiCap: String = "",
    val noiLamViec: String = "",
    val tinhThanh: String = "",
    val quanHuyen: String = "",
    val xaPhuong: String = "",
    val diaChiChiTiet: String = "",
    // BỎ: val maPhong: Long? = null,
    // BỎ: val trangThai: String = "dang_o",
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)

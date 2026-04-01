package com.example.btl_mobile_son.data.model

data class HopDongThanhVien(
    val maThanhVien: Long = 0,
    val maHopDong: Long,
    val maKhach: Long,
    val vaiTro: String = "thanh_vien", // "dai_dien" | "thanh_vien"
    val ngayVaoO: Long = System.currentTimeMillis(),
    val ngayRoiDi: Long? = null,
    val trangThai: String = "dang_o", // "dang_o" | "da_roi"
    val ghiChu: String = ""
)

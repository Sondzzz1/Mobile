package com.example.btl_mobile_son.data.model

data class SuCo(
    val maSuCo: Int = 0,
    val maPhong: Int,
    val loaiSuCo: String,
    val moTa: String = "",
    val trangThai: String = "chua_xu_ly", // chua_xu_ly, dang_xu_ly, da_xu_ly
    val nguoiBaoGao: String = "",
    val nguoiXuLy: String = "",
    val ngayBaoGao: Long = System.currentTimeMillis(),
    val ngayXuLy: Long? = null,
    val chiPhi: Double = 0.0,
    val ghiChu: String = ""
)

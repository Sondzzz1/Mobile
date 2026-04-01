package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.model.HopDongThanhVien

class HopDongThanhVienDao(private val db: SQLiteDatabase) {
    
    companion object {
        const val TABLE_NAME = "HopDongThanhVien"
        const val COL_MA_THANH_VIEN = "maThanhVien"
        const val COL_MA_HOP_DONG = "maHopDong"
        const val COL_MA_KHACH = "maKhach"
        const val COL_VAI_TRO = "vaiTro"
        const val COL_NGAY_VAO_O = "ngayVaoO"
        const val COL_NGAY_ROI_DI = "ngayRoiDi"
        const val COL_TRANG_THAI = "trangThai"
        const val COL_GHI_CHU = "ghiChu"
    }
    
    fun them(tv: HopDongThanhVien): Long {
        val values = ContentValues().apply {
            put(COL_MA_HOP_DONG, tv.maHopDong)
            put(COL_MA_KHACH, tv.maKhach)
            put(COL_VAI_TRO, tv.vaiTro)
            put(COL_NGAY_VAO_O, tv.ngayVaoO)
            put(COL_NGAY_ROI_DI, tv.ngayRoiDi)
            put(COL_TRANG_THAI, tv.trangThai)
            put(COL_GHI_CHU, tv.ghiChu)
        }
        return db.insert(TABLE_NAME, null, values)
    }
    
    fun capNhat(tv: HopDongThanhVien): Int {
        val values = ContentValues().apply {
            put(COL_MA_HOP_DONG, tv.maHopDong)
            put(COL_MA_KHACH, tv.maKhach)
            put(COL_VAI_TRO, tv.vaiTro)
            put(COL_NGAY_VAO_O, tv.ngayVaoO)
            put(COL_NGAY_ROI_DI, tv.ngayRoiDi)
            put(COL_TRANG_THAI, tv.trangThai)
            put(COL_GHI_CHU, tv.ghiChu)
        }
        return db.update(TABLE_NAME, values, "$COL_MA_THANH_VIEN = ?", arrayOf(tv.maThanhVien.toString()))
    }
    
    fun xoa(maThanhVien: Long): Int {
        return db.delete(TABLE_NAME, "$COL_MA_THANH_VIEN = ?", arrayOf(maThanhVien.toString()))
    }
    
    fun layTheoMa(maThanhVien: Long): HopDongThanhVien? {
        val cursor = db.query(
            TABLE_NAME, null,
            "$COL_MA_THANH_VIEN = ?", arrayOf(maThanhVien.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return HopDongThanhVien(
                    maThanhVien = it.getLong(it.getColumnIndexOrThrow(COL_MA_THANH_VIEN)),
                    maHopDong = it.getLong(it.getColumnIndexOrThrow(COL_MA_HOP_DONG)),
                    maKhach = it.getLong(it.getColumnIndexOrThrow(COL_MA_KHACH)),
                    vaiTro = it.getString(it.getColumnIndexOrThrow(COL_VAI_TRO)),
                    ngayVaoO = it.getLong(it.getColumnIndexOrThrow(COL_NGAY_VAO_O)),
                    ngayRoiDi = if (it.isNull(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI))) null 
                                else it.getLong(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI)),
                    trangThai = it.getString(it.getColumnIndexOrThrow(COL_TRANG_THAI)),
                    ghiChu = it.getString(it.getColumnIndexOrThrow(COL_GHI_CHU))
                )
            }
        }
        return null
    }
    
    fun layTheoHopDong(maHopDong: Long): List<HopDongThanhVien> {
        val list = mutableListOf<HopDongThanhVien>()
        val cursor = db.query(
            TABLE_NAME, null,
            "$COL_MA_HOP_DONG = ?", arrayOf(maHopDong.toString()),
            null, null, "$COL_NGAY_VAO_O ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                list.add(HopDongThanhVien(
                    maThanhVien = it.getLong(it.getColumnIndexOrThrow(COL_MA_THANH_VIEN)),
                    maHopDong = it.getLong(it.getColumnIndexOrThrow(COL_MA_HOP_DONG)),
                    maKhach = it.getLong(it.getColumnIndexOrThrow(COL_MA_KHACH)),
                    vaiTro = it.getString(it.getColumnIndexOrThrow(COL_VAI_TRO)),
                    ngayVaoO = it.getLong(it.getColumnIndexOrThrow(COL_NGAY_VAO_O)),
                    ngayRoiDi = if (it.isNull(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI))) null 
                                else it.getLong(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI)),
                    trangThai = it.getString(it.getColumnIndexOrThrow(COL_TRANG_THAI)),
                    ghiChu = it.getString(it.getColumnIndexOrThrow(COL_GHI_CHU))
                ))
            }
        }
        return list
    }
    
    fun layTheoKhach(maKhach: Long): List<HopDongThanhVien> {
        val list = mutableListOf<HopDongThanhVien>()
        val cursor = db.query(
            TABLE_NAME, null,
            "$COL_MA_KHACH = ?", arrayOf(maKhach.toString()),
            null, null, "$COL_NGAY_VAO_O DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                list.add(HopDongThanhVien(
                    maThanhVien = it.getLong(it.getColumnIndexOrThrow(COL_MA_THANH_VIEN)),
                    maHopDong = it.getLong(it.getColumnIndexOrThrow(COL_MA_HOP_DONG)),
                    maKhach = it.getLong(it.getColumnIndexOrThrow(COL_MA_KHACH)),
                    vaiTro = it.getString(it.getColumnIndexOrThrow(COL_VAI_TRO)),
                    ngayVaoO = it.getLong(it.getColumnIndexOrThrow(COL_NGAY_VAO_O)),
                    ngayRoiDi = if (it.isNull(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI))) null 
                                else it.getLong(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI)),
                    trangThai = it.getString(it.getColumnIndexOrThrow(COL_TRANG_THAI)),
                    ghiChu = it.getString(it.getColumnIndexOrThrow(COL_GHI_CHU))
                ))
            }
        }
        return list
    }
    
    fun layNguoiDangOTheoHopDong(maHopDong: Long): List<HopDongThanhVien> {
        return layTheoHopDong(maHopDong).filter { it.trangThai == "dang_o" }
    }
    
    fun demNguoiDangO(maHopDong: Long): Int {
        return layNguoiDangOTheoHopDong(maHopDong).size
    }
    
    fun layTatCa(): List<HopDongThanhVien> {
        val list = mutableListOf<HopDongThanhVien>()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COL_NGAY_VAO_O DESC")
        
        cursor.use {
            while (it.moveToNext()) {
                list.add(HopDongThanhVien(
                    maThanhVien = it.getLong(it.getColumnIndexOrThrow(COL_MA_THANH_VIEN)),
                    maHopDong = it.getLong(it.getColumnIndexOrThrow(COL_MA_HOP_DONG)),
                    maKhach = it.getLong(it.getColumnIndexOrThrow(COL_MA_KHACH)),
                    vaiTro = it.getString(it.getColumnIndexOrThrow(COL_VAI_TRO)),
                    ngayVaoO = it.getLong(it.getColumnIndexOrThrow(COL_NGAY_VAO_O)),
                    ngayRoiDi = if (it.isNull(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI))) null 
                                else it.getLong(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI)),
                    trangThai = it.getString(it.getColumnIndexOrThrow(COL_TRANG_THAI)),
                    ghiChu = it.getString(it.getColumnIndexOrThrow(COL_GHI_CHU))
                ))
            }
        }
        return list
    }
}

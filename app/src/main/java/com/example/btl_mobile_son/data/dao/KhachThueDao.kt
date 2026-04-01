package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.KhachThue

class KhachThueDao(private val db: SQLiteDatabase) {

    fun them(khachThue: KhachThue): Long {
        val values = ContentValues().apply {
            put("ho_ten", khachThue.hoTen)
            put("so_dien_thoai", khachThue.soDienThoai)
            put("email", khachThue.email)
            put("so_cmnd", khachThue.soCmnd)
            put("ngay_sinh", khachThue.ngaySinh)
            put("ngay_cap", khachThue.ngayCap)
            put("noi_cap", khachThue.noiCap)
            put("noi_lam_viec", khachThue.noiLamViec)
            put("tinh_thanh", khachThue.tinhThanh)
            put("quan_huyen", khachThue.quanHuyen)
            put("xa_phuong", khachThue.xaPhuong)
            put("dia_chi_chi_tiet", khachThue.diaChiChiTiet)
            // BỎ: put("ma_phong", khachThue.maPhong)
            // BỎ: put("trang_thai", khachThue.trangThai)
            put("ghi_chu", khachThue.ghiChu)
            put("ngay_tao", khachThue.ngayTao)
        }
        return db.insert(DatabaseHelper.TABLE_KHACH_THUE, null, values)
    }

    fun capNhat(khachThue: KhachThue): Int {
        val values = ContentValues().apply {
            put("ho_ten", khachThue.hoTen)
            put("so_dien_thoai", khachThue.soDienThoai)
            put("email", khachThue.email)
            put("so_cmnd", khachThue.soCmnd)
            put("ngay_sinh", khachThue.ngaySinh)
            put("ngay_cap", khachThue.ngayCap)
            put("noi_cap", khachThue.noiCap)
            put("noi_lam_viec", khachThue.noiLamViec)
            put("tinh_thanh", khachThue.tinhThanh)
            put("quan_huyen", khachThue.quanHuyen)
            put("xa_phuong", khachThue.xaPhuong)
            put("dia_chi_chi_tiet", khachThue.diaChiChiTiet)
            // BỎ: put("ma_phong", khachThue.maPhong)
            // BỎ: put("trang_thai", khachThue.trangThai)
            put("ghi_chu", khachThue.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_KHACH_THUE,
            values,
            "ma_khach = ?",
            arrayOf(khachThue.maKhach.toString())
        )
    }

    fun xoa(maKhach: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_KHACH_THUE,
            "ma_khach = ?",
            arrayOf(maKhach.toString())
        )
    }

    fun layTatCa(): List<KhachThue> {
        val danhSach = mutableListOf<KhachThue>()
        val cursor = db.query(
            DatabaseHelper.TABLE_KHACH_THUE,
            null,
            null,
            null,
            null,
            null,
            "ho_ten ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToKhachThue(it))
            }
        }
        return danhSach
    }

    fun layTheoMa(maKhach: Long): KhachThue? {
        val cursor = db.query(
            DatabaseHelper.TABLE_KHACH_THUE,
            null,
            "ma_khach = ?",
            arrayOf(maKhach.toString()),
            null,
            null,
            null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToKhachThue(it)
            }
        }
        return null
    }

    fun timKiem(tuKhoa: String): List<KhachThue> {
        val danhSach = mutableListOf<KhachThue>()
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_KHACH_THUE} WHERE ho_ten LIKE ? OR so_dien_thoai LIKE ? ORDER BY ho_ten ASC",
            arrayOf("%$tuKhoa%", "%$tuKhoa%")
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToKhachThue(it))
            }
        }
        return danhSach
    }

    // BỎ hàm layTheoPhong vì không còn maPhong trong KhachThue
    // Thay vào đó sẽ dùng HopDongThanhVienDao để lấy danh sách người trong phòng

    private fun cursorToKhachThue(cursor: Cursor): KhachThue {
        return KhachThue(
            maKhach = cursor.getLong(cursor.getColumnIndexOrThrow("ma_khach")),
            hoTen = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
            soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
            soCmnd = cursor.getString(cursor.getColumnIndexOrThrow("so_cmnd")) ?: "",
            ngaySinh = if (cursor.isNull(cursor.getColumnIndexOrThrow("ngay_sinh"))) null 
                       else cursor.getLong(cursor.getColumnIndexOrThrow("ngay_sinh")),
            ngayCap = if (cursor.isNull(cursor.getColumnIndexOrThrow("ngay_cap"))) null 
                      else cursor.getLong(cursor.getColumnIndexOrThrow("ngay_cap")),
            noiCap = cursor.getString(cursor.getColumnIndexOrThrow("noi_cap")) ?: "",
            noiLamViec = cursor.getString(cursor.getColumnIndexOrThrow("noi_lam_viec")) ?: "",
            tinhThanh = cursor.getString(cursor.getColumnIndexOrThrow("tinh_thanh")) ?: "",
            quanHuyen = cursor.getString(cursor.getColumnIndexOrThrow("quan_huyen")) ?: "",
            xaPhuong = cursor.getString(cursor.getColumnIndexOrThrow("xa_phuong")) ?: "",
            diaChiChiTiet = cursor.getString(cursor.getColumnIndexOrThrow("dia_chi_chi_tiet")) ?: "",
            // BỎ: maPhong
            // BỎ: trangThai
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: "",
            ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
        )
    }
}

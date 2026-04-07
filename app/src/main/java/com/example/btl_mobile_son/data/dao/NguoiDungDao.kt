package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.NguoiDung

class NguoiDungDao(private val db: SQLiteDatabase) {

    fun them(nguoiDung: NguoiDung): Long {
        val values = ContentValues().apply {
            put("ten_dang_nhap", nguoiDung.tenDangNhap)
            put("mat_khau", nguoiDung.matKhau)
            put("ho_ten", nguoiDung.hoTen)
            put("vai_tro", nguoiDung.vaiTro)
            put("so_dien_thoai", nguoiDung.soDienThoai)
            put("email", nguoiDung.email)
            put("trang_thai", nguoiDung.trangThai)
            put("ngay_tao", nguoiDung.ngayTao)
        }
        return db.insert(DatabaseHelper.TABLE_NGUOI_DUNG, null, values)
    }

    fun capNhat(nguoiDung: NguoiDung): Int {
        val values = ContentValues().apply {
            put("ten_dang_nhap", nguoiDung.tenDangNhap)
            put("mat_khau", nguoiDung.matKhau)
            put("ho_ten", nguoiDung.hoTen)
            put("vai_tro", nguoiDung.vaiTro)
            put("so_dien_thoai", nguoiDung.soDienThoai)
            put("email", nguoiDung.email)
            put("trang_thai", nguoiDung.trangThai)
        }
        return db.update(
            DatabaseHelper.TABLE_NGUOI_DUNG,
            values,
            "ma_nguoi_dung = ?",
            arrayOf(nguoiDung.maNguoiDung.toString())
        )
    }

    fun xoa(maNguoiDung: Int): Int {
        return db.delete(
            DatabaseHelper.TABLE_NGUOI_DUNG,
            "ma_nguoi_dung = ?",
            arrayOf(maNguoiDung.toString())
        )
    }

    fun layTheoId(maNguoiDung: Int): NguoiDung? {
        val cursor = db.query(
            DatabaseHelper.TABLE_NGUOI_DUNG,
            null,
            "ma_nguoi_dung = ?",
            arrayOf(maNguoiDung.toString()),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val nguoiDung = NguoiDung(
                maNguoiDung = cursor.getInt(cursor.getColumnIndexOrThrow("ma_nguoi_dung")),
                tenDangNhap = cursor.getString(cursor.getColumnIndexOrThrow("ten_dang_nhap")),
                matKhau = cursor.getString(cursor.getColumnIndexOrThrow("mat_khau")),
                hoTen = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
                vaiTro = cursor.getString(cursor.getColumnIndexOrThrow("vai_tro")),
                soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
                trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
            )
            cursor.close()
            nguoiDung
        } else {
            cursor.close()
            null
        }
    }

    fun layTheoTenDangNhap(tenDangNhap: String): NguoiDung? {
        val cursor = db.query(
            DatabaseHelper.TABLE_NGUOI_DUNG,
            null,
            "ten_dang_nhap = ?",
            arrayOf(tenDangNhap),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val nguoiDung = NguoiDung(
                maNguoiDung = cursor.getInt(cursor.getColumnIndexOrThrow("ma_nguoi_dung")),
                tenDangNhap = cursor.getString(cursor.getColumnIndexOrThrow("ten_dang_nhap")),
                matKhau = cursor.getString(cursor.getColumnIndexOrThrow("mat_khau")),
                hoTen = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
                vaiTro = cursor.getString(cursor.getColumnIndexOrThrow("vai_tro")),
                soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
                trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
            )
            cursor.close()
            nguoiDung
        } else {
            cursor.close()
            null
        }
    }

    fun dangNhap(tenDangNhap: String, matKhau: String): NguoiDung? {
        val cursor = db.query(
            DatabaseHelper.TABLE_NGUOI_DUNG,
            null,
            "ten_dang_nhap = ? AND mat_khau = ? AND trang_thai = ?",
            arrayOf(tenDangNhap, matKhau, "hoat_dong"),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val nguoiDung = NguoiDung(
                maNguoiDung = cursor.getInt(cursor.getColumnIndexOrThrow("ma_nguoi_dung")),
                tenDangNhap = cursor.getString(cursor.getColumnIndexOrThrow("ten_dang_nhap")),
                matKhau = cursor.getString(cursor.getColumnIndexOrThrow("mat_khau")),
                hoTen = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
                vaiTro = cursor.getString(cursor.getColumnIndexOrThrow("vai_tro")),
                soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
                trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
            )
            cursor.close()
            nguoiDung
        } else {
            cursor.close()
            null
        }
    }

    fun layTatCa(): List<NguoiDung> {
        val danhSach = mutableListOf<NguoiDung>()
        val cursor = db.query(
            DatabaseHelper.TABLE_NGUOI_DUNG,
            null, null, null, null, null,
            "ngay_tao DESC"
        )
        
        while (cursor.moveToNext()) {
            danhSach.add(
                NguoiDung(
                    maNguoiDung = cursor.getInt(cursor.getColumnIndexOrThrow("ma_nguoi_dung")),
                    tenDangNhap = cursor.getString(cursor.getColumnIndexOrThrow("ten_dang_nhap")),
                    matKhau = cursor.getString(cursor.getColumnIndexOrThrow("mat_khau")),
                    hoTen = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
                    vaiTro = cursor.getString(cursor.getColumnIndexOrThrow("vai_tro")),
                    soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
                    trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                    ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
                )
            )
        }
        cursor.close()
        return danhSach
    }
}

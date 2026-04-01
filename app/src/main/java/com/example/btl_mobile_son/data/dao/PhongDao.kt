package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.Phong

class PhongDao(private val db: SQLiteDatabase) {

    fun them(phong: Phong): Long {
        val values = ContentValues().apply {
            put("ma_nha", phong.maNha)
            put("ten_phong", phong.tenPhong)
            put("dien_tich_m2", phong.dienTichM2)
            put("gia_co_ban", phong.giaCoBan)
            put("trang_thai", phong.trangThai)
            put("so_nguoi_toi_da", phong.soNguoiToiDa)
            put("ghi_chu", phong.ghiChu)
        }
        return db.insert(DatabaseHelper.TABLE_PHONG, null, values)
    }

    fun capNhat(phong: Phong): Int {
        val values = ContentValues().apply {
            put("ma_nha", phong.maNha)
            put("ten_phong", phong.tenPhong)
            put("dien_tich_m2", phong.dienTichM2)
            put("gia_co_ban", phong.giaCoBan)
            put("trang_thai", phong.trangThai)
            put("so_nguoi_toi_da", phong.soNguoiToiDa)
            put("ghi_chu", phong.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_PHONG,
            values,
            "ma_phong = ?",
            arrayOf(phong.maPhong.toString())
        )
    }

    fun xoa(maPhong: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_PHONG,
            "ma_phong = ?",
            arrayOf(maPhong.toString())
        )
    }

    fun layTatCa(): List<Phong> {
        val danhSach = mutableListOf<Phong>()
        val cursor = db.query(
            DatabaseHelper.TABLE_PHONG,
            null,
            null,
            null,
            null,
            null,
            "ten_phong ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToPhong(it))
            }
        }
        return danhSach
    }

    fun layTheoNha(maNha: Long): List<Phong> {
        val danhSach = mutableListOf<Phong>()
        val cursor = db.query(
            DatabaseHelper.TABLE_PHONG,
            null,
            "ma_nha = ?",
            arrayOf(maNha.toString()),
            null,
            null,
            "ten_phong ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToPhong(it))
            }
        }
        return danhSach
    }

    fun layTheoMa(maPhong: Long): Phong? {
        val cursor = db.query(
            DatabaseHelper.TABLE_PHONG,
            null,
            "ma_phong = ?",
            arrayOf(maPhong.toString()),
            null,
            null,
            null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToPhong(it)
            }
        }
        return null
    }

    fun demPhongTrong(maNha: Long): Int {
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} WHERE ma_nha = ? AND trang_thai = 'trong'",
            arrayOf(maNha.toString())
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    /**
     * Kiểm tra trùng tên phòng trong cùng nhà
     * @param maNha ID của nhà trọ
     * @param tenPhong Tên phòng cần kiểm tra
     * @param maPhongLoaiTru ID phòng cần loại trừ (dùng khi sửa phòng)
     * @return true nếu tên phòng đã tồn tại, false nếu chưa
     */
    fun kiemTraTrungTen(maNha: Long, tenPhong: String, maPhongLoaiTru: Long = -1): Boolean {
        val cursor = if (maPhongLoaiTru > 0) {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} " +
                "WHERE ma_nha = ? AND ten_phong = ? AND ma_phong != ?",
                arrayOf(maNha.toString(), tenPhong, maPhongLoaiTru.toString())
            )
        } else {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} " +
                "WHERE ma_nha = ? AND ten_phong = ?",
                arrayOf(maNha.toString(), tenPhong)
            )
        }
        
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) > 0
            }
        }
        return false
    }

    private fun cursorToPhong(cursor: Cursor): Phong {
        // Convert từ REAL (Double) sang Long cho database v6
        val giaDouble = cursor.getDouble(cursor.getColumnIndexOrThrow("gia_co_ban"))
        
        return Phong(
            maPhong = cursor.getLong(cursor.getColumnIndexOrThrow("ma_phong")),
            maNha = cursor.getLong(cursor.getColumnIndexOrThrow("ma_nha")),
            tenPhong = cursor.getString(cursor.getColumnIndexOrThrow("ten_phong")),
            dienTichM2 = cursor.getFloat(cursor.getColumnIndexOrThrow("dien_tich_m2")),
            giaCoBan = giaDouble.toLong(), // Convert Double → Long
            trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
            soNguoiToiDa = cursor.getInt(cursor.getColumnIndexOrThrow("so_nguoi_toi_da")),
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
        )
    }
}

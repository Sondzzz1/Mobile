package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.ChiSoDienNuoc

class ChiSoDienNuocDao(private val db: SQLiteDatabase) {

    fun them(chiSo: ChiSoDienNuoc): Long {
        val values = ContentValues().apply {
            put("ma_phong", chiSo.maPhong)
            put("loai", chiSo.loai)
            put("thang", chiSo.thang)
            put("nam", chiSo.nam)
            put("chi_so_cu", chiSo.chiSoCu)
            put("chi_so_moi", chiSo.chiSoMoi)
            put("so_tieu_thu", chiSo.soTieuThu)
            put("don_gia", chiSo.donGia)
            put("ghi_chu", chiSo.ghiChu)
        }
        return db.insert(DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC, null, values)
    }

    fun capNhat(chiSo: ChiSoDienNuoc): Int {
        val values = ContentValues().apply {
            put("ma_phong", chiSo.maPhong)
            put("loai", chiSo.loai)
            put("thang", chiSo.thang)
            put("nam", chiSo.nam)
            put("chi_so_cu", chiSo.chiSoCu)
            put("chi_so_moi", chiSo.chiSoMoi)
            put("so_tieu_thu", chiSo.soTieuThu)
            put("don_gia", chiSo.donGia)
            put("ghi_chu", chiSo.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC,
            values,
            "ma_chi_so = ?",
            arrayOf(chiSo.maChiSo.toString())
        )
    }

    fun xoa(maChiSo: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC,
            "ma_chi_so = ?",
            arrayOf(maChiSo.toString())
        )
    }

    fun layTheoThangNam(thang: Int, nam: Int): List<ChiSoDienNuoc> {
        val danhSach = mutableListOf<ChiSoDienNuoc>()
        val cursor = db.query(
            DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC,
            null,
            "thang = ? AND nam = ?",
            arrayOf(thang.toString(), nam.toString()),
            null, null,
            "ma_phong ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToChiSo(it))
            }
        }
        return danhSach
    }

    fun layChiSoMoiNhat(maPhong: Long): ChiSoDienNuoc? {
        val cursor = db.query(
            DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC,
            null,
            "ma_phong = ?",
            arrayOf(maPhong.toString()),
            null, null,
            "nam DESC, thang DESC",
            "1"
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToChiSo(it)
            }
        }
        return null
    }

    fun layTheoMa(maChiSo: Long): ChiSoDienNuoc? {
        val cursor = db.query(
            DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC,
            null,
            "ma_chi_so = ?",
            arrayOf(maChiSo.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToChiSo(it)
            }
        }
        return null
    }

    /**
     * Lấy chỉ số tháng trước - xử lý đúng tháng 1 (VĐ5)
     */
    fun layChiSoThangTruoc(maPhong: Long, loai: String, thang: Int, nam: Int): ChiSoDienNuoc? {
        val (thangTruoc, namTruoc) = if (thang == 1) {
            Pair(12, nam - 1)  // Tháng 1 → lấy tháng 12 năm trước
        } else {
            Pair(thang - 1, nam)
        }
        
        val cursor = db.query(
            DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC,
            null,
            "ma_phong = ? AND loai = ? AND thang = ? AND nam = ?",
            arrayOf(maPhong.toString(), loai, thangTruoc.toString(), namTruoc.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToChiSo(it)
            }
        }
        return null
    }

    /**
     * Kiểm tra trùng chỉ số (VĐ4)
     */
    fun kiemTraTrungChiSo(maPhong: Long, loai: String, thang: Int, nam: Int, maChiSoLoaiTru: Long = -1): Boolean {
        val cursor = if (maChiSoLoaiTru > 0) {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC} " +
                "WHERE ma_phong = ? AND loai = ? AND thang = ? AND nam = ? AND ma_chi_so != ?",
                arrayOf(maPhong.toString(), loai, thang.toString(), nam.toString(), maChiSoLoaiTru.toString())
            )
        } else {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_CHI_SO_DIEN_NUOC} " +
                "WHERE ma_phong = ? AND loai = ? AND thang = ? AND nam = ?",
                arrayOf(maPhong.toString(), loai, thang.toString(), nam.toString())
            )
        }
        
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) > 0
            }
        }
        return false
    }

    private fun cursorToChiSo(cursor: Cursor): ChiSoDienNuoc {
        val chiSoCu = cursor.getLong(cursor.getColumnIndexOrThrow("chi_so_cu"))
        val chiSoMoi = cursor.getLong(cursor.getColumnIndexOrThrow("chi_so_moi"))
        val soTieuThu = try {
            cursor.getLong(cursor.getColumnIndexOrThrow("so_tieu_thu"))
        } catch (e: Exception) {
            chiSoMoi - chiSoCu  // Tính lại nếu cột chưa có
        }
        
        return ChiSoDienNuoc(
            maChiSo = cursor.getLong(cursor.getColumnIndexOrThrow("ma_chi_so")),
            maPhong = cursor.getLong(cursor.getColumnIndexOrThrow("ma_phong")),
            loai = cursor.getString(cursor.getColumnIndexOrThrow("loai")),
            thang = cursor.getInt(cursor.getColumnIndexOrThrow("thang")),
            nam = cursor.getInt(cursor.getColumnIndexOrThrow("nam")),
            chiSoCu = chiSoCu,
            chiSoMoi = chiSoMoi,
            soTieuThu = soTieuThu,
            donGia = cursor.getLong(cursor.getColumnIndexOrThrow("don_gia")),
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
        )
    }
}

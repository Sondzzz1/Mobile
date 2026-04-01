package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.model.PhongDichVu

class PhongDichVuDao(private val db: SQLiteDatabase) {

    companion object {
        const val TABLE_NAME = "phong_dich_vu"
        const val COL_MA = "ma_phong_dich_vu"
        const val COL_MA_PHONG = "ma_phong"
        const val COL_MA_DICH_VU = "ma_dich_vu"
        const val COL_DON_GIA_RIENG = "don_gia_rieng"
        const val COL_GHI_CHU = "ghi_chu"
    }

    fun them(phongDichVu: PhongDichVu): Long {
        val values = ContentValues().apply {
            put(COL_MA_PHONG, phongDichVu.maPhong)
            put(COL_MA_DICH_VU, phongDichVu.maDichVu)
            put(COL_DON_GIA_RIENG, phongDichVu.donGiaRieng)
            put(COL_GHI_CHU, phongDichVu.ghiChu)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun capNhat(phongDichVu: PhongDichVu): Int {
        val values = ContentValues().apply {
            put(COL_MA_PHONG, phongDichVu.maPhong)
            put(COL_MA_DICH_VU, phongDichVu.maDichVu)
            put(COL_DON_GIA_RIENG, phongDichVu.donGiaRieng)
            put(COL_GHI_CHU, phongDichVu.ghiChu)
        }
        return db.update(TABLE_NAME, values, "$COL_MA = ?", arrayOf(phongDichVu.maPhongDichVu.toString()))
    }

    fun xoa(maPhongDichVu: Long): Int {
        return db.delete(TABLE_NAME, "$COL_MA = ?", arrayOf(maPhongDichVu.toString()))
    }

    fun layTheoMa(maPhongDichVu: Long): PhongDichVu? {
        val cursor = db.query(TABLE_NAME, null, "$COL_MA = ?", arrayOf(maPhongDichVu.toString()), null, null, null)
        return if (cursor.moveToFirst()) {
            val result = cursorToPhongDichVu(cursor)
            cursor.close()
            result
        } else {
            cursor.close()
            null
        }
    }

    fun layTheoPhong(maPhong: Long): List<PhongDichVu> {
        val list = mutableListOf<PhongDichVu>()
        val cursor = db.query(TABLE_NAME, null, "$COL_MA_PHONG = ?", arrayOf(maPhong.toString()), null, null, null)
        while (cursor.moveToNext()) {
            list.add(cursorToPhongDichVu(cursor))
        }
        cursor.close()
        return list
    }

    fun layTatCa(): List<PhongDichVu> {
        val list = mutableListOf<PhongDichVu>()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(cursorToPhongDichVu(cursor))
        }
        cursor.close()
        return list
    }

    /**
     * VĐ1: Kiểm tra xem phòng đã có dịch vụ này chưa
     */
    fun kiemTraTrung(maPhong: Long, maDichVu: Long, maPhongDichVuLoaiTru: Long = -1): Boolean {
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COL_MA),
            "$COL_MA_PHONG = ? AND $COL_MA_DICH_VU = ? AND $COL_MA != ?",
            arrayOf(maPhong.toString(), maDichVu.toString(), maPhongDichVuLoaiTru.toString()),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    /**
     * VĐ1: Xóa tất cả dịch vụ của phòng
     */
    fun xoaTheoPhong(maPhong: Long): Int {
        return db.delete(TABLE_NAME, "$COL_MA_PHONG = ?", arrayOf(maPhong.toString()))
    }

    private fun cursorToPhongDichVu(cursor: Cursor): PhongDichVu {
        return PhongDichVu(
            maPhongDichVu = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MA)),
            maPhong = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MA_PHONG)),
            maDichVu = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MA_DICH_VU)),
            donGiaRieng = try {
                if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_DON_GIA_RIENG))) null
                else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DON_GIA_RIENG))
            } catch (e: Exception) { null },
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow(COL_GHI_CHU)) ?: ""
        )
    }
}

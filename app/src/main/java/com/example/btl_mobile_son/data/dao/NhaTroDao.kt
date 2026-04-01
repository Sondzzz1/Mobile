package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.NhaTro

class NhaTroDao(private val db: SQLiteDatabase) {

    fun them(nhaTro: NhaTro): Long {
        val values = ContentValues().apply {
            put("ten_nha", nhaTro.tenNha)
            put("dia_chi", nhaTro.diaChi)
            put("ten_chu_nha", nhaTro.tenChuNha)
            put("so_dien_thoai", nhaTro.soDienThoai)
            put("ghi_chu", nhaTro.ghiChu)
        }
        return db.insert(DatabaseHelper.TABLE_NHA_TRO, null, values)
    }

    fun capNhat(nhaTro: NhaTro): Int {
        val values = ContentValues().apply {
            put("ten_nha", nhaTro.tenNha)
            put("dia_chi", nhaTro.diaChi)
            put("ten_chu_nha", nhaTro.tenChuNha)
            put("so_dien_thoai", nhaTro.soDienThoai)
            put("ghi_chu", nhaTro.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_NHA_TRO,
            values,
            "ma_nha = ?",
            arrayOf(nhaTro.maNha.toString())
        )
    }

    fun xoa(maNha: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_NHA_TRO,
            "ma_nha = ?",
            arrayOf(maNha.toString())
        )
    }

    fun layTatCa(): List<NhaTro> {
        val danhSach = mutableListOf<NhaTro>()
        val cursor = db.query(
            DatabaseHelper.TABLE_NHA_TRO,
            null,
            null,
            null,
            null,
            null,
            "ten_nha ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToNhaTro(it))
            }
        }
        return danhSach
    }

    fun layTheoMa(maNha: Long): NhaTro? {
        val cursor = db.query(
            DatabaseHelper.TABLE_NHA_TRO,
            null,
            "ma_nha = ?",
            arrayOf(maNha.toString()),
            null,
            null,
            null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToNhaTro(it)
            }
        }
        return null
    }

    /**
     * Kiểm tra nhà trọ đã có dữ liệu phát sinh chưa
     * @param maNha ID nhà trọ
     * @return true nếu đã có phòng, false nếu chưa
     */
    fun coThePhatSinhDuLieu(maNha: Long): Boolean {
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} WHERE ma_nha = ?",
            arrayOf(maNha.toString())
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) > 0
            }
        }
        return false
    }

    private fun cursorToNhaTro(cursor: Cursor): NhaTro {
        return NhaTro(
            maNha = cursor.getLong(cursor.getColumnIndexOrThrow("ma_nha")),
            tenNha = cursor.getString(cursor.getColumnIndexOrThrow("ten_nha")),
            diaChi = cursor.getString(cursor.getColumnIndexOrThrow("dia_chi")) ?: "",
            tenChuNha = cursor.getString(cursor.getColumnIndexOrThrow("ten_chu_nha")) ?: "",
            soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
        )
    }
}

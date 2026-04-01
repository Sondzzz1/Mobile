package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.DichVu

class DichVuDao(private val db: SQLiteDatabase) {

    fun them(dichVu: DichVu): Long {
        val values = ContentValues().apply {
            put("ma_nha", dichVu.maNha)
            put("ten_dich_vu", dichVu.tenDichVu)
            put("don_vi", dichVu.donVi)
            put("don_gia", dichVu.donGia)
            put("cach_tinh", dichVu.cachTinh)
            put("loai_dich_vu", dichVu.loaiDichVu)
            put("is_active", if (dichVu.isActive) 1 else 0)
        }
        return db.insert(DatabaseHelper.TABLE_DICH_VU, null, values)
    }

    fun capNhat(dichVu: DichVu): Int {
        val values = ContentValues().apply {
            put("ma_nha", dichVu.maNha)
            put("ten_dich_vu", dichVu.tenDichVu)
            put("don_vi", dichVu.donVi)
            put("don_gia", dichVu.donGia)
            put("cach_tinh", dichVu.cachTinh)
            put("loai_dich_vu", dichVu.loaiDichVu)
            put("is_active", if (dichVu.isActive) 1 else 0)
        }
        return db.update(
            DatabaseHelper.TABLE_DICH_VU,
            values,
            "ma_dich_vu = ?",
            arrayOf(dichVu.maDichVu.toString())
        )
    }

    fun xoa(maDichVu: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_DICH_VU,
            "ma_dich_vu = ?",
            arrayOf(maDichVu.toString())
        )
    }

    fun layTatCa(): List<DichVu> {
        val danhSach = mutableListOf<DichVu>()
        val cursor = db.query(
            DatabaseHelper.TABLE_DICH_VU,
            null,
            null,
            null,
            null,
            null,
            "ten_dich_vu ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToDichVu(it))
            }
        }
        return danhSach
    }

    fun layTheoNha(maNha: Long): List<DichVu> {
        val danhSach = mutableListOf<DichVu>()
        val cursor = db.query(
            DatabaseHelper.TABLE_DICH_VU,
            null,
            "ma_nha = ?",
            arrayOf(maNha.toString()),
            null,
            null,
            "ten_dich_vu ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToDichVu(it))
            }
        }
        return danhSach
    }
    
    fun layDichVuHoatDong(maNha: Long): List<DichVu> {
        val danhSach = mutableListOf<DichVu>()
        val cursor = db.query(
            DatabaseHelper.TABLE_DICH_VU,
            null,
            "ma_nha = ? AND is_active = 1",
            arrayOf(maNha.toString()),
            null,
            null,
            "ten_dich_vu ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToDichVu(it))
            }
        }
        return danhSach
    }
    
    fun anHienDichVu(maDichVu: Long, isActive: Boolean): Int {
        val values = ContentValues().apply {
            put("is_active", if (isActive) 1 else 0)
        }
        return db.update(
            DatabaseHelper.TABLE_DICH_VU,
            values,
            "ma_dich_vu = ?",
            arrayOf(maDichVu.toString())
        )
    }

    fun layTheoMa(maDichVu: Long): DichVu? {
        val cursor = db.query(
            DatabaseHelper.TABLE_DICH_VU,
            null,
            "ma_dich_vu = ?",
            arrayOf(maDichVu.toString()),
            null,
            null,
            null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToDichVu(it)
            }
        }
        return null
    }

    private fun cursorToDichVu(cursor: Cursor): DichVu {
        val cachTinhIndex = cursor.getColumnIndex("cach_tinh")
        val isActiveIndex = cursor.getColumnIndex("is_active")
        
        // Convert từ REAL (Double) sang Long cho database v6
        val donGiaDouble = cursor.getDouble(cursor.getColumnIndexOrThrow("don_gia"))
        
        return DichVu(
            maDichVu = cursor.getLong(cursor.getColumnIndexOrThrow("ma_dich_vu")),
            maNha = cursor.getLong(cursor.getColumnIndexOrThrow("ma_nha")),
            tenDichVu = cursor.getString(cursor.getColumnIndexOrThrow("ten_dich_vu")),
            donVi = cursor.getString(cursor.getColumnIndexOrThrow("don_vi")) ?: "",
            donGia = donGiaDouble.toLong(), // Convert Double → Long
            cachTinh = if (cachTinhIndex >= 0) cursor.getString(cachTinhIndex) ?: "theo_phong" else "theo_phong",
            loaiDichVu = cursor.getString(cursor.getColumnIndexOrThrow("loai_dich_vu")),
            isActive = if (isActiveIndex >= 0) cursor.getInt(isActiveIndex) == 1 else true
        )
    }
}

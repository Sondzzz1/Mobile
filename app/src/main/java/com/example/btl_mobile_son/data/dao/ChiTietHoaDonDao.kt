package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.ChiTietHoaDon

class ChiTietHoaDonDao(private val db: SQLiteDatabase) {

    fun them(chiTiet: ChiTietHoaDon): Long {
        val values = ContentValues().apply {
            put("ma_hoa_don", chiTiet.maHoaDon)
            put("ten_dich_vu", chiTiet.tenDichVu)
            put("so_luong", chiTiet.soLuong)
            put("don_gia", chiTiet.donGia)
            put("thanh_tien", chiTiet.thanhTien)
        }
        return db.insert(DatabaseHelper.TABLE_CHI_TIET_HOA_DON, null, values)
    }

    fun capNhat(chiTiet: ChiTietHoaDon): Int {
        val values = ContentValues().apply {
            put("ma_hoa_don", chiTiet.maHoaDon)
            put("ten_dich_vu", chiTiet.tenDichVu)
            put("so_luong", chiTiet.soLuong)
            put("don_gia", chiTiet.donGia)
            put("thanh_tien", chiTiet.thanhTien)
        }
        return db.update(
            DatabaseHelper.TABLE_CHI_TIET_HOA_DON,
            values,
            "ma_chi_tiet = ?",
            arrayOf(chiTiet.maChiTiet.toString())
        )
    }

    fun xoa(maChiTiet: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_CHI_TIET_HOA_DON,
            "ma_chi_tiet = ?",
            arrayOf(maChiTiet.toString())
        )
    }

    fun layTheoHoaDon(maHoaDon: Long): List<ChiTietHoaDon> {
        val danhSach = mutableListOf<ChiTietHoaDon>()
        val cursor = db.query(
            DatabaseHelper.TABLE_CHI_TIET_HOA_DON,
            null,
            "ma_hoa_don = ?",
            arrayOf(maHoaDon.toString()),
            null, null, null
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToChiTiet(it))
            }
        }
        return danhSach
    }

    fun xoaTheoHoaDon(maHoaDon: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_CHI_TIET_HOA_DON,
            "ma_hoa_don = ?",
            arrayOf(maHoaDon.toString())
        )
    }

    private fun cursorToChiTiet(cursor: Cursor): ChiTietHoaDon {
        return ChiTietHoaDon(
            maChiTiet = cursor.getLong(cursor.getColumnIndexOrThrow("ma_chi_tiet")),
            maHoaDon = cursor.getLong(cursor.getColumnIndexOrThrow("ma_hoa_don")),
            tenDichVu = cursor.getString(cursor.getColumnIndexOrThrow("ten_dich_vu")),
            soLuong = cursor.getDouble(cursor.getColumnIndexOrThrow("so_luong")),
            donGia = cursor.getDouble(cursor.getColumnIndexOrThrow("don_gia")),
            thanhTien = cursor.getDouble(cursor.getColumnIndexOrThrow("thanh_tien"))
        )
    }
}

package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.HopDong

class HopDongDao(private val db: SQLiteDatabase) {

    fun them(hopDong: HopDong): Long {
        val values = ContentValues().apply {
            put("ma_phong", hopDong.maPhong)
            put("ma_khach", hopDong.maKhach)
            put("ngay_bat_dau", hopDong.ngayBatDau)
            put("ngay_ket_thuc", hopDong.ngayKetThuc)
            put("gia_thue_thang", hopDong.giaThueThang)
            put("tien_dat_coc", hopDong.tienDatCoc)
            put("trang_thai", hopDong.trangThai)
        }
        return db.insert(DatabaseHelper.TABLE_HOP_DONG, null, values)
    }

    fun capNhat(hopDong: HopDong): Int {
        val values = ContentValues().apply {
            put("ma_phong", hopDong.maPhong)
            put("ma_khach", hopDong.maKhach)
            put("ngay_bat_dau", hopDong.ngayBatDau)
            put("ngay_ket_thuc", hopDong.ngayKetThuc)
            put("gia_thue_thang", hopDong.giaThueThang)
            put("tien_dat_coc", hopDong.tienDatCoc)
            put("trang_thai", hopDong.trangThai)
        }
        return db.update(
            DatabaseHelper.TABLE_HOP_DONG,
            values,
            "ma_hop_dong = ?",
            arrayOf(hopDong.maHopDong.toString())
        )
    }

    fun xoa(maHopDong: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_HOP_DONG,
            "ma_hop_dong = ?",
            arrayOf(maHopDong.toString())
        )
    }

    fun layTatCa(): List<HopDong> {
        val danhSach = mutableListOf<HopDong>()
        val cursor = db.query(
            DatabaseHelper.TABLE_HOP_DONG,
            null,
            null,
            null,
            null,
            null,
            "ngay_bat_dau DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToHopDong(it))
            }
        }
        return danhSach
    }

    fun layTheoPhong(maPhong: Long): List<HopDong> {
        val danhSach = mutableListOf<HopDong>()
        val cursor = db.query(
            DatabaseHelper.TABLE_HOP_DONG,
            null,
            "ma_phong = ?",
            arrayOf(maPhong.toString()),
            null,
            null,
            "ngay_bat_dau DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToHopDong(it))
            }
        }
        return danhSach
    }

    fun layHopDongDangThue(maPhong: Long): HopDong? {
        val cursor = db.query(
            DatabaseHelper.TABLE_HOP_DONG,
            null,
            "ma_phong = ? AND trang_thai = 'dang_thue'",
            arrayOf(maPhong.toString()),
            null,
            null,
            null,
            "1"
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToHopDong(it)
            }
        }
        return null
    }

    fun layTheoMa(maHopDong: Long): HopDong? {
        val cursor = db.query(
            DatabaseHelper.TABLE_HOP_DONG,
            null,
            "ma_hop_dong = ?",
            arrayOf(maHopDong.toString()),
            null,
            null,
            null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToHopDong(it)
            }
        }
        return null
    }

    /**
     * Cập nhật trạng thái hợp đồng hết hạn
     * Gọi method này định kỳ để tự động cập nhật trạng thái
     */
    fun capNhatHopDongHetHan(): Int {
        val values = ContentValues().apply {
            put("trang_thai", "het_han")
        }
        val now = System.currentTimeMillis()
        return db.update(
            DatabaseHelper.TABLE_HOP_DONG,
            values,
            "ngay_ket_thuc < ? AND trang_thai = 'dang_thue'",
            arrayOf(now.toString())
        )
    }

    private fun cursorToHopDong(cursor: Cursor): HopDong {
        return HopDong(
            maHopDong = cursor.getLong(cursor.getColumnIndexOrThrow("ma_hop_dong")),
            maPhong = cursor.getLong(cursor.getColumnIndexOrThrow("ma_phong")),
            maKhach = cursor.getLong(cursor.getColumnIndexOrThrow("ma_khach")),
            ngayBatDau = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_bat_dau")),
            ngayKetThuc = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_ket_thuc")),
            giaThueThang = cursor.getLong(cursor.getColumnIndexOrThrow("gia_thue_thang")),
            tienDatCoc = cursor.getLong(cursor.getColumnIndexOrThrow("tien_dat_coc")),
            trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai"))
        )
    }
}

package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.DatCoc

class DatCocDao(private val db: SQLiteDatabase) {

    fun them(datCoc: DatCoc): Long {
        val values = ContentValues().apply {
            put("ma_phong", datCoc.maPhong)
            put("ten_khach", datCoc.tenKhach)
            put("so_dien_thoai", datCoc.soDienThoai)
            put("so_cmnd", datCoc.soCmnd)
            put("email", datCoc.email)
            put("tien_dat_coc", datCoc.tienDatCoc)
            put("gia_phong", datCoc.giaPhong)
            put("ngay_du_kien_vao", datCoc.ngayDuKienVao)
            put("ghi_chu", datCoc.ghiChu)
            put("ngay_tao", datCoc.ngayTao)
        }
        return db.insert(DatabaseHelper.TABLE_DAT_COC, null, values)
    }

    fun capNhat(datCoc: DatCoc): Int {
        val values = ContentValues().apply {
            put("ma_phong", datCoc.maPhong)
            put("ten_khach", datCoc.tenKhach)
            put("so_dien_thoai", datCoc.soDienThoai)
            put("so_cmnd", datCoc.soCmnd)
            put("email", datCoc.email)
            put("tien_dat_coc", datCoc.tienDatCoc)
            put("gia_phong", datCoc.giaPhong)
            put("ngay_du_kien_vao", datCoc.ngayDuKienVao)
            put("trang_thai", datCoc.trangThai)
            put("ghi_chu", datCoc.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_DAT_COC,
            values,
            "ma_dat_coc = ?",
            arrayOf(datCoc.maDatCoc.toString())
        )
    }

    /**
     * Cập nhật trạng thái đặt cọc
     * @param maDatCoc ID đặt cọc
     * @param trangThaiMoi Trạng thái mới: "hieu_luc" | "da_chuyen_hop_dong" | "da_huy" | "mat_coc" | "da_hoan"
     */
    fun capNhatTrangThai(maDatCoc: Long, trangThaiMoi: String): Int {
        val values = ContentValues().apply {
            put("trang_thai", trangThaiMoi)
        }
        return db.update(
            DatabaseHelper.TABLE_DAT_COC,
            values,
            "ma_dat_coc = ?",
            arrayOf(maDatCoc.toString())
        )
    }

    fun xoa(maDatCoc: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_DAT_COC,
            "ma_dat_coc = ?",
            arrayOf(maDatCoc.toString())
        )
    }

    fun layTatCa(): List<DatCoc> {
        val danhSach = mutableListOf<DatCoc>()
        val cursor = db.query(
            DatabaseHelper.TABLE_DAT_COC,
            null, null, null, null, null,
            "ngay_tao DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToDatCoc(it))
            }
        }
        return danhSach
    }

    fun layTheoMa(maDatCoc: Long): DatCoc? {
        val cursor = db.query(
            DatabaseHelper.TABLE_DAT_COC,
            null,
            "ma_dat_coc = ?",
            arrayOf(maDatCoc.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToDatCoc(it)
            }
        }
        return null
    }

    fun tinhTongDatCoc(): Long {
        val cursor = db.rawQuery(
            "SELECT SUM(tien_dat_coc) FROM ${DatabaseHelper.TABLE_DAT_COC}",
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        return 0
    }

    fun layTheoPhong(maPhong: Long): List<DatCoc> {
        val danhSach = mutableListOf<DatCoc>()
        val cursor = db.query(
            DatabaseHelper.TABLE_DAT_COC,
            null,
            "ma_phong = ?",
            arrayOf(maPhong.toString()),
            null, null,
            "ngay_tao DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToDatCoc(it))
            }
        }
        return danhSach
    }

    private fun cursorToDatCoc(cursor: Cursor): DatCoc {
        // Convert từ REAL (Double) sang Long cho database v6
        val tienDatCocDouble = cursor.getDouble(cursor.getColumnIndexOrThrow("tien_dat_coc"))
        val giaPhongDouble = cursor.getDouble(cursor.getColumnIndexOrThrow("gia_phong"))
        
        return DatCoc(
            maDatCoc = cursor.getLong(cursor.getColumnIndexOrThrow("ma_dat_coc")),
            maPhong = cursor.getLong(cursor.getColumnIndexOrThrow("ma_phong")),
            tenKhach = cursor.getString(cursor.getColumnIndexOrThrow("ten_khach")),
            soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) ?: "",
            soCmnd = cursor.getString(cursor.getColumnIndexOrThrow("so_cmnd")) ?: "",
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")) ?: "",
            tienDatCoc = tienDatCocDouble.toLong(), // Convert Double → Long
            giaPhong = giaPhongDouble.toLong(), // Convert Double → Long
            ngayDuKienVao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_du_kien_vao")),
            trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")) ?: "hieu_luc",
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: "",
            ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
        )
    }
}

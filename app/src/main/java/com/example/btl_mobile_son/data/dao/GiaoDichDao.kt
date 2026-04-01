package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.GiaoDich

class GiaoDichDao(private val db: SQLiteDatabase) {

    fun them(giaoDich: GiaoDich): Long {
        val values = ContentValues().apply {
            put("loai", giaoDich.loai)
            put("ma_phong", giaoDich.maPhong)
            put("ma_hoa_don", giaoDich.maHoaDon)
            put("ma_hop_dong", giaoDich.maHopDong)
            put("ma_dat_coc", giaoDich.maDatCoc)
            put("so_tien", giaoDich.soTien)
            put("danh_muc", giaoDich.danhMuc)
            put("ngay_giao_dich", giaoDich.ngayGiaoDich)
            put("noi_dung", giaoDich.noiDung)
            put("ten_nguoi", giaoDich.tenNguoi)
            put("phuong_thuc_thanh_toan", giaoDich.phuongThucThanhToan)
            put("ghi_chu", giaoDich.ghiChu)
            put("ngay_tao", giaoDich.ngayTao)
        }
        return db.insert(DatabaseHelper.TABLE_GIAO_DICH, null, values)
    }

    fun capNhat(giaoDich: GiaoDich): Int {
        val values = ContentValues().apply {
            put("loai", giaoDich.loai)
            put("ma_phong", giaoDich.maPhong)
            put("ma_hoa_don", giaoDich.maHoaDon)
            put("ma_hop_dong", giaoDich.maHopDong)
            put("ma_dat_coc", giaoDich.maDatCoc)
            put("so_tien", giaoDich.soTien)
            put("danh_muc", giaoDich.danhMuc)
            put("ngay_giao_dich", giaoDich.ngayGiaoDich)
            put("noi_dung", giaoDich.noiDung)
            put("ten_nguoi", giaoDich.tenNguoi)
            put("phuong_thuc_thanh_toan", giaoDich.phuongThucThanhToan)
            put("ghi_chu", giaoDich.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_GIAO_DICH,
            values,
            "ma_giao_dich = ?",
            arrayOf(giaoDich.maGiaoDich.toString())
        )
    }

    fun xoa(maGiaoDich: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_GIAO_DICH,
            "ma_giao_dich = ?",
            arrayOf(maGiaoDich.toString())
        )
    }

    fun layTatCa(): List<GiaoDich> {
        val danhSach = mutableListOf<GiaoDich>()
        val cursor = db.query(
            DatabaseHelper.TABLE_GIAO_DICH,
            null, null, null, null, null,
            "ngay_giao_dich DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToGiaoDich(it))
            }
        }
        return danhSach
    }

    fun layTheoLoai(loai: String): List<GiaoDich> {
        val danhSach = mutableListOf<GiaoDich>()
        val cursor = db.query(
            DatabaseHelper.TABLE_GIAO_DICH,
            null,
            "loai = ?",
            arrayOf(loai),
            null, null,
            "ngay_giao_dich DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToGiaoDich(it))
            }
        }
        return danhSach
    }

    fun tinhTongTheoLoai(loai: String): Long {
        val cursor = db.rawQuery(
            "SELECT SUM(so_tien) FROM ${DatabaseHelper.TABLE_GIAO_DICH} WHERE loai = ?",
            arrayOf(loai)
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        return 0
    }

    fun layTheoMa(maGiaoDich: Long): GiaoDich? {
        val cursor = db.query(
            DatabaseHelper.TABLE_GIAO_DICH,
            null,
            "ma_giao_dich = ?",
            arrayOf(maGiaoDich.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToGiaoDich(it)
            }
        }
        return null
    }

    private fun cursorToGiaoDich(cursor: Cursor): GiaoDich {
        return GiaoDich(
            maGiaoDich = cursor.getLong(cursor.getColumnIndexOrThrow("ma_giao_dich")),
            loai = cursor.getString(cursor.getColumnIndexOrThrow("loai")),
            maPhong = if (cursor.isNull(cursor.getColumnIndexOrThrow("ma_phong"))) null 
                      else cursor.getLong(cursor.getColumnIndexOrThrow("ma_phong")),
            maHoaDon = try {
                if (cursor.isNull(cursor.getColumnIndexOrThrow("ma_hoa_don"))) null
                else cursor.getLong(cursor.getColumnIndexOrThrow("ma_hoa_don"))
            } catch (e: Exception) { null },
            maHopDong = try {
                if (cursor.isNull(cursor.getColumnIndexOrThrow("ma_hop_dong"))) null
                else cursor.getLong(cursor.getColumnIndexOrThrow("ma_hop_dong"))
            } catch (e: Exception) { null },
            maDatCoc = try {
                if (cursor.isNull(cursor.getColumnIndexOrThrow("ma_dat_coc"))) null
                else cursor.getLong(cursor.getColumnIndexOrThrow("ma_dat_coc"))
            } catch (e: Exception) { null },
            soTien = cursor.getLong(cursor.getColumnIndexOrThrow("so_tien")),
            danhMuc = cursor.getString(cursor.getColumnIndexOrThrow("danh_muc")) ?: "",
            ngayGiaoDich = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_giao_dich")),
            noiDung = cursor.getString(cursor.getColumnIndexOrThrow("noi_dung")) ?: "",
            tenNguoi = cursor.getString(cursor.getColumnIndexOrThrow("ten_nguoi")) ?: "",
            phuongThucThanhToan = cursor.getString(cursor.getColumnIndexOrThrow("phuong_thuc_thanh_toan")),
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: "",
            ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
        )
    }
}

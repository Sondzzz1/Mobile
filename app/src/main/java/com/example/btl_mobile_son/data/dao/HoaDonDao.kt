package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.HoaDon

class HoaDonDao(private val db: SQLiteDatabase) {

    fun them(hoaDon: HoaDon): Long {
        val values = ContentValues().apply {
            put("ma_hop_dong", hoaDon.maHopDong)
            put("thang", hoaDon.thang)
            put("nam", hoaDon.nam)
            put("tien_phong", hoaDon.tienPhong)
            put("tong_tien_dich_vu", hoaDon.tongTienDichVu)
            put("giam_gia", hoaDon.giamGia)
            put("tong_tien", hoaDon.tongTien)
            put("tien_da_thanh_toan", hoaDon.tienDaThanhToan)
            put("trang_thai", hoaDon.trangThai)
            put("ghi_chu", hoaDon.ghiChu)
            put("ngay_tao", hoaDon.ngayTao)
        }
        return db.insert(DatabaseHelper.TABLE_HOA_DON, null, values)
    }

    fun capNhat(hoaDon: HoaDon): Int {
        val values = ContentValues().apply {
            put("ma_hop_dong", hoaDon.maHopDong)
            put("thang", hoaDon.thang)
            put("nam", hoaDon.nam)
            put("tien_phong", hoaDon.tienPhong)
            put("tong_tien_dich_vu", hoaDon.tongTienDichVu)
            put("giam_gia", hoaDon.giamGia)
            put("tong_tien", hoaDon.tongTien)
            put("tien_da_thanh_toan", hoaDon.tienDaThanhToan)
            put("trang_thai", hoaDon.trangThai)
            put("ghi_chu", hoaDon.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_HOA_DON,
            values,
            "ma_hoa_don = ?",
            arrayOf(hoaDon.maHoaDon.toString())
        )
    }

    fun xoa(maHoaDon: Long): Int {
        return db.delete(
            DatabaseHelper.TABLE_HOA_DON,
            "ma_hoa_don = ?",
            arrayOf(maHoaDon.toString())
        )
    }

    fun layTatCa(): List<HoaDon> {
        val danhSach = mutableListOf<HoaDon>()
        val cursor = db.query(
            DatabaseHelper.TABLE_HOA_DON,
            null, null, null, null, null,
            "ngay_tao DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToHoaDon(it))
            }
        }
        return danhSach
    }

    fun layTheoThangNam(thang: Int, nam: Int): List<HoaDon> {
        val danhSach = mutableListOf<HoaDon>()
        val cursor = db.query(
            DatabaseHelper.TABLE_HOA_DON,
            null,
            "thang = ? AND nam = ?",
            arrayOf(thang.toString(), nam.toString()),
            null, null,
            "ngay_tao DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                danhSach.add(cursorToHoaDon(it))
            }
        }
        return danhSach
    }

    fun layTheoMa(maHoaDon: Long): HoaDon? {
        val cursor = db.query(
            DatabaseHelper.TABLE_HOA_DON,
            null,
            "ma_hoa_don = ?",
            arrayOf(maHoaDon.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToHoaDon(it)
            }
        }
        return null
    }

    fun tinhTongChuaThanhToan(): Long {
        val cursor = db.rawQuery(
            "SELECT SUM(tong_tien - tien_da_thanh_toan) FROM ${DatabaseHelper.TABLE_HOA_DON} WHERE trang_thai != 'da_thanh_toan'",
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        return 0
    }

    fun thanhToanHoaDon(maHoaDon: Long, soTien: Long): Int {
        val hoaDon = layTheoMa(maHoaDon) ?: return 0
        val tienMoi = hoaDon.tienDaThanhToan + soTien
        val trangThaiMoi = when {
            tienMoi >= hoaDon.tongTien -> "da_thanh_toan"
            tienMoi > 0 -> "thanh_toan_mot_phan"
            else -> "chua_thanh_toan"
        }
        
        val values = ContentValues().apply {
            put("tien_da_thanh_toan", tienMoi)
            put("trang_thai", trangThaiMoi)
        }
        return db.update(
            DatabaseHelper.TABLE_HOA_DON,
            values,
            "ma_hoa_don = ?",
            arrayOf(maHoaDon.toString())
        )
    }

    fun danhDauDaThanhToan(maHoaDon: Long): Int {
        val hoaDon = layTheoMa(maHoaDon) ?: return 0
        val values = ContentValues().apply {
            put("tien_da_thanh_toan", hoaDon.tongTien)
            put("trang_thai", "da_thanh_toan")
        }
        return db.update(
            DatabaseHelper.TABLE_HOA_DON,
            values,
            "ma_hoa_don = ?",
            arrayOf(maHoaDon.toString())
        )
    }

    /**
     * Kiểm tra trùng hóa đơn (VĐ8)
     * Mỗi hợp đồng chỉ có 1 hóa đơn cho mỗi tháng/năm
     */
    fun kiemTraTrungHoaDon(maHopDong: Long, thang: Int, nam: Int, maHoaDonLoaiTru: Long = -1): Boolean {
        val cursor = if (maHoaDonLoaiTru > 0) {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_HOA_DON} " +
                "WHERE ma_hop_dong = ? AND thang = ? AND nam = ? AND ma_hoa_don != ?",
                arrayOf(maHopDong.toString(), thang.toString(), nam.toString(), maHoaDonLoaiTru.toString())
            )
        } else {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_HOA_DON} " +
                "WHERE ma_hop_dong = ? AND thang = ? AND nam = ?",
                arrayOf(maHopDong.toString(), thang.toString(), nam.toString())
            )
        }
        
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) > 0
            }
        }
        return false
    }

    /**
     * Lấy hóa đơn theo hợp đồng và tháng/năm
     */
    fun layHoaDonTheoHopDongVaThang(maHopDong: Long, thang: Int, nam: Int): HoaDon? {
        val cursor = db.query(
            DatabaseHelper.TABLE_HOA_DON,
            null,
            "ma_hop_dong = ? AND thang = ? AND nam = ?",
            arrayOf(maHopDong.toString(), thang.toString(), nam.toString()),
            null, null, null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToHoaDon(it)
            }
        }
        return null
    }

    private fun cursorToHoaDon(cursor: Cursor): HoaDon {
        return HoaDon(
            maHoaDon = cursor.getLong(cursor.getColumnIndexOrThrow("ma_hoa_don")),
            maHopDong = cursor.getLong(cursor.getColumnIndexOrThrow("ma_hop_dong")),
            thang = cursor.getInt(cursor.getColumnIndexOrThrow("thang")),
            nam = cursor.getInt(cursor.getColumnIndexOrThrow("nam")),
            tienPhong = cursor.getLong(cursor.getColumnIndexOrThrow("tien_phong")),
            tongTienDichVu = cursor.getLong(cursor.getColumnIndexOrThrow("tong_tien_dich_vu")),
            giamGia = cursor.getLong(cursor.getColumnIndexOrThrow("giam_gia")),
            tongTien = cursor.getLong(cursor.getColumnIndexOrThrow("tong_tien")),
            tienDaThanhToan = cursor.getLong(cursor.getColumnIndexOrThrow("tien_da_thanh_toan")),
            trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")) ?: "chua_thanh_toan",
            ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: "",
            ngayTao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_tao"))
        )
    }
}

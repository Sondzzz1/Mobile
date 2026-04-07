package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.db.DatabaseHelper
import com.example.btl_mobile_son.data.model.SuCo

class SuCoDao(private val db: SQLiteDatabase) {

    fun them(suCo: SuCo): Long {
        val values = ContentValues().apply {
            put("ma_phong", suCo.maPhong)
            put("loai_su_co", suCo.loaiSuCo)
            put("mo_ta", suCo.moTa)
            put("trang_thai", suCo.trangThai)
            put("nguoi_bao_cao", suCo.nguoiBaoGao)
            put("nguoi_xu_ly", suCo.nguoiXuLy)
            put("ngay_bao_cao", suCo.ngayBaoGao)
            if (suCo.ngayXuLy != null) put("ngay_xu_ly", suCo.ngayXuLy)
            put("chi_phi", suCo.chiPhi)
            put("ghi_chu", suCo.ghiChu)
        }
        return db.insert(DatabaseHelper.TABLE_SU_CO, null, values)
    }

    fun capNhat(suCo: SuCo): Int {
        val values = ContentValues().apply {
            put("ma_phong", suCo.maPhong)
            put("loai_su_co", suCo.loaiSuCo)
            put("mo_ta", suCo.moTa)
            put("trang_thai", suCo.trangThai)
            put("nguoi_bao_cao", suCo.nguoiBaoGao)
            put("nguoi_xu_ly", suCo.nguoiXuLy)
            put("ngay_bao_cao", suCo.ngayBaoGao)
            if (suCo.ngayXuLy != null) put("ngay_xu_ly", suCo.ngayXuLy)
            put("chi_phi", suCo.chiPhi)
            put("ghi_chu", suCo.ghiChu)
        }
        return db.update(
            DatabaseHelper.TABLE_SU_CO,
            values,
            "ma_su_co = ?",
            arrayOf(suCo.maSuCo.toString())
        )
    }

    fun xoa(maSuCo: Int): Int {
        return db.delete(
            DatabaseHelper.TABLE_SU_CO,
            "ma_su_co = ?",
            arrayOf(maSuCo.toString())
        )
    }

    fun layTheoId(maSuCo: Int): SuCo? {
        val cursor = db.query(
            DatabaseHelper.TABLE_SU_CO,
            null,
            "ma_su_co = ?",
            arrayOf(maSuCo.toString()),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val suCo = SuCo(
                maSuCo = cursor.getInt(cursor.getColumnIndexOrThrow("ma_su_co")),
                maPhong = cursor.getInt(cursor.getColumnIndexOrThrow("ma_phong")),
                loaiSuCo = cursor.getString(cursor.getColumnIndexOrThrow("loai_su_co")),
                moTa = cursor.getString(cursor.getColumnIndexOrThrow("mo_ta")) ?: "",
                trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                nguoiBaoGao = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_bao_cao")) ?: "",
                nguoiXuLy = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_xu_ly")) ?: "",
                ngayBaoGao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_bao_cao")),
                ngayXuLy = if (cursor.isNull(cursor.getColumnIndexOrThrow("ngay_xu_ly"))) null 
                          else cursor.getLong(cursor.getColumnIndexOrThrow("ngay_xu_ly")),
                chiPhi = cursor.getDouble(cursor.getColumnIndexOrThrow("chi_phi")),
                ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
            )
            cursor.close()
            suCo
        } else {
            cursor.close()
            null
        }
    }

    fun layTatCa(): List<SuCo> {
        val danhSach = mutableListOf<SuCo>()
        val cursor = db.query(
            DatabaseHelper.TABLE_SU_CO,
            null, null, null, null, null,
            "ngay_bao_cao DESC"
        )
        
        while (cursor.moveToNext()) {
            danhSach.add(
                SuCo(
                    maSuCo = cursor.getInt(cursor.getColumnIndexOrThrow("ma_su_co")),
                    maPhong = cursor.getInt(cursor.getColumnIndexOrThrow("ma_phong")),
                    loaiSuCo = cursor.getString(cursor.getColumnIndexOrThrow("loai_su_co")),
                    moTa = cursor.getString(cursor.getColumnIndexOrThrow("mo_ta")) ?: "",
                    trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                    nguoiBaoGao = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_bao_cao")) ?: "",
                    nguoiXuLy = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_xu_ly")) ?: "",
                    ngayBaoGao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_bao_cao")),
                    ngayXuLy = if (cursor.isNull(cursor.getColumnIndexOrThrow("ngay_xu_ly"))) null 
                              else cursor.getLong(cursor.getColumnIndexOrThrow("ngay_xu_ly")),
                    chiPhi = cursor.getDouble(cursor.getColumnIndexOrThrow("chi_phi")),
                    ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
                )
            )
        }
        cursor.close()
        return danhSach
    }

    fun layTheoPhong(maPhong: Int): List<SuCo> {
        val danhSach = mutableListOf<SuCo>()
        val cursor = db.query(
            DatabaseHelper.TABLE_SU_CO,
            null,
            "ma_phong = ?",
            arrayOf(maPhong.toString()),
            null, null,
            "ngay_bao_cao DESC"
        )
        
        while (cursor.moveToNext()) {
            danhSach.add(
                SuCo(
                    maSuCo = cursor.getInt(cursor.getColumnIndexOrThrow("ma_su_co")),
                    maPhong = cursor.getInt(cursor.getColumnIndexOrThrow("ma_phong")),
                    loaiSuCo = cursor.getString(cursor.getColumnIndexOrThrow("loai_su_co")),
                    moTa = cursor.getString(cursor.getColumnIndexOrThrow("mo_ta")) ?: "",
                    trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                    nguoiBaoGao = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_bao_cao")) ?: "",
                    nguoiXuLy = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_xu_ly")) ?: "",
                    ngayBaoGao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_bao_cao")),
                    ngayXuLy = if (cursor.isNull(cursor.getColumnIndexOrThrow("ngay_xu_ly"))) null 
                              else cursor.getLong(cursor.getColumnIndexOrThrow("ngay_xu_ly")),
                    chiPhi = cursor.getDouble(cursor.getColumnIndexOrThrow("chi_phi")),
                    ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
                )
            )
        }
        cursor.close()
        return danhSach
    }

    fun layTheoTrangThai(trangThai: String): List<SuCo> {
        val danhSach = mutableListOf<SuCo>()
        val cursor = db.query(
            DatabaseHelper.TABLE_SU_CO,
            null,
            "trang_thai = ?",
            arrayOf(trangThai),
            null, null,
            "ngay_bao_cao DESC"
        )
        
        while (cursor.moveToNext()) {
            danhSach.add(
                SuCo(
                    maSuCo = cursor.getInt(cursor.getColumnIndexOrThrow("ma_su_co")),
                    maPhong = cursor.getInt(cursor.getColumnIndexOrThrow("ma_phong")),
                    loaiSuCo = cursor.getString(cursor.getColumnIndexOrThrow("loai_su_co")),
                    moTa = cursor.getString(cursor.getColumnIndexOrThrow("mo_ta")) ?: "",
                    trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                    nguoiBaoGao = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_bao_cao")) ?: "",
                    nguoiXuLy = cursor.getString(cursor.getColumnIndexOrThrow("nguoi_xu_ly")) ?: "",
                    ngayBaoGao = cursor.getLong(cursor.getColumnIndexOrThrow("ngay_bao_cao")),
                    ngayXuLy = if (cursor.isNull(cursor.getColumnIndexOrThrow("ngay_xu_ly"))) null 
                              else cursor.getLong(cursor.getColumnIndexOrThrow("ngay_xu_ly")),
                    chiPhi = cursor.getDouble(cursor.getColumnIndexOrThrow("chi_phi")),
                    ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")) ?: ""
                )
            )
        }
        cursor.close()
        return danhSach
    }
}

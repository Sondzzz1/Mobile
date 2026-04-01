# KẾ HOẠCH THỰC HIỆN CẢI TIẾN MÔ HÌNH DỮ LIỆU

## 📋 TỔNG QUAN

### Các thay đổi cần thực hiện:
1. ✅ Tạo bảng HopDongThanhVien
2. ✅ Sửa bảng KhachThue (bỏ maPhong, trangThai)
3. ✅ Sửa bảng DatCoc (thêm trangThai)
4. ✅ Tạo DAO cho HopDongThanhVien
5. ✅ Cập nhật DatabaseHelper (migration)
6. ✅ Sửa logic nghiệp vụ

---

## BƯỚC 1: TẠO MODEL MỚI

### 1.1. HopDongThanhVien.kt
```kotlin
package com.example.btl_mobile_son.data.model

data class HopDongThanhVien(
    val maThanhVien: Long = 0,
    val maHopDong: Long,
    val maKhach: Long,
    val vaiTro: String = "thanh_vien", // "dai_dien" | "thanh_vien"
    val ngayVaoO: Long = System.currentTimeMillis(),
    val ngayRoiDi: Long? = null,
    val trangThai: String = "dang_o", // "dang_o" | "da_roi"
    val ghiChu: String = ""
)
```

### 1.2. Sửa KhachThue.kt
```kotlin
package com.example.btl_mobile_son.data.model

data class KhachThue(
    val maKhach: Long = 0,
    val hoTen: String,
    val soDienThoai: String = "",
    val email: String = "",
    val soCmnd: String = "",
    val ngaySinh: Long? = null,
    val ngayCap: Long? = null,
    val noiCap: String = "",
    val noiLamViec: String = "",
    val tinhThanh: String = "",
    val quanHuyen: String = "",
    val xaPhuong: String = "",
    val diaChiChiTiet: String = "",
    // BỎ: val maPhong: Long?
    // BỎ: val trangThai: String
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

### 1.3. Sửa DatCoc.kt
```kotlin
package com.example.btl_mobile_son.data.model

data class DatCoc(
    val maDatCoc: Long = 0,
    val maPhong: Long,
    val tenKhach: String,
    val soDienThoai: String = "",
    val soCmnd: String = "",
    val email: String = "",
    val tienDatCoc: Double,
    val giaPhong: Double = 0.0,
    val ngayDuKienVao: Long = 0,
    val trangThai: String = "hieu_luc", // THÊM MỚI
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

---

## BƯỚC 2: TẠO DAO

### 2.1. HopDongThanhVienDao.kt
```kotlin
package com.example.btl_mobile_son.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.model.HopDongThanhVien

class HopDongThanhVienDao(private val db: SQLiteDatabase) {
    
    companion object {
        const val TABLE_NAME = "HopDongThanhVien"
        const val COL_MA_THANH_VIEN = "maThanhVien"
        const val COL_MA_HOP_DONG = "maHopDong"
        const val COL_MA_KHACH = "maKhach"
        const val COL_VAI_TRO = "vaiTro"
        const val COL_NGAY_VAO_O = "ngayVaoO"
        const val COL_NGAY_ROI_DI = "ngayRoiDi"
        const val COL_TRANG_THAI = "trangThai"
        const val COL_GHI_CHU = "ghiChu"
    }
    
    fun them(tv: HopDongThanhVien): Long {
        val values = ContentValues().apply {
            put(COL_MA_HOP_DONG, tv.maHopDong)
            put(COL_MA_KHACH, tv.maKhach)
            put(COL_VAI_TRO, tv.vaiTro)
            put(COL_NGAY_VAO_O, tv.ngayVaoO)
            put(COL_NGAY_ROI_DI, tv.ngayRoiDi)
            put(COL_TRANG_THAI, tv.trangThai)
            put(COL_GHI_CHU, tv.ghiChu)
        }
        return db.insert(TABLE_NAME, null, values)
    }
    
    fun capNhat(tv: HopDongThanhVien): Int {
        val values = ContentValues().apply {
            put(COL_MA_HOP_DONG, tv.maHopDong)
            put(COL_MA_KHACH, tv.maKhach)
            put(COL_VAI_TRO, tv.vaiTro)
            put(COL_NGAY_VAO_O, tv.ngayVaoO)
            put(COL_NGAY_ROI_DI, tv.ngayRoiDi)
            put(COL_TRANG_THAI, tv.trangThai)
            put(COL_GHI_CHU, tv.ghiChu)
        }
        return db.update(TABLE_NAME, values, "$COL_MA_THANH_VIEN = ?", arrayOf(tv.maThanhVien.toString()))
    }
    
    fun xoa(maThanhVien: Long): Int {
        return db.delete(TABLE_NAME, "$COL_MA_THANH_VIEN = ?", arrayOf(maThanhVien.toString()))
    }
    
    fun layTheoHopDong(maHopDong: Long): List<HopDongThanhVien> {
        val list = mutableListOf<HopDongThanhVien>()
        val cursor = db.query(
            TABLE_NAME, null,
            "$COL_MA_HOP_DONG = ?", arrayOf(maHopDong.toString()),
            null, null, "$COL_NGAY_VAO_O ASC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                list.add(HopDongThanhVien(
                    maThanhVien = it.getLong(it.getColumnIndexOrThrow(COL_MA_THANH_VIEN)),
                    maHopDong = it.getLong(it.getColumnIndexOrThrow(COL_MA_HOP_DONG)),
                    maKhach = it.getLong(it.getColumnIndexOrThrow(COL_MA_KHACH)),
                    vaiTro = it.getString(it.getColumnIndexOrThrow(COL_VAI_TRO)),
                    ngayVaoO = it.getLong(it.getColumnIndexOrThrow(COL_NGAY_VAO_O)),
                    ngayRoiDi = if (it.isNull(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI))) null 
                                else it.getLong(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI)),
                    trangThai = it.getString(it.getColumnIndexOrThrow(COL_TRANG_THAI)),
                    ghiChu = it.getString(it.getColumnIndexOrThrow(COL_GHI_CHU))
                ))
            }
        }
        return list
    }
    
    fun layTheoKhach(maKhach: Long): List<HopDongThanhVien> {
        val list = mutableListOf<HopDongThanhVien>()
        val cursor = db.query(
            TABLE_NAME, null,
            "$COL_MA_KHACH = ?", arrayOf(maKhach.toString()),
            null, null, "$COL_NGAY_VAO_O DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                list.add(HopDongThanhVien(
                    maThanhVien = it.getLong(it.getColumnIndexOrThrow(COL_MA_THANH_VIEN)),
                    maHopDong = it.getLong(it.getColumnIndexOrThrow(COL_MA_HOP_DONG)),
                    maKhach = it.getLong(it.getColumnIndexOrThrow(COL_MA_KHACH)),
                    vaiTro = it.getString(it.getColumnIndexOrThrow(COL_VAI_TRO)),
                    ngayVaoO = it.getLong(it.getColumnIndexOrThrow(COL_NGAY_VAO_O)),
                    ngayRoiDi = if (it.isNull(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI))) null 
                                else it.getLong(it.getColumnIndexOrThrow(COL_NGAY_ROI_DI)),
                    trangThai = it.getString(it.getColumnIndexOrThrow(COL_TRANG_THAI)),
                    ghiChu = it.getString(it.getColumnIndexOrThrow(COL_GHI_CHU))
                ))
            }
        }
        return list
    }
    
    fun layNguoiDangOTheoHopDong(maHopDong: Long): List<HopDongThanhVien> {
        return layTheoHopDong(maHopDong).filter { it.trangThai == "dang_o" }
    }
    
    fun demNguoiDangO(maHopDong: Long): Int {
        return layNguoiDangOTheoHopDong(maHopDong).size
    }
}
```

---

## BƯỚC 3: CẬP NHẬT DATABASE

### 3.1. DatabaseHelper.kt - Migration
```kotlin
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        const val DATABASE_NAME = "QuanLyNhaTro.db"
        const val DATABASE_VERSION = 4 // Tăng từ 3 lên 4
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1 -> {
                // Migration từ v1 → v2
                upgradeToVersion2(db)
                upgradeToVersion3(db)
                upgradeToVersion4(db)
            }
            2 -> {
                // Migration từ v2 → v3
                upgradeToVersion3(db)
                upgradeToVersion4(db)
            }
            3 -> {
                // Migration từ v3 → v4
                upgradeToVersion4(db)
            }
        }
    }
    
    private fun upgradeToVersion4(db: SQLiteDatabase) {
        // 1. Tạo bảng HopDongThanhVien
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS HopDongThanhVien (
                maThanhVien INTEGER PRIMARY KEY AUTOINCREMENT,
                maHopDong INTEGER NOT NULL,
                maKhach INTEGER NOT NULL,
                vaiTro TEXT NOT NULL DEFAULT 'thanh_vien',
                ngayVaoO INTEGER NOT NULL,
                ngayRoiDi INTEGER,
                trangThai TEXT NOT NULL DEFAULT 'dang_o',
                ghiChu TEXT,
                FOREIGN KEY (maHopDong) REFERENCES HopDong(maHopDong),
                FOREIGN KEY (maKhach) REFERENCES KhachThue(maKhach)
            )
        """)
        
        // 2. Migrate dữ liệu cũ: Tạo HopDongThanhVien từ HopDong hiện tại
        db.execSQL("""
            INSERT INTO HopDongThanhVien (maHopDong, maKhach, vaiTro, ngayVaoO, trangThai)
            SELECT maHopDong, maKhach, 'dai_dien', ngayBatDau, 
                   CASE WHEN trangThai = 'dang_thue' THEN 'dang_o' ELSE 'da_roi' END
            FROM HopDong
        """)
        
        // 3. Thêm cột trangThai vào DatCoc
        db.execSQL("ALTER TABLE DatCoc ADD COLUMN trangThai TEXT NOT NULL DEFAULT 'hieu_luc'")
        
        // 4. Xóa cột maPhong và trangThai từ KhachThue
        // SQLite không hỗ trợ DROP COLUMN, phải tạo bảng mới
        db.execSQL("""
            CREATE TABLE KhachThue_new (
                maKhach INTEGER PRIMARY KEY AUTOINCREMENT,
                hoTen TEXT NOT NULL,
                soDienThoai TEXT,
                email TEXT,
                soCmnd TEXT,
                ngaySinh INTEGER,
                ngayCap INTEGER,
                noiCap TEXT,
                noiLamViec TEXT,
                tinhThanh TEXT,
                quanHuyen TEXT,
                xaPhuong TEXT,
                diaChiChiTiet TEXT,
                ghiChu TEXT,
                ngayTao INTEGER NOT NULL
            )
        """)
        
        // Copy dữ liệu
        db.execSQL("""
            INSERT INTO KhachThue_new 
            SELECT maKhach, hoTen, soDienThoai, email, soCmnd, ngaySinh, ngayCap,
                   noiCap, noiLamViec, tinhThanh, quanHuyen, xaPhuong, diaChiChiTiet,
                   ghiChu, ngayTao
            FROM KhachThue
        """)
        
        // Xóa bảng cũ và đổi tên
        db.execSQL("DROP TABLE KhachThue")
        db.execSQL("ALTER TABLE KhachThue_new RENAME TO KhachThue")
    }
}
```

---

## BƯỚC 4: CẬP NHẬT LOGIC NGHIỆP VỤ

### 4.1. Quy trình thêm khách thuê MỚI

```kotlin
// CreateTenantFragment.kt

fun themKhachThue(khach: KhachThue, maPhong: Long) {
    CoroutineScope(Dispatchers.IO).launch {
        // Kiểm tra phòng có hợp đồng hiệu lực không
        val hopDongHienTai = dbManager.hopDongDao.layHopDongDangThue(maPhong)
        
        if (hopDongHienTai == null) {
            // TRƯỜNG HỢP 1: Phòng trống, chưa có hợp đồng
            withContext(Dispatchers.Main) {
                showDialogTaoHopDongMoi(khach, maPhong)
            }
        } else {
            // TRƯỜNG HỢP 2: Phòng đã có hợp đồng
            val soNguoiDangO = dbManager.hopDongThanhVienDao
                .demNguoiDangO(hopDongHienTai.maHopDong)
            val phong = dbManager.phongDao.layTheoMa(maPhong)
            
            if (soNguoiDangO >= phong.soNguoiToiDa) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "⚠️ Phòng đã đủ người!", Toast.LENGTH_LONG).show()
                }
                return@launch
            }
            
            withContext(Dispatchers.Main) {
                showDialogThemOGhep(khach, hopDongHienTai)
            }
        }
    }
}

fun showDialogTaoHopDongMoi(khach: KhachThue, maPhong: Long) {
    // Dialog tạo hợp đồng mới
    // Khi lưu:
    // 1. Lưu KhachThue
    // 2. Tạo HopDong (maKhach = người đại diện)
    // 3. Tạo HopDongThanhVien (vaiTro = "dai_dien")
    // 4. Cập nhật Phong.trangThai = "da_thue"
}

fun showDialogThemOGhep(khach: KhachThue, hopDong: HopDong) {
    AlertDialog.Builder(requireContext())
        .setTitle("Thêm người ở ghép")
        .setMessage("Phòng đã có hợp đồng. Thêm người này vào hợp đồng hiện tại?")
        .setPositiveButton("Thêm vào") { _, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                // 1. Lưu KhachThue
                val maKhach = dbManager.khachThueDao.them(khach)
                
                // 2. Thêm vào HopDongThanhVien
                val thanhVien = HopDongThanhVien(
                    maHopDong = hopDong.maHopDong,
                    maKhach = maKhach,
                    vaiTro = "thanh_vien",
                    ngayVaoO = System.currentTimeMillis(),
                    trangThai = "dang_o"
                )
                dbManager.hopDongThanhVienDao.them(thanhVien)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✓ Đã thêm người ở ghép", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }
        .setNegativeButton("Hủy", null)
        .show()
}
```

### 4.2. Xem danh sách người trong phòng

```kotlin
fun layDanhSachNguoiTrongPhong(maPhong: Long): List<KhachThueInfo> {
    // 1. Lấy hợp đồng hiện tại
    val hopDong = hopDongDao.layHopDongDangThue(maPhong) ?: return emptyList()
    
    // 2. Lấy danh sách thành viên
    val danhSachThanhVien = hopDongThanhVienDao.layNguoiDangOTheoHopDong(hopDong.maHopDong)
    
    // 3. Lấy thông tin khách thuê
    return danhSachThanhVien.map { tv ->
        val khach = khachThueDao.layTheoMa(tv.maKhach)
        KhachThueInfo(
            khach = khach,
            vaiTro = tv.vaiTro,
            ngayVaoO = tv.ngayVaoO
        )
    }
}
```

---

## BƯỚC 5: CẬP NHẬT UI

### 5.1. Hiển thị danh sách người trong phòng
```
Phòng 101 - Đang thuê (3/4 người)

👤 Nguyễn Văn A (Đại diện)
   Vào: 01/01/2024
   SĐT: 0123456789

👤 Trần Văn B (Thành viên)
   Vào: 15/01/2024
   SĐT: 0987654321

👤 Lê Văn C (Thành viên)
   Vào: 01/02/2024
   SĐT: 0912345678
```

---

## TÓM TẮT THAY ĐỔI

### Database
- ✅ Tạo bảng HopDongThanhVien
- ✅ Sửa KhachThue: Bỏ maPhong, trangThai
- ✅ Sửa DatCoc: Thêm trangThai
- ✅ Migration từ v3 → v4

### Logic
- ✅ 1 phòng chỉ 1 hợp đồng hiệu lực
- ✅ Người vào sau thêm vào HopDongThanhVien
- ✅ Phân biệt đại diện vs thành viên
- ✅ Lưu lịch sử vào/ra

### Lợi ích
- ✅ Mô hình chuẩn, linh hoạt
- ✅ Lưu lịch sử đầy đủ
- ✅ Dễ quản lý, dễ báo cáo
- ✅ Đúng thực tế nghiệp vụ

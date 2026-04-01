package com.example.btl_mobile_son.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "quan_ly_nha_tro.db"
        private const val DATABASE_VERSION = 7 // Tăng từ 6 lên 7 (đổi Double → Long, thêm constraints)

        // Tên bảng
        const val TABLE_NHA_TRO = "nha_tro"
        const val TABLE_PHONG = "phong"
        const val TABLE_KHACH_THUE = "khach_thue"
        const val TABLE_HOP_DONG = "hop_dong"
        const val TABLE_HOP_DONG_THANH_VIEN = "hop_dong_thanh_vien"
        const val TABLE_DICH_VU = "dich_vu"
        const val TABLE_PHONG_DICH_VU = "phong_dich_vu" // VĐ1: Bảng mới
        const val TABLE_DAT_COC = "dat_coc"
        const val TABLE_CHI_SO_DIEN_NUOC = "chi_so_dien_nuoc"
        const val TABLE_HOA_DON = "hoa_don"
        const val TABLE_CHI_TIET_HOA_DON = "chi_tiet_hoa_don"
        const val TABLE_GIAO_DICH = "giao_dich"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Thiết lập encoding UTF-8 để hỗ trợ tiếng Việt
        db.execSQL("PRAGMA encoding = 'UTF-8'")
        
        // Bảng Nhà trọ
        db.execSQL("""
            CREATE TABLE $TABLE_NHA_TRO (
                ma_nha INTEGER PRIMARY KEY AUTOINCREMENT,
                ten_nha TEXT NOT NULL,
                dia_chi TEXT,
                ten_chu_nha TEXT,
                so_dien_thoai TEXT,
                ghi_chu TEXT
            )
        """)

        // Bảng Phòng
        db.execSQL("""
            CREATE TABLE $TABLE_PHONG (
                ma_phong INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_nha INTEGER NOT NULL,
                ten_phong TEXT NOT NULL,
                dien_tich_m2 REAL DEFAULT 0,
                gia_co_ban REAL DEFAULT 0,
                trang_thai TEXT DEFAULT 'trong',
                so_nguoi_toi_da INTEGER DEFAULT 1,
                ghi_chu TEXT,
                FOREIGN KEY (ma_nha) REFERENCES $TABLE_NHA_TRO(ma_nha) ON DELETE CASCADE
            )
        """)

        // Bảng Khách thuê
        db.execSQL("""
            CREATE TABLE $TABLE_KHACH_THUE (
                ma_khach INTEGER PRIMARY KEY AUTOINCREMENT,
                ho_ten TEXT NOT NULL,
                so_dien_thoai TEXT,
                email TEXT,
                so_cmnd TEXT,
                ngay_sinh INTEGER,
                ngay_cap INTEGER,
                noi_cap TEXT,
                noi_lam_viec TEXT,
                tinh_thanh TEXT,
                quan_huyen TEXT,
                xa_phuong TEXT,
                dia_chi_chi_tiet TEXT,
                ma_phong INTEGER,
                trang_thai TEXT DEFAULT 'dang_o',
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE SET NULL
            )
        """)

        // Bảng Hợp đồng
        db.execSQL("""
            CREATE TABLE $TABLE_HOP_DONG (
                ma_hop_dong INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                ma_khach INTEGER NOT NULL,
                ngay_bat_dau INTEGER NOT NULL,
                ngay_ket_thuc INTEGER NOT NULL,
                gia_thue_thang REAL NOT NULL,
                tien_dat_coc REAL DEFAULT 0,
                trang_thai TEXT DEFAULT 'dang_thue',
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE,
                FOREIGN KEY (ma_khach) REFERENCES $TABLE_KHACH_THUE(ma_khach) ON DELETE CASCADE
            )
        """)

        // Bảng Dịch vụ
        db.execSQL("""
            CREATE TABLE $TABLE_DICH_VU (
                ma_dich_vu INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_nha INTEGER NOT NULL,
                ten_dich_vu TEXT NOT NULL,
                don_vi TEXT,
                don_gia REAL DEFAULT 0,
                cach_tinh TEXT DEFAULT 'theo_phong',
                loai_dich_vu TEXT DEFAULT 'khac',
                FOREIGN KEY (ma_nha) REFERENCES $TABLE_NHA_TRO(ma_nha) ON DELETE CASCADE
            )
        """)

        // VĐ1: Bảng Phòng - Dịch vụ (liên kết phòng với dịch vụ)
        db.execSQL("""
            CREATE TABLE $TABLE_PHONG_DICH_VU (
                ma_phong_dich_vu INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                ma_dich_vu INTEGER NOT NULL,
                don_gia_rieng REAL,
                ghi_chu TEXT,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE,
                FOREIGN KEY (ma_dich_vu) REFERENCES $TABLE_DICH_VU(ma_dich_vu) ON DELETE CASCADE,
                UNIQUE(ma_phong, ma_dich_vu)
            )
        """)

        // Bảng Đặt cọc
        db.execSQL("""
            CREATE TABLE $TABLE_DAT_COC (
                ma_dat_coc INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                ten_khach TEXT NOT NULL,
                so_dien_thoai TEXT,
                so_cmnd TEXT,
                email TEXT,
                tien_dat_coc REAL NOT NULL,
                gia_phong REAL DEFAULT 0,
                ngay_du_kien_vao INTEGER DEFAULT 0,
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE
            )
        """)

        // Bảng Chỉ số điện nước
        db.execSQL("""
            CREATE TABLE $TABLE_CHI_SO_DIEN_NUOC (
                ma_chi_so INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                loai TEXT NOT NULL,
                thang INTEGER NOT NULL,
                nam INTEGER NOT NULL,
                chi_so_cu REAL DEFAULT 0,
                chi_so_moi REAL DEFAULT 0,
                so_tieu_thu REAL DEFAULT 0,
                don_gia REAL DEFAULT 0,
                ghi_chu TEXT,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE
            )
        """)

        // Bảng Hóa đơn
        db.execSQL("""
            CREATE TABLE $TABLE_HOA_DON (
                ma_hoa_don INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_hop_dong INTEGER NOT NULL,
                thang INTEGER NOT NULL,
                nam INTEGER NOT NULL,
                tien_phong REAL DEFAULT 0,
                tong_tien_dich_vu REAL DEFAULT 0,
                giam_gia REAL DEFAULT 0,
                tong_tien REAL DEFAULT 0,
                da_thanh_toan INTEGER DEFAULT 0,
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_hop_dong) REFERENCES $TABLE_HOP_DONG(ma_hop_dong) ON DELETE CASCADE
            )
        """)

        // Bảng Chi tiết hóa đơn
        db.execSQL("""
            CREATE TABLE $TABLE_CHI_TIET_HOA_DON (
                ma_chi_tiet INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_hoa_don INTEGER NOT NULL,
                ten_dich_vu TEXT NOT NULL,
                so_luong REAL DEFAULT 0,
                don_gia REAL DEFAULT 0,
                thanh_tien REAL DEFAULT 0,
                FOREIGN KEY (ma_hoa_don) REFERENCES $TABLE_HOA_DON(ma_hoa_don) ON DELETE CASCADE
            )
        """)

        // Bảng Giao dịch
        db.execSQL("""
            CREATE TABLE $TABLE_GIAO_DICH (
                ma_giao_dich INTEGER PRIMARY KEY AUTOINCREMENT,
                loai TEXT NOT NULL,
                ma_phong INTEGER,
                ma_hoa_don INTEGER,
                ma_hop_dong INTEGER,
                ma_dat_coc INTEGER,
                so_tien REAL NOT NULL,
                danh_muc TEXT,
                ngay_giao_dich INTEGER DEFAULT 0,
                noi_dung TEXT,
                ten_nguoi TEXT,
                phuong_thuc_thanh_toan TEXT DEFAULT 'tien_mat',
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_hoa_don) REFERENCES $TABLE_HOA_DON(ma_hoa_don) ON DELETE SET NULL,
                FOREIGN KEY (ma_hop_dong) REFERENCES $TABLE_HOP_DONG(ma_hop_dong) ON DELETE SET NULL,
                FOREIGN KEY (ma_dat_coc) REFERENCES $TABLE_DAT_COC(ma_dat_coc) ON DELETE SET NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN ma_phong INTEGER")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN trang_thai TEXT DEFAULT 'dang_o'")
        }
        if (oldVersion < 3) {
            // Thêm các trường mới cho khách thuê
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN ngay_sinh INTEGER")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN ngay_cap INTEGER")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN noi_cap TEXT")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN noi_lam_viec TEXT")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN tinh_thanh TEXT")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN quan_huyen TEXT")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN xa_phuong TEXT")
            db.execSQL("ALTER TABLE $TABLE_KHACH_THUE ADD COLUMN dia_chi_chi_tiet TEXT")
        }
        if (oldVersion < 4) {
            upgradeToVersion4(db)
        }
        if (oldVersion < 5) {
            upgradeToVersion5(db)
        }
        if (oldVersion < 6) {
            upgradeToVersion6(db)
        }
        if (oldVersion < 7) {
            upgradeToVersion7(db)
        }
    }
    
    private fun upgradeToVersion4(db: SQLiteDatabase) {
        // 1. Tạo bảng HopDongThanhVien
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_HOP_DONG_THANH_VIEN (
                ma_thanh_vien INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_hop_dong INTEGER NOT NULL,
                ma_khach INTEGER NOT NULL,
                vai_tro TEXT NOT NULL DEFAULT 'thanh_vien',
                ngay_vao_o INTEGER NOT NULL,
                ngay_roi_di INTEGER,
                trang_thai TEXT NOT NULL DEFAULT 'dang_o',
                ghi_chu TEXT,
                FOREIGN KEY (ma_hop_dong) REFERENCES $TABLE_HOP_DONG(ma_hop_dong) ON DELETE CASCADE,
                FOREIGN KEY (ma_khach) REFERENCES $TABLE_KHACH_THUE(ma_khach) ON DELETE CASCADE
            )
        """)
        
        // 2. Migrate dữ liệu cũ: Tạo HopDongThanhVien từ HopDong hiện tại
        db.execSQL("""
            INSERT INTO $TABLE_HOP_DONG_THANH_VIEN (ma_hop_dong, ma_khach, vai_tro, ngay_vao_o, trang_thai)
            SELECT ma_hop_dong, ma_khach, 'dai_dien', ngay_bat_dau, 
                   CASE WHEN trang_thai = 'dang_thue' THEN 'dang_o' ELSE 'da_roi' END
            FROM $TABLE_HOP_DONG
        """)
        
        // 3. Thêm cột trangThai vào DatCoc
        db.execSQL("ALTER TABLE $TABLE_DAT_COC ADD COLUMN trang_thai TEXT NOT NULL DEFAULT 'hieu_luc'")
        
        // 4. Xóa cột ma_phong và trang_thai từ KhachThue
        // SQLite không hỗ trợ DROP COLUMN, phải tạo bảng mới
        db.execSQL("""
            CREATE TABLE ${TABLE_KHACH_THUE}_new (
                ma_khach INTEGER PRIMARY KEY AUTOINCREMENT,
                ho_ten TEXT NOT NULL,
                so_dien_thoai TEXT,
                email TEXT,
                so_cmnd TEXT,
                ngay_sinh INTEGER,
                ngay_cap INTEGER,
                noi_cap TEXT,
                noi_lam_viec TEXT,
                tinh_thanh TEXT,
                quan_huyen TEXT,
                xa_phuong TEXT,
                dia_chi_chi_tiet TEXT,
                ghi_chu TEXT,
                ngay_tao INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Copy dữ liệu
        db.execSQL("""
            INSERT INTO ${TABLE_KHACH_THUE}_new 
            SELECT ma_khach, ho_ten, so_dien_thoai, email, so_cmnd, ngay_sinh, ngay_cap,
                   noi_cap, noi_lam_viec, tinh_thanh, quan_huyen, xa_phuong, dia_chi_chi_tiet,
                   ghi_chu, ngay_tao
            FROM $TABLE_KHACH_THUE
        """)
        
        // Xóa bảng cũ và đổi tên
        db.execSQL("DROP TABLE $TABLE_KHACH_THUE")
        db.execSQL("ALTER TABLE ${TABLE_KHACH_THUE}_new RENAME TO $TABLE_KHACH_THUE")
    }
    
    private fun upgradeToVersion5(db: SQLiteDatabase) {
        // 1. Thêm cột cach_tinh vào DichVu
        try {
            db.execSQL("ALTER TABLE $TABLE_DICH_VU ADD COLUMN cach_tinh TEXT DEFAULT 'theo_phong'")
        } catch (e: Exception) {
            // Cột đã tồn tại, bỏ qua
        }
        
        // 2. Thêm cột so_tieu_thu vào ChiSoDienNuoc
        try {
            db.execSQL("ALTER TABLE $TABLE_CHI_SO_DIEN_NUOC ADD COLUMN so_tieu_thu REAL DEFAULT 0")
        } catch (e: Exception) {
            // Cột đã tồn tại, bỏ qua
        }
        
        // 3. Cập nhật so_tieu_thu cho dữ liệu cũ
        db.execSQL("""
            UPDATE $TABLE_CHI_SO_DIEN_NUOC 
            SET so_tieu_thu = chi_so_moi - chi_so_cu
            WHERE so_tieu_thu = 0
        """)
        
        // 4. Thêm các cột liên kết vào GiaoDich
        try {
            db.execSQL("ALTER TABLE $TABLE_GIAO_DICH ADD COLUMN ma_hoa_don INTEGER")
        } catch (e: Exception) {
            // Cột đã tồn tại, bỏ qua
        }
        
        try {
            db.execSQL("ALTER TABLE $TABLE_GIAO_DICH ADD COLUMN ma_hop_dong INTEGER")
        } catch (e: Exception) {
            // Cột đã tồn tại, bỏ qua
        }
        
        try {
            db.execSQL("ALTER TABLE $TABLE_GIAO_DICH ADD COLUMN ma_dat_coc INTEGER")
        } catch (e: Exception) {
            // Cột đã tồn tại, bỏ qua
        }
    }
    
    private fun upgradeToVersion6(db: SQLiteDatabase) {
        // VĐ1: Tạo bảng PhongDichVu để quản lý dịch vụ theo từng phòng
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PHONG_DICH_VU (
                ma_phong_dich_vu INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                ma_dich_vu INTEGER NOT NULL,
                don_gia_rieng REAL,
                ghi_chu TEXT,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE,
                FOREIGN KEY (ma_dich_vu) REFERENCES $TABLE_DICH_VU(ma_dich_vu) ON DELETE CASCADE,
                UNIQUE(ma_phong, ma_dich_vu)
            )
        """)
    }
    
    private fun upgradeToVersion7(db: SQLiteDatabase) {
        // ===== VẤN ĐỀ 1: ĐỔI DOUBLE → LONG CHO TẤT CẢ TRƯỜNG TIỀN =====
        // Vì giá trị đã là số nguyên, chỉ cần đổi kiểu dữ liệu từ REAL → INTEGER
        
        // 1. Phong: giaCoBan
        recreateTablePhong(db)
        
        // 2. HopDong: giaThueThang, tienDatCoc
        recreateTableHopDong(db)
        
        // 3. DatCoc: tienDatCoc, giaPhong
        recreateTableDatCoc(db)
        
        // 4. DichVu: donGia + thêm isActive
        recreateTableDichVu(db)
        
        // 5. ChiSoDienNuoc: chiSoCu, chiSoMoi, soTieuThu, donGia + thêm UNIQUE constraint
        recreateTableChiSoDienNuoc(db)
        
        // 6. HoaDon: tienPhong, tongTienDichVu, giamGia, tongTien + thêm tienDaThanhToan, trangThai + UNIQUE constraint
        recreateTableHoaDon(db)
        
        // 7. ChiTietHoaDon: soLuong, donGia, thanhTien
        recreateTableChiTietHoaDon(db)
        
        // 8. GiaoDich: soTien
        recreateTableGiaoDich(db)
    }
    
    // Helper methods cho migration v7
    private fun recreateTablePhong(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_PHONG RENAME TO ${TABLE_PHONG}_old")
        
        db.execSQL("""
            CREATE TABLE $TABLE_PHONG (
                ma_phong INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_nha INTEGER NOT NULL,
                ten_phong TEXT NOT NULL,
                dien_tich_m2 REAL DEFAULT 0,
                gia_co_ban INTEGER DEFAULT 0,
                trang_thai TEXT DEFAULT 'trong',
                so_nguoi_toi_da INTEGER DEFAULT 1,
                ghi_chu TEXT,
                FOREIGN KEY (ma_nha) REFERENCES $TABLE_NHA_TRO(ma_nha) ON DELETE CASCADE
            )
        """)
        
        db.execSQL("""
            INSERT INTO $TABLE_PHONG 
            SELECT ma_phong, ma_nha, ten_phong, dien_tich_m2, 
                   CAST(gia_co_ban AS INTEGER), trang_thai, so_nguoi_toi_da, ghi_chu
            FROM ${TABLE_PHONG}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_PHONG}_old")
    }
    
    private fun recreateTableHopDong(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_HOP_DONG RENAME TO ${TABLE_HOP_DONG}_old")
        
        db.execSQL("""
            CREATE TABLE $TABLE_HOP_DONG (
                ma_hop_dong INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                ma_khach INTEGER NOT NULL,
                ngay_bat_dau INTEGER NOT NULL,
                ngay_ket_thuc INTEGER NOT NULL,
                gia_thue_thang INTEGER NOT NULL,
                tien_dat_coc INTEGER DEFAULT 0,
                trang_thai TEXT DEFAULT 'dang_thue',
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE,
                FOREIGN KEY (ma_khach) REFERENCES $TABLE_KHACH_THUE(ma_khach) ON DELETE CASCADE
            )
        """)
        
        db.execSQL("""
            INSERT INTO $TABLE_HOP_DONG 
            SELECT ma_hop_dong, ma_phong, ma_khach, ngay_bat_dau, ngay_ket_thuc,
                   CAST(gia_thue_thang AS INTEGER), CAST(tien_dat_coc AS INTEGER), trang_thai
            FROM ${TABLE_HOP_DONG}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_HOP_DONG}_old")
    }
    
    private fun recreateTableDatCoc(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_DAT_COC RENAME TO ${TABLE_DAT_COC}_old")
        
        db.execSQL("""
            CREATE TABLE $TABLE_DAT_COC (
                ma_dat_coc INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                ten_khach TEXT NOT NULL,
                so_dien_thoai TEXT,
                so_cmnd TEXT,
                email TEXT,
                tien_dat_coc INTEGER NOT NULL,
                gia_phong INTEGER DEFAULT 0,
                ngay_du_kien_vao INTEGER DEFAULT 0,
                trang_thai TEXT DEFAULT 'hieu_luc',
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE
            )
        """)
        
        db.execSQL("""
            INSERT INTO $TABLE_DAT_COC 
            SELECT ma_dat_coc, ma_phong, ten_khach, so_dien_thoai, so_cmnd, email,
                   CAST(tien_dat_coc AS INTEGER), CAST(gia_phong AS INTEGER), 
                   ngay_du_kien_vao, trang_thai, ghi_chu, ngay_tao
            FROM ${TABLE_DAT_COC}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_DAT_COC}_old")
    }
    
    private fun recreateTableDichVu(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_DICH_VU RENAME TO ${TABLE_DICH_VU}_old")
        
        db.execSQL("""
            CREATE TABLE $TABLE_DICH_VU (
                ma_dich_vu INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_nha INTEGER NOT NULL,
                ten_dich_vu TEXT NOT NULL,
                don_vi TEXT,
                don_gia INTEGER DEFAULT 0,
                cach_tinh TEXT DEFAULT 'theo_phong',
                loai_dich_vu TEXT DEFAULT 'khac',
                is_active INTEGER DEFAULT 1,
                FOREIGN KEY (ma_nha) REFERENCES $TABLE_NHA_TRO(ma_nha) ON DELETE CASCADE
            )
        """)
        
        db.execSQL("""
            INSERT INTO $TABLE_DICH_VU 
            SELECT ma_dich_vu, ma_nha, ten_dich_vu, don_vi, 
                   CAST(don_gia AS INTEGER), cach_tinh, loai_dich_vu, 1
            FROM ${TABLE_DICH_VU}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_DICH_VU}_old")
    }
    
    private fun recreateTableChiSoDienNuoc(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_CHI_SO_DIEN_NUOC RENAME TO ${TABLE_CHI_SO_DIEN_NUOC}_old")
        
        // VẤN ĐỀ 3: Thêm UNIQUE constraint (maPhong, loai, thang, nam)
        db.execSQL("""
            CREATE TABLE $TABLE_CHI_SO_DIEN_NUOC (
                ma_chi_so INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                loai TEXT NOT NULL,
                thang INTEGER NOT NULL,
                nam INTEGER NOT NULL,
                chi_so_cu INTEGER DEFAULT 0,
                chi_so_moi INTEGER DEFAULT 0,
                so_tieu_thu INTEGER DEFAULT 0,
                don_gia INTEGER DEFAULT 0,
                ghi_chu TEXT,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE,
                UNIQUE(ma_phong, loai, thang, nam)
            )
        """)
        
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_CHI_SO_DIEN_NUOC 
            SELECT ma_chi_so, ma_phong, loai, thang, nam,
                   CAST(chi_so_cu AS INTEGER), CAST(chi_so_moi AS INTEGER), 
                   CAST(so_tieu_thu AS INTEGER), CAST(don_gia AS INTEGER), ghi_chu
            FROM ${TABLE_CHI_SO_DIEN_NUOC}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_CHI_SO_DIEN_NUOC}_old")
    }
    
    private fun recreateTableHoaDon(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_HOA_DON RENAME TO ${TABLE_HOA_DON}_old")
        
        // VẤN ĐỀ 2: Thêm UNIQUE constraint (maHopDong, thang, nam)
        // VẤN ĐỀ 4: Thêm tienDaThanhToan, đổi daThanhToan → trangThai
        db.execSQL("""
            CREATE TABLE $TABLE_HOA_DON (
                ma_hoa_don INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_hop_dong INTEGER NOT NULL,
                thang INTEGER NOT NULL,
                nam INTEGER NOT NULL,
                tien_phong INTEGER DEFAULT 0,
                tong_tien_dich_vu INTEGER DEFAULT 0,
                giam_gia INTEGER DEFAULT 0,
                tong_tien INTEGER DEFAULT 0,
                tien_da_thanh_toan INTEGER DEFAULT 0,
                trang_thai TEXT DEFAULT 'chua_thanh_toan',
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_hop_dong) REFERENCES $TABLE_HOP_DONG(ma_hop_dong) ON DELETE CASCADE,
                UNIQUE(ma_hop_dong, thang, nam)
            )
        """)
        
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_HOA_DON 
            SELECT ma_hoa_don, ma_hop_dong, thang, nam,
                   CAST(tien_phong AS INTEGER), CAST(tong_tien_dich_vu AS INTEGER), 
                   CAST(giam_gia AS INTEGER), CAST(tong_tien AS INTEGER),
                   CASE WHEN da_thanh_toan = 1 THEN CAST(tong_tien AS INTEGER) ELSE 0 END,
                   CASE WHEN da_thanh_toan = 1 THEN 'da_thanh_toan' ELSE 'chua_thanh_toan' END,
                   ghi_chu, ngay_tao
            FROM ${TABLE_HOA_DON}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_HOA_DON}_old")
    }
    
    private fun recreateTableChiTietHoaDon(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_CHI_TIET_HOA_DON RENAME TO ${TABLE_CHI_TIET_HOA_DON}_old")
        
        db.execSQL("""
            CREATE TABLE $TABLE_CHI_TIET_HOA_DON (
                ma_chi_tiet INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_hoa_don INTEGER NOT NULL,
                ten_dich_vu TEXT NOT NULL,
                so_luong REAL DEFAULT 0,
                don_gia INTEGER DEFAULT 0,
                thanh_tien INTEGER DEFAULT 0,
                FOREIGN KEY (ma_hoa_don) REFERENCES $TABLE_HOA_DON(ma_hoa_don) ON DELETE CASCADE
            )
        """)
        
        db.execSQL("""
            INSERT INTO $TABLE_CHI_TIET_HOA_DON 
            SELECT ma_chi_tiet, ma_hoa_don, ten_dich_vu, so_luong,
                   CAST(don_gia AS INTEGER), CAST(thanh_tien AS INTEGER)
            FROM ${TABLE_CHI_TIET_HOA_DON}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_CHI_TIET_HOA_DON}_old")
    }
    
    private fun recreateTableGiaoDich(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_GIAO_DICH RENAME TO ${TABLE_GIAO_DICH}_old")
        
        db.execSQL("""
            CREATE TABLE $TABLE_GIAO_DICH (
                ma_giao_dich INTEGER PRIMARY KEY AUTOINCREMENT,
                loai TEXT NOT NULL,
                ma_phong INTEGER,
                ma_hoa_don INTEGER,
                ma_hop_dong INTEGER,
                ma_dat_coc INTEGER,
                so_tien INTEGER NOT NULL,
                danh_muc TEXT,
                ngay_giao_dich INTEGER DEFAULT 0,
                noi_dung TEXT,
                ten_nguoi TEXT,
                phuong_thuc_thanh_toan TEXT DEFAULT 'tien_mat',
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                FOREIGN KEY (ma_hoa_don) REFERENCES $TABLE_HOA_DON(ma_hoa_don) ON DELETE SET NULL,
                FOREIGN KEY (ma_hop_dong) REFERENCES $TABLE_HOP_DONG(ma_hop_dong) ON DELETE SET NULL,
                FOREIGN KEY (ma_dat_coc) REFERENCES $TABLE_DAT_COC(ma_dat_coc) ON DELETE SET NULL
            )
        """)
        
        db.execSQL("""
            INSERT INTO $TABLE_GIAO_DICH 
            SELECT ma_giao_dich, loai, ma_phong, ma_hoa_don, ma_hop_dong, ma_dat_coc,
                   CAST(so_tien AS INTEGER), danh_muc, ngay_giao_dich, noi_dung, 
                   ten_nguoi, phuong_thuc_thanh_toan, ghi_chu, ngay_tao
            FROM ${TABLE_GIAO_DICH}_old
        """)
        
        db.execSQL("DROP TABLE ${TABLE_GIAO_DICH}_old")
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Bật foreign key constraints
        db.setForeignKeyConstraintsEnabled(true)
        // Thiết lập encoding UTF-8 cho tiếng Việt
        db.execSQL("PRAGMA encoding = 'UTF-8'")
    }
    
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Đảm bảo encoding UTF-8 mỗi khi mở database
        db.execSQL("PRAGMA encoding = 'UTF-8'")
    }
}

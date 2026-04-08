package com.example.btl_mobile_son.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "quan_ly_nha_tro.db"
        private const val DATABASE_VERSION = 10  // Tăng lên 10 để thêm username/password cho khách thuê

        // Tên bảng
        const val TABLE_NHA_TRO = "nha_tro"
        const val TABLE_PHONG = "phong"
        const val TABLE_KHACH_THUE = "khach_thue"
        const val TABLE_HOP_DONG = "hop_dong"
        const val TABLE_HOP_DONG_THANH_VIEN = "hop_dong_thanh_vien"
        const val TABLE_DICH_VU = "dich_vu"
        const val TABLE_PHONG_DICH_VU = "phong_dich_vu"
        const val TABLE_DAT_COC = "dat_coc"
        const val TABLE_CHI_SO_DIEN_NUOC = "chi_so_dien_nuoc"
        const val TABLE_HOA_DON = "hoa_don"
        const val TABLE_CHI_TIET_HOA_DON = "chi_tiet_hoa_don"
        const val TABLE_GIAO_DICH = "giao_dich"
        const val TABLE_SU_CO = "su_co"
        const val TABLE_NGUOI_DUNG = "nguoi_dung"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Thiết lập encoding UTF-8 để hỗ trợ tiếng Việt
        db.execSQL("PRAGMA encoding = 'UTF-8'")
        
        android.util.Log.d("DatabaseHelper", "Creating database tables...")
        
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

        // Bảng Khách thuê (đã bỏ ma_phong và trang_thai - dùng HopDongThanhVien thay thế)
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
                ghi_chu TEXT,
                ngay_tao INTEGER DEFAULT 0,
                ten_dang_nhap TEXT UNIQUE,
                mat_khau TEXT
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

        // Bảng Hợp đồng thành viên (hỗ trợ nhiều người ở ghép)
        db.execSQL("""
            CREATE TABLE $TABLE_HOP_DONG_THANH_VIEN (
                maThanhVien INTEGER PRIMARY KEY AUTOINCREMENT,
                maHopDong INTEGER NOT NULL,
                maKhach INTEGER NOT NULL,
                vaiTro TEXT DEFAULT 'thanh_vien',
                ngayVaoO INTEGER DEFAULT 0,
                ngayRoiDi INTEGER,
                trangThai TEXT DEFAULT 'dang_o',
                ghiChu TEXT,
                FOREIGN KEY (maHopDong) REFERENCES $TABLE_HOP_DONG(ma_hop_dong) ON DELETE CASCADE,
                FOREIGN KEY (maKhach) REFERENCES $TABLE_KHACH_THUE(ma_khach) ON DELETE CASCADE
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
                is_active INTEGER DEFAULT 1,
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
                trang_thai TEXT DEFAULT 'hieu_luc',
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
                tien_da_thanh_toan REAL DEFAULT 0,
                trang_thai TEXT DEFAULT 'chua_thanh_toan',
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

        // Bảng Sự cố
        db.execSQL("""
            CREATE TABLE $TABLE_SU_CO (
                ma_su_co INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_phong INTEGER NOT NULL,
                loai_su_co TEXT NOT NULL,
                mo_ta TEXT,
                trang_thai TEXT DEFAULT 'chua_xu_ly',
                nguoi_bao_cao TEXT,
                nguoi_xu_ly TEXT,
                ngay_bao_cao INTEGER DEFAULT 0,
                ngay_xu_ly INTEGER,
                chi_phi REAL DEFAULT 0,
                ghi_chu TEXT,
                FOREIGN KEY (ma_phong) REFERENCES $TABLE_PHONG(ma_phong) ON DELETE CASCADE
            )
        """)

        // Bảng Người dùng
        db.execSQL("""
            CREATE TABLE $TABLE_NGUOI_DUNG (
                ma_nguoi_dung INTEGER PRIMARY KEY AUTOINCREMENT,
                ten_dang_nhap TEXT NOT NULL UNIQUE,
                mat_khau TEXT NOT NULL,
                ho_ten TEXT NOT NULL,
                vai_tro TEXT DEFAULT 'nhan_vien',
                so_dien_thoai TEXT,
                email TEXT,
                trang_thai TEXT DEFAULT 'hoat_dong',
                ngay_tao INTEGER DEFAULT 0
            )
        """)

        // Tạo tài khoản admin mặc định (mật khẩu: admin123)
        db.execSQL("""
            INSERT INTO $TABLE_NGUOI_DUNG (ten_dang_nhap, mat_khau, ho_ten, vai_tro, ngay_tao)
            VALUES ('admin', 'admin123', 'Quản trị viên', 'admin', ${System.currentTimeMillis()})
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Nếu upgrade từ version cũ, đơn giản là drop tất cả và tạo lại
        // Điều này đảm bảo không có lỗi migration phức tạp
        android.util.Log.d("DatabaseHelper", "Upgrading database from $oldVersion to $newVersion")
        
        // Drop tất cả các bảng theo thứ tự ngược lại (để tránh foreign key constraint)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NGUOI_DUNG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SU_CO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GIAO_DICH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHI_TIET_HOA_DON")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOA_DON")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHI_SO_DIEN_NUOC")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAT_COC")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PHONG_DICH_VU")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DICH_VU")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOP_DONG_THANH_VIEN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOP_DONG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_KHACH_THUE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PHONG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NHA_TRO")
        
        // Tạo lại tất cả
        onCreate(db)
        android.util.Log.d("DatabaseHelper", "Database upgrade completed")
    }
    
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Xử lý downgrade (từ version cao xuống thấp)
        android.util.Log.d("DatabaseHelper", "Downgrading database from $oldVersion to $newVersion")
        // Drop tất cả và tạo lại
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NGUOI_DUNG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SU_CO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GIAO_DICH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHI_TIET_HOA_DON")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOA_DON")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHI_SO_DIEN_NUOC")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAT_COC")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PHONG_DICH_VU")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DICH_VU")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOP_DONG_THANH_VIEN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOP_DONG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_KHACH_THUE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PHONG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NHA_TRO")
        
        // Tạo lại tất cả
        onCreate(db)
        android.util.Log.d("DatabaseHelper", "Database downgrade completed")
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

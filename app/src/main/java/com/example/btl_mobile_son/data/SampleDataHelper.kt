package com.example.btl_mobile_son.data

import android.content.Context
import android.content.SharedPreferences
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class SampleDataHelper(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val KEY_SAMPLE_DATA_LOADED = "sample_data_loaded"

    suspend fun loadSampleDataIfNeeded() {
        if (prefs.getBoolean(KEY_SAMPLE_DATA_LOADED, false)) {
            return // Đã load rồi
        }

        withContext(Dispatchers.IO) {
            val dbManager = DatabaseManager.getInstance(context)
            
            // 1. Tạo nhà trọ mẫu
            val maNha1 = dbManager.nhaTroDao.them(NhaTro(
                tenNha = "Nhà trọ Việt Tín",
                diaChi = "Quận 1, TP. Hồ Chí Minh",
                tenChuNha = "Nguyễn Văn A",
                soDienThoai = "0901234567",
                ghiChu = "Nhà trọ gần trường đại học"
            ))

            val maNha2 = dbManager.nhaTroDao.them(NhaTro(
                tenNha = "Nhà trọ Hòa Bình",
                diaChi = "Quận Bình Thạnh, TP. Hồ Chí Minh",
                tenChuNha = "Trần Thị B",
                soDienThoai = "0912345678",
                ghiChu = "Gần chợ và bệnh viện"
            ))

            val maNha3 = dbManager.nhaTroDao.them(NhaTro(
                tenNha = "Nhà trọ Phú Quý",
                diaChi = "Quận Gò Vấp, TP. Hồ Chí Minh",
                tenChuNha = "Lê Văn C",
                soDienThoai = "0923456789"
            ))

            // 2. Tạo phòng mẫu
            val phong101 = dbManager.phongDao.them(Phong(
                maNha = maNha1,
                tenPhong = "Phòng 101",
                dienTichM2 = 20f,
                giaCoBan = 2500000.0,
                trangThai = "da_thue",
                soNguoiToiDa = 2,
                ghiChu = "Phòng có ban công"
            ))

            val phong102 = dbManager.phongDao.them(Phong(
                maNha = maNha1,
                tenPhong = "Phòng 102",
                dienTichM2 = 18f,
                giaCoBan = 2200000.0,
                trangThai = "trong",
                soNguoiToiDa = 2
            ))

            val phong103 = dbManager.phongDao.them(Phong(
                maNha = maNha1,
                tenPhong = "Phòng 103",
                dienTichM2 = 25f,
                giaCoBan = 3000000.0,
                trangThai = "da_thue",
                soNguoiToiDa = 3,
                ghiChu = "Phòng rộng, có gác lửng"
            ))

            val phong201 = dbManager.phongDao.them(Phong(
                maNha = maNha2,
                tenPhong = "Phòng 201",
                dienTichM2 = 22f,
                giaCoBan = 2800000.0,
                trangThai = "da_thue",
                soNguoiToiDa = 2
            ))

            val phong202 = dbManager.phongDao.them(Phong(
                maNha = maNha2,
                tenPhong = "Phòng 202",
                dienTichM2 = 20f,
                giaCoBan = 2600000.0,
                trangThai = "trong",
                soNguoiToiDa = 2
            ))

            dbManager.phongDao.them(Phong(
                maNha = maNha3,
                tenPhong = "Phòng A1",
                dienTichM2 = 15f,
                giaCoBan = 1800000.0,
                trangThai = "trong",
                soNguoiToiDa = 1
            ))

            // 3. Tạo khách thuê mẫu
            val khach1 = dbManager.khachThueDao.them(KhachThue(
                hoTen = "Nguyễn Văn Minh",
                soDienThoai = "0987654321",
                email = "minh.nv@gmail.com",
                soCmnd = "079123456789",
                ghiChu = "Sinh viên năm 3"
            ))

            val khach2 = dbManager.khachThueDao.them(KhachThue(
                hoTen = "Trần Thị Hoa",
                soDienThoai = "0976543210",
                email = "hoa.tt@gmail.com",
                soCmnd = "079987654321",
                ghiChu = "Nhân viên văn phòng"
            ))

            val khach3 = dbManager.khachThueDao.them(KhachThue(
                hoTen = "Lê Hoàng Nam",
                soDienThoai = "0965432109",
                email = "nam.lh@gmail.com",
                soCmnd = "079111222333"
            ))

            // 4. Tạo hợp đồng mẫu
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -2) // 2 tháng trước
            val ngayBatDau1 = cal.timeInMillis
            cal.add(Calendar.YEAR, 1) // Hợp đồng 1 năm
            val ngayKetThuc1 = cal.timeInMillis

            val hopDong1 = dbManager.hopDongDao.them(HopDong(
                maPhong = phong101,
                maKhach = khach1,
                ngayBatDau = ngayBatDau1,
                ngayKetThuc = ngayKetThuc1,
                giaThueThang = 2500000.0,
                tienDatCoc = 5000000.0,
                trangThai = "dang_thue"
            ))

            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MONTH, -1)
            val ngayBatDau2 = cal.timeInMillis
            cal.add(Calendar.YEAR, 1)
            val ngayKetThuc2 = cal.timeInMillis

            val hopDong2 = dbManager.hopDongDao.them(HopDong(
                maPhong = phong103,
                maKhach = khach2,
                ngayBatDau = ngayBatDau2,
                ngayKetThuc = ngayKetThuc2,
                giaThueThang = 3000000.0,
                tienDatCoc = 6000000.0,
                trangThai = "dang_thue"
            ))

            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_MONTH, -15)
            val ngayBatDau3 = cal.timeInMillis
            cal.add(Calendar.YEAR, 1)
            val ngayKetThuc3 = cal.timeInMillis

            dbManager.hopDongDao.them(HopDong(
                maPhong = phong201,
                maKhach = khach3,
                ngayBatDau = ngayBatDau3,
                ngayKetThuc = ngayKetThuc3,
                giaThueThang = 2800000.0,
                tienDatCoc = 5600000.0,
                trangThai = "dang_thue"
            ))

            // 5. Tạo dịch vụ mẫu
            dbManager.dichVuDao.them(DichVu(
                maNha = maNha1,
                tenDichVu = "Điện",
                donVi = "kWh",
                donGia = 3500.0,
                loaiDichVu = "dien"
            ))

            dbManager.dichVuDao.them(DichVu(
                maNha = maNha1,
                tenDichVu = "Nước",
                donVi = "m³",
                donGia = 20000.0,
                loaiDichVu = "nuoc"
            ))

            dbManager.dichVuDao.them(DichVu(
                maNha = maNha1,
                tenDichVu = "Internet",
                donVi = "tháng",
                donGia = 100000.0,
                loaiDichVu = "khac"
            ))

            dbManager.dichVuDao.them(DichVu(
                maNha = maNha2,
                tenDichVu = "Điện",
                donVi = "kWh",
                donGia = 3800.0,
                loaiDichVu = "dien"
            ))

            dbManager.dichVuDao.them(DichVu(
                maNha = maNha2,
                tenDichVu = "Nước",
                donVi = "m³",
                donGia = 25000.0,
                loaiDichVu = "nuoc"
            ))

            // 6. Tạo chỉ số điện nước mẫu
            cal.timeInMillis = System.currentTimeMillis()
            val thangHienTai = cal.get(Calendar.MONTH) + 1
            val namHienTai = cal.get(Calendar.YEAR)

            dbManager.chiSoDienNuocDao.them(ChiSoDienNuoc(
                maPhong = phong101,
                loai = "dien",
                thang = thangHienTai,
                nam = namHienTai,
                chiSoCu = 100.0,
                chiSoMoi = 150.0,
                donGia = 3500.0,
                ghiChu = "Chỉ số tháng ${thangHienTai}"
            ))

            dbManager.chiSoDienNuocDao.them(ChiSoDienNuoc(
                maPhong = phong101,
                loai = "nuoc",
                thang = thangHienTai,
                nam = namHienTai,
                chiSoCu = 10.0,
                chiSoMoi = 15.0,
                donGia = 20000.0
            ))

            // 7. Tạo hóa đơn mẫu
            val hoaDon1 = dbManager.hoaDonDao.them(HoaDon(
                maHopDong = hopDong1,
                thang = thangHienTai,
                nam = namHienTai,
                tienPhong = 2500000.0,
                tongTienDichVu = 275000.0, // 50kWh * 3500 + 5m³ * 20000
                giamGia = 0.0,
                tongTien = 2775000.0,
                daThanhToan = false,
                ghiChu = "Hóa đơn tháng ${thangHienTai}/${namHienTai}"
            ))

            // Tháng trước đã thanh toán
            dbManager.hoaDonDao.them(HoaDon(
                maHopDong = hopDong2,
                thang = if (thangHienTai > 1) thangHienTai - 1 else 12,
                nam = if (thangHienTai > 1) namHienTai else namHienTai - 1,
                tienPhong = 3000000.0,
                tongTienDichVu = 300000.0,
                giamGia = 0.0,
                tongTien = 3300000.0,
                daThanhToan = true
            ))

            // 8. Tạo giao dịch thu chi mẫu
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_MONTH, -5)
            
            dbManager.giaoDichDao.them(GiaoDich(
                loai = "thu",
                maPhong = phong101,
                soTien = 2775000.0,
                danhMuc = "Tiền thuê phòng",
                ngayGiaoDich = cal.timeInMillis,
                noiDung = "Thu tiền phòng 101 tháng ${thangHienTai}",
                tenNguoi = "Nguyễn Văn Minh",
                phuongThucThanhToan = "chuyen_khoan"
            ))

            cal.add(Calendar.DAY_OF_MONTH, -10)
            dbManager.giaoDichDao.them(GiaoDich(
                loai = "chi",
                soTien = 500000.0,
                danhMuc = "Sửa chữa",
                ngayGiaoDich = cal.timeInMillis,
                noiDung = "Sửa ống nước phòng 102",
                tenNguoi = "Thợ sửa ống nước",
                phuongThucThanhToan = "tien_mat"
            ))

            cal.add(Calendar.DAY_OF_MONTH, -3)
            dbManager.giaoDichDao.them(GiaoDich(
                loai = "thu",
                maPhong = phong103,
                soTien = 3300000.0,
                danhMuc = "Tiền thuê phòng",
                ngayGiaoDich = cal.timeInMillis,
                noiDung = "Thu tiền phòng 103",
                tenNguoi = "Trần Thị Hoa",
                phuongThucThanhToan = "tien_mat"
            ))

            // 9. Tạo đặt cọc mẫu
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_MONTH, 5) // Dự kiến vào 5 ngày nữa
            
            dbManager.datCocDao.them(DatCoc(
                maPhong = phong102,
                tenKhach = "Phạm Văn Đức",
                soDienThoai = "0954321098",
                soCmnd = "079444555666",
                email = "duc.pv@gmail.com",
                tienDatCoc = 2000000.0,
                giaPhong = 2200000.0,
                ngayDuKienVao = cal.timeInMillis,
                ghiChu = "Khách đặt cọc giữ phòng"
            ))

            // Đánh dấu đã load sample data
            prefs.edit().putBoolean(KEY_SAMPLE_DATA_LOADED, true).apply()
        }
    }

    fun resetSampleData() {
        prefs.edit().putBoolean(KEY_SAMPLE_DATA_LOADED, false).apply()
    }
}

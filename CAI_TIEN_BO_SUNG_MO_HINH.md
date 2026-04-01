# CẢI TIẾN BỔ SUNG MÔ HÌNH DỮ LIỆU - PHẦN 2

## 📋 DANH SÁCH VẤN ĐỀ CẦN SỬA

### ✅ ĐÃ HOÀN THÀNH (Phần 1)
1. ✅ Tạo bảng HopDongThanhVien
2. ✅ Bỏ maPhong và trangThai khỏi KhachThue
3. ✅ Thêm trangThai vào DatCoc
4. ✅ Migration database v3 → v4

### 🔄 CẦN LÀM TIẾP (Phần 2)
5. ⚠️ Xem lại KhachThue.maPhong (đã bỏ rồi - OK)
6. 🔴 Dịch vụ cần quan hệ với phòng → Tạo PhongDichVu
7. 🔴 Logic dịch vụ bổ sung: cachTinh, isActive
8. 🔴 Hóa đơn: Unique constraint, trangThai thay Boolean
9. 🔴 Chỉ số điện nước: Unique constraint
10. 🔴 Giao dịch: Liên kết nguồn (maHoaDon, maHopDong, maDatCoc)
11. 🔴 Đổi tiền từ Double → Long
12. 🔴 Đồng bộ trạng thái phòng tự động

---

## VẤN ĐỀ 6: DỊCH VỤ CẦN QUAN HỆ VỚI PHÒNG

### Hiện tại (THIẾU)
```kotlin
// DichVu chỉ là danh mục theo nhà
data class DichVu(
    val maDichVu: Long,
    val maNha: Long,
    val tenDichVu: String,
    val donGia: Double,
    ...
)
```

**Vấn đề:**
- ❌ Không biết phòng nào dùng dịch vụ nào
- ❌ Không biết giá áp dụng cho từng phòng
- ❌ Không biết thời gian áp dụng
- ❌ Không quản lý được khi phòng ngừng dùng dịch vụ

### Đề xuất (ĐÚNG)

**Tạo bảng PhongDichVu:**
```kotlin
data class PhongDichVu(
    val maPhongDichVu: Long = 0,
    val maPhong: Long,
    val maDichVu: Long,
    val donGiaApDung: Long,        // Giá áp dụng cho phòng này
    val soLuong: Int = 1,          // Số lượng (VD: 2 xe)
    val trangThai: String = "dang_ap_dung", // "dang_ap_dung" | "ngung_ap_dung"
    val ngayBatDau: Long = System.currentTimeMillis(),
    val ngayKetThuc: Long? = null,
    val ghiChu: String = ""
)
```

**Ý nghĩa:**
- `DichVu`: Danh mục dịch vụ của nhà (Internet, Rác, Gửi xe...)
- `PhongDichVu`: Dịch vụ thực tế áp dụng cho từng phòng

**Ví dụ:**
```
DichVu:
- Internet (100,000đ)
- Gửi xe (50,000đ)

PhongDichVu:
- Phòng 101: Internet 100,000đ (đang dùng)
- Phòng 101: Gửi xe 50,000đ x 2 xe (đang dùng)
- Phòng 102: Internet 100,000đ (đang dùng)
- Phòng 103: Internet 150,000đ (đang dùng - giá khác)
```

---

## VẤN ĐỀ 7: LOGIC DỊCH VỤ BỔ SUNG

### Hiện tại (THIẾU)
```kotlin
data class DichVu(
    val maDichVu: Long,
    val tenDichVu: String,
    val donVi: String,
    val donGia: Double,
    val loaiDichVu: String
)
```

### Đề xuất (BỔ SUNG)
```kotlin
data class DichVu(
    val maDichVu: Long,
    val maNha: Long,
    val tenDichVu: String,
    val donVi: String,
    val donGia: Long,              // Đổi Double → Long
    val loaiDichVu: String,
    val cachTinh: String = "theo_phong", // THÊM MỚI
    val isActive: Boolean = true,  // THÊM MỚI
    val ghiChu: String = ""
)
```

**Các cách tính:**
```
"theo_phong"  - Tính theo phòng (VD: Internet)
"theo_nguoi"  - Tính theo số người (VD: Vệ sinh)
"mot_lan"     - Tính 1 lần (VD: Sửa chữa)
"theo_thang"  - Tính theo tháng (VD: Quản lý)
"theo_so"     - Tính theo số (VD: Điện, nước)
```

**Lợi ích:**
- Khi tính hóa đơn, biết cách tính
- VD: Vệ sinh theo người → Lấy số người trong phòng x đơn giá
- isActive: Dịch vụ còn hoạt động không (để ẩn/hiện)

---

## VẤN ĐỀ 8: HÓA ĐƠN CẦN RULE CHẶT HƠN

### Hiện tại (YẾU)
```kotlin
data class HoaDon(
    val maHoaDon: Long,
    val maHopDong: Long,
    val thang: Int,
    val nam: Int,
    ...
    val daThanhToan: Boolean = false
)
```

**Vấn đề:**
- ❌ Có thể tạo nhiều hóa đơn cho cùng hợp đồng/tháng/năm
- ❌ Boolean không thể hiện thanh toán một phần

### Đề xuất (CHẶT HƠN)

**1. Thêm Unique Constraint:**
```sql
UNIQUE(maHopDong, thang, nam)
```

**2. Đổi Boolean → trangThai:**
```kotlin
data class HoaDon(
    val maHoaDon: Long,
    val maHopDong: Long,
    val thang: Int,
    val nam: Int,
    val tienPhong: Long,
    val tongTienDichVu: Long,
    val giamGia: Long,
    val tongTien: Long,
    val tienDaThanhToan: Long = 0,     // THÊM MỚI
    val trangThai: String = "chua_thanh_toan", // THAY Boolean
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

**Các trạng thái:**
```
"chua_thanh_toan"      - Chưa thanh toán
"thanh_toan_mot_phan"  - Đã thanh toán một phần
"da_thanh_toan"        - Đã thanh toán đủ
"qua_han"              - Quá hạn chưa thanh toán
```

**Logic:**
```kotlin
fun capNhatTrangThaiHoaDon(hoaDon: HoaDon): String {
    return when {
        hoaDon.tienDaThanhToan == 0L -> "chua_thanh_toan"
        hoaDon.tienDaThanhToan >= hoaDon.tongTien -> "da_thanh_toan"
        else -> "thanh_toan_mot_phan"
    }
}
```

---

## VẤN ĐỀ 9: CHỈ SỐ ĐIỆN NƯỚC CẦN RÀNG BUỘC

### Hiện tại (YẾU)
```kotlin
data class ChiSoDienNuoc(
    val maChiSo: Long,
    val maPhong: Long,
    val loai: String,
    val thang: Int,
    val nam: Int,
    val chiSoCu: Double,
    val chiSoMoi: Double,
    ...
)
```

**Vấn đề:**
- ❌ Có thể nhập nhiều chỉ số cho cùng phòng/loại/tháng
- ❌ Không kiểm tra chiSoMoi >= chiSoCu ở DB level

### Đề xuất (CHẶT HƠN)

**1. Thêm Unique Constraint:**
```sql
UNIQUE(maPhong, loai, thang, nam)
```

**2. Thêm Check Constraint:**
```sql
CHECK(chiSoMoi >= chiSoCu)
```

**3. Đổi Double → Long:**
```kotlin
data class ChiSoDienNuoc(
    val maChiSo: Long,
    val maPhong: Long,
    val loai: String,
    val thang: Int,
    val nam: Int,
    val chiSoCu: Long,      // Đổi Double → Long
    val chiSoMoi: Long,     // Đổi Double → Long
    val donGia: Long,       // Đổi Double → Long
    val ghiChu: String = ""
)
```

---

## VẤN ĐỀ 10: GIAO DỊCH CẦN LIÊN KẾT NGUỒN

### Hiện tại (ĐỘC LẬP)
```kotlin
data class GiaoDich(
    val maGiaoDich: Long,
    val loai: String,
    val maPhong: Long?,
    val soTien: Double,
    ...
)
```

**Vấn đề:**
- ❌ Không biết giao dịch từ đâu
- ❌ Không liên kết với hóa đơn, đặt cọc, hợp đồng

### Đề xuất (LIÊN KẾT)
```kotlin
data class GiaoDich(
    val maGiaoDich: Long,
    val loai: String,              // "thu" | "chi"
    val maPhong: Long? = null,
    val maHoaDon: Long? = null,    // THÊM MỚI
    val maHopDong: Long? = null,   // THÊM MỚI
    val maDatCoc: Long? = null,    // THÊM MỚI
    val soTien: Long,              // Đổi Double → Long
    val danhMuc: String = "",
    val ngayGiaoDich: Long = 0,
    val noiDung: String = "",
    val tenNguoi: String = "",
    val phuongThucThanhToan: String = "tien_mat",
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

**Lợi ích:**
- Biết giao dịch thu từ hóa đơn nào
- Biết giao dịch thu từ đặt cọc nào
- Biết giao dịch chi cho hợp đồng/phòng nào
- Dễ đối soát, báo cáo

---

## VẤN ĐỀ 11: ĐỔI TIỀN TỪ DOUBLE → LONG

### Tại sao?
- ❌ Double có lỗi làm tròn: 0.1 + 0.2 = 0.30000000000000004
- ❌ Không chính xác cho tiền tệ
- ✅ Long chính xác tuyệt đối

### Các trường cần đổi:

**Phong:**
```kotlin
val giaCoBan: Long  // Thay Double
```

**HopDong:**
```kotlin
val giaThueThang: Long
val tienDatCoc: Long
```

**DatCoc:**
```kotlin
val tienDatCoc: Long
val giaPhong: Long
```

**DichVu:**
```kotlin
val donGia: Long
```

**ChiSoDienNuoc:**
```kotlin
val chiSoCu: Long
val chiSoMoi: Long
val donGia: Long
```

**HoaDon:**
```kotlin
val tienPhong: Long
val tongTienDichVu: Long
val giamGia: Long
val tongTien: Long
val tienDaThanhToan: Long
```

**GiaoDich:**
```kotlin
val soTien: Long
```

---

## VẤN ĐỀ 12: ĐỒNG BỘ TRẠNG THÁI PHÒNG TỰ ĐỘNG

### Hiện tại (THỦ CÔNG)
```kotlin
// Phải tự cập nhật tay
dbManager.phongDao.capNhat(phong.copy(trangThai = "da_thue"))
```

**Vấn đề:**
- ❌ Dễ quên cập nhật
- ❌ Dễ sai logic
- ❌ Không nhất quán

### Đề xuất (TỰ ĐỘNG)

**Tạo PhongService:**
```kotlin
class PhongService(private val dbManager: DatabaseManager) {
    
    fun capNhatTrangThaiPhong(maPhong: Long) {
        val phong = dbManager.phongDao.layTheoMa(maPhong) ?: return
        
        // Kiểm tra đặt cọc hiệu lực
        val datCocHieuLuc = dbManager.datCocDao.layTheoPhong(maPhong)
            .any { it.trangThai == "hieu_luc" }
        
        if (datCocHieuLuc) {
            dbManager.phongDao.capNhat(phong.copy(trangThai = "dat_coc"))
            return
        }
        
        // Kiểm tra hợp đồng đang thuê
        val hopDongDangThue = dbManager.hopDongDao.layHopDongDangThue(maPhong)
        
        if (hopDongDangThue != null) {
            dbManager.phongDao.capNhat(phong.copy(trangThai = "da_thue"))
            return
        }
        
        // Không có gì → Trống
        dbManager.phongDao.capNhat(phong.copy(trangThai = "trong"))
    }
}
```

**Gọi tự động:**
```kotlin
// Sau khi tạo đặt cọc
phongService.capNhatTrangThaiPhong(maPhong)

// Sau khi hủy đặt cọc
phongService.capNhatTrangThaiPhong(maPhong)

// Sau khi tạo hợp đồng
phongService.capNhatTrangThaiPhong(maPhong)

// Sau khi kết thúc hợp đồng
phongService.capNhatTrangThaiPhong(maPhong)
```

---

## TÓM TẮT ƯU TIÊN

### MỨC ĐỘ CAO (Phải làm)
1. ✅ Đổi tiền Double → Long (quan trọng nhất)
2. ✅ Unique constraints (HoaDon, ChiSoDienNuoc)
3. ✅ Đồng bộ trạng thái phòng tự động

### MỨC ĐỘ TRUNG BÌNH
4. ✅ HoaDon: trangThai thay Boolean
5. ✅ GiaoDich: Liên kết nguồn
6. ✅ DichVu: Thêm cachTinh, isActive

### MỨC ĐỘ THẤP (Nếu có thời gian)
7. ⚠️ PhongDichVu (bảng trung gian)

---

## KẾ HOẠCH THỰC HIỆN

### Phase 1: Đổi kiểu dữ liệu (v4 → v5)
- Đổi tất cả Double → Long
- Migration data: nhân 1 (vì đã là số nguyên)

### Phase 2: Thêm constraints (v5 → v6)
- Unique: (maHopDong, thang, nam) cho HoaDon
- Unique: (maPhong, loai, thang, nam) cho ChiSoDienNuoc

### Phase 3: Bổ sung trường (v6 → v7)
- HoaDon: trangThai, tienDaThanhToan
- GiaoDich: maHoaDon, maHopDong, maDatCoc
- DichVu: cachTinh, isActive

### Phase 4: Tạo service
- PhongService: Đồng bộ trạng thái tự động

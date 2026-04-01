# DANH SÁCH VẤN ĐỀ CẦN SỬA - THEO THỨ TỰ ƯU TIÊN

## 📊 TỔNG QUAN

**Tổng số vấn đề:** 8 vấn đề
**Đã sửa:** 0 vấn đề
**Còn lại:** 8 vấn đề

---

## 🔴 MỨC ĐỘ NGHIÊM TRỌNG - PHẢI SỬA NGAY

### VẤN ĐỀ 1: Dùng Double cho tiền tệ ⭐⭐⭐⭐⭐

**Mức độ:** NGHIÊM TRỌNG NHẤT
**Ảnh hưởng:** Tất cả tính toán tiền bị sai
**Thời gian:** 2-3 giờ

**Mô tả:**
```kotlin
// ❌ SAI - Hiện tại
val donGia: Double = 100000.0
val tienPhong: Double = 1000000.0

// Lỗi làm tròn
0.1 + 0.2 = 0.30000000000000004
100000.0 * 3 = 299999.9999999999
```

**Giải pháp:**
```kotlin
// ✅ ĐÚNG - Đổi sang Long
val donGia: Long = 100000  // 100,000đ
val tienPhong: Long = 1000000  // 1,000,000đ

// Chính xác tuyệt đối
100000 * 3 = 300000
```

**Các file cần sửa:**
1. `Phong.kt` - giaCoBan
2. `HopDong.kt` - giaThueThang, tienDatCoc
3. `DatCoc.kt` - tienDatCoc, giaPhong
4. `DichVu.kt` - donGia
5. `ChiSoDienNuoc.kt` - chiSoCu, chiSoMoi, donGia
6. `HoaDon.kt` - tienPhong, tongTienDichVu, giamGia, tongTien
7. `GiaoDich.kt` - soTien
8. `DatabaseHelper.kt` - Migration v4 → v5
9. Tất cả DAO tương ứng
10. Tất cả Fragment tương ứng

**Migration:**
```sql
-- Không cần nhân vì giá trị đã là số nguyên
-- Chỉ cần đổi kiểu dữ liệu
ALTER TABLE Phong RENAME TO Phong_old;
CREATE TABLE Phong (...giaCoBan INTEGER...);
INSERT INTO Phong SELECT * FROM Phong_old;
DROP TABLE Phong_old;
```

---

## 🟠 MỨC ĐỘ CAO - NÊN SỬA SỚM

### VẤN ĐỀ 2: HoaDon thiếu Unique Constraint ⭐⭐⭐⭐

**Mức độ:** CAO
**Ảnh hưởng:** Có thể tạo nhiều hóa đơn trùng
**Thời gian:** 30 phút

**Mô tả:**
```
Hiện tại: Có thể tạo nhiều hóa đơn cho cùng hợp đồng/tháng
VD: Hợp đồng #1, tháng 5/2024 → Tạo được 2, 3 hóa đơn ❌
```

**Giải pháp:**
```sql
-- Thêm constraint
UNIQUE(maHopDong, thang, nam)
```

**File cần sửa:**
- `DatabaseHelper.kt` - Thêm constraint trong CREATE TABLE

---

### VẤN ĐỀ 3: ChiSoDienNuoc thiếu Unique Constraint ⭐⭐⭐⭐

**Mức độ:** CAO
**Ảnh hưởng:** Có thể nhập nhiều chỉ số trùng
**Thời gian:** 30 phút

**Mô tả:**
```
Hiện tại: Có thể nhập nhiều chỉ số cho cùng phòng/loại/tháng
VD: Phòng 101, Điện, tháng 5/2024 → Nhập được 2, 3 lần ❌
```

**Giải pháp:**
```sql
-- Thêm constraint
UNIQUE(maPhong, loai, thang, nam)
```

**File cần sửa:**
- `DatabaseHelper.kt` - Thêm constraint trong CREATE TABLE

---

### VẤN ĐỀ 4: HoaDon dùng Boolean thay vì trangThai ⭐⭐⭐⭐

**Mức độ:** CAO
**Ảnh hưởng:** Không quản lý được thanh toán một phần
**Thời gian:** 1-2 giờ

**Mô tả:**
```kotlin
// ❌ SAI - Hiện tại
val daThanhToan: Boolean = false

// Vấn đề:
// - Không biết thanh toán bao nhiêu
// - Không biết còn nợ bao nhiêu
// - Không quản lý được thanh toán nhiều lần
```

**Giải pháp:**
```kotlin
// ✅ ĐÚNG
val tienDaThanhToan: Long = 0
val trangThai: String = "chua_thanh_toan"

// Trạng thái:
// "chua_thanh_toan" - tienDaThanhToan = 0
// "thanh_toan_mot_phan" - 0 < tienDaThanhToan < tongTien
// "da_thanh_toan" - tienDaThanhToan >= tongTien
// "qua_han" - Quá hạn
```

**File cần sửa:**
1. `HoaDon.kt` - Thêm tienDaThanhToan, đổi daThanhToan → trangThai
2. `HoaDonDao.kt` - Cập nhật CRUD
3. `DatabaseHelper.kt` - Migration
4. `CreateInvoiceFragment.kt` - Cập nhật logic
5. `InvoiceListFragment.kt` - Hiển thị trạng thái mới
6. `HoaDonAdapter.kt` - Hiển thị trạng thái

---

## 🟡 MỨC ĐỘ TRUNG BÌNH - NÊN CÓ

### VẤN ĐỀ 5: GiaoDich chưa tự động tạo khi thanh toán ⭐⭐⭐

**Mức độ:** TRUNG BÌNH
**Ảnh hưởng:** Phải tự tạo giao dịch thủ công
**Thời gian:** 1 giờ

**Mô tả:**
```
Hiện tại: Khi khách thanh toán hóa đơn
→ Phải tự vào màn hình Thu Chi để tạo giao dịch
→ Dễ quên, dễ sai
```

**Giải pháp:**
```kotlin
fun thanhToanHoaDon(maHoaDon: Long, soTien: Long) {
    // 1. Cập nhật hóa đơn
    val hoaDon = hoaDonDao.layTheoMa(maHoaDon)
    val tienMoi = hoaDon.tienDaThanhToan + soTien
    hoaDonDao.capNhat(hoaDon.copy(
        tienDaThanhToan = tienMoi,
        trangThai = tinhTrangThai(tienMoi, hoaDon.tongTien)
    ))
    
    // 2. Tự động tạo giao dịch
    val giaoDich = GiaoDich(
        loai = "thu",
        maHoaDon = maHoaDon,
        soTien = soTien,
        danhMuc = "Tiền thuê phòng",
        ngayGiaoDich = System.currentTimeMillis(),
        noiDung = "Thanh toán hóa đơn #$maHoaDon"
    )
    giaoDichDao.them(giaoDich)
}
```

**File cần sửa:**
- `CreateInvoiceFragment.kt` hoặc tạo `InvoicePaymentFragment.kt`
- Thêm nút "Thanh toán" trong `InvoiceListFragment.kt`

---

### VẤN ĐỀ 6: DichVu thiếu isActive ⭐⭐⭐

**Mức độ:** TRUNG BÌNH
**Ảnh hưởng:** Không ẩn được dịch vụ không dùng
**Thời gian:** 30 phút

**Mô tả:**
```
Hiện tại: Dịch vụ không còn dùng vẫn hiển thị
VD: Internet cũ 100k → Đổi sang 150k
→ Dịch vụ 100k vẫn hiển thị, gây rối
```

**Giải pháp:**
```kotlin
data class DichVu(
    ...
    val isActive: Boolean = true  // ✅ THÊM MỚI
)

// Khi query
fun layDichVuHoatDong(maNha: Long): List<DichVu> {
    return layTheoNha(maNha).filter { it.isActive }
}
```

**File cần sửa:**
1. `DichVu.kt` - Thêm isActive
2. `DichVuDao.kt` - Thêm method layDichVuHoatDong()
3. `DatabaseHelper.kt` - Migration
4. `ServiceListFragment.kt` - Thêm nút Ẩn/Hiện

---

## 🟢 MỨC ĐỘ THẤP - TÙY CHỌN

### VẤN ĐỀ 7: Thiếu bảng PhongDichVu ⭐⭐

**Mức độ:** THẤP
**Ảnh hưởng:** Không quản lý chi tiết dịch vụ từng phòng
**Thời gian:** 3-4 giờ

**Mô tả:**
```
Hiện tại: DichVu là danh mục chung
→ Không biết phòng nào dùng dịch vụ nào
→ Không biết thời gian áp dụng
→ Không quản lý số lượng (VD: gửi 2 xe)
```

**Giải pháp:**
```kotlin
data class PhongDichVu(
    val maPhongDichVu: Long = 0,
    val maPhong: Long,
    val maDichVu: Long,
    val donGiaApDung: Long,
    val soLuong: Int = 1,
    val trangThai: String = "dang_ap_dung",
    val ngayBatDau: Long = System.currentTimeMillis(),
    val ngayKetThuc: Long? = null,
    val ghiChu: String = ""
)
```

**Lợi ích:**
- Biết phòng nào dùng dịch vụ gì
- Quản lý giá riêng cho từng phòng
- Quản lý số lượng
- Lưu lịch sử

**File cần tạo/sửa:**
1. Tạo `PhongDichVu.kt`
2. Tạo `PhongDichVuDao.kt`
3. Sửa `DatabaseHelper.kt` - Tạo bảng
4. Sửa `CreateInvoiceFragment.kt` - Load dịch vụ từ PhongDichVu
5. Tạo màn hình quản lý dịch vụ phòng

---

### VẤN ĐỀ 8: ChiSoDienNuoc thiếu Check Constraint ⭐

**Mức độ:** THẤP
**Ảnh hưởng:** Có thể nhập chiSoMoi < chiSoCu
**Thời gian:** 15 phút

**Mô tả:**
```
Hiện tại: Chỉ validate ở code
→ Nếu insert trực tiếp vào DB, có thể sai
```

**Giải pháp:**
```sql
CHECK(chiSoMoi >= chiSoCu)
```

**File cần sửa:**
- `DatabaseHelper.kt` - Thêm constraint trong CREATE TABLE

---

## 📋 KẾ HOẠCH THỰC HIỆN

### Tuần 1: Sửa vấn đề nghiêm trọng
- [ ] VẤN ĐỀ 1: Đổi Double → Long (2-3 giờ)
- [ ] Test kỹ sau khi đổi

### Tuần 2: Sửa vấn đề mức cao
- [ ] VẤN ĐỀ 2: HoaDon unique constraint (30 phút)
- [ ] VẤN ĐỀ 3: ChiSoDienNuoc unique constraint (30 phút)
- [ ] VẤN ĐỀ 4: HoaDon trangThai (1-2 giờ)

### Tuần 3: Sửa vấn đề mức trung bình
- [ ] VẤN ĐỀ 5: Tự động tạo giao dịch (1 giờ)
- [ ] VẤN ĐỀ 6: DichVu isActive (30 phút)

### Tuần 4: Sửa vấn đề mức thấp (tùy chọn)
- [ ] VẤN ĐỀ 7: PhongDichVu (3-4 giờ)
- [ ] VẤN ĐỀ 8: Check constraint (15 phút)

---

## 🎯 KẾT LUẬN

**Bắt buộc phải làm:**
- VẤN ĐỀ 1: Đổi Double → Long (QUAN TRỌNG NHẤT)

**Nên làm:**
- VẤN ĐỀ 2, 3, 4: Constraints và trangThai

**Tùy chọn:**
- VẤN ĐỀ 5, 6, 7, 8: Cải tiến thêm

**Tổng thời gian ước tính:**
- Bắt buộc: 2-3 giờ
- Nên làm: 2-3 giờ
- Tùy chọn: 5-6 giờ
- **TỔNG:** 9-12 giờ

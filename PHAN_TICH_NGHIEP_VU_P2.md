# PHÂN TÍCH NGHIỆP VỤ - PHẦN 2: CẤU TRÚC DỮ LIỆU (tiếp)

#### 2.2.4. HopDong (Hợp đồng thuê)
```kotlin
data class HopDong(
    val maHopDong: Long,       // ID tự động
    val maPhong: Long,         // FK → Phong
    val maKhach: Long,         // FK → KhachThue
    val ngayBatDau: Long,      // Ngày bắt đầu (timestamp)
    val ngayKetThuc: Long,     // Ngày kết thúc (timestamp)
    val giaThueThang: Double,  // Giá thuê/tháng
    val tienDatCoc: Double,    // Tiền đặt cọc
    val trangThai: String      // "dang_thue" | "het_han" | "da_huy"
)
```

**Ý nghĩa:** 
- Hợp đồng thuê giữa chủ nhà và khách thuê
- Một phòng có thể có nhiều hợp đồng (theo thời gian)
- Một khách thuê có thể có nhiều hợp đồng

#### 2.2.5. DatCoc (Đặt cọc phòng)
```kotlin
data class DatCoc(
    val maDatCoc: Long,        // ID tự động
    val maPhong: Long,         // FK → Phong
    val tenKhach: String,      // Tên người đặt cọc
    val soDienThoai: String,   // SĐT
    val soCmnd: String,        // CMND/CCCD
    val email: String,         // Email
    val tienDatCoc: Double,    // Số tiền đặt cọc
    val giaPhong: Double,      // Giá phòng thỏa thuận
    val ngayDuKienVao: Long,   // Ngày dự kiến vào ở
    val ghiChu: String,        // Ghi chú
    val ngayTao: Long          // Ngày tạo
)
```

**Ý nghĩa:** Quản lý thông tin đặt cọc trước khi ký hợp đồng chính thức

#### 2.2.6. HoaDon (Hóa đơn)
```kotlin
data class HoaDon(
    val maHoaDon: Long,        // ID tự động
    val maHopDong: Long,       // FK → HopDong
    val thang: Int,            // Tháng (1-12)
    val nam: Int,              // Năm
    val tienPhong: Double,     // Tiền phòng
    val tongTienDichVu: Double,// Tổng tiền dịch vụ
    val giamGia: Double,       // Giảm giá
    val tongTien: Double,      // Tổng tiền
    val daThanhToan: Boolean,  // Đã thanh toán?
    val ghiChu: String,        // Ghi chú
    val ngayTao: Long          // Ngày tạo
)
```

**Công thức tính:**
```
tongTien = tienPhong + tongTienDichVu - giamGia
```

#### 2.2.7. ChiSoDienNuoc (Chỉ số điện nước)
```kotlin
data class ChiSoDienNuoc(
    val maChiSo: Long,         // ID tự động
    val maPhong: Long,         // FK → Phong
    val loai: String,          // "dien" | "nuoc"
    val thang: Int,            // Tháng (1-12)
    val nam: Int,              // Năm
    val chiSoCu: Double,       // Chỉ số cũ
    val chiSoMoi: Double,      // Chỉ số mới
    val donGia: Double,        // Đơn giá (đ/kWh hoặc đ/m³)
    val ghiChu: String         // Ghi chú
)
```

**Công thức tính:**
```
tieuThu = chiSoMoi - chiSoCu
thanhTien = tieuThu * donGia
```

#### 2.2.8. DichVu (Dịch vụ)
```kotlin
data class DichVu(
    val maDichVu: Long,        // ID tự động
    val maNha: Long,           // FK → NhaTro
    val tenDichVu: String,     // Tên dịch vụ
    val donVi: String,         // Đơn vị tính
    val donGia: Double,        // Đơn giá
    val loaiDichVu: String     // "dien" | "nuoc" | "khac"
)
```

**Ví dụ:** Internet, Vệ sinh, Gửi xe, Rác...

#### 2.2.9. GiaoDich (Thu chi)
```kotlin
data class GiaoDich(
    val maGiaoDich: Long,      // ID tự động
    val loai: String,          // "thu" | "chi"
    val maPhong: Long?,        // FK → Phong (optional)
    val soTien: Double,        // Số tiền
    val danhMuc: String,       // Danh mục
    val ngayGiaoDich: Long,    // Ngày giao dịch
    val noiDung: String,       // Nội dung
    val tenNguoi: String,      // Tên người
    val phuongThucThanhToan: String, // "tien_mat" | "chuyen_khoan" | "the"
    val ghiChu: String,        // Ghi chú
    val ngayTao: Long          // Ngày tạo
)
```

**Danh mục thu:** Tiền thuê phòng, Tiền điện, Tiền nước, Dịch vụ, Đặt cọc, Khác
**Danh mục chi:** Sửa chữa, Điện, Nước, Vệ sinh, Bảo trì, Lương, Khác

---

## 3. QUY TRÌNH NGHIỆP VỤ

### 3.1. Quy trình cho thuê phòng (Flow chính)

```
[1. Tạo nhà trọ] 
    ↓
[2. Tạo phòng] (trangThai = "trong")
    ↓
[3a. Đặt cọc] → Cập nhật phòng (trangThai = "dat_coc")
    ↓
[3b. Thêm khách thuê]
    ↓
[4. Tạo hợp đồng] → Cập nhật phòng (trangThai = "da_thue")
    ↓
[5. Nhập chỉ số điện nước hàng tháng]
    ↓
[6. Tạo hóa đơn hàng tháng]
    ↓
[7. Thu tiền] → Ghi nhận giao dịch
```

### 3.2. Quy trình đặt cọc

```
Khách muốn đặt cọc
    ↓
Kiểm tra phòng trống? (trangThai = "trong")
    ├─ Không → Thông báo lỗi
    └─ Có → Tiếp tục
        ↓
    Nhập thông tin đặt cọc
        ↓
    Lưu thông tin DatCoc
        ↓
    Cập nhật Phong.trangThai = "dat_coc"
        ↓
    Thành công
```

**Quy tắc:**
- Chỉ đặt cọc phòng trống
- Không đặt cọc phòng đã có người thuê
- Không đặt cọc phòng đã được đặt cọc

### 3.3. Quy trình thêm khách thuê

```
Thêm khách thuê mới
    ↓
Chọn phòng
    ↓
Kiểm tra trạng thái phòng
    ├─ Phòng đã đặt cọc? → Cảnh báo, không cho thêm
    ├─ Phòng đã đủ người? → Cảnh báo, không cho thêm
    └─ OK → Tiếp tục
        ↓
    Nhập thông tin khách thuê
        ↓
    Kiểm tra số người trong phòng
        ├─ Phòng trống (0 người)
        │   ↓
        │   Hiện dialog: "Tạo hợp đồng?"
        │   ├─ Có → Tạo hợp đồng + Cập nhật trangThai = "da_thue"
        │   └─ Không → Chỉ lưu khách thuê
        │
        └─ Phòng có người (< soNguoiToiDa)
            ↓
            Hiện dialog: "Thêm ở ghép?"
            ├─ Thêm ở ghép → Lưu khách, không tạo hợp đồng
            ├─ Tạo hợp đồng mới → Tạo hợp đồng riêng
            └─ Hủy → Không làm gì
```

**Quy tắc:**
- Một phòng có thể có nhiều khách thuê (ở ghép)
- Số người <= soNguoiToiDa
- Người đầu tiên nên có hợp đồng
- Người ở ghép có thể không có hợp đồng riêng

# PHÂN TÍCH NGHIỆP VỤ - PHẦN 4: DỊCH VỤ, ĐIỆN NƯỚC, HÓA ĐƠN

### 4.6. QUẢN LÝ DỊCH VỤ

#### 4.6.1. Thêm/Sửa dịch vụ
**Input:**
- Nhà trọ (chọn)
- Tên dịch vụ (bắt buộc)
- Đơn vị tính (mặc định: "lần")
- Đơn giá (bắt buộc, > 0)
- Loại: Điện | Nước | Khác

**Validation:**
- Tên dịch vụ không rỗng
- Đơn giá > 0

**Business Logic - Kiểm tra trùng lặp:**
```
Kiểm tra: cùng nhà + cùng tên + cùng giá?
├─ Có → Cảnh báo: "Đã tồn tại dịch vụ 'X' với giá Yđ"
└─ Không → Cho phép lưu
```

**Quy tắc:**
- ✅ Cho phép: Cùng tên, khác giá (VD: Internet 100k, Internet 150k)
- ❌ Không cho phép: Cùng tên, cùng giá

**Hiển thị:**
- Format: "Tên dịch vụ - Giá"
- VD: "Internet - 100,000đ"

---

### 4.7. QUẢN LÝ ĐIỆN NƯỚC

#### 4.7.1. Nhập chỉ số điện nước
**Input:**
- Nhà trọ (chọn)
- Phòng (chọn)
- Loại: Điện | Nước
- Tháng (1-12, mặc định: tháng hiện tại)
- Năm (mặc định: năm hiện tại)
- Chỉ số cũ (tự động, chỉ đọc)
- Chỉ số mới (bắt buộc)
- Đơn giá (bắt buộc, > 0)
- Ghi chú

**Validation:**
- Tháng: 1-12
- Chỉ số mới >= Chỉ số cũ
- Đơn giá > 0

**Business Logic - Tự động điền chỉ số cũ:**
```
Khi chọn phòng hoặc đổi loại:
    Lấy chỉ số tháng trước = layTheoThangNam(thang-1, nam)
    Lọc theo: maPhong + loai
    IF tìm thấy THEN
        etChiSoCu = chiSoThangTruoc.chiSoMoi
        etDonGia = chiSoThangTruoc.donGia
```

**Business Logic - Kiểm tra trùng lặp:**
```
Kiểm tra: cùng phòng + cùng loại + cùng tháng/năm?
├─ Có → Hiện dialog cảnh báo
│   Message: "Phòng này đã có chỉ số điện/nước tháng X/Y"
│   ├─ Tiếp tục → Cho phép lưu (có thể nhập lại)
│   └─ Hủy → Không lưu
│
└─ Không → Lưu bình thường
```

**Business Logic - Sau khi lưu:**
```
Lưu ChiSoDienNuoc thành công
↓
Hiện dialog: "Tạo hóa đơn?"
Message: "Đã nhập chỉ số điện/nước tháng X/Y. Tạo hóa đơn cho phòng này?"
├─ Tạo hóa đơn
│   └─ Chuyển sang CreateInvoiceFragment
│       với maPhong, thang, nam đã điền sẵn
│
└─ Để sau
    └─ Quay lại danh sách
```

#### 4.7.2. Xem danh sách chỉ số
**Chức năng lọc:**
- Spinner tháng (1-12)
- Spinner năm (2020-2030)
- Nút "Tất cả" / "Lọc"

**Business Logic:**
```
IF xemTatCa == true THEN
    Hiển thị tất cả chỉ số (sắp xếp giảm dần)
    Disable spinner tháng/năm
ELSE
    Hiển thị chỉ số theo tháng/năm đã chọn
    Enable spinner tháng/năm
```

**Hiển thị:**
- Tên phòng - Tên nhà (VD: "Phòng 101 - Nhà A")
- Loại: Điện | Nước
- Tháng/năm
- Chỉ số: "Cũ → Mới (Tiêu thụ)"
- Thành tiền

---

### 4.8. QUẢN LÝ HÓA ĐƠN

#### 4.8.1. Tạo hóa đơn
**Input:**
- Nhà trọ (chọn)
- Phòng (chọn)
- Hợp đồng (chọn - chỉ hợp đồng đang thuê)
- Tháng (1-12)
- Năm
- Tiền phòng (tự động từ hợp đồng)
- Danh sách dịch vụ (checkbox)
- Giảm giá
- Ghi chú

**Business Logic - Load dữ liệu:**
```
Khi chọn hợp đồng:
    1. Load thông tin khách thuê
    2. Tự động điền tiền phòng = hopDong.giaThueThang
    3. Load chỉ số điện nước tháng này
    4. Tính tiền điện = (chiSoMoi - chiSoCu) * donGia
    5. Tính tiền nước = (chiSoMoi - chiSoCu) * donGia
    6. Load danh sách dịch vụ của nhà
```

**Business Logic - Tính toán:**
```
tongTienDichVu = 0

// Điện
IF có chỉ số điện THEN
    tieuThuDien = chiSoMoi - chiSoCu
    tienDien = tieuThuDien * donGiaDien
    tongTienDichVu += tienDien

// Nước
IF có chỉ số nước THEN
    tieuThuNuoc = chiSoMoi - chiSoCu
    tienNuoc = tieuThuNuoc * donGiaNuoc
    tongTienDichVu += tienNuoc

// Dịch vụ khác
FOR EACH dịch vụ được chọn:
    tongTienDichVu += dichVu.donGia

// Tổng tiền
tongTien = tienPhong + tongTienDichVu - giamGia
```

**Output:**
- Lưu HoaDon
- Lưu ChiTietHoaDon (chi tiết từng dịch vụ)

#### 4.8.2. Xem/In hóa đơn
**Hiển thị:**
```
=== HÓA ĐƠN TIỀN PHÒNG ===
Tháng: X/Y
Phòng: ABC
Khách thuê: XYZ

1. Tiền phòng:        1,000,000đ
2. Tiền điện:           200,000đ
   (100 kWh x 2,000đ)
3. Tiền nước:            50,000đ
   (10 m³ x 5,000đ)
4. Internet:            100,000đ
5. Vệ sinh:              50,000đ
   
Tổng dịch vụ:          400,000đ
Giảm giá:              -50,000đ
----------------------------
TỔNG CỘNG:           1,350,000đ

Trạng thái: Chưa thanh toán / Đã thanh toán
```

---

### 4.9. QUẢN LÝ THU CHI

#### 4.9.1. Thêm khoản thu
**Input:**
- Số tiền (bắt buộc, > 0)
- Danh mục: Tiền thuê phòng | Tiền điện | Tiền nước | Dịch vụ | Đặt cọc | Khác
- Ngày giao dịch (date picker)
- Nội dung
- Tên người
- Phương thức: Tiền mặt | Chuyển khoản | Thẻ
- Ghi chú

**Validation:**
- Số tiền > 0

**Output:**
- Lưu GiaoDich với loai = "thu"

#### 4.9.2. Thêm khoản chi
**Input:**
- Số tiền (bắt buộc, > 0)
- Danh mục: Sửa chữa | Điện | Nước | Vệ sinh | Bảo trì | Lương | Khác
- Ngày giao dịch (date picker)
- Nội dung
- Tên người
- Phương thức: Tiền mặt | Chuyển khoản | Thẻ
- Ghi chú

**Validation:**
- Số tiền > 0

**Output:**
- Lưu GiaoDich với loai = "chi"

#### 4.9.3. Báo cáo thu chi
**Hiển thị:**
```
Tổng thu:     10,000,000đ
Tổng chi:      3,000,000đ
----------------------------
Lợi nhuận:     7,000,000đ
```

**Lọc theo:**
- Tháng/năm
- Loại (thu/chi)
- Danh mục

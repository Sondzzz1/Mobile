# PHÂN TÍCH NGHIỆP VỤ - PHẦN 3: CHI TIẾT CHỨC NĂNG

## 4. CHI TIẾT TỪNG CHỨC NĂNG

### 4.1. QUẢN LÝ NHÀ TRỌ

#### 4.1.1. Thêm/Sửa nhà trọ
**Input:**
- Tên nhà trọ (bắt buộc)
- Địa chỉ chi tiết
- Xã/phường
- Quận/huyện
- Tỉnh/thành
- Tên chủ nhà
- Số điện thoại (validate format)
- Ghi chú

**Validation:**
- Tên nhà trọ không được rỗng
- Số điện thoại phải đúng định dạng (10-11 số)

**Output:**
- Lưu vào bảng NhaTro
- Địa chỉ được ghép: "Chi tiết, Xã, Huyện, Tỉnh"

**Business Logic:**
- Không kiểm tra trùng tên nhà (cho phép nhiều nhà cùng tên)
- Khi sửa: giữ nguyên các phòng đã tạo

#### 4.1.2. Xóa nhà trọ
**Quy tắc:**
- Kiểm tra có phòng không?
- Nếu có phòng → Không cho xóa (hoặc xóa cascade)
- Nếu không có phòng → Cho phép xóa

---

### 4.2. QUẢN LÝ PHÒNG

#### 4.2.1. Thêm/Sửa phòng
**Input:**
- Nhà trọ (chọn từ danh sách)
- Tên phòng (bắt buộc)
- Giá phòng (bắt buộc, > 0)
- Diện tích (m²)
- Số người tối đa (mặc định: 1)
- Ghi chú

**Validation:**
- Tên phòng không rỗng
- Giá phòng > 0
- Diện tích > 0 (nếu nhập)

**Output:**
- Lưu vào bảng Phong
- Trạng thái mặc định: "trong"

**Business Logic:**
- Khi tạo mới: trangThai = "trong"
- Khi sửa: giữ nguyên trangThai cũ
- Không kiểm tra trùng tên phòng trong cùng nhà

#### 4.2.2. Trạng thái phòng
**3 trạng thái:**

1. **"trong"** (Trống)
   - Phòng chưa có người
   - Có thể đặt cọc
   - Có thể cho thuê

2. **"dat_coc"** (Đã đặt cọc)
   - Đã có người đặt cọc
   - Không thể đặt cọc thêm
   - Không thể thêm khách thuê
   - Chờ khách vào ở

3. **"da_thue"** (Đã thuê)
   - Đang có người ở
   - Không thể đặt cọc
   - Có thể thêm người ở ghép (nếu chưa đủ)

**Chuyển đổi trạng thái:**
```
"trong" → "dat_coc" (khi đặt cọc)
"dat_coc" → "trong" (khi hủy đặt cọc)
"trong" → "da_thue" (khi tạo hợp đồng)
"da_thue" → "trong" (khi hết hợp đồng)
```

---

### 4.3. QUẢN LÝ KHÁCH THUÊ

#### 4.3.1. Thêm khách thuê
**Input:**
- Nhà trọ (chọn)
- Phòng (chọn - chỉ hiển thị phòng hợp lệ)
- Họ tên (bắt buộc)
- Số điện thoại (validate)
- Email (validate)
- CMND/CCCD (validate 9 hoặc 12 số)
- Ngày sinh (date picker)
- Ngày cấp CMND (date picker)
- Nơi cấp
- Nơi làm việc
- Quê quán: Tỉnh, Huyện, Xã, Địa chỉ chi tiết
- Ghi chú

**Validation:**
- Họ tên không rỗng
- SĐT: 10-11 số
- Email: format email
- CMND: 9 hoặc 12 số

**Business Logic - Lọc phòng:**
Khi thêm mới, chỉ hiển thị phòng:
- Không phải "dat_coc"
- Chưa đủ người (soNguoiDangO < soNguoiToiDa)

**Business Logic - Sau khi lưu:**

**Trường hợp 1: Phòng trống (0 người)**
```
Hiện dialog: "Tạo hợp đồng thuê?"
├─ Tạo hợp đồng
│   ├─ Nhập: Ngày bắt đầu, Thời hạn (tháng), Giá thuê, Đặt cọc
│   ├─ Tự động tính ngày kết thúc
│   ├─ Lưu KhachThue
│   ├─ Tạo HopDong
│   └─ Cập nhật Phong.trangThai = "da_thue"
│
└─ Để sau
    └─ Chỉ lưu KhachThue
```

**Trường hợp 2: Phòng có người (< soNguoiToiDa)**
```
Hiện dialog: "Thêm người ở ghép?"
Message: "Phòng đang có X người. Tối đa: Y người."

├─ Thêm ở ghép
│   └─ Lưu KhachThue (không tạo hợp đồng)
│
├─ Tạo hợp đồng mới
│   └─ Tạo hợp đồng riêng cho người ở ghép
│
└─ Hủy
    └─ Không làm gì
```

**Trường hợp 3: Phòng đã đủ người**
```
Cảnh báo: "Phòng đã đủ người! (X/Y người)"
→ Không cho thêm
```

**Trường hợp 4: Phòng đã đặt cọc**
```
Cảnh báo: "Phòng đã được đặt cọc!"
→ Không cho thêm
```

#### 4.3.2. Sửa khách thuê
**Business Logic:**
- Cho phép sửa tất cả thông tin
- Cho phép chuyển phòng
- Không kiểm tra số người khi sửa

---

### 4.4. QUẢN LÝ ĐẶT CỌC

#### 4.4.1. Thêm đặt cọc
**Input:**
- Nhà trọ (chọn)
- Phòng (chọn - chỉ phòng trống)
- Tên khách (bắt buộc)
- Số điện thoại (validate)
- CMND/CCCD (validate)
- Tiền đặt cọc (bắt buộc, > 0)
- Giá phòng thỏa thuận
- Ngày dự kiến vào ở (date picker)
- Ghi chú

**Validation:**
- Tên khách không rỗng
- SĐT: 10-11 số
- CMND: 9 hoặc 12 số
- Tiền đặt cọc > 0

**Business Logic - Lọc phòng:**
Chỉ hiển thị phòng có trangThai = "trong"

**Business Logic - Kiểm tra trước khi lưu:**
```
IF phòng.trangThai == "da_thue" THEN
    Cảnh báo: "Phòng đã có người thuê!"
    → Không cho đặt cọc
    
ELSE IF phòng.trangThai == "dat_coc" THEN
    Cảnh báo: "Phòng đã được đặt cọc!"
    → Không cho đặt cọc
    
ELSE
    Lưu DatCoc
    Cập nhật Phong.trangThai = "dat_coc"
    → Thành công
```

#### 4.4.2. Hủy đặt cọc
**Business Logic:**
```
Xóa DatCoc
Cập nhật Phong.trangThai = "trong"
```

---

### 4.5. QUẢN LÝ HỢP ĐỒNG

#### 4.5.1. Tạo hợp đồng
**Input:**
- Nhà trọ (chọn)
- Phòng (chọn)
- Tên khách thuê (bắt buộc)
- Số điện thoại (bắt buộc, validate)
- Ngày bắt đầu (date picker, bắt buộc)
- Ngày kết thúc (date picker, bắt buộc)
- Giá thuê/tháng (bắt buộc, > 0)
- Tiền đặt cọc

**Validation:**
- Tên khách không rỗng
- SĐT: 10-11 số
- Ngày bắt đầu < Ngày kết thúc
- Giá thuê > 0

**Business Logic:**
```
Kiểm tra phòng đã có hợp đồng đang thuê?
├─ Có → Cảnh báo: "Phòng đã có hợp đồng đang thuê"
└─ Không → Tiếp tục
    ↓
Tìm khách thuê theo SĐT
├─ Tìm thấy → Dùng maKhach có sẵn
└─ Không tìm thấy → Tạo KhachThue mới
    ↓
Tạo HopDong
Cập nhật Phong.trangThai = "da_thue"
```

#### 4.5.2. Sửa hợp đồng
**Business Logic:**
- Cho phép sửa tất cả thông tin
- Không kiểm tra phòng đã có hợp đồng (vì đang sửa chính nó)

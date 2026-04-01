# PHÂN TÍCH NGHIỆP VỤ - PHẦN 5: QUY TẮC VÀ RÀNG BUỘC

## 5. LUỒNG DỮ LIỆU

### 5.1. Luồng tạo hóa đơn đầy đủ

```
[Tạo nhà trọ]
    ↓
[Tạo phòng] → trangThai = "trong"
    ↓
[Tạo dịch vụ] (Internet, Vệ sinh, Rác...)
    ↓
[Thêm khách thuê]
    ↓
[Tạo hợp đồng] → trangThai = "da_thue"
    ↓
[Nhập chỉ số điện tháng 1]
    ↓
[Nhập chỉ số nước tháng 1]
    ↓
[Tạo hóa đơn tháng 1]
    ├─ Tự động load tiền phòng từ hợp đồng
    ├─ Tự động load chỉ số điện nước
    ├─ Chọn dịch vụ cần tính
    └─ Tính tổng tiền
    ↓
[Thu tiền] → Ghi nhận giao dịch thu
    ↓
[Đánh dấu hóa đơn đã thanh toán]
```

### 5.2. Luồng chuyển trạng thái phòng

```
PHÒNG MỚI TẠO
    trangThai = "trong"
    ↓
    ├─ [Đặt cọc] → trangThai = "dat_coc"
    │   ↓
    │   └─ [Hủy đặt cọc] → trangThai = "trong"
    │
    └─ [Tạo hợp đồng] → trangThai = "da_thue"
        ↓
        └─ [Hết hợp đồng] → trangThai = "trong"
```

### 5.3. Luồng ở ghép

```
PHÒNG TRỐNG (0 người)
    ↓
[Thêm khách A] → Tạo hợp đồng
    ↓
PHÒNG CÓ 1 NGƯỜI (A)
    ↓
[Thêm khách B]
    ├─ Chọn "Ở ghép" → Không tạo hợp đồng
    │   ↓
    │   PHÒNG CÓ 2 NGƯỜI (A có HĐ, B không HĐ)
    │
    └─ Chọn "Tạo HĐ mới" → Tạo hợp đồng riêng
        ↓
        PHÒNG CÓ 2 NGƯỜI (A có HĐ, B có HĐ)
```

---

## 6. QUY TẮC NGHIỆP VỤ

### 6.1. Quy tắc về trạng thái phòng

| Trạng thái | Có thể đặt cọc? | Có thể thêm khách? | Có thể tạo HĐ? |
|------------|-----------------|-------------------|----------------|
| trong      | ✅ Có           | ✅ Có             | ✅ Có          |
| dat_coc    | ❌ Không        | ❌ Không          | ❌ Không       |
| da_thue    | ❌ Không        | ✅ Có (nếu chưa đủ)| ✅ Có         |

### 6.2. Quy tắc về số người trong phòng

```
soNguoiDangO = COUNT(KhachThue WHERE maPhong = X AND trangThai = "dang_o")

IF soNguoiDangO >= soNguoiToiDa THEN
    Không cho thêm khách mới
ELSE
    Cho phép thêm khách
```

### 6.3. Quy tắc về hợp đồng

**Một phòng:**
- Có thể có nhiều hợp đồng (theo thời gian)
- Chỉ có 1 hợp đồng "dang_thue" tại một thời điểm
- Khi tạo HĐ mới: kiểm tra không có HĐ "dang_thue"

**Một khách thuê:**
- Có thể có nhiều hợp đồng (thuê nhiều lần)
- Có thể ở ghép không cần hợp đồng

### 6.4. Quy tắc về đặt cọc

**Điều kiện đặt cọc:**
```
IF phong.trangThai == "trong" THEN
    Cho phép đặt cọc
ELSE
    Không cho phép
```

**Khi đặt cọc thành công:**
```
Lưu DatCoc
Cập nhật Phong.trangThai = "dat_coc"
```

**Khi hủy đặt cọc:**
```
Xóa DatCoc
Cập nhật Phong.trangThai = "trong"
```

### 6.5. Quy tắc về chỉ số điện nước

**Tự động điền chỉ số cũ:**
```
chiSoCu = chiSoMoi của tháng trước (cùng phòng, cùng loại)
```

**Validation:**
```
chiSoMoi >= chiSoCu
```

**Kiểm tra trùng lặp:**
```
Cảnh báo nếu: cùng phòng + cùng loại + cùng tháng/năm
Nhưng vẫn cho phép lưu (có thể nhập lại)
```

### 6.6. Quy tắc về dịch vụ

**Cho phép trùng tên, khác giá:**
```
Internet - 100,000đ  ✅
Internet - 150,000đ  ✅
Internet - 200,000đ  ✅
```

**Không cho phép trùng tên và giá:**
```
Internet - 100,000đ  ✅
Internet - 100,000đ  ❌ (Cảnh báo)
```

**Kiểm tra:**
```
IF EXISTS (
    cùng maNha + cùng tenDichVu + cùng donGia
) THEN
    Cảnh báo: "Đã tồn tại dịch vụ 'X' với giá Yđ"
ELSE
    Cho phép lưu
```

### 6.7. Quy tắc về hóa đơn

**Tính toán:**
```
tongTienDichVu = tienDien + tienNuoc + SUM(dichVuKhac)
tongTien = tienPhong + tongTienDichVu - giamGia
```

**Validation:**
```
tongTien >= 0
giamGia <= (tienPhong + tongTienDichVu)
```

### 6.8. Quy tắc về validation

**Số điện thoại:**
```
- Độ dài: 10-11 số
- Chỉ chứa số
- Bắt đầu bằng 0
```

**Email:**
```
- Format: xxx@yyy.zzz
- Có ký tự @
- Có domain
```

**CMND/CCCD:**
```
- CMND: 9 số
- CCCD: 12 số
- Chỉ chứa số
```

**Số tiền:**
```
- Phải > 0
- Không âm
- Là số hợp lệ
```

**Ngày tháng:**
```
- Ngày bắt đầu < Ngày kết thúc
- Không được để trống (nếu bắt buộc)
```

---

## 7. CÁC TRƯỜNG HỢP ĐẶC BIỆT

### 7.1. Ở ghép

**Scenario 1: Thêm người thứ 2 vào phòng**
```
Phòng: soNguoiToiDa = 3
Hiện có: 1 người (A)

Thêm người B:
├─ Option 1: Ở ghép (không HĐ)
│   → B không có hợp đồng riêng
│   → Dùng chung HĐ của A
│
└─ Option 2: Tạo HĐ mới
    → B có hợp đồng riêng
    → Phòng có 2 HĐ song song
```

**Scenario 2: Phòng đã đủ người**
```
Phòng: soNguoiToiDa = 2
Hiện có: 2 người

Thêm người thứ 3:
→ Cảnh báo: "Phòng đã đủ người!"
→ Không cho thêm
```

### 7.2. Chuyển phòng

**Cách xử lý:**
```
1. Sửa thông tin khách thuê
2. Chọn phòng mới
3. Lưu → maPhong được cập nhật
```

**Lưu ý:**
- Hợp đồng vẫn giữ nguyên maPhong cũ
- Nếu muốn chuyển HĐ: phải tạo HĐ mới

### 7.3. Hết hợp đồng

**Cách xử lý:**
```
1. Cập nhật HopDong.trangThai = "het_han"
2. Cập nhật KhachThue.trangThai = "da_chuyen_di"
3. Kiểm tra còn khách nào trong phòng không?
   ├─ Không còn → Cập nhật Phong.trangThai = "trong"
   └─ Còn → Giữ nguyên Phong.trangThai = "da_thue"
```

### 7.4. Hủy đặt cọc

**Cách xử lý:**
```
1. Xóa DatCoc
2. Cập nhật Phong.trangThai = "trong"
3. (Optional) Ghi nhận giao dịch hoàn cọc
```

---

## 8. TỔNG KẾT

### 8.1. Các thực thể chính
1. NhaTro (Nhà trọ)
2. Phong (Phòng)
3. KhachThue (Khách thuê)
4. HopDong (Hợp đồng)
5. DatCoc (Đặt cọc)
6. HoaDon (Hóa đơn)
7. ChiSoDienNuoc (Chỉ số điện nước)
8. DichVu (Dịch vụ)
9. GiaoDich (Thu chi)

### 8.2. Các quy trình chính
1. Cho thuê phòng (từ tạo nhà → thu tiền)
2. Đặt cọc phòng
3. Quản lý ở ghép
4. Tạo hóa đơn hàng tháng
5. Quản lý thu chi

### 8.3. Các quy tắc quan trọng
1. Trạng thái phòng: trong → dat_coc → da_thue
2. Số người <= soNguoiToiDa
3. Chỉ số mới >= Chỉ số cũ
4. Một phòng chỉ 1 HĐ "dang_thue"
5. Cho phép dịch vụ trùng tên, khác giá

---

**Tài liệu này mô tả đầy đủ logic nghiệp vụ của hệ thống quản lý nhà trọ.**

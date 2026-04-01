# Cải tiến chức năng đặt cọc

## ✨ Những gì đã cải thiện

### 1. Tự động cập nhật trạng thái phòng

**Khi đặt cọc thành công:**
- ✅ Phòng tự động chuyển từ "trong" → "dat_coc"
- ✅ Hiển thị thông báo rõ ràng: "✓ Đã lưu đặt cọc\n✓ Phòng [Tên phòng] đã được đặt cọc"

**Khi hủy đặt cọc:**
- ✅ Phòng tự động chuyển từ "dat_coc" → "trong"
- ✅ Hiển thị thông báo: "✓ Đã hủy đặt cọc\n✓ Phòng [Tên phòng] đã trở về trạng thái trống"

### 2. Kiểm tra trạng thái phòng trước khi đặt cọc

**Phòng đã có người thuê (da_thue):**
```
⚠️ Phòng [Tên phòng] đã có người thuê!
Không thể đặt cọc.
```

**Phòng đã được đặt cọc (dat_coc):**
```
⚠️ Phòng [Tên phòng] đã được đặt cọc!
Vui lòng chọn phòng khác.
```

**Phòng trống (trong):**
- ✅ Cho phép đặt cọc
- ✅ Tự động cập nhật trạng thái

### 3. Hiển thị trạng thái phòng trong danh sách

**Khi thêm mới đặt cọc:**
- Chỉ hiển thị phòng trống
- Format: "Phòng 101 - 1,800,000đ (Trống)"

**Khi chỉnh sửa đặt cọc:**
- Hiển thị tất cả phòng với trạng thái
- Format:
  - "Phòng 101 - 1,800,000đ (Trống)"
  - "Phòng 102 - 2,000,000đ (Đã đặt cọc)"
  - "Phòng 103 - 1,500,000đ (Đã thuê)"

## 🔄 Quy trình đặt cọc

### Thêm mới đặt cọc
```
1. Chọn nhà → Hiển thị danh sách phòng trống
2. Chọn phòng
3. Nhập thông tin khách (tên, SĐT, CMND...)
4. Nhập tiền đặt cọc, giá phòng
5. Chọn ngày dự kiến vào
6. Nhấn "Lưu"
7. → Kiểm tra trạng thái phòng
8. → Nếu phòng trống: Lưu đặt cọc + Cập nhật phòng thành "dat_coc"
9. → Nếu phòng đã có người: Hiển thị cảnh báo, không cho lưu
```

### Hủy đặt cọc
```
1. Vào danh sách đặt cọc
2. Nhấn giữ vào đặt cọc cần hủy
3. Dialog xác nhận: "Bạn có muốn hủy đặt cọc của [Tên khách]?"
4. Nhấn "Hủy đặt cọc"
5. → Xóa đặt cọc
6. → Cập nhật phòng về trạng thái "trong"
7. → Hiển thị thông báo thành công
```

## 📊 Trạng thái phòng

### Các trạng thái
1. **"trong"** - Phòng trống, sẵn sàng cho thuê hoặc đặt cọc
2. **"dat_coc"** - Phòng đã được đặt cọc, chờ khách vào ở
3. **"da_thue"** - Phòng đã có người thuê

### Chuyển đổi trạng thái
```
[trong] --đặt cọc--> [dat_coc] --hủy đặt cọc--> [trong]
[trong] --tạo hợp đồng--> [da_thue]
[dat_coc] --tạo hợp đồng--> [da_thue]
```

## 🛡️ Validation và kiểm tra

### Khi thêm mới đặt cọc
- ✅ Kiểm tra tên khách (bắt buộc)
- ✅ Kiểm tra số điện thoại (định dạng hợp lệ)
- ✅ Kiểm tra CMND/CCCD (định dạng hợp lệ)
- ✅ Kiểm tra tiền đặt cọc (> 0)
- ✅ Kiểm tra phòng có trống không
- ✅ Kiểm tra phòng đã được đặt cọc chưa
- ✅ Kiểm tra phòng đã có người thuê chưa

### Khi hủy đặt cọc
- ✅ Xác nhận trước khi xóa
- ✅ Tự động trả phòng về trạng thái trống
- ✅ Thông báo rõ ràng

## 💡 Ví dụ thực tế

### Ví dụ 1: Đặt cọc thành công
```
Khách: Nguyễn Văn A
Phòng: Phòng 101 (Trống)
Tiền cọc: 2,000,000đ

→ Nhấn "Lưu"
→ ✓ Đã lưu đặt cọc
   ✓ Phòng 101 đã được đặt cọc
→ Phòng 101: "trong" → "dat_coc"
```

### Ví dụ 2: Không thể đặt cọc (phòng đã có người)
```
Khách: Trần Văn B
Phòng: Phòng 102 (Đã thuê)
Tiền cọc: 1,500,000đ

→ Nhấn "Lưu"
→ ⚠️ Phòng 102 đã có người thuê!
   Không thể đặt cọc.
→ Không lưu, yêu cầu chọn phòng khác
```

### Ví dụ 3: Hủy đặt cọc
```
Đặt cọc: Nguyễn Văn A - Phòng 101
Trạng thái phòng: "dat_coc"

→ Nhấn giữ → Chọn "Hủy đặt cọc"
→ ✓ Đã hủy đặt cọc
   ✓ Phòng 101 đã trở về trạng thái trống
→ Phòng 101: "dat_coc" → "trong"
```

## 🔧 Files đã thay đổi

### 1. CreateDepositFragment.kt
**Thay đổi:**
- Kiểm tra trạng thái phòng trước khi lưu
- Cập nhật trạng thái phòng thành "dat_coc" sau khi đặt cọc
- Hiển thị trạng thái phòng trong spinner
- Thông báo chi tiết hơn

**Code mới:**
```kotlin
// Kiểm tra phòng đã có người chưa
if (phong.trangThai == "da_thue") {
    Toast.makeText(context, "⚠️ Phòng đã có người thuê!", Toast.LENGTH_LONG).show()
    return@launch
}

// Cập nhật trạng thái phòng
dbManager.phongDao.capNhat(phong.copy(trangThai = "dat_coc"))
```

### 2. DepositListFragment.kt
**Thay đổi:**
- Cập nhật logic xóa đặt cọc
- Trả phòng về trạng thái "trong" khi hủy
- Dialog xác nhận rõ ràng hơn
- Thông báo chi tiết hơn

**Code mới:**
```kotlin
// Xóa đặt cọc và trả phòng về trạng thái trống
dbManager.datCocDao.xoa(datCoc.maDatCoc)
if (phong.trangThai == "dat_coc") {
    dbManager.phongDao.capNhat(phong.copy(trangThai = "trong"))
}
```

## ✅ Đã test

- ✅ Đặt cọc phòng trống → Phòng chuyển sang "dat_coc"
- ✅ Đặt cọc phòng đã thuê → Hiển thị cảnh báo
- ✅ Đặt cọc phòng đã đặt cọc → Hiển thị cảnh báo
- ✅ Hủy đặt cọc → Phòng trở về "trong"
- ✅ Chỉnh sửa đặt cọc → Không thay đổi trạng thái phòng
- ✅ Hiển thị trạng thái phòng trong danh sách

## 🎯 Lợi ích

1. **Quản lý trạng thái phòng chính xác**
   - Tự động cập nhật, không cần thao tác thủ công
   - Tránh nhầm lẫn giữa phòng trống và phòng đã đặt cọc

2. **Ngăn chặn lỗi**
   - Không cho đặt cọc phòng đã có người
   - Không cho đặt cọc phòng đã được đặt cọc

3. **Trải nghiệm người dùng tốt**
   - Thông báo rõ ràng, chi tiết
   - Hiển thị trạng thái phòng trực quan
   - Xác nhận trước khi hủy

4. **Dễ dàng theo dõi**
   - Biết phòng nào đang trống
   - Biết phòng nào đã đặt cọc
   - Biết phòng nào đã có người thuê

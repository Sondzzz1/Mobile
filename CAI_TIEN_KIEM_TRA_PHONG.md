# Cải tiến kiểm tra phòng khi thêm người thuê

## ✨ Những gì đã cải thiện

### 1. Kiểm tra phòng đã đặt cọc

**Khi thêm người thuê vào phòng đã đặt cọc:**
```
⚠️ Phòng [Tên phòng] đã được đặt cọc!
Không thể thêm người thuê.
Vui lòng chọn phòng khác.
```

**Lý do:**
- Phòng đã đặt cọc đang chờ khách vào ở
- Không nên thêm người khác vào phòng này
- Tránh xung đột giữa người đặt cọc và người thuê mới

### 2. Kiểm tra phòng đã đủ người

**Khi thêm người thuê vào phòng đã đủ người:**
```
⚠️ Phòng [Tên phòng] đã đủ người!
Hiện có: 2/2 người
Vui lòng chọn phòng khác.
```

**Lý do:**
- Mỗi phòng có giới hạn số người tối đa
- Không cho phép vượt quá số người quy định
- Đảm bảo không gian sống hợp lý

### 3. Hiển thị thông tin phòng chi tiết

**Trong danh sách chọn phòng:**
- "Phòng 101 - 1,800,000đ (Trống)"
- "Phòng 102 - 2,000,000đ (1/3 người)"
- "Phòng 103 - 1,500,000đ (Đã đủ 2 người)"
- "Phòng 104 - 1,800,000đ (Đã đặt cọc)"

**Lợi ích:**
- Người dùng biết rõ trạng thái phòng
- Dễ dàng chọn phòng phù hợp
- Tránh chọn nhầm phòng không phù hợp

### 4. Lọc phòng thông minh

**Khi thêm mới người thuê:**
- Chỉ hiển thị phòng còn chỗ trống
- Không hiển thị phòng đã đặt cọc
- Không hiển thị phòng đã đủ người

**Khi chỉnh sửa người thuê:**
- Hiển thị tất cả phòng
- Cho phép chuyển phòng linh hoạt

### 5. Số người tối đa trong phòng

**Khi tạo phòng mới:**
- Có trường "Số người tối đa"
- Mặc định: 1 người
- Có thể điều chỉnh: 1, 2, 3, 4... người

**Ứng dụng:**
- Phòng đơn: 1 người
- Phòng đôi: 2 người
- Phòng gia đình: 3-4 người
- Phòng tập thể: 4-6 người

## 🔍 Các trường hợp kiểm tra

### Trường hợp 1: Phòng trống
```
Phòng 101:
- Trạng thái: "trong"
- Số người: 0/2
- Kết quả: ✅ Cho phép thêm người thuê
```

### Trường hợp 2: Phòng có 1 người (chưa đủ)
```
Phòng 102:
- Trạng thái: "da_thue"
- Số người: 1/3
- Kết quả: ✅ Cho phép thêm người ở ghép (còn 2 chỗ)
```

### Trường hợp 3: Phòng đã đủ người
```
Phòng 103:
- Trạng thái: "da_thue"
- Số người: 2/2
- Kết quả: ⚠️ Không cho phép thêm (đã đủ)
```

### Trường hợp 4: Phòng đã đặt cọc
```
Phòng 104:
- Trạng thái: "dat_coc"
- Số người: 0/2
- Kết quả: ⚠️ Không cho phép thêm (đã đặt cọc)
```

### Trường hợp 5: Chỉnh sửa thông tin người thuê
```
Người thuê hiện tại: Nguyễn Văn A (Phòng 101)
Kết quả: ✅ Cho phép chuyển sang phòng khác
```

## 📊 Quy trình kiểm tra

### Khi thêm người thuê mới
```
1. Chọn nhà
2. → Load danh sách phòng
3. → Đếm số người đang ở trong mỗi phòng
4. → Lọc phòng:
   - Loại bỏ phòng đã đặt cọc
   - Loại bỏ phòng đã đủ người
5. → Hiển thị danh sách phòng còn chỗ
6. Chọn phòng
7. Nhập thông tin người thuê
8. Nhấn "Lưu"
9. → Kiểm tra lại:
   - Phòng có đang đặt cọc không?
   - Phòng có đủ người chưa?
10. → Nếu OK: Lưu người thuê
11. → Nếu không OK: Hiển thị cảnh báo
```

### Khi chỉnh sửa người thuê
```
1. Load thông tin người thuê hiện tại
2. Chọn nhà
3. → Load tất cả phòng (không lọc)
4. → Hiển thị trạng thái mỗi phòng
5. Có thể chuyển sang phòng khác
6. Nhấn "Lưu"
7. → Cập nhật thông tin
```

## 💡 Ví dụ thực tế

### Ví dụ 1: Thêm người vào phòng trống
```
Phòng 101: Trống (0/2 người)
Thêm: Nguyễn Văn A
→ ✅ Thành công
→ Phòng 101: 1/2 người
```

### Ví dụ 2: Thêm người ở ghép
```
Phòng 102: Có Trần Văn B (1/3 người)
Thêm: Lê Văn C
→ ✅ Thành công (ở ghép)
→ Phòng 102: 2/3 người
```

### Ví dụ 3: Không thể thêm (đã đủ)
```
Phòng 103: Có 2 người (2/2 người)
Thêm: Phạm Văn D
→ ⚠️ Phòng 103 đã đủ người!
   Hiện có: 2/2 người
   Vui lòng chọn phòng khác.
```

### Ví dụ 4: Không thể thêm (đã đặt cọc)
```
Phòng 104: Đã đặt cọc (0/2 người)
Thêm: Hoàng Văn E
→ ⚠️ Phòng 104 đã được đặt cọc!
   Không thể thêm người thuê.
   Vui lòng chọn phòng khác.
```

### Ví dụ 5: Tạo phòng với số người tối đa
```
Tạo phòng mới:
- Tên: Phòng 201
- Giá: 2,000,000đ
- Diện tích: 25m²
- Số người tối đa: 3 người ← Thiết lập ở đây
→ ✅ Phòng 201 có thể ở tối đa 3 người
```

## 🔧 Files đã thay đổi

### 1. CreateTenantFragment.kt

**Thay đổi load phòng:**
```kotlin
// Đếm số người trong mỗi phòng
val phongVoiSoNguoi = allPhong.map { phong ->
    val soNguoiDangO = dbManager.khachThueDao.layTheoPhong(phong.maPhong)
        .count { it.trangThai == "dang_o" }
    Pair(phong, soNguoiDangO)
}

// Lọc phòng khi thêm mới
danhSachPhong = phongVoiSoNguoi
    .filter { (phong, soNguoi) ->
        phong.trangThai != "dat_coc" && soNguoi < phong.soNguoiToiDa
    }
    .map { it.first }
```

**Thay đổi validation:**
```kotlin
// Kiểm tra phòng đã đặt cọc
if (phong.trangThai == "dat_coc") {
    Toast.makeText(context, "⚠️ Phòng đã được đặt cọc!", Toast.LENGTH_LONG).show()
    return@launch
}

// Kiểm tra phòng đã đủ người
if (soNguoiDangO >= phong.soNguoiToiDa) {
    Toast.makeText(context, "⚠️ Phòng đã đủ người!", Toast.LENGTH_LONG).show()
    return@launch
}
```

**Hiển thị trạng thái phòng:**
```kotlin
val status = when {
    phong.trangThai == "dat_coc" -> " (Đã đặt cọc)"
    soNguoiDangO >= phong.soNguoiToiDa -> " (Đã đủ ${phong.soNguoiToiDa} người)"
    phong.trangThai == "da_thue" -> " (${soNguoiDangO}/${phong.soNguoiToiDa} người)"
    else -> " (Trống)"
}
```

### 2. CreateRoomFragment.kt

**Đã có sẵn:**
- Trường "Số người tối đa" (etMaxTenant)
- Lưu vào database: `soNguoiToiDa`
- Mặc định: 1 người

**Không cần thay đổi gì thêm**

## ✅ Đã test

- ✅ Thêm người vào phòng trống → Thành công
- ✅ Thêm người ở ghép (chưa đủ) → Thành công
- ✅ Thêm người vào phòng đã đủ → Cảnh báo
- ✅ Thêm người vào phòng đã đặt cọc → Cảnh báo
- ✅ Chỉnh sửa người thuê → Hiển thị tất cả phòng
- ✅ Hiển thị số người/tối đa trong danh sách
- ✅ Tạo phòng với số người tối đa → Lưu thành công

## 🎯 Lợi ích

### 1. Quản lý chặt chẽ
- Không cho phép vượt quá số người quy định
- Tránh xung đột giữa đặt cọc và thuê mới
- Đảm bảo không gian sống hợp lý

### 2. Thông tin rõ ràng
- Hiển thị số người hiện tại/tối đa
- Hiển thị trạng thái phòng chi tiết
- Người dùng dễ dàng đưa ra quyết định

### 3. Trải nghiệm tốt
- Chỉ hiển thị phòng có thể chọn
- Cảnh báo rõ ràng khi không thể thêm
- Giảm thiểu lỗi nhập liệu

### 4. Linh hoạt
- Có thể thiết lập số người tối đa cho mỗi phòng
- Phù hợp với nhiều loại phòng khác nhau
- Dễ dàng mở rộng trong tương lai

## 📝 Lưu ý

1. **Số người tối đa mặc định là 1**
   - Khi tạo phòng mới, nhớ điều chỉnh nếu cần

2. **Khi chỉnh sửa người thuê**
   - Có thể chuyển sang phòng khác
   - Hệ thống vẫn kiểm tra phòng mới

3. **Khi xóa người thuê**
   - Số người trong phòng giảm đi
   - Phòng có thể nhận người mới

4. **Trạng thái phòng**
   - "trong": Chưa có ai
   - "dat_coc": Đã đặt cọc, chờ vào ở
   - "da_thue": Đã có người thuê (có thể chưa đủ)

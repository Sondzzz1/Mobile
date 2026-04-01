# CẢI TIẾN QUẢN LÝ DỊCH VỤ - CHO PHÉP TRÙNG TÊN

## 🎯 VẤN ĐỀ

Bạn muốn có thể tạo nhiều dịch vụ cùng tên nhưng giá khác nhau:
- Internet 100,000đ
- Internet 150,000đ
- Internet 200,000đ

## ✅ GIẢI PHÁP ĐÃ THỰC HIỆN

### 1. CHO PHÉP TRÙNG TÊN, KHÁC GIÁ
**Logic mới:**
- ✅ Cho phép: "Internet 100,000đ" và "Internet 150,000đ" (cùng tên, khác giá)
- ❌ Không cho phép: "Internet 100,000đ" và "Internet 100,000đ" (trùng cả tên và giá)

**Code kiểm tra:**
```kotlin
val daTonTai = dbManager.dichVuDao.layTatCa()
    .any { it.maNha == maNha && it.tenDichVu == ten && it.donGia == donGia && it.maDichVu != maDichVuEdit }

if (daTonTai) {
    Toast.makeText(context, "⚠️ Đã tồn tại dịch vụ '$ten' với giá ${donGia.toLong()}đ", Toast.LENGTH_LONG).show()
    return
}
```

### 2. HIỂN THỊ RÕ RÀNG
**Trước:**
```
Internet
100,000đ
```

**Sau:**
```
Internet - 100,000đ
100,000đ/tháng
```

**Code:**
```kotlin
holder.tvTenDichVu.text = "${dv.tenDichVu} - ${fmt.format(dv.donGia.toLong())}đ"
holder.tvDonGia.text = "${fmt.format(dv.donGia.toLong())}đ/${dv.donVi.ifEmpty { "lần" }}"
```

### 3. ĐƠN VỊ MẶC ĐỊNH
- Nếu không nhập đơn vị → Tự động điền "lần"
- Giúp hiển thị đầy đủ: "100,000đ/lần"

## 📋 VÍ DỤ SỬ DỤNG

### Tạo nhiều gói Internet:
1. Tạo dịch vụ:
   - Tên: Internet
   - Đơn vị: tháng
   - Đơn giá: 100000
   - Loại: Khác
   → Lưu thành công ✓

2. Tạo dịch vụ:
   - Tên: Internet
   - Đơn vị: tháng
   - Đơn giá: 150000
   - Loại: Khác
   → Lưu thành công ✓

3. Tạo dịch vụ:
   - Tên: Internet
   - Đơn vị: tháng
   - Đơn giá: 100000 (trùng với #1)
   - Loại: Khác
   → ⚠️ Cảnh báo: "Đã tồn tại dịch vụ 'Internet' với giá 100000đ"

### Danh sách hiển thị:
```
Internet - 100,000đ
100,000đ/tháng
[Sửa] [Xóa]

Internet - 150,000đ
150,000đ/tháng
[Sửa] [Xóa]

Internet - 200,000đ
200,000đ/tháng
[Sửa] [Xóa]
```

## 🔧 FILES ĐÃ THAY ĐỔI

1. **CreateServiceFragment.kt**
   - Thêm kiểm tra trùng tên + giá
   - Đơn vị mặc định "lần"
   - Toast message rõ ràng hơn

2. **DichVuAdapter.kt**
   - Hiển thị: "Tên - Giá"
   - Format: "Giá/Đơn vị"
   - Tiếng Việt có dấu

3. **ServiceListFragment.kt**
   - Dialog xóa hiển thị giá
   - Toast message có dấu ✓


## 🧪 TEST CASES

### TEST 1: Tạo dịch vụ cùng tên, khác giá
**Các bước:**
1. Tạo "Internet" - 100,000đ
2. Tạo "Internet" - 150,000đ
3. Tạo "Internet" - 200,000đ

**Kết quả mong đợi:**
- Cả 3 đều lưu thành công
- Hiển thị 3 dịch vụ riêng biệt trong danh sách

### TEST 2: Tạo dịch vụ trùng tên và giá
**Các bước:**
1. Tạo "Internet" - 100,000đ
2. Tạo "Internet" - 100,000đ (trùng)

**Kết quả mong đợi:**
- Bước 1: Lưu thành công
- Bước 2: Hiện cảnh báo "⚠️ Đã tồn tại dịch vụ 'Internet' với giá 100000đ"
- Không lưu

### TEST 3: Hiển thị trong danh sách
**Các bước:**
1. Tạo 3 dịch vụ Internet với giá khác nhau
2. Vào danh sách dịch vụ

**Kết quả mong đợi:**
- Hiển thị rõ ràng:
  - "Internet - 100,000đ"
  - "Internet - 150,000đ"
  - "Internet - 200,000đ"
- Dễ phân biệt

### TEST 4: Sửa dịch vụ
**Các bước:**
1. Có "Internet - 100,000đ"
2. Sửa thành "Internet - 120,000đ"

**Kết quả mong đợi:**
- Lưu thành công
- Không bị cảnh báo trùng (vì đang sửa chính nó)

### TEST 5: Đơn vị mặc định
**Các bước:**
1. Tạo dịch vụ "Vệ sinh"
2. Không nhập đơn vị
3. Nhập giá: 50,000đ
4. Lưu

**Kết quả mong đợi:**
- Lưu thành công
- Hiển thị: "Vệ sinh - 50,000đ"
- Đơn giá: "50,000đ/lần"

## 💡 LƯU Ý QUAN TRỌNG

### Khi nào bị cảnh báo trùng?
Chỉ khi **CẢ 3 điều kiện** sau đều trùng:
1. Cùng nhà trọ (maNha)
2. Cùng tên dịch vụ (tenDichVu)
3. Cùng đơn giá (donGia)

### Các trường hợp được phép:
✅ Cùng tên, khác giá
✅ Cùng tên, khác nhà
✅ Cùng giá, khác tên
✅ Cùng tên, cùng giá, khác nhà

### Trường hợp KHÔNG được phép:
❌ Cùng nhà + Cùng tên + Cùng giá

## 🎨 CẢI TIẾN GIAO DIỆN

### Trước:
```
[Internet]
[100,000 d]
[thang]
[Khac]
```

### Sau:
```
[Internet - 100,000đ]
[100,000đ/tháng]
[tháng]
[Khác]
```

Rõ ràng và dễ phân biệt hơn nhiều!

## ✅ TỔNG KẾT

**Đã hoàn thành:**
1. ✅ Cho phép tạo dịch vụ cùng tên, khác giá
2. ✅ Kiểm tra trùng lặp (tên + giá)
3. ✅ Hiển thị rõ ràng: "Tên - Giá"
4. ✅ Đơn vị mặc định "lần"
5. ✅ Tiếng Việt có dấu
6. ✅ Toast message rõ ràng

**Lợi ích:**
- Linh hoạt quản lý nhiều gói dịch vụ
- Dễ phân biệt các gói khác nhau
- Tránh nhầm lẫn khi chọn dịch vụ
- UI/UX tốt hơn

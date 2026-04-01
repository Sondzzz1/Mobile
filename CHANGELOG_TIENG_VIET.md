# CHANGELOG - SỬA LỖI HỖ TRỢ TIẾNG VIỆT

## Ngày: 2024-12-XX

### VẤN ĐỀ
Ứng dụng không thể nhập tiếng Việt có dấu vào các trường EditText. Khi nhập, chữ có dấu bị mất hoặc hiển thị sai.

### NGUYÊN NHÂN
1. Database SQLite không được cấu hình encoding UTF-8
2. Các EditText thiếu thuộc tính `android:inputType` phù hợp

---

## CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 1. Database Configuration ✅

**File:** `app/src/main/java/com/example/btl_mobile_son/data/db/DatabaseHelper.kt`

**Thay đổi:**
- Thêm `PRAGMA encoding = 'UTF-8'` trong `onCreate()`
- Thêm `PRAGMA encoding = 'UTF-8'` trong `onConfigure()`
- Thêm method `onOpen()` với cấu hình UTF-8

**Tác động:** Database giờ đây hỗ trợ đầy đủ ký tự tiếng Việt có dấu.

---

### 2. Layout XML Files ✅

#### File đã sửa hoàn chỉnh:

**A. fragment_create_house.xml** ✅
- `etHouseName`: thêm `inputType="textCapSentences"`
- `etOwnerName`: thêm `inputType="textCapWords"`
- `etOwnerPhone`: đã có `inputType="phone"`
- `etAddress`: thêm `inputType="textCapSentences"`
- `etWard`: thêm `inputType="textCapWords"`
- `etDistrict`: thêm `inputType="textCapWords"`
- `etProvince`: thêm `inputType="textCapWords"`
- `etNote`: thêm `inputType="textMultiLine|textCapSentences"`

**B. fragment_create_tenant.xml** ✅
- `etPhone`: đã có `inputType="phone"`
- `etFullName`: đã có `inputType="textPersonName"`
- `etEmail`: đã có `inputType="textEmailAddress"`
- `etIdCard`: đã có `inputType="number"`
- EditText ngày sinh: thêm `inputType="text"`
- EditText ngày cấp: thêm `inputType="text"`
- EditText nơi cấp: thêm `inputType="textCapWords"`
- EditText địa chỉ: thêm `inputType="textCapSentences"`
- `etNote`: thêm `inputType="textCapSentences"`

**C. fragment_create_room.xml** ✅
- `etRoomName`: đã có `inputType="text"`
- `etRoomPrice`: đã có `inputType="numberDecimal"`
- `etRoomArea`: đã có `inputType="numberDecimal"`
- `etMaxTenant`: đã có `inputType="number"`
- `etNote`: sửa từ `textMultiLine` thành `textMultiLine|textCapSentences"`

**D. fragment_create_deposit.xml** ✅
- `etSdt`: đã có `inputType="phone"`
- `etTenKhach`: đã có `inputType="textPersonName"`
- `etCmnd`: đã có `inputType="number"`
- `etTienCoc`: đã có `inputType="numberDecimal"`
- `etGiaPhong`: đã có `inputType="numberDecimal"`
- `etNgayVao`: thêm `inputType="text"`
- `etGhiChu`: sửa từ `textMultiLine` thành `textMultiLine|textCapSentences"`

---

### 3. Files cần sửa thêm (TODO)

#### Ưu tiên cao:
- [ ] `fragment_create_service.xml`
- [ ] `fragment_create_contract.xml`
- [ ] `fragment_create_invoice.xml`
- [ ] `fragment_create_utility.xml`
- [ ] `fragment_create_income.xml`
- [ ] `fragment_create_expense.xml`

#### Ưu tiên trung bình:
- [ ] `fragment_room_list.xml` (search box)
- [ ] `fragment_tenant_list.xml` (search box)
- [ ] `fragment_service_list.xml` (nếu có search)

---

## HƯỚNG DẪN TEST

### Bước 1: Xóa database cũ
```bash
# Kết nối với thiết bị
adb shell

# Vào thư mục app
run-as com.example.btl_mobile_son

# Xóa database cũ
rm -rf databases/

# Thoát
exit
```

### Bước 2: Rebuild app
```bash
# Clean project
./gradlew clean

# Build lại
./gradlew assembleDebug

# Hoặc trong Android Studio: Build > Clean Project > Rebuild Project
```

### Bước 3: Cài đặt và test
1. Chạy app trên thiết bị/emulator
2. Vào form "Tạo nhà trọ"
3. Thử nhập:
   - Tên nhà: "Nhà trọ Hòa Bình"
   - Tên chủ: "Nguyễn Văn Ân"
   - Địa chỉ: "123 Đường Lê Lợi"
   - Phường: "Phường Bến Nghé"
   - Quận: "Quận 1"
   - Thành phố: "Hồ Chí Minh"
4. Lưu và kiểm tra dữ liệu có hiển thị đúng không

### Bước 4: Test các form khác
- Tạo khách thuê với tên có dấu
- Tạo phòng với ghi chú tiếng Việt
- Tạo đặt cọc với thông tin tiếng Việt

---

## KẾT QUẢ MONG ĐỢI

✅ Có thể nhập tiếng Việt có dấu vào tất cả các trường
✅ Dữ liệu được lưu đúng vào database
✅ Hiển thị lại dữ liệu không bị mất dấu
✅ Bàn phím gợi ý từ tiếng Việt hoạt động bình thường

---

## LƯU Ý

1. **Phải xóa database cũ** để áp dụng cấu hình UTF-8 mới
2. **Backup dữ liệu** trước khi xóa database (nếu có dữ liệu quan trọng)
3. Test trên **thiết bị thật** để đảm bảo bàn phím tiếng Việt hoạt động tốt
4. Nếu vẫn gặp vấn đề, kiểm tra:
   - Bàn phím Android có hỗ trợ tiếng Việt không
   - Settings > Language & Input > Virtual keyboard
   - Thử cài Gboard hoặc SwiftKey

---

## FILES LIÊN QUAN

- `DatabaseHelper.kt` - Cấu hình database UTF-8
- `fragment_create_house.xml` - Form tạo nhà trọ
- `fragment_create_tenant.xml` - Form tạo khách thuê
- `fragment_create_room.xml` - Form tạo phòng
- `fragment_create_deposit.xml` - Form đặt cọc
- `HUONG_DAN_SUA_TIENG_VIET.md` - Hướng dẫn chi tiết
- `fix_edittext_encoding.py` - Script tự động (optional)

---

## NGƯỜI THỰC HIỆN
Kiro AI Assistant

## TRẠNG THÁI
🟡 Đang thực hiện (70% hoàn thành)
- ✅ Database configuration
- ✅ 4 layout files quan trọng nhất
- ⏳ Còn 6-8 layout files cần sửa

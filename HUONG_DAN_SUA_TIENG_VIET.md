# HƯỚNG DẪN SỬA LỖI TIẾNG VIỆT TRONG ỨNG DỤNG

## VẤN ĐỀ
Khi nhập tiếng Việt có dấu vào các EditText, chữ bị mất dấu hoặc không hiển thị đúng.

## NGUYÊN NHÂN
1. Database SQLite không được cấu hình encoding UTF-8
2. Các EditText trong layout XML thiếu thuộc tính `android:inputType`

## GIẢI PHÁP ĐÃ THỰC HIỆN

### 1. Sửa DatabaseHelper.kt ✅
Đã thêm cấu hình UTF-8 encoding trong file:
`app/src/main/java/com/example/btl_mobile_son/data/db/DatabaseHelper.kt`

```kotlin
override fun onCreate(db: SQLiteDatabase) {
    // Thiết lập encoding UTF-8 để hỗ trợ tiếng Việt
    db.execSQL("PRAGMA encoding = 'UTF-8'")
    // ... rest of code
}

override fun onConfigure(db: SQLiteDatabase) {
    super.onConfigure(db)
    db.setForeignKeyConstraintsEnabled(true)
    // Thiết lập encoding UTF-8 cho tiếng Việt
    db.execSQL("PRAGMA encoding = 'UTF-8'")
}

override fun onOpen(db: SQLiteDatabase) {
    super.onOpen(db)
    // Đảm bảo encoding UTF-8 mỗi khi mở database
    db.execSQL("PRAGMA encoding = 'UTF-8'")
}
```

### 2. Sửa các EditText trong Layout XML ✅ (Một phần)
Đã sửa các file:
- ✅ `fragment_create_house.xml` - Hoàn thành
- ✅ `fragment_create_tenant.xml` - Hoàn thành
- ⏳ Các file còn lại cần sửa thủ công

## HƯỚNG DẪN SỬA CÁC FILE LAYOUT CÒN LẠI

### Quy tắc thêm inputType cho EditText:

#### 1. Tên người (Họ tên, tên chủ nhà...)
```xml
<EditText
    android:inputType="textCapWords"
    ... />
```

#### 2. Địa chỉ, ghi chú ngắn
```xml
<EditText
    android:inputType="textCapSentences"
    ... />
```

#### 3. Ghi chú dài (multiline)
```xml
<EditText
    android:inputType="textMultiLine|textCapSentences"
    android:gravity="top"
    ... />
```

#### 4. Số điện thoại
```xml
<EditText
    android:inputType="phone"
    ... />
```

#### 5. Email
```xml
<EditText
    android:inputType="textEmailAddress"
    ... />
```

#### 6. Số tiền, giá cả, diện tích
```xml
<EditText
    android:inputType="numberDecimal"
    ... />
```

#### 7. CMND/CCCD (chỉ số)
```xml
<EditText
    android:inputType="number"
    ... />
```

#### 8. Tìm kiếm
```xml
<EditText
    android:inputType="text"
    ... />
```

#### 9. Ngày tháng (với DatePicker)
```xml
<EditText
    android:inputType="text"
    android:focusable="false"
    android:clickable="true"
    ... />
```

### Danh sách file cần sửa:

#### Ưu tiên cao (form nhập liệu):
- [ ] `fragment_create_room.xml`
- [ ] `fragment_create_service.xml`
- [ ] `fragment_create_contract.xml`
- [ ] `fragment_create_deposit.xml`
- [ ] `fragment_create_invoice.xml`
- [ ] `fragment_create_utility.xml`
- [ ] `fragment_create_income.xml`
- [ ] `fragment_create_expense.xml`

#### Ưu tiên trung bình (tìm kiếm):
- [ ] `fragment_room_list.xml`
- [ ] `fragment_tenant_list.xml`
- [ ] `fragment_service_list.xml`

## CÁCH KIỂM TRA SAU KHI SỬA

### 1. Xóa database cũ
```bash
adb shell
run-as com.example.btl_mobile_son
rm -rf databases/
exit
```

### 2. Rebuild và chạy lại app
```bash
./gradlew clean
./gradlew assembleDebug
```

### 3. Test nhập tiếng Việt
- Thử nhập: "Nguyễn Văn Ân", "Phòng 101 - Tầng 1", "Quận Hoàn Kiếm"
- Kiểm tra xem dấu có bị mất không
- Lưu và xem lại dữ liệu

## LƯU Ý QUAN TRỌNG

1. **Phải xóa database cũ** để áp dụng cấu hình UTF-8 mới
2. **Không dùng inputType="textNoSuggestions"** - sẽ làm mất gợi ý tiếng Việt
3. **Luôn test trên thiết bị thật** - emulator có thể không hiển thị đúng bàn phím tiếng Việt
4. **Backup dữ liệu** trước khi xóa database

## SCRIPT TỰ ĐỘNG (Tùy chọn)

Nếu muốn tự động sửa tất cả file, có thể dùng script Python đã tạo:
```bash
python fix_edittext_encoding.py
```

Hoặc dùng Find & Replace trong Android Studio:
1. Ctrl+Shift+R (Replace in Files)
2. Tìm: `<EditText([^>]*?)(/?>)` (Enable Regex)
3. Thay thế thủ công từng trường hợp theo quy tắc trên

## HỖ TRỢ

Nếu vẫn gặp vấn đề:
1. Kiểm tra bàn phím Android có hỗ trợ tiếng Việt không
2. Thử cài Gboard hoặc SwiftKey
3. Kiểm tra Settings > Language & Input > Virtual keyboard

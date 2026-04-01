# Sửa lỗi các ô nhập liệu không thể nhập được

## Vấn đề
Các ô nhập liệu cho tỉnh, huyện, xã, nơi làm việc, nơi cấp, ngày cấp trong các form tạo khách thuê và hợp đồng không thể nhập được vì đang sử dụng `Spinner` (dropdown) nhưng không có dữ liệu.

## Giải pháp
Đã chuyển các `Spinner` không có dữ liệu thành `EditText` để người dùng có thể nhập trực tiếp.

## Các file đã sửa

### 1. fragment_create_tenant.xml
Đã thay đổi các trường sau từ `Spinner` sang `EditText`:
- ✅ Nơi làm việc (Workplace) - thêm ID `etWorkplace`
- ✅ Ngày sinh (Date of Birth) - thêm ID `etDob`
- ✅ Ngày cấp (Issue Date) - thêm ID `etIssueDate`
- ✅ Nơi cấp (Issue Place) - thêm ID `etIssuePlace`
- ✅ Tỉnh/Thành phố (Province) - thêm ID `etProvince`
- ✅ Quận/Huyện (District) - thêm ID `etDistrict`
- ✅ Xã/Phường (Ward) - thêm ID `etWard`
- ✅ Địa chỉ chi tiết (Detailed Address) - thêm ID `etDetailedAddress`

### 2. fragment_create_contract.xml
Đã thay đổi các trường sau từ `Spinner` sang `EditText`:
- ✅ Nơi làm việc (Workplace) - thêm ID `etWorkplace`
- ✅ Ngày cấp (Issue Date) - thêm ID `etIssueDate`, thêm inputType
- ✅ Nơi cấp (Issue Place) - thêm ID `etIssuePlace`, thêm inputType
- ✅ Tỉnh/Thành phố (Province) - thêm ID `etProvince`
- ✅ Quận/Huyện (District) - thêm ID `etDistrict` (mới thêm)
- ✅ Xã/Phường (Ward) - thêm ID `etWard`
- ✅ Địa chỉ chi tiết (Detailed Address) - thêm ID `etDetailedAddress`, thêm inputType

## Các trường đã thêm thuộc tính

Tất cả các `EditText` mới đã được thêm:
- `android:id` - để có thể truy cập từ code Kotlin
- `android:inputType` - để hiển thị bàn phím phù hợp
  - `textCapWords` - cho tỉnh, huyện, xã, nơi làm việc, nơi cấp
  - `textCapSentences` - cho địa chỉ chi tiết
  - `text` - cho ngày cấp
- `android:hint` - gợi ý cho người dùng
- `android:paddingHorizontal="12dp"` - padding ngang

## Lưu ý cho developer

Các file Fragment Kotlin tương ứng cần được cập nhật để xử lý các trường mới:
1. `CreateTenantFragment.kt` - cần thêm code xử lý các trường mới
2. `CreateContractFragment.kt` - cần thêm code xử lý các trường mới

Ví dụ code cần thêm:
```kotlin
val etWorkplace = view.findViewById<EditText>(R.id.etWorkplace)
val etProvince = view.findViewById<EditText>(R.id.etProvince)
val etDistrict = view.findViewById<EditText>(R.id.etDistrict)
val etWard = view.findViewById<EditText>(R.id.etWard)
val etIssueDate = view.findViewById<EditText>(R.id.etIssueDate)
val etIssuePlace = view.findViewById<EditText>(R.id.etIssuePlace)
val etDetailedAddress = view.findViewById<EditText>(R.id.etDetailedAddress)
```

## Kết quả
Sau khi sửa, người dùng có thể:
- ✅ Nhập trực tiếp tỉnh/thành phố
- ✅ Nhập trực tiếp quận/huyện
- ✅ Nhập trực tiếp xã/phường
- ✅ Nhập trực tiếp nơi làm việc
- ✅ Nhập trực tiếp nơi cấp CMND/CCCD
- ✅ Nhập trực tiếp ngày cấp
- ✅ Nhập trực tiếp địa chỉ chi tiết

## Các Spinner còn lại (không thay đổi)
Các Spinner sau vẫn giữ nguyên vì chúng cần chọn từ danh sách có sẵn:
- Chọn nhà (spinnerHouse)
- Chọn phòng (spinnerRoom)
- Chu kỳ thanh toán (spinnerPaymentCycle)
- Ngày chốt sổ (spinnerClosingDate)
- Loại dịch vụ (spinnerLoai)
- Danh mục thu/chi (spinnerDanhMuc)
- Phương thức thanh toán (spinnerPhuongThuc)

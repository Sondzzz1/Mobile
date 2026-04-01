# Sửa lỗi CreateInvoiceFragment.kt

## Lỗi gặp phải
```
Argument type mismatch: actual type is 'com.example.btl_mobile_son.<anonymous>', but 'kotlin.Int' was expected.
```

## Nguyên nhân
1. Code `DatePickerDialog` bị thiếu và không hoàn chỉnh
2. Thiếu code load danh sách nhà trọ
3. Sử dụng sai tên trường `maKhachThue` thay vì `maKhach`

## Các thay đổi đã thực hiện

### 1. Sửa DatePickerDialog
Đã thêm code hoàn chỉnh cho DatePickerDialog để chọn tháng/năm:
```kotlin
etMonth.setOnClickListener {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        requireContext(),
        { _, year, month, _ ->
            thangChon = month + 1
            namChon = year
            etMonth.setText("$thangChon/$namChon")
        },
        namChon,
        thangChon - 1,
        1
    ).show()
}
```

### 2. Thêm code load danh sách nhà trọ
Đã thêm code để load danh sách nhà trọ khi fragment được tạo:
```kotlin
CoroutineScope(Dispatchers.IO).launch {
    danhSachNha = dbManager.nhaTroDao.layTatCa()
    
    withContext(Dispatchers.Main) {
        if (danhSachNha.isEmpty()) {
            spinnerHouse.adapter = ArrayAdapter(...)
        } else {
            spinnerHouse.adapter = ArrayAdapter(...)
        }
        
        // Listener cho nhà - load phòng
        spinnerHouse.onItemSelectedListener = ...
    }
}
```

### 3. Sửa tên trường từ maKhachThue thành maKhach
Đã sửa lỗi tham chiếu sai tên trường trong model HopDong:
```kotlin
// Trước: hopDong.maKhachThue (SAI)
// Sau: hopDong.maKhach (ĐÚNG)
val khachThue = dbManager.khachThueDao.layTheoMa(hopDong.maKhach)
```

### 4. Thêm xử lý checkbox giảm giá
Đã thêm code xử lý khi người dùng check/uncheck giảm giá:
```kotlin
cbDiscount.setOnCheckedChangeListener { _, isChecked ->
    layoutDiscount.visibility = if (isChecked) View.VISIBLE else View.GONE
    if (!isChecked) {
        etDiscount.setText("")
    }
}
```

### 5. Thêm chức năng xem trước hóa đơn
Đã thêm code cho nút "Xem trước" để hiển thị preview hóa đơn:
```kotlin
btnPreview.setOnClickListener {
    // Validate dữ liệu
    // Tính toán tiền phòng, dịch vụ, giảm giá
    // Hiển thị preview
    layoutPreview.visibility = View.VISIBLE
}
```

### 6. Cập nhật logic tạo hóa đơn
Đã cập nhật để xử lý giảm giá khi tạo hóa đơn:
```kotlin
val giamGia = etDiscount.text.toString().toDoubleOrNull() ?: 0.0
val tongTien = hopDong.giaThueThang + tongTienDichVu - giamGia

val hoaDon = HoaDon(
    ...
    giamGia = giamGia,
    tongTien = tongTien,
    ...
)
```

## Kết quả
✅ Build thành công
✅ Không còn lỗi compile
✅ Chức năng tạo hóa đơn hoạt động đầy đủ:
  - Chọn nhà trọ
  - Chọn phòng (chỉ phòng đang thuê)
  - Chọn hợp đồng (chỉ hợp đồng đang thuê)
  - Chọn tháng/năm
  - Xem trước hóa đơn
  - Áp dụng giảm giá (tùy chọn)
  - Tạo hóa đơn

## Lưu ý
Còn một số warning về deprecated methods (`onBackPressed()`, `Locale` constructor) nhưng không ảnh hưởng đến chức năng. Có thể sửa sau nếu cần.

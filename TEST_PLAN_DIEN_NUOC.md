# KẾ HOẠCH KIỂM TRA CHỨC NĂNG ĐIỆN NƯỚC

## ✅ BUILD STATUS
**BUILD SUCCESSFUL** - Không có lỗi compilation!

## 🧪 CÁC TRƯỜNG HỢP CẦN KIỂM TRA

### TEST 1: Kiểm tra trùng lặp
**Mục đích:** Đảm bảo hệ thống cảnh báo khi nhập trùng chỉ số

**Các bước:**
1. Vào "Chỉ số điện nước" → Nhấn "Thêm mới"
2. Chọn Phòng 101, Loại: Điện, Tháng: 3, Năm: 2026
3. Nhập chỉ số mới: 100, Đơn giá: 3000
4. Nhấn "Lưu" → Chọn "Để sau"
5. Nhấn "Thêm mới" lại
6. Chọn lại Phòng 101, Loại: Điện, Tháng: 3, Năm: 2026
7. Nhập chỉ số mới: 150
8. Nhấn "Lưu"

**Kết quả mong đợi:**
- Hiện dialog: "⚠️ Đã tồn tại chỉ số"
- Message: "Phòng này đã có chỉ số điện tháng 3/2026. Bạn có muốn tiếp tục không?"
- 2 nút: "Tiếp tục" và "Hủy"

### TEST 2: Hiển thị tên phòng
**Mục đích:** Kiểm tra hiển thị tên phòng đầy đủ

**Các bước:**
1. Tạo nhà "Nhà A"
2. Tạo phòng "Phòng 101" trong Nhà A
3. Nhập chỉ số điện cho Phòng 101
4. Quay lại danh sách chỉ số

**Kết quả mong đợi:**
- Hiển thị: "Phòng 101 - Nhà A"
- KHÔNG hiển thị: "Phong 1" (ID)

### TEST 3: Bộ lọc tháng/năm
**Mục đích:** Kiểm tra chức năng lọc theo tháng/năm

**Các bước:**
1. Nhập chỉ số cho tháng 1/2026
2. Nhập chỉ số cho tháng 2/2026
3. Nhập chỉ số cho tháng 3/2026
4. Vào danh sách chỉ số
5. Chọn Spinner tháng: "Tháng 1"
6. Chọn Spinner tháng: "Tháng 2"
7. Nhấn nút "Tất cả"
8. Nhấn nút "Lọc"

**Kết quả mong đợi:**
- Bước 5: Chỉ hiển thị chỉ số tháng 1/2026
- Bước 6: Chỉ hiển thị chỉ số tháng 2/2026
- Bước 7: Hiển thị tất cả chỉ số (1, 2, 3/2026)
- Bước 8: Quay lại chế độ lọc (hiển thị tháng hiện tại)

### TEST 4: Chỉ số cũ tự động
**Mục đích:** Kiểm tra chỉ số cũ tự động điền và không thể sửa

**Các bước:**
1. Nhập chỉ số điện Phòng 101 tháng 2/2026: chỉ số mới = 100
2. Lưu thành công
3. Nhập chỉ số điện Phòng 101 tháng 3/2026
4. Kiểm tra trường "Chỉ số cũ"
5. Thử click vào trường "Chỉ số cũ"

**Kết quả mong đợi:**
- Bước 4: Trường "Chỉ số cũ" tự động hiển thị: 100
- Bước 5: Không thể click hoặc sửa (màu xám, chỉ đọc)

### TEST 5: Tích hợp hóa đơn
**Mục đích:** Kiểm tra dialog tạo hóa đơn sau khi lưu

**Các bước:**
1. Nhập chỉ số điện Phòng 101 tháng 3/2026
2. Nhấn "Lưu"
3. Kiểm tra dialog xuất hiện
4. Nhấn "Tạo hóa đơn"

**Kết quả mong đợi:**
- Bước 2: Toast "✓ Đã lưu chỉ số"
- Bước 3: Dialog "Tạo hóa đơn" xuất hiện
  - Title: "Tạo hóa đơn"
  - Message: "Đã nhập chỉ số điện tháng 3/2026. Tạo hóa đơn cho phòng này?"
  - 2 nút: "Tạo hóa đơn" và "Để sau"
- Bước 4: Chuyển sang màn hình CreateInvoiceFragment
  - Phòng đã được chọn sẵn: Phòng 101
  - Tháng/năm đã được điền: 3/2026

### TEST 6: Tích hợp hóa đơn - Để sau
**Mục đích:** Kiểm tra nút "Để sau"

**Các bước:**
1. Nhập chỉ số nước Phòng 102 tháng 3/2026
2. Nhấn "Lưu"
3. Nhấn "Để sau"

**Kết quả mong đợi:**
- Quay lại màn hình danh sách chỉ số
- Chỉ số đã được lưu thành công

### TEST 7: Validation chỉ số mới
**Mục đích:** Kiểm tra validation

**Các bước:**
1. Nhập chỉ số điện Phòng 101 tháng 4/2026
2. Chỉ số cũ: 100 (tự động)
3. Chỉ số mới: 50 (nhỏ hơn chỉ số cũ)
4. Nhấn "Lưu"

**Kết quả mong đợi:**
- Toast: "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ"
- Không lưu

### TEST 8: Xem tất cả lịch sử
**Mục đích:** Kiểm tra xem toàn bộ lịch sử

**Các bước:**
1. Nhập chỉ số cho nhiều tháng khác nhau (1/2026, 2/2026, 3/2026)
2. Vào danh sách chỉ số
3. Nhấn "Tất cả"
4. Kiểm tra danh sách

**Kết quả mong đợi:**
- Hiển thị tất cả chỉ số từ mọi tháng
- Sắp xếp theo thứ tự giảm dần (mới nhất trước)
- Spinner tháng/năm bị disable (màu xám)

## 📊 LOGIC KIỂM TRA

### Logic 1: Kiểm tra trùng lặp
```
IF (tồn tại chỉ số với: maPhong = X, loai = Y, thang = Z, nam = T)
  AND (không phải đang sửa chỉ số đó)
THEN
  Hiển thị cảnh báo
ELSE
  Lưu bình thường
```

### Logic 2: Tự động điền chỉ số cũ
```
Khi chọn phòng hoặc đổi loại:
  Lấy chỉ số tháng trước = layTheoThangNam(thang-1, nam)
  IF (tìm thấy chỉ số tháng trước)
  THEN
    etChiSoCu.setText(chiSoThangTruoc.chiSoMoi)
    etDonGia.setText(chiSoThangTruoc.donGia)
```

### Logic 3: Bộ lọc
```
IF (xemTatCa == true)
THEN
  Lấy tất cả chỉ số từ mọi tháng/năm
ELSE
  Lấy chỉ số theo thangHienTai và namHienTai
```

## ✅ CHECKLIST

- [ ] TEST 1: Kiểm tra trùng lặp
- [ ] TEST 2: Hiển thị tên phòng
- [ ] TEST 3: Bộ lọc tháng/năm
- [ ] TEST 4: Chỉ số cũ tự động
- [ ] TEST 5: Tích hợp hóa đơn - Tạo
- [ ] TEST 6: Tích hợp hóa đơn - Để sau
- [ ] TEST 7: Validation chỉ số mới
- [ ] TEST 8: Xem tất cả lịch sử

## 🎯 KẾT LUẬN

Build thành công! Tất cả các cải tiến đã được implement:
✅ Kiểm tra trùng lặp
✅ Hiển thị tên phòng đầy đủ
✅ Bộ lọc tháng/năm
✅ Khóa chỉ số cũ
✅ Tích hợp với hóa đơn

Bạn có thể chạy app và test theo các bước trên để xác nhận logic hoạt động đúng!

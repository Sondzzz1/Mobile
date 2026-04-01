# Hướng dẫn thêm người thuê và xử lý nhiều người cùng phòng

## Tính năng mới

### 1. Tự động tạo hợp đồng sau khi thêm người thuê

Khi bạn thêm người thuê mới vào phòng trống, hệ thống sẽ tự động hiện dialog để tạo hợp đồng ngay lập tức.

**Quy trình:**
1. Nhập thông tin người thuê (họ tên, SĐT, email, CMND...)
2. Chọn nhà và phòng
3. Nhấn "Lưu"
4. ✨ **Tự động hiện dialog tạo hợp đồng** với các thông tin:
   - Khách thuê: [Tên người vừa nhập]
   - SĐT: [Số điện thoại]
   - Phòng: [Tên phòng]
   - Ngày bắt đầu (có thể chọn)
   - Thời hạn (mặc định 12 tháng)
   - Tiền cọc
   - Giá thuê/tháng (tự động lấy từ giá phòng)

5. Nhấn "Tạo hợp đồng" để hoàn tất
6. Hệ thống sẽ:
   - ✓ Thêm khách thuê vào database
   - ✓ Tạo hợp đồng thuê phòng
   - ✓ Cập nhật trạng thái phòng thành "đã thuê"

### 2. Xử lý nhiều người cùng phòng (Ở ghép)

Hệ thống hỗ trợ nhiều người cùng thuê 1 phòng với 3 tùy chọn:

#### Trường hợp 1: Phòng trống (chưa có ai)
- Tự động hiện dialog tạo hợp đồng
- Người này sẽ là người thuê chính

#### Trường hợp 2: Phòng đã có người nhưng chưa đầy
Ví dụ: Phòng tối đa 3 người, hiện đang có 1 người

Hệ thống sẽ hiện dialog với 3 lựa chọn:

**A. Thêm ở ghép (không hợp đồng riêng)**
- Người mới sẽ được thêm vào phòng
- Không tạo hợp đồng riêng
- Phù hợp cho: người thân, bạn bè của người thuê chính

**B. Tạo hợp đồng mới**
- Người mới sẽ có hợp đồng riêng
- Phù hợp cho: người thuê độc lập, chia tiền riêng
- Dialog sẽ hiển thị: "⚠️ Hợp đồng cho người ở ghép"

**C. Hủy**
- Không thêm người này vào phòng

#### Trường hợp 3: Phòng đã đầy
Ví dụ: Phòng tối đa 2 người, đã có 2 người

- Hệ thống sẽ thông báo: "Phòng đã đầy (2/2 người)"
- Không cho phép thêm người mới

## Ví dụ thực tế

### Ví dụ 1: Thêm người thuê vào phòng trống
```
1. Nhập: Nguyễn Văn A, SĐT: 0912345678
2. Chọn: Nhà trọ ABC > Phòng 101 (trống)
3. Nhấn "Lưu"
4. → Hiện dialog tạo hợp đồng
5. Nhập: Thời hạn 12 tháng, Tiền cọc 2,000,000đ
6. Nhấn "Tạo hợp đồng"
7. ✓ Hoàn tất!
```

### Ví dụ 2: Thêm người ở ghép (không hợp đồng)
```
1. Phòng 102 đang có: Trần Văn B (người thuê chính)
2. Thêm: Trần Thị C (vợ của B)
3. Chọn: Nhà trọ ABC > Phòng 102
4. Nhấn "Lưu"
5. → Hiện dialog: "Phòng đang có 1 người. Tối đa: 2 người"
6. Chọn: "Thêm ở ghép"
7. ✓ Trần Thị C được thêm vào phòng 102 (không có hợp đồng riêng)
```

### Ví dụ 3: Thêm người ở ghép (có hợp đồng riêng)
```
1. Phòng 103 đang có: Lê Văn D (sinh viên)
2. Thêm: Phạm Văn E (sinh viên khác, chia tiền riêng)
3. Chọn: Nhà trọ ABC > Phòng 103
4. Nhấn "Lưu"
5. → Hiện dialog: "Phòng đang có 1 người. Tối đa: 3 người"
6. Chọn: "Tạo hợp đồng mới"
7. → Hiện dialog tạo hợp đồng với cảnh báo "⚠️ Hợp đồng cho người ở ghép"
8. Nhập thông tin hợp đồng
9. Nhấn "Tạo hợp đồng"
10. ✓ Phạm Văn E có hợp đồng riêng trong phòng 103
```

## Lợi ích

### 1. Tiết kiệm thời gian
- Không cần vào màn hình "Tạo hợp đồng" riêng
- Tạo hợp đồng ngay sau khi thêm người thuê

### 2. Linh hoạt
- Hỗ trợ nhiều người cùng phòng
- Có thể tạo hợp đồng riêng hoặc không tùy tình huống

### 3. An toàn
- Kiểm tra số người tối đa trong phòng
- Cảnh báo khi phòng đã đầy
- Hiển thị rõ ràng số người đang ở

### 4. Rõ ràng
- Thông báo chi tiết sau mỗi thao tác
- Phân biệt rõ người thuê chính và người ở ghép
- Hiển thị thông tin đầy đủ trong dialog

## Lưu ý quan trọng

1. **Số người tối đa**: Được thiết lập khi tạo phòng (trường `soNguoiToiDa`)
2. **Người ở ghép không hợp đồng**: Không tạo hóa đơn riêng, tính chung với người thuê chính
3. **Người ở ghép có hợp đồng**: Có hóa đơn riêng, quản lý độc lập
4. **Chỉnh sửa thông tin**: Khi chỉnh sửa thông tin người thuê, không hiện dialog tạo hợp đồng

## Code thay đổi

File: `CreateTenantFragment.kt`

### Các hàm mới:
1. `showAddRoommateDialog()` - Hiện dialog hỏi có muốn thêm người ở ghép
2. `showCreateContractDialog()` - Cải thiện dialog tạo hợp đồng với thông tin đầy đủ hơn

### Logic mới:
- Kiểm tra số người đang ở trong phòng
- So sánh với số người tối đa
- Hiển thị các tùy chọn phù hợp
- Xử lý từng trường hợp cụ thể

## Kiểm tra

Để test tính năng:
1. Tạo phòng với `soNguoiToiDa = 2` hoặc `3`
2. Thêm người thuê thứ nhất → Kiểm tra dialog tạo hợp đồng
3. Thêm người thuê thứ hai → Kiểm tra dialog ở ghép
4. Thêm người thuê thứ ba (nếu max = 3) → Kiểm tra dialog ở ghép
5. Thử thêm người thứ tư → Kiểm tra thông báo "Phòng đã đầy"

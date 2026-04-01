# Cải tiến thông tin khách thuê và dialog tạo hợp đồng

## ✨ Những gì đã cải thiện

### 1. Bổ sung thông tin khách thuê đầy đủ

**Các trường mới được thêm vào:**
- ✅ Ngày sinh
- ✅ Ngày cấp CMND/CCCD
- ✅ Nơi cấp CMND/CCCD
- ✅ Nơi làm việc
- ✅ Quê quán đầy đủ:
  - Tỉnh/Thành phố
  - Quận/Huyện
  - Xã/Phường
  - Địa chỉ chi tiết

### 2. Dialog tạo hợp đồng mới - Đẹp và chi tiết

**Trước đây:**
- Dialog đơn giản với 4 ô input không có label rõ ràng
- Không hiển thị thông tin khách thuê
- Không tính tự động ngày kết thúc
- Giao diện xấu, khó sử dụng

**Bây giờ:**
- ✅ Layout riêng biệt, thiết kế đẹp
- ✅ Hiển thị đầy đủ thông tin khách thuê:
  - Họ tên
  - Số điện thoại
  - CMND/CCCD
  - Tên phòng
- ✅ Tự động tính ngày kết thúc khi thay đổi thời hạn
- ✅ Hiển thị rõ ràng đơn vị (tháng, đ)
- ✅ Có trường ghi chú cho hợp đồng
- ✅ Cảnh báo rõ ràng nếu là hợp đồng cho người ở ghép
- ✅ Validation đầy đủ trước khi lưu

## 📊 Cấu trúc database mới

### Bảng khach_thue (đã cập nhật)
```sql
CREATE TABLE khach_thue (
    ma_khach INTEGER PRIMARY KEY AUTOINCREMENT,
    ho_ten TEXT NOT NULL,
    so_dien_thoai TEXT,
    email TEXT,
    so_cmnd TEXT,
    ngay_sinh INTEGER,           -- MỚI
    ngay_cap INTEGER,             -- MỚI
    noi_cap TEXT,                 -- MỚI
    noi_lam_viec TEXT,            -- MỚI
    tinh_thanh TEXT,              -- MỚI
    quan_huyen TEXT,              -- MỚI
    xa_phuong TEXT,               -- MỚI
    dia_chi_chi_tiet TEXT,        -- MỚI
    ma_phong INTEGER,
    trang_thai TEXT DEFAULT 'dang_o',
    ghi_chu TEXT,
    ngay_tao INTEGER DEFAULT 0,
    FOREIGN KEY (ma_phong) REFERENCES phong(ma_phong)
)
```

### Database version: 2 → 3
- Tự động migrate dữ liệu cũ
- Không mất dữ liệu hiện có
- Thêm các cột mới với giá trị mặc định

## 🎨 Giao diện dialog mới

### Cấu trúc
```
┌─────────────────────────────────────┐
│   TẠO HỢP ĐỒNG THUÊ PHÒNG          │
├─────────────────────────────────────┤
│ ┌─ THÔNG TIN KHÁCH THUÊ ─────────┐ │
│ │ Họ tên: Nguyễn Văn A           │ │
│ │ SĐT: 0987654321                │ │
│ │ CMND/CCCD: 001234567890        │ │
│ │ Phòng: Phòng 101               │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Ngày bắt đầu *                      │
│ [26/03/2026]              [📅]      │
│                                     │
│ Thời hạn hợp đồng *                 │
│ [12]                      tháng     │
│                                     │
│ Ngày kết thúc (tự động)             │
│ 26/03/2027                          │
│                                     │
│ Giá thuê/tháng *                    │
│ [1800000]                 đ         │
│                                     │
│ Tiền đặt cọc                        │
│ [0]                       đ         │
│                                     │
│ Ghi chú                             │
│ [                                 ] │
│ [                                 ] │
│                                     │
│ ⚠️ Hợp đồng cho người ở ghép       │
│                                     │
│     [HỦY]      [TẠO HỢP ĐỒNG]      │
└─────────────────────────────────────┘
```

## 🔧 Files đã thay đổi

### 1. Database
- `DatabaseHelper.kt` - Version 3, thêm 8 cột mới
- `KhachThue.kt` - Model với 8 trường mới
- `KhachThueDao.kt` - Cập nhật CRUD với trường mới

### 2. UI
- `dialog_create_contract.xml` - Layout mới cho dialog (MỚI)
- `fragment_create_tenant.xml` - Đã có các trường mới từ trước

### 3. Logic
- `CreateTenantFragment.kt` - Cập nhật:
  - Lấy giá trị từ các trường mới
  - Date picker cho ngày sinh, ngày cấp
  - Lưu đầy đủ thông tin
  - Dialog tạo hợp đồng mới đẹp hơn

## 📱 Cách sử dụng

### Thêm người thuê mới
1. Nhập đầy đủ thông tin cá nhân
2. Chọn ngày sinh, ngày cấp từ date picker
3. Nhập quê quán (tỉnh, huyện, xã, địa chỉ)
4. Nhập nơi làm việc, nơi cấp
5. Chọn nhà và phòng
6. Nhấn "Lưu"
7. → Dialog tạo hợp đồng hiện ra với thông tin đầy đủ
8. Điều chỉnh thông tin hợp đồng nếu cần
9. Nhấn "Tạo hợp đồng"

### Xem thông tin khách thuê
- Khi xem chi tiết khách thuê, sẽ hiển thị đầy đủ:
  - Thông tin cá nhân
  - Ngày sinh, CMND/CCCD, ngày cấp, nơi cấp
  - Nơi làm việc
  - Quê quán đầy đủ
  - Phòng đang thuê
  - Trạng thái

## ⚠️ Lưu ý quan trọng

### Migration database
- App sẽ tự động nâng cấp database từ version 2 lên 3
- Dữ liệu cũ được giữ nguyên
- Các trường mới sẽ có giá trị NULL hoặc rỗng cho dữ liệu cũ
- Người dùng cần cập nhật thông tin cho khách thuê cũ

### Validation
- Các trường có dấu * là bắt buộc
- Số điện thoại phải đúng định dạng
- Email phải đúng định dạng (nếu nhập)
- CMND/CCCD phải đúng định dạng (nếu nhập)
- Giá thuê phải > 0
- Thời hạn phải > 0

## 🎯 Lợi ích

1. **Quản lý thông tin đầy đủ hơn**
   - Lưu trữ đầy đủ thông tin khách thuê
   - Dễ dàng tra cứu khi cần

2. **Giao diện đẹp, chuyên nghiệp**
   - Dialog tạo hợp đồng rõ ràng, dễ sử dụng
   - Hiển thị đầy đủ thông tin trước khi tạo

3. **Tự động hóa**
   - Tự động tính ngày kết thúc
   - Tự động điền giá phòng
   - Giảm thiểu lỗi nhập liệu

4. **Trải nghiệm người dùng tốt**
   - Date picker cho ngày tháng
   - Validation rõ ràng
   - Thông báo chi tiết sau khi lưu

## 🚀 Tiếp theo

Có thể cải thiện thêm:
- Màn hình xem chi tiết khách thuê đẹp hơn
- Màn hình xem chi tiết hợp đồng đẹp hơn
- Thêm ảnh CMND/CCCD
- Thêm chữ ký điện tử
- Export hợp đồng ra PDF

# HƯỚNG DẪN CHẠY ỨNG DỤNG QUẢN LÝ NHÀ TRỌ

## YÊU CẦU HỆ THỐNG
- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 11 trở lên
- Android SDK API 24 (Android 7.0) trở lên
- Thiết bị Android hoặc Emulator API 24+

## CÁCH CÀI ĐẶT VÀ CHẠY

### Bước 1: Clone project
```
git clone <link_repository>
cd btl_mobile_son
```

### Bước 2: Mở Android Studio
- Chọn File > Open > chọn thư mục project

### Bước 3: Sync Gradle
- Android Studio sẽ tự động sync
- Hoặc nhấn "Sync Now" nếu có thông báo

### Bước 4: Chạy ứng dụng
- Kết nối thiết bị Android hoặc khởi động Emulator
- Nhấn nút Run (Shift + F10)

## CÁC CHỨC NĂNG CHÍNH

| Chức năng | Mô tả |
|-----------|-------|
| Quản lý Nhà trọ | Thêm, sửa, xóa thông tin nhà trọ |
| Quản lý Phòng | Quản lý phòng theo từng nhà |
| Quản lý Khách thuê | Thông tin khách thuê, CCCD |
| Hợp đồng | Tạo và quản lý hợp đồng thuê |
| Hóa đơn | Tạo hóa đơn hàng tháng |
| Điện nước | Ghi chỉ số điện nước |
| Thu chi | Quản lý giao dịch thu chi |
| Đặt cọc | Quản lý tiền đặt cọc giữ chỗ |

## CẤU TRÚC DATABASE

Database SQLite: `quan_ly_nha_tro.db`
- 10 bảng dữ liệu
- Tự động tạo khi chạy app lần đầu
- Lưu tại: `/data/data/com.example.btl_mobile_son/databases/`

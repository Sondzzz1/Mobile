# DATA MẪU - ỨNG DỤNG QUẢN LÝ NHÀ TRỌ

## MÔ TẢ

Thư mục này chứa script tạo dữ liệu mẫu để test ứng dụng.

## FILE SQL TẠO DỮ LIỆU MẪU

Chạy file `sample_data.sql` để chèn dữ liệu mẫu vào database.

## CÁCH SỬ DỤNG

1. Chạy ứng dụng lần đầu để tạo database
2. Dùng ADB để push dữ liệu mẫu:
```
adb shell
run-as com.example.btl_mobile_son
cd databases
sqlite3 quan_ly_nha_tro.db < /sdcard/sample_data.sql
```

# PHÂN TÍCH NGHIỆP VỤ CHI TIẾT - HỆ THỐNG QUẢN LÝ NHÀ TRỌ

## 📋 MỤC LỤC
1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Cấu trúc dữ liệu](#2-cấu-trúc-dữ-liệu)
3. [Quy trình nghiệp vụ](#3-quy-trình-nghiệp-vụ)
4. [Chi tiết từng chức năng](#4-chi-tiết-từng-chức-năng)
5. [Luồng dữ liệu](#5-luồng-dữ-liệu)
6. [Quy tắc nghiệp vụ](#6-quy-tắc-nghiệp-vụ)

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1. Mục đích
Hệ thống quản lý nhà trọ toàn diện, hỗ trợ chủ nhà trọ quản lý:
- Thông tin nhà trọ và phòng
- Khách thuê và hợp đồng
- Đặt cọc phòng
- Hóa đơn và dịch vụ
- Chỉ số điện nước
- Thu chi tài chính

### 1.2. Đối tượng sử dụng
- Chủ nhà trọ
- Người quản lý nhà trọ

### 1.3. Kiến trúc hệ thống
```
[Android App] → [DatabaseManager] → [SQLite Database]
     ↓
[Fragments] → [DAOs] → [Models]
```

---

## 2. CẤU TRÚC DỮ LIỆU

### 2.1. Sơ đồ quan hệ thực thể (ERD)

```
NhaTro (1) ----< (N) Phong
                      ↓ (1)
                      ↓
                    (N) KhachThue
                      ↓ (1)
                      ↓
                    (N) HopDong
                      ↓ (1)
                      ↓
                    (N) HoaDon
                      
Phong (1) ----< (N) DatCoc
Phong (1) ----< (N) ChiSoDienNuoc
Phong (1) ----< (N) DichVu
```

### 2.2. Các bảng dữ liệu

#### 2.2.1. NhaTro (Nhà trọ)
```kotlin
data class NhaTro(
    val maNha: Long,           // ID tự động
    val tenNha: String,        // Tên nhà trọ
    val diaChi: String,        // Địa chỉ đầy đủ
    val tenChuNha: String,     // Tên chủ nhà
    val soDienThoai: String,   // SĐT chủ nhà
    val ghiChu: String         // Ghi chú
)
```

**Ý nghĩa:** Đại diện cho một tòa nhà trọ/chung cư mini

#### 2.2.2. Phong (Phòng trọ)
```kotlin
data class Phong(
    val maPhong: Long,         // ID tự động
    val maNha: Long,           // FK → NhaTro
    val tenPhong: String,      // Tên/số phòng
    val dienTichM2: Float,     // Diện tích (m²)
    val giaCoBan: Double,      // Giá thuê cơ bản
    val trangThai: String,     // "trong" | "dat_coc" | "da_thue"
    val soNguoiToiDa: Int,     // Số người tối đa
    val ghiChu: String         // Ghi chú
)
```

**Trạng thái phòng:**
- `trong`: Phòng trống, sẵn sàng cho thuê
- `dat_coc`: Đã có người đặt cọc
- `da_thue`: Đang có người thuê

#### 2.2.3. KhachThue (Khách thuê)
```kotlin
data class KhachThue(
    val maKhach: Long,         // ID tự động
    val hoTen: String,         // Họ tên đầy đủ
    val soDienThoai: String,   // Số điện thoại
    val email: String,         // Email
    val soCmnd: String,        // CMND/CCCD
    val ngaySinh: Long?,       // Ngày sinh (timestamp)
    val ngayCap: Long?,        // Ngày cấp CMND (timestamp)
    val noiCap: String,        // Nơi cấp CMND
    val noiLamViec: String,    // Nơi làm việc
    val tinhThanh: String,     // Tỉnh/thành quê quán
    val quanHuyen: String,     // Quận/huyện
    val xaPhuong: String,      // Xã/phường
    val diaChiChiTiet: String, // Địa chỉ chi tiết
    val maPhong: Long?,        // FK → Phong
    val trangThai: String,     // "dang_o" | "da_chuyen_di"
    val ghiChu: String,        // Ghi chú
    val ngayTao: Long          // Ngày tạo
)
```

**Lưu ý:** Một phòng có thể có nhiều khách thuê (ở ghép)
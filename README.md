# 🏠 Ứng dụng Quản lý Nhà trọ - Mobile

Ứng dụng Android quản lý nhà trọ toàn diện, hỗ trợ quản lý phòng, khách thuê, hợp đồng, hóa đơn, điện nước và thu chi.

## 📱 Tính năng chính

### 1. Quản lý Nhà trọ & Phòng
- ✅ Quản lý nhiều nhà trọ
- ✅ Quản lý phòng: thêm, sửa, xóa
- ✅ Theo dõi trạng thái phòng (trống, đã thuê, đặt cọc)
- ✅ Quản lý số người tối đa/phòng

### 2. Quản lý Khách thuê
- ✅ Thông tin chi tiết khách thuê (CMND, địa chỉ, nơi làm việc...)
- ✅ Hỗ trợ nhiều người ở chung phòng
- ✅ Phân biệt người đại diện và thành viên
- ✅ Lưu lịch sử vào/ra

### 3. Quản lý Hợp đồng
- ✅ Tạo hợp đồng thuê phòng
- ✅ Quản lý thành viên hợp đồng
- ✅ Theo dõi trạng thái: đang thuê, hết hạn, đã hủy
- ✅ 1 phòng chỉ 1 hợp đồng hiệu lực

### 4. Quản lý Đặt cọc
- ✅ Đặt cọc phòng trước khi thuê
- ✅ Quản lý trạng thái: hiệu lực, đã chuyển hợp đồng, đã hủy, mất cọc, đã hoàn
- ✅ Tự động cập nhật trạng thái phòng

### 5. Quản lý Dịch vụ
- ✅ Thêm dịch vụ theo nhà (Internet, rác, gửi xe...)
- ✅ Hỗ trợ nhiều cách tính: theo phòng, theo người, một lần, theo tháng
- ✅ Cho phép dịch vụ cùng tên khác giá
- ✅ Ẩn/hiện dịch vụ không còn dùng

### 6. Quản lý Điện nước
- ✅ Nhập chỉ số điện/nước theo tháng
- ✅ Tự động điền chỉ số cũ từ tháng trước
- ✅ Kiểm tra trùng lặp (unique constraint)
- ✅ Tích hợp tạo hóa đơn sau khi nhập
- ✅ Lọc theo tháng/năm

### 7. Quản lý Hóa đơn
- ✅ Tạo hóa đơn tự động từ hợp đồng
- ✅ Tính toán tiền phòng + điện + nước + dịch vụ
- ✅ Hỗ trợ giảm giá
- ✅ Quản lý thanh toán linh hoạt (một phần, đủ)
- ✅ Trạng thái: chưa thanh toán, thanh toán một phần, đã thanh toán
- ✅ Unique constraint: 1 hợp đồng chỉ 1 hóa đơn/tháng

### 8. Quản lý Thu chi
- ✅ Ghi nhận khoản thu (tiền thuê, điện, nước, dịch vụ, đặt cọc...)
- ✅ Ghi nhận khoản chi (sửa chữa, bảo trì, lương...)
- ✅ Liên kết với hóa đơn, hợp đồng, đặt cọc
- ✅ Báo cáo tổng thu/chi, lợi nhuận
- ✅ Lọc theo tháng/năm, loại, danh mục

### 9. Dashboard & Báo cáo
- ✅ Tổng quan doanh thu
- ✅ Thống kê phòng trống/đã thuê
- ✅ Hóa đơn chưa thanh toán
- ✅ Báo cáo thu chi

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ:** Kotlin
- **Platform:** Android (API 24+)
- **Database:** SQLite
- **Architecture:** MVVM pattern
- **UI:** Material Design 3

## 📦 Cấu trúc dự án

```
app/src/main/java/com/example/btl_mobile_son/
├── data/
│   ├── model/          # Data models
│   ├── dao/            # Data Access Objects
│   └── db/             # Database helper
├── adapter/            # RecyclerView adapters
├── utils/              # Helper utilities
└── [Fragments]         # UI screens
```

## 🚀 Cài đặt

1. Clone repository:
```bash
git clone https://github.com/Sondzzz1/Mobile.git
```

2. Mở project bằng Android Studio

3. Sync Gradle và build project

4. Chạy trên emulator hoặc thiết bị thật

## 💡 Điểm nổi bật

### Xử lý tiền tệ chính xác
- ✅ Sử dụng `Long` thay vì `Double` để tránh lỗi làm tròn
- ✅ Helper functions format tiền: `1000000.formatCurrency()` → "1,000,000 đ"

### Database chặt chẽ
- ✅ Unique constraints ngăn dữ liệu trùng lặp
- ✅ Foreign keys đảm bảo tính toàn vẹn
- ✅ Migration tự động khi nâng cấp

### Logic nghiệp vụ chuẩn
- ✅ 1 phòng chỉ 1 hợp đồng hiệu lực
- ✅ Nhiều người ở chung phòng (HopDongThanhVien)
- ✅ Thanh toán hóa đơn linh hoạt (một phần, nhiều lần)
- ✅ Tự động cập nhật trạng thái phòng

## 📊 Database Schema

### Các bảng chính:
- `nha_tro` - Nhà trọ
- `phong` - Phòng
- `khach_thue` - Khách thuê
- `hop_dong` - Hợp đồng
- `hop_dong_thanh_vien` - Thành viên hợp đồng
- `dat_coc` - Đặt cọc
- `dich_vu` - Dịch vụ
- `chi_so_dien_nuoc` - Chỉ số điện nước
- `hoa_don` - Hóa đơn
- `chi_tiet_hoa_don` - Chi tiết hóa đơn
- `giao_dich` - Thu chi

## 📝 Tài liệu

Xem thêm tài liệu chi tiết trong thư mục gốc:
- `PHAN_TICH_NGHIEP_VU_CHI_TIET.md` - Phân tích nghiệp vụ
- `DANH_GIA_LOGIC_NGHIEP_VU.md` - Đánh giá logic
- `HOAN_THANH_100_PHAN_TRAM.md` - Báo cáo hoàn thành

## 👨‍💻 Tác giả

- **Tên:** [Tên của bạn]
- **Lớp:** [Lớp của bạn]
- **Trường:** [Trường của bạn]
- **Môn:** Lập trình di động (Mobile)

## 📄 License

Dự án này được tạo cho mục đích học tập - Bài tập lớn môn Mobile.

## 🙏 Lời cảm ơn

Cảm ơn giảng viên và các bạn đã hỗ trợ trong quá trình thực hiện dự án.

---

**⭐ Nếu thấy hữu ích, hãy cho project một star nhé!**

# CHỈ MỤC - PHÂN TÍCH NGHIỆP VỤ HỆ THỐNG QUẢN LÝ NHÀ TRỌ

## 📚 DANH SÁCH TÀI LIỆU

Tài liệu phân tích nghiệp vụ được chia thành 5 phần:

### 1. PHAN_TICH_NGHIEP_VU_CHI_TIET.md
**Nội dung:**
- Tổng quan hệ thống
- Mục đích và đối tượng sử dụng
- Kiến trúc hệ thống
- Sơ đồ ERD
- Bảng NhaTro, Phong, KhachThue (phần đầu)

### 2. PHAN_TICH_NGHIEP_VU_P2.md
**Nội dung:**
- Cấu trúc dữ liệu (tiếp)
- Các bảng: HopDong, DatCoc, HoaDon, ChiSoDienNuoc, DichVu, GiaoDich
- Quy trình nghiệp vụ chính
- Quy trình cho thuê phòng
- Quy trình đặt cọc
- Quy trình thêm khách thuê

### 3. PHAN_TICH_NGHIEP_VU_P3.md
**Nội dung:**
- Chi tiết chức năng quản lý nhà trọ
- Chi tiết chức năng quản lý phòng
- Chi tiết chức năng quản lý khách thuê
- Chi tiết chức năng quản lý đặt cọc
- Chi tiết chức năng quản lý hợp đồng
- Trạng thái phòng và chuyển đổi
- Logic ở ghép

### 4. PHAN_TICH_NGHIEP_VU_P4.md
**Nội dung:**
- Chi tiết chức năng quản lý dịch vụ
- Chi tiết chức năng quản lý điện nước
- Chi tiết chức năng quản lý hóa đơn
- Chi tiết chức năng quản lý thu chi
- Công thức tính toán
- Logic tự động điền
- Kiểm tra trùng lặp

### 5. PHAN_TICH_NGHIEP_VU_P5.md
**Nội dung:**
- Luồng dữ liệu đầy đủ
- Luồng chuyển trạng thái phòng
- Luồng ở ghép
- Quy tắc nghiệp vụ chi tiết
- Các trường hợp đặc biệt
- Validation rules
- Tổng kết

---

## 🎯 ĐỌC THEO THỨ TỰ

**Để hiểu toàn bộ hệ thống, đọc theo thứ tự:**

1. **PHAN_TICH_NGHIEP_VU_CHI_TIET.md** - Hiểu tổng quan
2. **PHAN_TICH_NGHIEP_VU_P2.md** - Hiểu cấu trúc dữ liệu
3. **PHAN_TICH_NGHIEP_VU_P3.md** - Hiểu chi tiết chức năng (phần 1)
4. **PHAN_TICH_NGHIEP_VU_P4.md** - Hiểu chi tiết chức năng (phần 2)
5. **PHAN_TICH_NGHIEP_VU_P5.md** - Hiểu quy tắc và luồng

---

## 📊 TÓM TẮT NHANH

### Các thực thể chính (9 bảng)
1. NhaTro - Nhà trọ
2. Phong - Phòng trọ
3. KhachThue - Khách thuê
4. HopDong - Hợp đồng thuê
5. DatCoc - Đặt cọc
6. HoaDon - Hóa đơn
7. ChiSoDienNuoc - Chỉ số điện nước
8. DichVu - Dịch vụ
9. GiaoDich - Thu chi

### Các chức năng chính (10 modules)
1. Quản lý nhà trọ (CRUD)
2. Quản lý phòng (CRUD + trạng thái)
3. Quản lý khách thuê (CRUD + ở ghép)
4. Quản lý hợp đồng (CRUD)
5. Quản lý đặt cọc (CRUD + cập nhật trạng thái phòng)
6. Quản lý dịch vụ (CRUD + cho phép trùng tên)
7. Quản lý điện nước (CRUD + tự động điền + kiểm tra trùng)
8. Quản lý hóa đơn (Tạo + Tính toán + In)
9. Quản lý thu (CRUD + báo cáo)
10. Quản lý chi (CRUD + báo cáo)

### Trạng thái phòng (3 trạng thái)
- `trong` - Phòng trống
- `dat_coc` - Đã đặt cọc
- `da_thue` - Đang cho thuê

### Quy tắc quan trọng
1. Số người trong phòng <= soNguoiToiDa
2. Chỉ số mới >= Chỉ số cũ
3. Một phòng chỉ 1 hợp đồng "dang_thue"
4. Cho phép dịch vụ trùng tên, khác giá
5. Không đặt cọc phòng đã thuê
6. Không thêm khách vào phòng đã đặt cọc

---

## 🔍 TÌM KIẾM NHANH

### Muốn tìm hiểu về...

**Cấu trúc dữ liệu?**
→ Đọc PHAN_TICH_NGHIEP_VU_P2.md

**Chức năng thêm khách thuê?**
→ Đọc PHAN_TICH_NGHIEP_VU_P3.md → Mục 4.3

**Logic ở ghép?**
→ Đọc PHAN_TICH_NGHIEP_VU_P3.md → Mục 4.3.1
→ Đọc PHAN_TICH_NGHIEP_VU_P5.md → Mục 5.3 và 7.1

**Chức năng điện nước?**
→ Đọc PHAN_TICH_NGHIEP_VU_P4.md → Mục 4.7

**Chức năng hóa đơn?**
→ Đọc PHAN_TICH_NGHIEP_VU_P4.md → Mục 4.8

**Quy tắc validation?**
→ Đọc PHAN_TICH_NGHIEP_VU_P5.md → Mục 6.8

**Trạng thái phòng?**
→ Đọc PHAN_TICH_NGHIEP_VU_P3.md → Mục 4.2.2
→ Đọc PHAN_TICH_NGHIEP_VU_P5.md → Mục 5.2 và 6.1

**Quy trình đầy đủ?**
→ Đọc PHAN_TICH_NGHIEP_VU_P2.md → Mục 3.1
→ Đọc PHAN_TICH_NGHIEP_VU_P5.md → Mục 5.1

---

## ✅ CHECKLIST ĐỌC HIỂU

- [ ] Đã hiểu 9 thực thể chính
- [ ] Đã hiểu quan hệ giữa các bảng
- [ ] Đã hiểu 3 trạng thái phòng
- [ ] Đã hiểu quy trình cho thuê phòng
- [ ] Đã hiểu logic ở ghép
- [ ] Đã hiểu cách tính hóa đơn
- [ ] Đã hiểu quy tắc validation
- [ ] Đã hiểu các trường hợp đặc biệt

---

**Tài liệu được tạo bởi: Kiro AI**
**Ngày: 2026-03-30**
**Phiên bản: 1.0**

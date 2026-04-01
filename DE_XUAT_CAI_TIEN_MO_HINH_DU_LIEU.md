# ĐỀ XUẤT CẢI TIẾN MÔ HÌNH DỮ LIỆU

## 🚨 CÁC VẤN ĐỀ NGHIÊM TRỌNG CẦN SỬA

### VẤN ĐỀ 1: Mô hình 1 phòng nhiều người ở - CHƯA CHUẨN

#### Hiện tại (SAI)
```kotlin
// KhachThue có maPhong - Liên kết cứng
data class KhachThue(
    val maKhach: Long,
    val maPhong: Long?,  // ❌ SAI: Liên kết trực tiếp
    ...
)

// HopDong chỉ có 1 maKhach
data class HopDong(
    val maHopDong: Long,
    val maPhong: Long,
    val maKhach: Long,   // ❌ SAI: Chỉ 1 người
    ...
)
```

**Vấn đề:**
- ❌ Không biểu diễn được: 1 phòng nhiều người, chỉ 1 người đại diện
- ❌ Không lưu được lịch sử vào/ra của từng người
- ❌ Không phân biệt được người đại diện vs người ở ghép
- ❌ Khi người chuyển đi, phải xóa hoặc update maPhong → mất lịch sử

#### Đề xuất (ĐÚNG)

**Bước 1: Tạo bảng trung gian HopDongThanhVien**

```kotlin
data class HopDongThanhVien(
    val maThanhVien: Long = 0,      // ID tự động
    val maHopDong: Long,             // FK → HopDong
    val maKhach: Long,               // FK → KhachThue
    val vaiTro: String,              // "dai_dien" | "thanh_vien"
    val ngayVaoO: Long,              // Ngày vào ở (timestamp)
    val ngayRoiDi: Long? = null,    // Ngày rời đi (null = đang ở)
    val trangThai: String = "dang_o", // "dang_o" | "da_roi"
    val ghiChu: String = ""
)
```

**Bước 2: Sửa KhachThue - Bỏ maPhong**

```kotlin
data class KhachThue(
    val maKhach: Long = 0,
    val hoTen: String,
    val soDienThoai: String = "",
    val email: String = "",
    val soCmnd: String = "",
    val ngaySinh: Long? = null,
    val ngayCap: Long? = null,
    val noiCap: String = "",
    val noiLamViec: String = "",
    val tinhThanh: String = "",
    val quanHuyen: String = "",
    val xaPhuong: String = "",
    val diaChiChiTiet: String = "",
    // ❌ BỎ: val maPhong: Long?
    // ❌ BỎ: val trangThai: String
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

**Bước 3: Giữ HopDong.maKhach làm người đại diện**

```kotlin
data class HopDong(
    val maHopDong: Long = 0,
    val maPhong: Long,               // FK → Phong
    val maKhach: Long,               // FK → KhachThue (người đại diện)
    val ngayBatDau: Long,
    val ngayKetThuc: Long,
    val giaThueThang: Double,
    val tienDatCoc: Double,
    val trangThai: String = "dang_thue", // "dang_thue" | "het_han" | "da_huy"
    val ghiChu: String = ""
)
```

#### Lợi ích của mô hình mới

✅ **1 hợp đồng có nhiều người ở**
```
HopDong #1 (Phòng 101)
├─ HopDongThanhVien #1: Nguyễn Văn A (đại diện)
├─ HopDongThanhVien #2: Trần Văn B (thành viên)
└─ HopDongThanhVien #3: Lê Văn C (thành viên)
```

✅ **Lưu lịch sử vào/ra**
```
Nguyễn Văn A:
- Phòng 101: 01/01/2024 → 31/03/2024 (đã rời)
- Phòng 102: 01/04/2024 → hiện tại (đang ở)
```

✅ **Phân biệt vai trò**
```
- Người đại diện: Ký hợp đồng, chịu trách nhiệm chính
- Thành viên: Ở ghép, không ký hợp đồng riêng
```

✅ **Quản lý linh hoạt**
```
- Người A rời đi → Cập nhật ngayRoiDi, trangThai = "da_roi"
- Người B vào thêm → Thêm HopDongThanhVien mới
- Không mất lịch sử
```

---

### VẤN ĐỀ 2: Rule "1 phòng nhiều hợp đồng" - GÂY RỐI

#### Hiện tại (SAI)
```
Phòng 101 đang có người A (có hợp đồng)
→ Người B vào ở ghép
→ Cho phép tạo hợp đồng mới cho B
→ Phòng 101 có 2 hợp đồng song song ❌
```

**Vấn đề:**
- ❌ Khó quản lý: Phòng có 2 hợp đồng, lập hóa đơn cho ai?
- ❌ Rối logic: Ai là người chịu trách nhiệm chính?
- ❌ Không đúng thực tế: Thuê phòng trọ thường 1 phòng 1 hợp đồng

#### Đề xuất (ĐÚNG)

**Rule cứng:**
```
1 phòng tại 1 thời điểm CHỈ CÓ 1 hợp đồng đang hiệu lực
```

**Quy trình:**
```
Phòng 101 trống
    ↓
Người A vào → Tạo HopDong #1
    ├─ HopDong.maKhach = A (đại diện)
    └─ HopDongThanhVien: A (vai trò: dai_dien)
    ↓
Người B vào ở ghép
    ├─ KHÔNG tạo hợp đồng mới
    └─ Thêm HopDongThanhVien: B (vai trò: thanh_vien)
    ↓
Người C vào ở ghép
    ├─ KHÔNG tạo hợp đồng mới
    └─ Thêm HopDongThanhVien: C (vai trò: thanh_vien)
```

**Kiểm tra khi tạo hợp đồng:**
```kotlin
fun kiemTraHopDongHienTai(maPhong: Long): HopDong? {
    return hopDongDao.layHopDongDangThue(maPhong)
}

// Khi tạo hợp đồng mới
val hopDongHienTai = kiemTraHopDongHienTai(maPhong)
if (hopDongHienTai != null) {
    throw Exception("Phòng đã có hợp đồng đang hiệu lực!")
}
```

**Lợi ích:**
✅ Dễ lập hóa đơn: 1 phòng → 1 hợp đồng → 1 hóa đơn
✅ Rõ ràng trách nhiệm: Người đại diện chịu trách nhiệm chính
✅ Đúng thực tế: Phù hợp với cách thuê phòng trọ

---

### VẤN ĐỀ 3: Đặt cọc thiếu vòng đời - CHƯA ĐẦY ĐỦ

#### Hiện tại (THIẾU)
```kotlin
data class DatCoc(
    val maDatCoc: Long,
    val maPhong: Long,
    val tenKhach: String,
    val tienDatCoc: Double,
    ...
    // ❌ THIẾU: trangThai
)
```

**Vấn đề:**
- ❌ Không biết đặt cọc còn hiệu lực không
- ❌ Không biết đã chuyển thành hợp đồng chưa
- ❌ Không biết đã hủy/hoàn cọc chưa
- ❌ 1 phòng có thể có nhiều bản ghi đặt cọc → rối

#### Đề xuất (ĐÚNG)

**Thêm trường trangThai:**
```kotlin
data class DatCoc(
    val maDatCoc: Long = 0,
    val maPhong: Long,
    val tenKhach: String,
    val soDienThoai: String = "",
    val soCmnd: String = "",
    val email: String = "",
    val tienDatCoc: Double,
    val giaPhong: Double = 0.0,
    val ngayDuKienVao: Long = 0,
    val trangThai: String = "hieu_luc", // ✅ THÊM MỚI
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

**Các trạng thái:**
```
"hieu_luc"           - Đặt cọc còn hiệu lực, chờ vào ở
"da_chuyen_hop_dong" - Đã ký hợp đồng chính thức
"da_huy"             - Đã hủy đặt cọc (khách không vào)
"mat_coc"            - Mất cọc (khách vi phạm)
"da_hoan"            - Đã hoàn cọc (chủ nhà hủy)
```

**Vòng đời đặt cọc:**
```
[Tạo đặt cọc] → trangThai = "hieu_luc"
    ↓
    ├─ [Ký hợp đồng] → trangThai = "da_chuyen_hop_dong"
    ├─ [Khách hủy] → trangThai = "da_huy" hoặc "mat_coc"
    └─ [Chủ nhà hủy] → trangThai = "da_hoan"
```

**Rule:**
```
1 phòng chỉ có 1 đặt cọc "hieu_luc" tại một thời điểm
```

**Kiểm tra:**
```kotlin
fun kiemTraDatCocHienTai(maPhong: Long): DatCoc? {
    return datCocDao.layTheoPhong(maPhong)
        .firstOrNull { it.trangThai == "hieu_luc" }
}

// Khi đặt cọc mới
val datCocHienTai = kiemTraDatCocHienTai(maPhong)
if (datCocHienTai != null) {
    throw Exception("Phòng đã có đặt cọc hiệu lực!")
}
```

**Lợi ích:**
✅ Quản lý đầy đủ vòng đời đặt cọc
✅ Tránh trùng lặp đặt cọc
✅ Lưu lịch sử đầy đủ
✅ Dễ báo cáo: Bao nhiêu cọc đang hiệu lực, bao nhiêu đã chuyển HĐ...

---

### VẤN ĐỀ 4: ERD chưa chuẩn - CẦN SỬA LẠI

#### Hiện tại (SAI)
```
NhaTro (1) ----< (N) Phong
                      ↓ (1)
                      ↓
                    (N) KhachThue  ❌ Liên kết cứng
                      ↓ (1)
                      ↓
                    (N) HopDong
```

**Vấn đề:**
- ❌ KhachThue → Phong: Liên kết cứng, không linh hoạt
- ❌ Không thể hiện được lịch sử chuyển phòng
- ❌ Không thể hiện được quan hệ nhiều-nhiều

#### Đề xuất (ĐÚNG)

**ERD mới:**
```
NhaTro (1) ----< (N) Phong
                      ↓ (1)
                      ├────< (N) HopDong
                      │         ↓ (1)
                      │         ├────< (N) HoaDon
                      │         │
                      │         └────< (N) HopDongThanhVien
                      │                     ↑ (N)
                      │                     │
                      ├────< (N) DatCoc     │
                      │                     │
                      └────< (N) ChiSoDienNuoc
                                            │
KhachThue (1) ──────────────────────────────┘
```

**Quan hệ chi tiết:**
```
NhaTro 1 ----< N Phong
NhaTro 1 ----< N DichVu

Phong 1 ----< N HopDong
Phong 1 ----< N DatCoc
Phong 1 ----< N ChiSoDienNuoc

HopDong 1 ----< N HoaDon
HopDong 1 ----< N HopDongThanhVien

KhachThue 1 ----< N HopDongThanhVien

HopDong N ----< 1 KhachThue (người đại diện)
```

**Giải thích:**
- ✅ KhachThue không nối trực tiếp với Phong
- ✅ KhachThue nối với HopDong qua HopDongThanhVien
- ✅ 1 KhachThue có thể ở nhiều phòng (theo thời gian)
- ✅ 1 HopDong có nhiều KhachThue (qua HopDongThanhVien)
- ✅ Lưu được lịch sử đầy đủ

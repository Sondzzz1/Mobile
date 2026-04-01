# PHÂN TÍCH VÀ SỬA LỖI LOGIC NGHIỆP VỤ

## 📋 TỔNG QUAN

Tài liệu này phân tích chi tiết 8 vấn đề nghiêm trọng trong logic nghiệp vụ hiện tại và đưa ra giải pháp sửa chữa.

---

## ❌ VẤN ĐỀ 1: Logic "Để sau" khi thêm khách thuê vào phòng trống

### Hiện trạng
```kotlin
// CreateTenantFragment.kt - showCreateContractDialog()
.setNegativeButton("Hủy", null)  // Chỉ đóng dialog, không lưu gì
```

**Vấn đề:**
- Khi phòng trống, nếu chọn "Để sau" → dialog đóng → KHÔNG lưu khách thuê
- Nhưng trong code có đoạn lưu khách TRƯỚC KHI hiện dialog
- Dẫn đến: Khách được lưu vào DB nhưng KHÔNG có hợp đồng, KHÔNG có quan hệ với phòng
- Trạng thái phòng không rõ ràng

**Hậu quả:**
- Dữ liệu không nhất quán
- Khách thuê "lơ lửng" không thuộc phòng nào
- Vi phạm quy tắc: "Chỉ cho thuê phòng khi có hợp đồng"

### Giải pháp

**Phương án 1 (Khuyến nghị):** BẮT BUỘC tạo hợp đồng
```kotlin
// Không có nút "Để sau"
// Chỉ có: "Tạo hợp đồng" hoặc "Hủy"
// Nếu Hủy → KHÔNG lưu khách thuê
```

**Phương án 2:** Thêm trạng thái trung gian
```kotlin
// Nếu chọn "Để sau":
// 1. Lưu khách thuê với trangThai = "cho_ky_hop_dong"
// 2. Cập nhật phòng: trangThai = "cho_ky_hop_dong"
// 3. Tạo bản ghi DatCoc với trangThai = "cho_ky_hop_dong"
```

**Quyết định:** Sử dụng Phương án 1 - đơn giản và chặt chẽ hơn

---

## ❌ VẤN ĐỀ 2: Mâu thuẫn logic người ở ghép

### Hiện trạng

**Trong CreateTenantFragment:**
```kotlin
// Cho phép tạo hợp đồng mới cho người ở ghép
.setNegativeButton("Tạo hợp đồng mới") { _, _ ->
    showCreateContractDialog(khach, phong, isRoommate = true)
}
```

**Trong CreateContractFragment:**
```kotlin
// Không cho tạo hợp đồng nếu phòng đã có hợp đồng
if (maHopDongEdit <= 0) {
    val hopDongHienTai = dbManager.hopDongDao.layHopDongDangThue(maPhong)
    if (hopDongHienTai != null) {
        Toast.makeText(context, "Phòng đã có hợp đồng đang thuê", ...)
        return@launch
    }
}
```

**Vấn đề:**
- Hai chỗ đang mâu thuẫn nhau
- CreateTenantFragment cho phép tạo hợp đồng mới cho người ở ghép
- CreateContractFragment lại chặn không cho tạo

### Giải pháp

**Chốt quy tắc:**
```
1 phòng tại 1 thời điểm CHỈ CÓ 1 hợp đồng đang hiệu lực
Người vào sau = thành viên của hợp đồng đó (qua HopDongThanhVien)
KHÔNG tạo hợp đồng mới
```

**Sửa CreateTenantFragment:**
```kotlin
// Bỏ nút "Tạo hợp đồng mới"
// Chỉ có: "Thêm ở ghép" (thêm vào HopDongThanhVien)
.setPositiveButton("Thêm ở ghép") { _, _ ->
    // 1. Lưu khách thuê
    // 2. Lấy hợp đồng đang active của phòng
    // 3. Thêm vào HopDongThanhVien với vaiTro = "thanh_vien"
}
```

---

## ❌ VẤN ĐỀ 3: Sửa khách thuê cho đổi phòng không kiểm tra

### Hiện trạng
```kotlin
// CreateTenantFragment.kt - khi maKhach > 0 (edit mode)
// Không có kiểm tra gì khi đổi phòng
val khach = KhachThue(
    maKhach = maKhach,
    maPhong = phong.maPhong,  // Đổi phòng tự do
    ...
)
dbManager.khachThueDao.capNhat(khach)
```

**Vấn đề:**
- Không kiểm tra phòng mới có đủ chỗ không
- Không kiểm tra phòng mới có đang đặt cọc không
- Không kiểm tra phòng mới có hợp đồng active không
- Có thể vượt số người tối đa

### Giải pháp

**Quy tắc mới:**
```kotlin
// KHÔNG CHO đổi phòng qua chức năng "Sửa khách thuê"
// Muốn chuyển phòng phải:
// 1. Kết thúc hợp đồng cũ
// 2. Tạo hợp đồng mới ở phòng khác
```

**Hoặc nếu vẫn muốn cho đổi:**
```kotlin
// Kiểm tra đầy đủ:
if (phongMoi.maPhong != phongCu.maPhong) {
    // 1. Kiểm tra phòng mới không đặt cọc
    // 2. Kiểm tra phòng mới chưa đủ người
    // 3. Kiểm tra phòng mới có hợp đồng active không
    // 4. Cập nhật HopDongThanhVien
    // 5. Cập nhật trạng thái cả 2 phòng
}
```

**Quyết định:** KHÔNG CHO đổi phòng qua "Sửa khách thuê"

---

## ❌ VẤN ĐỀ 4: Sửa hợp đồng cho đổi phòng không kiểm tra trùng

### Hiện trạng
```kotlin
// CreateContractFragment.kt
// Chỉ kiểm tra khi tạo mới (maHopDongEdit <= 0)
// Khi sửa (maHopDongEdit > 0) không kiểm tra gì
```

**Vấn đề:**
- Khi sửa hợp đồng, nếu đổi sang phòng khác
- Phòng mới có thể đã có hợp đồng active
- Dẫn đến 1 phòng có 2 hợp đồng active

### Giải pháp

```kotlin
// Kiểm tra cả khi tạo mới VÀ khi sửa
val hopDongHienTai = dbManager.hopDongDao.layHopDongDangThue(maPhong)

if (maHopDongEdit > 0) {
    // Khi sửa: loại trừ chính bản ghi đang sửa
    if (hopDongHienTai != null && hopDongHienTai.maHopDong != maHopDongEdit) {
        Toast.makeText(context, "Phòng đã có hợp đồng đang thuê khác", ...)
        return@launch
    }
} else {
    // Khi tạo mới: không được có hợp đồng nào
    if (hopDongHienTai != null) {
        Toast.makeText(context, "Phòng đã có hợp đồng đang thuê", ...)
        return@launch
    }
}
```

---

## ❌ VẤN ĐỀ 5: Xóa đặt cọc mất lịch sử

### Hiện trạng
```kotlin
// DepositListFragment.kt
dbManager.datCocDao.xoa(maDatCoc)  // Xóa cứng
```

**Vấn đề:**
- Mất hoàn toàn lịch sử đặt cọc
- Không biết ai đã đặt cọc, bao giờ, bao nhiêu tiền
- Không theo dõi được mất cọc, hoàn cọc

### Giải pháp

```kotlin
// KHÔNG xóa, chỉ cập nhật trạng thái
fun huyDatCoc(maDatCoc: Long, lyDo: String) {
    val datCoc = dbManager.datCocDao.layTheoMa(maDatCoc)
    datCoc?.let {
        val trangThaiMoi = when(lyDo) {
            "khach_huy" -> "da_huy"
            "mat_coc" -> "mat_coc"
            "da_thue" -> "da_chuyen_hop_dong"
            else -> "da_hoan"
        }
        
        dbManager.datCocDao.capNhat(it.copy(trangThai = trangThaiMoi))
        
        // Cập nhật trạng thái phòng
        if (trangThaiMoi != "da_chuyen_hop_dong") {
            val phong = dbManager.phongDao.layTheoMa(it.maPhong)
            phong?.let { p ->
                dbManager.phongDao.capNhat(p.copy(trangThai = "trong"))
            }
        }
    }
}
```

---

## ❌ VẤN ĐỀ 6: Không kiểm tra trùng tên phòng trong cùng nhà

### Hiện trạng
```kotlin
// CreateRoomFragment.kt
// Không có kiểm tra trùng tên
dbManager.phongDao.them(phong)
```

**Vấn đề:**
- Cùng nhà có thể có 2 phòng tên "101"
- Gây nhầm lẫn khi lập hóa đơn, hợp đồng

### Giải pháp

**Thêm vào PhongDao:**
```kotlin
fun kiemTraTrungTen(maNha: Long, tenPhong: String, maPhongLoaiTru: Long = -1): Boolean {
    val cursor = if (maPhongLoaiTru > 0) {
        db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} " +
            "WHERE ma_nha = ? AND ten_phong = ? AND ma_phong != ?",
            arrayOf(maNha.toString(), tenPhong, maPhongLoaiTru.toString())
        )
    } else {
        db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} " +
            "WHERE ma_nha = ? AND ten_phong = ?",
            arrayOf(maNha.toString(), tenPhong)
        )
    }
    
    cursor.use {
        if (it.moveToFirst()) {
            return it.getInt(0) > 0
        }
    }
    return false
}
```

**Sử dụng trong CreateRoomFragment:**
```kotlin
// Trước khi lưu
if (dbManager.phongDao.kiemTraTrungTen(maNha, tenPhong, maPhong)) {
    Toast.makeText(
        context,
        "⚠️ Tên phòng '$tenPhong' đã tồn tại trong nhà này!\nVui lòng chọn tên khác.",
        Toast.LENGTH_LONG
    ).show()
    return@setOnClickListener
}
```

---

## ❌ VẤN ĐỀ 7: Xóa nhà trọ cascade nguy hiểm

### Hiện trạng
```kotlin
// NhaTroDao.kt
fun xoa(maNha: Long): Int {
    return db.delete(...)  // Xóa trực tiếp
}
```

**Vấn đề:**
- Nếu có CASCADE trong DB → mất toàn bộ phòng, hợp đồng, hóa đơn
- Nếu không có CASCADE → lỗi foreign key

### Giải pháp

**Phương án 1 (Khuyến nghị):** Không cho xóa nếu đã có dữ liệu
```kotlin
fun coThePhatSinhDuLieu(maNha: Long): Boolean {
    // Kiểm tra có phòng không
    val soPhong = db.rawQuery(
        "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PHONG} WHERE ma_nha = ?",
        arrayOf(maNha.toString())
    ).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }
    
    return soPhong > 0
}

// Trong HouseListFragment
if (dbManager.nhaTroDao.coThePhatSinhDuLieu(maNha)) {
    AlertDialog.Builder(requireContext())
        .setTitle("Không thể xóa")
        .setMessage(
            "Nhà trọ này đã có phòng và dữ liệu liên quan.\n\n" +
            "Không thể xóa để đảm bảo tính toàn vẹn dữ liệu."
        )
        .setPositiveButton("Đóng", null)
        .show()
    return
}
```

**Phương án 2:** Thêm trạng thái "ngung_hoat_dong"
```kotlin
data class NhaTro(
    ...
    val trangThai: String = "hoat_dong"  // "hoat_dong" | "ngung_hoat_dong"
)

// Thay vì xóa, đổi trạng thái
fun ngungHoatDong(maNha: Long) {
    val nha = layTheoMa(maNha)
    nha?.let {
        capNhat(it.copy(trangThai = "ngung_hoat_dong"))
    }
}
```

**Quyết định:** Sử dụng Phương án 1

---

## ❌ VẤN ĐỀ 8: Tìm khách theo SĐT để tái sử dụng không chắc chắn

### Hiện trạng
```kotlin
// CreateContractFragment.kt
var maKhach = -1L
val ds = dbManager.khachThueDao.timKiem(sdt)
maKhach = if (ds.isNotEmpty()) ds[0].maKhach
else dbManager.khachThueDao.them(...)
```

**Vấn đề:**
- SĐT có thể đổi
- SĐT có thể trùng (người thân)
- Một khách cũ quay lại với thông tin mới
- Ghép sai người

### Giải pháp

**Ưu tiên CMND/CCCD:**
```kotlin
fun timKhachTheoThongTin(sdt: String, cmnd: String, hoTen: String): KhachThue? {
    // Ưu tiên 1: Tìm theo CMND (chính xác nhất)
    if (cmnd.isNotEmpty()) {
        val cursor = db.query(
            DatabaseHelper.TABLE_KHACH_THUE,
            null,
            "so_cmnd = ?",
            arrayOf(cmnd),
            null, null, null, "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToKhachThue(it)
            }
        }
    }
    
    // Ưu tiên 2: Tìm theo SĐT + Họ tên (gần đúng)
    if (sdt.isNotEmpty() && hoTen.isNotEmpty()) {
        val cursor = db.query(
            DatabaseHelper.TABLE_KHACH_THUE,
            null,
            "so_dien_thoai = ? AND ho_ten = ?",
            arrayOf(sdt, hoTen),
            null, null, null, "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToKhachThue(it)
            }
        }
    }
    
    return null
}
```

**Hoặc cho người dùng chọn:**
```kotlin
// Hiển thị dialog danh sách khách có SĐT/CMND tương tự
// Cho người dùng chọn: "Đúng người này" hoặc "Tạo mới"
```

**Quyết định:** Ưu tiên CMND, nếu không có thì hỏi người dùng

---

## 📊 TỔNG KẾT ƯU TIÊN SỬA

### Mức độ Nghiêm trọng

| Vấn đề | Mức độ | Ưu tiên | Ảnh hưởng |
|--------|--------|---------|-----------|
| VĐ2: Mâu thuẫn ở ghép | 🔴 Cao | 1 | Dữ liệu sai, logic vỡ |
| VĐ1: "Để sau" không rõ | 🔴 Cao | 2 | Dữ liệu lơ lửng |
| VĐ4: Sửa HĐ không check | 🔴 Cao | 3 | 1 phòng nhiều HĐ |
| VĐ3: Đổi phòng tự do | 🟡 Trung bình | 4 | Vượt sức chứa |
| VĐ6: Trùng tên phòng | 🟡 Trung bình | 5 | Nhầm lẫn |
| VĐ5: Xóa đặt cọc | 🟢 Thấp | 6 | Mất lịch sử |
| VĐ7: Xóa nhà cascade | 🟢 Thấp | 7 | Mất dữ liệu |
| VĐ8: Tìm khách theo SĐT | 🟢 Thấp | 8 | Ghép sai người |

### Kế hoạch thực hiện

1. **Phase 1 - Sửa logic nghiệp vụ cốt lõi** (VĐ1, VĐ2, VĐ4)
2. **Phase 2 - Thêm validation** (VĐ3, VĐ6)
3. **Phase 3 - Cải thiện UX** (VĐ5, VĐ7, VĐ8)

---

## 🎯 HÀNH ĐỘNG TIẾP THEO

Tôi sẽ bắt đầu sửa từng vấn đề theo thứ tự ưu tiên.

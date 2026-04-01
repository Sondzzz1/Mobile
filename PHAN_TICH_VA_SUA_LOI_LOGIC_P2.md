# PHÂN TÍCH VÀ SỬA LỖI LOGIC NGHIỆP VỤ - PHẦN 2
## Dịch vụ, Hóa đơn, Điện nước, Giao dịch

## 📋 TỔNG QUAN

Tài liệu này phân tích chi tiết 10 vấn đề nghiệm trọng về logic nghiệp vụ liên quan đến:
- Quản lý dịch vụ
- Tạo hóa đơn
- Điện nước
- Giao dịch thu chi

---

## ❌ VẤN ĐỀ 1: Dịch vụ chỉ ở cấp nhà, chưa có cấp phòng

### Hiện trạng
```kotlin
// DichVu chỉ có maNha, không có maPhong
data class DichVu(
    val maDichVu: Long = 0,
    val maNha: Long,  // Chỉ gắn với nhà
    val tenDichVu: String,
    val donVi: String = "",
    val donGia: Double = 0.0,
    val loaiDichVu: String = "khac"
)
```

**Vấn đề:**
- Tất cả phòng trong nhà dùng chung danh sách dịch vụ
- Không thể có giá khác nhau cho từng phòng
- Ví dụ: Phòng 101 Internet 100k, Phòng 102 Internet 150k → không làm được
- Khi tạo hóa đơn phải nhớ chọn đúng, rất dễ sai

### Giải pháp

**Tạo bảng trung gian PhongDichVu:**
```kotlin
data class PhongDichVu(
    val maPhongDichVu: Long = 0,
    val maPhong: Long,
    val maDichVu: Long,
    val donGiaApDung: Long,  // Giá áp dụng cho phòng này
    val soLuong: Int = 1,
    val cachTinh: String = "theo_phong", // "theo_phong" | "theo_nguoi" | "mot_lan" | "theo_thang"
    val trangThai: String = "dang_ap_dung", // "dang_ap_dung" | "ngung_ap_dung"
    val ngayBatDau: Long = System.currentTimeMillis(),
    val ngayKetThuc: Long? = null,
    val ghiChu: String = ""
)
```

**Ý nghĩa:**
- `DichVu` = danh mục dịch vụ của nhà (template)
- `PhongDichVu` = dịch vụ thực tế áp dụng cho từng phòng
- Mỗi phòng có thể có giá khác nhau
- Có thể bật/tắt dịch vụ cho từng phòng

**Workflow:**
1. Tạo dịch vụ ở cấp nhà (DichVu) - là template
2. Khi cho thuê phòng, chọn dịch vụ nào áp dụng → tạo PhongDichVu
3. Khi tạo hóa đơn, lấy từ PhongDichVu của phòng đó

---

## ❌ VẤN ĐỀ 2: Rule trùng dịch vụ còn thiếu

### Hiện trạng
```kotlin
// Chỉ kiểm tra: cùng nhà + cùng tên + cùng giá
val daTonTai = danhSach.any { 
    it.tenDichVu == tenDichVu && it.donGia == donGia 
}
```

**Vấn đề:**
- Internet 100k/tháng vs Internet 100k/người → bị coi là trùng
- Thiếu kiểm tra đơn vị và cách tính

### Giải pháp

```kotlin
// Kiểm tra trùng đầy đủ
fun kiemTraTrungDichVu(
    maNha: Long,
    tenDichVu: String,
    donGia: Double,
    donVi: String,
    cachTinh: String,
    maDichVuLoaiTru: Long = -1
): Boolean {
    val danhSach = layTheoNha(maNha)
    return danhSach.any { dv ->
        dv.maDichVu != maDichVuLoaiTru &&
        dv.tenDichVu == tenDichVu &&
        dv.donGia == donGia &&
        dv.donVi == donVi &&
        dv.cachTinh == cachTinh
    }
}
```

**Bộ điều kiện trùng:**
- Cùng nhà
- Cùng tên
- Cùng giá
- Cùng đơn vị
- Cùng cách tính

---

## ❌ VẤN ĐỀ 3: Điện nước bị chồng giữa 2 module

### Hiện trạng
```kotlin
// DichVu có loaiDichVu = "dien" | "nuoc"
// Nhưng cũng có ChiSoDienNuoc riêng
```

**Vấn đề:**
- Điện/nước tồn tại ở 2 nơi
- Có thể tính tiền 2 lần nếu:
  - Nhập chỉ số điện/nước
  - Lại tick thêm dịch vụ điện/nước trong hóa đơn

### Giải pháp

**Chốt rule:**
```
1. Điện và Nước CHỈ tính từ chỉ số tiêu thụ (ChiSoDienNuoc)
2. Bảng DichVu KHÔNG chứa điện/nước
3. Loại bỏ loaiDichVu = "dien" | "nuoc"
4. Chỉ giữ loaiDichVu = "khac"
```

**Hoặc nếu muốn linh hoạt:**
```
1. Nếu phòng có nhập chỉ số → tính từ chỉ số
2. Nếu phòng không nhập chỉ số → có thể dùng dịch vụ điện/nước cố định
3. Khi tạo hóa đơn, kiểm tra:
   - Nếu có chỉ số tháng đó → tự động tính, không cho tick dịch vụ điện/nước
   - Nếu không có chỉ số → cho phép tick dịch vụ điện/nước
```

**Quyết định:** Sử dụng rule đơn giản - chỉ tính từ chỉ số

---

## ❌ VẤN ĐỀ 4: Nhập trùng chỉ số điện nước vẫn cho lưu

### Hiện trạng
```kotlin
// Có cảnh báo nhưng vẫn cho lưu
if (daTonTai) {
    AlertDialog.Builder(requireContext())
        .setMessage("Đã tồn tại chỉ số...")
        .setPositiveButton("Cập nhật") { ... }  // Cho phép tiếp tục
        .setNegativeButton("Hủy", null)
        .show()
}
```

**Vấn đề:**
- Có thể tạo 2 bản ghi chỉ số cho cùng kỳ
- Hóa đơn không biết lấy bản nào
- Dữ liệu không nhất quán

### Giải pháp

**Phương án 1 (Khuyến nghị):** Không cho lưu trùng
```kotlin
if (daTonTai) {
    val chiSoCu = dbManager.chiSoDienNuocDao.layTheoThangNam(thang, nam)
        .find { it.maPhong == maPhong && it.loai == loai }
    
    AlertDialog.Builder(requireContext())
        .setTitle("Đã tồn tại chỉ số")
        .setMessage(
            "Phòng này đã có chỉ số ${if(loai=="dien") "điện" else "nước"} " +
            "tháng $thang/$nam.\n\n" +
            "Chỉ số cũ: ${chiSoCu?.chiSoCu} → ${chiSoCu?.chiSoMoi}\n\n" +
            "Bạn muốn cập nhật?"
        )
        .setPositiveButton("Cập nhật") { _, _ ->
            // Cập nhật bản ghi cũ, KHÔNG tạo mới
            val chiSoCapNhat = chiSoCu!!.copy(
                chiSoMoi = chiSoMoi,
                soTieuThu = chiSoMoi - chiSoCu.chiSoCu
            )
            dbManager.chiSoDienNuocDao.capNhat(chiSoCapNhat)
        }
        .setNegativeButton("Hủy", null)
        .show()
    return@launch  // KHÔNG cho lưu mới
}
```

---

## ❌ VẤN ĐỀ 5: Logic lấy chỉ số tháng trước chưa xử lý tháng 1

### Hiện trạng
```kotlin
// Lỗi ở tháng 1
val chiSoCu = layTheoThangNam(thang - 1, nam)
// thang = 1 → thang - 1 = 0 (SAI!)
```

**Vấn đề:**
- Tháng 1 → tháng trước = tháng 0 (không tồn tại)
- Phải là tháng 12 năm trước

### Giải pháp

```kotlin
fun layChiSoThangTruoc(maPhong: Long, loai: String, thang: Int, nam: Int): ChiSoDienNuoc? {
    val (thangTruoc, namTruoc) = if (thang == 1) {
        Pair(12, nam - 1)  // Tháng 1 → lấy tháng 12 năm trước
    } else {
        Pair(thang - 1, nam)
    }
    
    return layTheoThangNam(thangTruoc, namTruoc)
        .find { it.maPhong == maPhong && it.loai == loai }
}
```

---

## ❌ VẤN ĐỀ 6: Tạo hóa đơn ngay sau khi nhập 1 loại chỉ số

### Hiện trạng
```kotlin
// Sau khi lưu chỉ số điện → hỏi tạo hóa đơn ngay
// Nhưng chưa nhập nước → hóa đơn thiếu
```

**Vấn đề:**
- Mới nhập điện chưa nhập nước → hóa đơn thiếu tiền nước
- Hoặc ngược lại

### Giải pháp

```kotlin
// Kiểm tra đủ dữ liệu trước khi gợi ý tạo hóa đơn
fun kiemTraDuDuLieuTaoHoaDon(maPhong: Long, thang: Int, nam: Int): Pair<Boolean, String> {
    val chiSoDien = dbManager.chiSoDienNuocDao.layTheoThangNam(thang, nam)
        .find { it.maPhong == maPhong && it.loai == "dien" }
    val chiSoNuoc = dbManager.chiSoDienNuocDao.layTheoThangNam(thang, nam)
        .find { it.maPhong == maPhong && it.loai == "nuoc" }
    
    return when {
        chiSoDien != null && chiSoNuoc != null -> 
            Pair(true, "Đã có đủ chỉ số điện và nước")
        chiSoDien != null && chiSoNuoc == null -> 
            Pair(false, "Chưa nhập chỉ số nước")
        chiSoDien == null && chiSoNuoc != null -> 
            Pair(false, "Chưa nhập chỉ số điện")
        else -> 
            Pair(false, "Chưa nhập chỉ số điện và nước")
    }
}

// Sau khi lưu chỉ số
val (duDuLieu, thongBao) = kiemTraDuDuLieuTaoHoaDon(maPhong, thang, nam)

if (duDuLieu) {
    AlertDialog.Builder(requireContext())
        .setTitle("Tạo hóa đơn")
        .setMessage("✓ $thongBao\n\nTạo hóa đơn cho phòng này?")
        .setPositiveButton("Tạo hóa đơn") { ... }
        .setNegativeButton("Để sau", null)
        .show()
} else {
    Toast.makeText(context, "⚠️ $thongBao\nVui lòng nhập đủ trước khi tạo hóa đơn", ...).show()
}
```

---

## ❌ VẤN ĐỀ 7: Tạo hóa đơn đang hơi dư input

### Hiện trạng
```kotlin
// Cho chọn cả 3: nhà → phòng → hợp đồng
// Nhưng hợp đồng đã gắn với phòng, phòng đã gắn với nhà
```

**Vấn đề:**
- Thừa input
- Dễ lệch: chọn phòng A nhưng chọn hợp đồng của phòng B

### Giải pháp

**Phương án 1 (Khuyến nghị):** Chỉ chọn hợp đồng
```kotlin
// Chỉ có 1 spinner: Hợp đồng
// Tự động suy ra phòng và nhà

spinnerHopDong.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
        val hopDong = danhSachHopDong[pos]
        
        // Tự động load phòng
        CoroutineScope(Dispatchers.IO).launch {
            val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
            val nha = phong?.let { dbManager.nhaTroDao.layTheoMa(it.maNha) }
            
            withContext(Dispatchers.Main) {
                tvPhong.text = "Phòng: ${phong?.tenPhong}"
                tvNha.text = "Nhà: ${nha?.tenNha}"
            }
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
```

**Phương án 2:** Chọn phòng → load hợp đồng active
```kotlin
// Chọn phòng → chỉ load hợp đồng đang active của phòng đó
spinnerPhong.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
        val phong = danhSachPhong[pos]
        
        CoroutineScope(Dispatchers.IO).launch {
            // Chỉ lấy hợp đồng đang thuê của phòng này
            val hopDong = dbManager.hopDongDao.layHopDongDangThue(phong.maPhong)
            
            withContext(Dispatchers.Main) {
                if (hopDong != null) {
                    // Hiển thị thông tin hợp đồng
                } else {
                    Toast.makeText(context, "Phòng chưa có hợp đồng active", ...).show()
                }
            }
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
```

**Quyết định:** Sử dụng Phương án 1 - chỉ chọn hợp đồng

---

## ❌ VẤN ĐỀ 8: Hóa đơn chưa có rule chống trùng

### Hiện trạng
```kotlin
// Không có kiểm tra trùng
// Có thể tạo 2 hóa đơn cho cùng hợp đồng cùng tháng
```

**Vấn đề:**
- Tạo trùng hóa đơn
- Dữ liệu không nhất quán
- Thu tiền 2 lần

### Giải pháp

**Thêm vào HoaDonDao:**
```kotlin
fun kiemTraTrungHoaDon(maHopDong: Long, thang: Int, nam: Int, maHoaDonLoaiTru: Long = -1): Boolean {
    val cursor = if (maHoaDonLoaiTru > 0) {
        db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_HOA_DON} " +
            "WHERE ma_hop_dong = ? AND thang = ? AND nam = ? AND ma_hoa_don != ?",
            arrayOf(maHopDong.toString(), thang.toString(), nam.toString(), maHoaDonLoaiTru.toString())
        )
    } else {
        db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_HOA_DON} " +
            "WHERE ma_hop_dong = ? AND thang = ? AND nam = ?",
            arrayOf(maHopDong.toString(), thang.toString(), nam.toString())
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

**Sử dụng trong CreateInvoiceFragment:**
```kotlin
// Trước khi lưu
if (dbManager.hoaDonDao.kiemTraTrungHoaDon(maHopDong, thang, nam, maHoaDon)) {
    AlertDialog.Builder(requireContext())
        .setTitle("Đã tồn tại hóa đơn")
        .setMessage(
            "Hợp đồng này đã có hóa đơn tháng $thang/$nam.\n\n" +
            "Bạn muốn cập nhật hóa đơn cũ?"
        )
        .setPositiveButton("Cập nhật") { _, _ ->
            // Lấy hóa đơn cũ và cập nhật
        }
        .setNegativeButton("Hủy", null)
        .show()
    return@setOnClickListener
}
```

---

## ❌ VẤN ĐỀ 9: Phần khách thuê và hóa đơn đang lệch nhau

### Hiện trạng
```kotlin
// Hóa đơn load 1 khách thuê
// Nhưng phòng có thể có nhiều người ở ghép
```

**Vấn đề:**
- Mô hình không nhất quán
- Hóa đơn chỉ hiển thị người đại diện
- Không rõ ai phải trả tiền

### Giải pháp

**Chốt mô hình:**
```
1. 1 phòng có 1 hợp đồng active
2. Hóa đơn lập theo hợp đồng
3. Người đại diện (dai_dien) chịu trách nhiệm thanh toán
4. Người ở ghép (thanh_vien) là thành viên phụ
5. Hóa đơn hiển thị:
   - Người đại diện (chính)
   - Danh sách thành viên (phụ)
```

**Cập nhật CreateInvoiceFragment:**
```kotlin
// Load thông tin hợp đồng
val hopDong = dbManager.hopDongDao.layTheoMa(maHopDong)
val nguoiDaiDien = dbManager.khachThueDao.layTheoMa(hopDong.maKhach)

// Load danh sách thành viên
val danhSachThanhVien = dbManager.hopDongThanhVienDao.layTheoHopDong(maHopDong)
    .filter { it.trangThai == "dang_o" }

// Hiển thị
tvNguoiDaiDien.text = "Người đại diện: ${nguoiDaiDien?.hoTen}"
tvSoThanhVien.text = "Số người ở: ${danhSachThanhVien.size}"

// Nếu muốn hiển thị chi tiết
val danhSachTen = danhSachThanhVien.joinToString(", ") { tv ->
    val khach = dbManager.khachThueDao.layTheoMa(tv.maKhach)
    khach?.hoTen ?: "?"
}
tvDanhSachThanhVien.text = "Thành viên: $danhSachTen"
```

---

## ❌ VẤN ĐỀ 10: Thu chi đang còn rời hóa đơn và hợp đồng

### Hiện trạng
```kotlin
data class GiaoDich(
    val maGiaoDich: Long = 0,
    val loai: String, // thu / chi
    val maPhong: Long? = null,
    val soTien: Double,
    // Không có maHoaDon, maHopDong, maDatCoc
    ...
)
```

**Vấn đề:**
- Giao dịch không liên kết với nguồn phát sinh
- Đã thu tiền hóa đơn nhưng hóa đơn chưa đổi trạng thái
- Hóa đơn đã thanh toán nhưng không có giao dịch đối ứng

### Giải pháp

**Cập nhật model GiaoDich:**
```kotlin
data class GiaoDich(
    val maGiaoDich: Long = 0,
    val loai: String, // thu / chi
    val maPhong: Long? = null,
    val maHoaDon: Long? = null,      // THÊM
    val maHopDong: Long? = null,     // THÊM
    val maDatCoc: Long? = null,      // THÊM
    val soTien: Long,                // Đổi Double → Long
    val danhMuc: String = "",
    val ngayGiaoDich: Long = 0,
    val noiDung: String = "",
    val tenNguoi: String = "",
    val phuongThucThanhToan: String = "tien_mat",
    val ghiChu: String = "",
    val ngayTao: Long = System.currentTimeMillis()
)
```

**Workflow thu tiền hóa đơn:**
```kotlin
fun thuTienHoaDon(hoaDon: HoaDon, soTienThu: Long, phuongThuc: String) {
    // 1. Tạo giao dịch thu
    val giaoDich = GiaoDich(
        loai = "thu",
        maHoaDon = hoaDon.maHoaDon,
        maHopDong = hoaDon.maHopDong,
        soTien = soTienThu,
        danhMuc = "Thu tiền phòng",
        ngayGiaoDich = System.currentTimeMillis(),
        noiDung = "Thu tiền hóa đơn tháng ${hoaDon.thang}/${hoaDon.nam}",
        phuongThucThanhToan = phuongThuc
    )
    dbManager.giaoDichDao.them(giaoDich)
    
    // 2. Cập nhật trạng thái hóa đơn
    val hoaDonCapNhat = hoaDon.copy(
        daThanhToan = true,
        tienDaThanhToan = soTienThu  // Nếu có trường này
    )
    dbManager.hoaDonDao.capNhat(hoaDonCapNhat)
    
    // 3. Thông báo
    Toast.makeText(context, "✓ Đã thu tiền hóa đơn\n✓ Đã tạo giao dịch", ...).show()
}
```

---

## 📊 TỔNG KẾT ƯU TIÊN SỬA

### Mức độ Nghiêm trọng

| Vấn đề | Mức độ | Ưu tiên | Ảnh hưởng |
|--------|--------|---------|-----------|
| VĐ3: Điện nước chồng | 🔴 Cao | 1 | Tính tiền 2 lần |
| VĐ4: Trùng chỉ số | 🔴 Cao | 2 | Dữ liệu sai |
| VĐ8: Trùng hóa đơn | 🔴 Cao | 3 | Thu tiền 2 lần |
| VĐ10: Thu chi rời | 🔴 Cao | 4 | Không đối soát được |
| VĐ5: Tháng 1 lỗi | 🟡 Trung bình | 5 | Lỗi logic |
| VĐ7: Dư input | 🟡 Trung bình | 6 | UX kém |
| VĐ1: Dịch vụ cấp nhà | 🟢 Thấp | 7 | Chưa linh hoạt |
| VĐ2: Rule trùng DV | 🟢 Thấp | 8 | Kiểm tra chưa đủ |
| VĐ6: Gợi ý HĐ sớm | 🟢 Thấp | 9 | UX chưa tốt |
| VĐ9: Lệch mô hình | 🟢 Thấp | 10 | Hiển thị chưa đủ |

### Kế hoạch thực hiện

1. **Phase 1 - Sửa logic nghiệp vụ cốt lõi** (VĐ3, VĐ4, VĐ8, VĐ10)
2. **Phase 2 - Sửa lỗi logic** (VĐ5, VĐ7)
3. **Phase 3 - Cải thiện mô hình** (VĐ1, VĐ2, VĐ6, VĐ9)

---

## 🎯 HÀNH ĐỘNG TIẾP THEO

Tôi sẽ bắt đầu sửa từng vấn đề theo thứ tự ưu tiên.

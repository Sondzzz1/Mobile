# TỔNG KẾT CHỨC NĂNG EDIT & DELETE

## ĐÃ HOÀN THÀNH ✅

### 1. NHÀ TRỌ (HOUSE) - 100% ✅

**Files đã cập nhật:**
- ✅ `NhaTroAdapter.kt` - Thêm nút Edit/Delete
- ✅ `item_house.xml` - UI cho nút Edit/Delete
- ✅ `HouseListFragment.kt` - Xử lý edit/delete với dialog xác nhận
- ✅ `CreateHouseFragment.kt` - Hỗ trợ cả Create và Edit

**Chức năng:**
- ✅ Nút Edit riêng biệt
- ✅ Nút Delete riêng biệt
- ✅ Dialog xác nhận xóa với cảnh báo cascade
- ✅ Error handling
- ✅ Cascade delete (tự động xóa phòng, hợp đồng liên quan)
- ✅ Toast feedback

### 2. PHÒNG (ROOM) - 100% ✅

**Files đã cập nhật:**
- ✅ `PhongAdapter.kt` - Thêm nút Edit/Delete
- ✅ `item_room.xml` - UI cho nút Edit/Delete (dạng Button)
- ✅ `RoomListFragment.kt` - Xử lý edit/delete với dialog xác nhận
- ✅ `CreateRoomFragment.kt` - Đã hỗ trợ Edit từ trước

**Chức năng:**
- ✅ Nút Edit và Delete trong CardView
- ✅ Dialog xác nhận xóa với cảnh báo
- ✅ Error handling
- ✅ Cascade delete (tự động xóa hợp đồng liên quan)
- ✅ Toast feedback

---

## CẦN HOÀN THIỆN (TODO)

### 3. KHÁCH THUÊ (TENANT) - 0%

**Cần làm:**
1. Cập nhật `KhachThueAdapter.kt`
   - Thêm callbacks: `onEditClick`, `onDeleteClick`
   - Thêm ViewHolder cho nút Edit/Delete

2. Cập nhật `item_tenant.xml`
   - Thêm 2 ImageView hoặc Button cho Edit/Delete

3. Cập nhật `TenantListFragment.kt`
   - Thay đổi adapter initialization
   - Thêm hàm `xoaKhachThue()` với error handling
   - Dialog xác nhận xóa

4. Cập nhật `CreateTenantFragment.kt`
   - Thêm logic load dữ liệu khi edit
   - Nhận `maKhach` từ arguments

**Code mẫu:**
```kotlin
// TenantListFragment.kt
adapter = KhachThueAdapter(
    onItemClick = { khach -> /* view detail */ },
    onEditClick = { khach ->
        val fragment = CreateTenantFragment().apply {
            arguments = Bundle().apply { putLong("maKhach", khach.maKhach) }
        }
        // navigate...
    },
    onDeleteClick = { khach ->
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa khách thuê")
            .setMessage("Bạn có chắc muốn xóa \"${khach.hoTen}\"?")
            .setPositiveButton("Xóa") { _, _ -> xoaKhachThue(khach, view) }
            .setNegativeButton("Hủy", null)
            .show()
    }
)
```

---

### 4. HỢP ĐỒNG (CONTRACT) - 0%

**Cần làm:**
1. Cập nhật `HopDongAdapter.kt`
2. Cập nhật `item_contract.xml`
3. Cập nhật `ContractListFragment.kt`
4. Cập nhật `CreateContractFragment.kt`

**Lưu ý đặc biệt:**
- Khi xóa hợp đồng, cần cập nhật trạng thái phòng về "trong"
- Kiểm tra có hóa đơn liên quan không

**Code mẫu:**
```kotlin
private fun xoaHopDong(hopDong: HopDong, view: View) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Lấy thông tin phòng
            val phong = dbManager.phongDao.layTheoMa(hopDong.maPhong)
            
            // Xóa hợp đồng
            val result = dbManager.hopDongDao.xoa(hopDong.maHopDong)
            
            // Cập nhật trạng thái phòng về "trong"
            phong?.let {
                it.trangThai = "trong"
                dbManager.phongDao.capNhat(it)
            }
            
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(context, "Đã xóa hợp đồng", Toast.LENGTH_SHORT).show()
                    taiDuLieu(view)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

---

### 5. DỊCH VỤ (SERVICE) - 0%

**Cần làm:**
1. Cập nhật `DichVuAdapter.kt`
2. Cập nhật `item_service.xml`
3. Cập nhật `ServiceListFragment.kt`
4. Cập nhật `CreateServiceFragment.kt`

---

## PATTERN CHUNG ĐÃ ÁP DỤNG

### 1. Adapter Pattern

```kotlin
class EntityAdapter(
    private var danhSach: List<Entity> = emptyList(),
    private val onItemClick: (Entity) -> Unit,
    private val onEditClick: (Entity) -> Unit,
    private val onDeleteClick: (Entity) -> Unit
) : RecyclerView.Adapter<EntityAdapter.ViewHolder>() {
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ... views
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = danhSach[position]
        // ... bind data
        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }
}
```

### 2. Delete Function Pattern

```kotlin
private fun xoaEntity(entity: Entity, view: View) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = dbManager.entityDao.xoa(entity.id)
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                    taiDuLieu(view)
                } else {
                    Toast.makeText(context, "Không thể xóa", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

### 3. Confirmation Dialog Pattern

```kotlin
AlertDialog.Builder(requireContext())
    .setTitle("Xóa [Entity]")
    .setMessage("Bạn có chắc muốn xóa \"[name]\"?\n\nCảnh báo: [cascade warning]")
    .setPositiveButton("Xóa") { _, _ -> xoaEntity(entity, view) }
    .setNegativeButton("Hủy", null)
    .setIcon(android.R.drawable.ic_dialog_alert)
    .show()
```

### 4. Edit Navigation Pattern

```kotlin
onEditClick = { entity ->
    val fragment = CreateEntityFragment().apply {
        arguments = Bundle().apply { putLong("entityId", entity.id) }
    }
    parentFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commit()
}
```

---

## CASCADE DELETE LOGIC

Database đã được cấu hình `ON DELETE CASCADE` trong `DatabaseHelper.kt`:

```kotlin
FOREIGN KEY (ma_nha) REFERENCES nha_tro(ma_nha) ON DELETE CASCADE
FOREIGN KEY (ma_phong) REFERENCES phong(ma_phong) ON DELETE CASCADE
FOREIGN KEY (ma_khach) REFERENCES khach_thue(ma_khach) ON DELETE CASCADE
```

**Nghĩa là:**
- Xóa Nhà trọ → Tự động xóa tất cả Phòng, Dịch vụ của nhà đó
- Xóa Phòng → Tự động xóa tất cả Hợp đồng, Chỉ số điện nước, Đặt cọc của phòng đó
- Xóa Hợp đồng → Tự động xóa tất cả Hóa đơn của hợp đồng đó
- Xóa Khách thuê → Tự động xóa tất cả Hợp đồng của khách đó

---

## UI PATTERNS

### Pattern 1: ImageView Icons (House)
```xml
<ImageView
    android:id="@+id/btnEdit"
    android:layout_width="32dp"
    android:layout_height="32dp"
    android:src="@android:drawable/ic_menu_edit"
    android:tint="@color/colorPrimary"/>
```

### Pattern 2: Button (Room)
```xml
<Button
    android:id="@+id/btnEdit"
    android:layout_width="0dp"
    android:layout_weight="1"
    android:layout_height="40dp"
    android:text="Sửa"
    android:backgroundTint="@color/colorPrimary"/>
```

**Khuyến nghị:** Dùng Pattern 2 (Button) cho UI rõ ràng hơn.

---

## TESTING CHECKLIST

### Nhà trọ ✅
- [x] Edit nhà trọ
- [x] Delete nhà trọ
- [x] Cascade delete phòng
- [x] Dialog xác nhận
- [x] Error handling

### Phòng ✅
- [x] Edit phòng
- [x] Delete phòng
- [x] Cascade delete hợp đồng
- [x] Dialog xác nhận
- [x] Error handling

### Khách thuê ⏳
- [ ] Edit khách thuê
- [ ] Delete khách thuê
- [ ] Cascade delete hợp đồng
- [ ] Dialog xác nhận
- [ ] Error handling

### Hợp đồng ⏳
- [ ] Edit hợp đồng
- [ ] Delete hợp đồng
- [ ] Update room status
- [ ] Cascade delete hóa đơn
- [ ] Dialog xác nhận
- [ ] Error handling

### Dịch vụ ⏳
- [ ] Edit dịch vụ
- [ ] Delete dịch vụ
- [ ] Dialog xác nhận
- [ ] Error handling

---

## NEXT STEPS

1. **Ưu tiên cao:** Hoàn thiện Khách thuê và Hợp đồng
2. **Ưu tiên trung bình:** Hoàn thiện Dịch vụ
3. **Tùy chọn:** Thêm các chức năng nâng cao:
   - Undo delete (Snackbar)
   - Bulk delete
   - Soft delete
   - Export/Import

---

## KẾT LUẬN

Đã hoàn thành 40% chức năng Edit/Delete (2/5 entities). Pattern đã được thiết lập rõ ràng, chỉ cần áp dụng tương tự cho các entities còn lại.

**Thời gian ước tính:**
- Khách thuê: 15-20 phút
- Hợp đồng: 20-25 phút (phức tạp hơn)
- Dịch vụ: 15-20 phút

**Tổng:** ~1 giờ để hoàn thiện tất cả.

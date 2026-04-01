# HƯỚNG DẪN HOÀN THIỆN CHỨC NĂNG EDIT & DELETE

## TỔNG QUAN

Đã hoàn thành:
- ✅ Edit & Delete cho Nhà trọ (House)
- ✅ Cascade delete logic trong DatabaseHelper
- ✅ Confirmation dialogs
- ✅ Error handling

Cần làm tiếp:
- ⏳ Edit & Delete cho Phòng (Room)
- ⏳ Edit & Delete cho Khách thuê (Tenant)
- ⏳ Edit & Delete cho Hợp đồng (Contract)
- ⏳ Edit & Delete cho Dịch vụ (Service)

---

## 1. NHÀTRỌ (HOUSE) - ĐÃ HOÀN THÀNH ✅

### Các file đã cập nhật:

**A. NhaTroAdapter.kt**
- Thêm callback `onEditClick` và `onDeleteClick`
- Thêm nút Edit và Delete trong ViewHolder
- Xử lý click events

**B. item_house.xml**
- Thêm 2 ImageView cho nút Edit và Delete
- Icon: `ic_menu_edit` và `ic_menu_delete`
- Màu: colorPrimary và error

**C. HouseListFragment.kt**
- Cập nhật adapter với 3 callbacks
- Thêm hàm `xoaNhaTro()` với error handling
- Dialog xác nhận với cảnh báo cascade delete

**D. CreateHouseFragment.kt**
- Đã hỗ trợ cả Create và Edit
- Nhận `maNha` từ arguments
- Load dữ liệu khi edit

---

## 2. PHÒNG (ROOM) - CẦN HOÀN THIỆN

### Bước 1: Cập nhật item_room.xml

Thêm nút Edit/Delete vào CardView:

```xml
<!-- Thêm sau LinearLayout chứa tvPrice và tvTenantCount -->
<View
    android:layout_width="match_parent"
    android:layout_height="1dp"
    android:background="#E0E0E0"/>

<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp">
    
    <Button
        android:id="@+id/btnEdit"
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="wrap_content"
        android:text="Sửa"
        android:textSize="14sp"
        android:backgroundTint="@color/colorPrimary"
        android:layout_marginEnd="4dp"/>
        
    <Button
        android:id="@+id/btnDelete"
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="wrap_content"
        android:text="Xóa"
        android:textSize="14sp"
        android:backgroundTint="@color/error"
        android:layout_marginStart="4dp"/>
</LinearLayout>
```

### Bước 2: Cập nhật PhongAdapter.kt

```kotlin
class PhongAdapter(
    private var danhSach: List<Phong> = emptyList(),
    private val onItemClick: (Phong) -> Unit,
    private val onEditClick: (Phong) -> Unit,
    private val onDeleteClick: (Phong) -> Unit
) : RecyclerView.Adapter<PhongAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenPhong: TextView = view.findViewById(R.id.tvRoomName)
        val tvGia: TextView = view.findViewById(R.id.tvPrice)
        val tvTrangThai: TextView = view.findViewById(R.id.tvRoomStatus)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phong = danhSach[position]
        holder.tvTenPhong.text = phong.tenPhong
        holder.tvGia.text = "${String.format("%,.0f", phong.giaCoBan)} đ/tháng"
        
        if (phong.trangThai == "trong") {
            holder.tvTrangThai.text = "Còn trống"
            holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvTrangThai.text = "Đã thuê"
            holder.tvTrangThai.setTextColor(Color.parseColor("#F44336"))
        }
        
        holder.itemView.setOnClickListener { onItemClick(phong) }
        holder.btnEdit.setOnClickListener { onEditClick(phong) }
        holder.btnDelete.setOnClickListener { onDeleteClick(phong) }
    }
}
```

### Bước 3: Cập nhật RoomListFragment.kt

```kotlin
adapter = PhongAdapter(
    onItemClick = { phong ->
        val fragment = RoomDetailFragment().apply {
            arguments = Bundle().apply { putLong("maPhong", phong.maPhong) }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    },
    onEditClick = { phong ->
        val fragment = CreateRoomFragment().apply {
            arguments = Bundle().apply {
                putLong("maNha", maNha)
                putLong("maPhong", phong.maPhong)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    },
    onDeleteClick = { phong ->
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa phòng")
            .setMessage("Bạn có chắc muốn xóa \"${phong.tenPhong}\"?\n\nCảnh báo: Tất cả hợp đồng và dữ liệu liên quan sẽ bị xóa!")
            .setPositiveButton("Xóa") { _, _ ->
                xoaPhong(phong, view)
            }
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
)

private fun xoaPhong(phong: Phong, view: View) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = dbManager.phongDao.xoa(phong.maPhong)
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(context, "Đã xóa \"${phong.tenPhong}\"", Toast.LENGTH_SHORT).show()
                    taiDuLieu(view)
                } else {
                    Toast.makeText(context, "Không thể xóa phòng", Toast.LENGTH_SHORT).show()
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

### Bước 4: Cập nhật CreateRoomFragment.kt

CreateRoomFragment cần hỗ trợ Edit. Thêm logic load dữ liệu:

```kotlin
private var maPhongEdit: Long = -1L

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dbManager = DatabaseManager.getInstance(requireContext())
    maNha = arguments?.getLong("maNha", -1L) ?: -1L
    maPhongEdit = arguments?.getLong("maPhong", -1L) ?: -1L

    // Load dữ liệu khi edit
    if (maPhongEdit > 0) {
        CoroutineScope(Dispatchers.IO).launch {
            val phong = dbManager.phongDao.layTheoMa(maPhongEdit)
            withContext(Dispatchers.Main) {
                phong?.let {
                    etRoomName.setText(it.tenPhong)
                    etRoomPrice.setText(it.giaCoBan.toString())
                    etRoomArea.setText(it.dienTichM2.toString())
                    etMaxTenant.setText(it.soNguoiToiDa.toString())
                    etNote.setText(it.ghiChu)
                    // Set spinner status...
                }
            }
        }
    }

    // Trong nút submit
    btnSubmit.setOnClickListener {
        // ... validation ...
        
        val phong = Phong(
            maPhong = if (maPhongEdit > 0) maPhongEdit else 0,
            maNha = maNha,
            tenPhong = tenPhong,
            // ... các field khác ...
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (maPhongEdit > 0) {
                dbManager.phongDao.capNhat(phong)
            } else {
                dbManager.phongDao.them(phong)
            }
            // ...
        }
    }
}
```

---

## 3. KHÁCH THUÊ (TENANT)

Làm tương tự như Room:
1. Cập nhật `item_tenant.xml` - thêm nút Edit/Delete
2. Cập nhật `KhachThueAdapter.kt` - thêm callbacks
3. Cập nhật `TenantListFragment.kt` - xử lý edit/delete
4. Cập nhật `CreateTenantFragment.kt` - hỗ trợ edit

---

## 4. HỢP ĐỒNG (CONTRACT)

Làm tương tự:
1. Cập nhật `item_contract.xml`
2. Cập nhật `HopDongAdapter.kt`
3. Cập nhật `ContractListFragment.kt`
4. Cập nhật `CreateContractFragment.kt`

**Lưu ý đặc biệt:** Khi xóa hợp đồng, cần kiểm tra:
- Có hóa đơn liên quan không?
- Cập nhật trạng thái phòng về "trong"

---

## 5. DỊCH VỤ (SERVICE)

Làm tương tự:
1. Cập nhật `item_service.xml`
2. Cập nhật `DichVuAdapter.kt`
3. Cập nhật `ServiceListFragment.kt`
4. Cập nhật `CreateServiceFragment.kt`

---

## TEMPLATE CODE - CONFIRMATION DIALOG

```kotlin
private fun showDeleteConfirmation(itemName: String, onConfirm: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle("Xác nhận xóa")
        .setMessage("Bạn có chắc muốn xóa \"$itemName\"?\n\nHành động này không thể hoàn tác!")
        .setPositiveButton("Xóa") { _, _ -> onConfirm() }
        .setNegativeButton("Hủy", null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
}
```

---

## TEMPLATE CODE - DELETE FUNCTION

```kotlin
private fun xoaItem(id: Long, name: String, view: View, dao: Any) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = when(dao) {
                is PhongDao -> dao.xoa(id)
                is KhachThueDao -> dao.xoa(id)
                is HopDongDao -> dao.xoa(id)
                is DichVuDao -> dao.xoa(id)
                else -> 0
            }
            
            withContext(Dispatchers.Main) {
                if (result > 0) {
                    Toast.makeText(context, "Đã xóa \"$name\"", Toast.LENGTH_SHORT).show()
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

---

## CHECKLIST HOÀN THÀNH

### Nhà trọ (House)
- [x] Edit UI
- [x] Delete UI
- [x] Confirmation dialog
- [x] Cascade delete
- [x] Error handling

### Phòng (Room)
- [ ] Edit UI
- [ ] Delete UI
- [ ] Confirmation dialog
- [ ] Error handling

### Khách thuê (Tenant)
- [ ] Edit UI
- [ ] Delete UI
- [ ] Confirmation dialog
- [ ] Error handling

### Hợp đồng (Contract)
- [ ] Edit UI
- [ ] Delete UI
- [ ] Confirmation dialog
- [ ] Update room status
- [ ] Error handling

### Dịch vụ (Service)
- [ ] Edit UI
- [ ] Delete UI
- [ ] Confirmation dialog
- [ ] Error handling

---

## LƯU Ý QUAN TRỌNG

1. **Cascade Delete**: Database đã được cấu hình `ON DELETE CASCADE` trong DatabaseHelper, nên khi xóa parent record, các child records sẽ tự động bị xóa.

2. **Error Handling**: Luôn wrap delete operations trong try-catch để xử lý lỗi.

3. **Confirmation**: Luôn hiển thị dialog xác nhận trước khi xóa, đặc biệt với các item có dữ liệu liên quan.

4. **UI Feedback**: Hiển thị Toast message sau khi thao tác thành công/thất bại.

5. **Refresh Data**: Gọi `taiDuLieu()` sau khi xóa để cập nhật danh sách.

---

## TIẾP THEO

Sau khi hoàn thành Edit/Delete, có thể thêm:
- Undo delete (với Snackbar)
- Bulk delete (chọn nhiều item)
- Soft delete (đánh dấu xóa thay vì xóa thật)
- Export/Import data

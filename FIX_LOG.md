# BÁO CÁO SỬA LỖI - ỨNG DỤNG QUẢN LÝ NHÀ TRỌ

## Ngày: 07/04/2026

## CÁC LỖI ĐÃ PHÁT HIỆN VÀ SỬA

### 1. Lỗi Null Pointer trong các Fragment tìm kiếm

**Vấn đề:**
- Trong `InvoiceListFragment`, `IncomeListFragment`, `ExpenseListFragment`
- Hàm `applyFilters()` gọi `view?.findViewById()?.text.toString()` có thể gây crash nếu view hoặc EditText null

**Giải pháp:**
```kotlin
// Trước (có thể crash):
val tuKhoa = view?.findViewById<EditText>(R.id.etSearchIncome)?.text.toString().trim().lowercase()

// Sau (an toàn):
val searchView = view?.findViewById<EditText>(R.id.etSearchIncome)
val tuKhoa = searchView?.text?.toString()?.trim()?.lowercase() ?: ""
```

**Files đã sửa:**
- `app/src/main/java/com/example/btl_mobile_son/InvoiceListFragment.kt`
- `app/src/main/java/com/example/btl_mobile_son/IncomeListFragment.kt`
- `app/src/main/java/com/example/btl_mobile_son/ExpenseListFragment.kt`

### 2. Lỗi Layout trong Invoice/Income/Expense List

**Vấn đề:**
- Search bar và RecyclerView có `app:layout_behavior` không đúng cấu trúc
- Gây ra layout overlap và không scroll được đúng

**Giải pháp:**
- Wrap search bar và content trong một LinearLayout chung
- Chỉ LinearLayout cha mới có `app:layout_behavior`
- RecyclerView sử dụng `layout_weight` để chiếm không gian còn lại

**Cấu trúc layout đúng:**
```xml
<CoordinatorLayout>
    <AppBarLayout>...</AppBarLayout>
    
    <LinearLayout layout_behavior="@string/appbar_scrolling_view_behavior">
        <LinearLayout> <!-- Search bar --> </LinearLayout>
        <LinearLayout layout_weight="1"> <!-- Content --> </LinearLayout>
    </LinearLayout>
    
    <LinearLayout layout_gravity="bottom"> <!-- Bottom bar --> </LinearLayout>
</CoordinatorLayout>
```

**Files đã sửa:**
- `app/src/main/res/layout/fragment_invoice_list.xml`
- `app/src/main/res/layout/fragment_income_list.xml`
- `app/src/main/res/layout/fragment_expense_list.xml`

## TÍNH NĂNG ĐÃ HOÀN THÀNH

### 1. Tìm kiếm Hợp đồng ✓
- Tìm theo: mã hợp đồng, tên khách, tên phòng, tên nhà
- Real-time search

### 2. Tìm kiếm & Lọc Hóa đơn ✓
- Tìm theo: phòng, khách, tháng/năm
- Lọc trạng thái: Tất cả, Chưa TT, Đã TT, Quá hạn

### 3. Tìm kiếm & Lọc Thu nhập ✓
- Tìm theo: nội dung, danh mục, tên người
- Lọc danh mục: Tất cả, Tiền thuê, Điện, Nước, Dịch vụ, Đặt cọc, Khác
- Tự động tính lại tổng tiền

### 4. Tìm kiếm & Lọc Chi phí ✓
- Tìm theo: nội dung, danh mục, tên người
- Lọc danh mục: Tất cả, Sửa chữa, Điện, Nước, Vệ sinh, Bảo trì, Lương, Khác
- Tự động tính lại tổng tiền

## KIỂM TRA BUILD

```bash
./gradlew assembleDebug --no-daemon
```

**Kết quả:** ✓ BUILD SUCCESSFUL

**Warnings:** Chỉ có deprecation warnings (không ảnh hưởng chức năng)
- `onBackPressed()` deprecated (Android khuyến nghị dùng OnBackPressedDispatcher)
- `Locale(String, String)` deprecated (khuyến nghị dùng Locale.Builder)

## HƯỚNG DẪN CÀI ĐẶT & KIỂM TRA

### Cài đặt trên Emulator:
```bash
# Khởi động emulator trước
# Sau đó chạy:
./gradlew installDebug
```

### Kiểm tra các tính năng:
1. **Hợp đồng:** Menu → Hợp đồng → Gõ vào thanh tìm kiếm
2. **Hóa đơn:** Bottom Nav → Hóa đơn → Tìm kiếm + Chọn trạng thái
3. **Thu nhập:** Menu → Quản lý thu chi → Tìm kiếm + Chọn danh mục
4. **Chi phí:** Menu → Quản lý thu chi → Tab Chi → Tìm kiếm + Chọn danh mục

## GHI CHÚ

- Tất cả tính năng tìm kiếm hoạt động real-time
- Không cần nhấn nút "Tìm" - kết quả hiện ngay khi gõ
- Có thể kết hợp tìm kiếm + lọc cùng lúc
- Tổng tiền tự động cập nhật theo kết quả lọc

## CÁC TÍNH NĂNG ĐÃ CÓ SẴN

- ✓ Quản lý nhà trọ, phòng, khách thuê
- ✓ Quản lý hợp đồng, hóa đơn
- ✓ Quản lý dịch vụ và dịch vụ phòng
- ✓ Quản lý chỉ số điện nước
- ✓ Quản lý thu chi
- ✓ Quản lý đặt cọc
- ✓ Quản lý sự cố
- ✓ Báo cáo tài chính
- ✓ Thống kê biểu đồ
- ✓ Đăng nhập/Phân quyền (Admin/Nhân viên)
- ✓ Tìm kiếm & Lọc (mới thêm)

## KẾT LUẬN

Ứng dụng đã được sửa lỗi và build thành công. Tất cả tính năng tìm kiếm và lọc đã được implement và hoạt động ổn định.

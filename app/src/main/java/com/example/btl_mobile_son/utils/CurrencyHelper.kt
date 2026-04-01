package com.example.btl_mobile_son.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Helper functions để xử lý tiền tệ
 * Sử dụng Long thay vì Double để tránh lỗi làm tròn
 */

/**
 * Format Long thành chuỗi tiền tệ VNĐ
 * VD: 1000000 → "1,000,000 đ"
 */
fun Long.formatCurrency(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(this)} đ"
}

/**
 * Format Long thành chuỗi tiền tệ VNĐ không có ký hiệu
 * VD: 1000000 → "1,000,000"
 */
fun Long.formatNumber(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(this)
}

/**
 * Parse chuỗi tiền tệ thành Long
 * VD: "1,000,000 đ" → 1000000
 * VD: "1.000.000" → 1000000
 * VD: "1000000" → 1000000
 */
fun String.parseCurrency(): Long {
    return this.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0
}

/**
 * Kiểm tra chuỗi có phải số tiền hợp lệ không
 */
fun String.isValidCurrency(): Boolean {
    val cleaned = this.replace("[^0-9]".toRegex(), "")
    return cleaned.isNotEmpty() && cleaned.toLongOrNull() != null
}

/**
 * Format Double thành Long (làm tròn)
 * Dùng khi migrate dữ liệu cũ
 */
fun Double.toLongCurrency(): Long {
    return this.toLong()
}

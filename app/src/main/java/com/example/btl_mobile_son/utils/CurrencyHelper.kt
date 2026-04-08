package com.example.btl_mobile_son.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {
    private val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    
    fun format(amount: Long): String {
        return "${formatter.format(amount)}đ"
    }
    
    fun format(amount: Int): String {
        return format(amount.toLong())
    }
    
    fun format(amount: Double): String {
        return "${formatter.format(amount)}đ"
    }
    
    fun formatWithoutSymbol(amount: Long): String {
        return formatter.format(amount)
    }
}

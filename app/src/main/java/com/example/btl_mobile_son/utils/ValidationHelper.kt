package com.example.btl_mobile_son.utils

import android.widget.EditText

object ValidationHelper {
    
    fun validateRequired(editText: EditText, fieldName: String): Boolean {
        val text = editText.text.toString().trim()
        if (text.isEmpty()) {
            editText.error = "$fieldName không được để trống"
            editText.requestFocus()
            return false
        }
        return true
    }
    
    fun validatePhone(editText: EditText): Boolean {
        val phone = editText.text.toString().trim()
        if (phone.isEmpty()) {
            editText.error = "Số điện thoại không được để trống"
            editText.requestFocus()
            return false
        }
        if (!phone.matches(Regex("^0\\d{9}$"))) {
            editText.error = "Số điện thoại phải có 10 số và bắt đầu bằng 0"
            editText.requestFocus()
            return false
        }
        return true
    }
    
    fun validateEmail(editText: EditText): Boolean {
        val email = editText.text.toString().trim()
        if (email.isEmpty()) {
            return true // Email không bắt buộc
        }
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            editText.error = "Email không hợp lệ"
            editText.requestFocus()
            return false
        }
        return true
    }
    
    fun validatePositiveNumber(editText: EditText, fieldName: String): Boolean {
        val text = editText.text.toString().trim()
        if (text.isEmpty()) {
            editText.error = "$fieldName không được để trống"
            editText.requestFocus()
            return false
        }
        try {
            val number = text.toLong()
            if (number <= 0) {
                editText.error = "$fieldName phải lớn hơn 0"
                editText.requestFocus()
                return false
            }
        } catch (e: NumberFormatException) {
            editText.error = "$fieldName phải là số"
            editText.requestFocus()
            return false
        }
        return true
    }
    
    fun validateNonNegativeNumber(editText: EditText, fieldName: String): Boolean {
        val text = editText.text.toString().trim()
        if (text.isEmpty()) {
            editText.error = "$fieldName không được để trống"
            editText.requestFocus()
            return false
        }
        try {
            val number = text.toLong()
            if (number < 0) {
                editText.error = "$fieldName không được âm"
                editText.requestFocus()
                return false
            }
        } catch (e: NumberFormatException) {
            editText.error = "$fieldName phải là số"
            editText.requestFocus()
            return false
        }
        return true
    }
    
    fun validateUtilityReading(oldReading: Long, newReading: Long, editText: EditText): Boolean {
        if (newReading < oldReading) {
            editText.error = "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ ($oldReading)"
            editText.requestFocus()
            return false
        }
        return true
    }
    
    // Backward compatibility methods
    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }
    
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^0\\d{9}$"))
    }
    
    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    
    fun isValidCMND(cmnd: String): Boolean {
        return cmnd.matches(Regex("^\\d{9}$|^\\d{12}$"))
    }
    
    fun isValidAmount(amount: String): Boolean {
        return try {
            amount.toDouble() > 0
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    fun isValidAmount(amount: Double): Boolean {
        return amount > 0
    }
    
    fun isValidDateRange(startDate: Long, endDate: Long): Boolean {
        return endDate > startDate
    }
    
    fun getPhoneErrorMessage(): String {
        return "Số điện thoại phải có 10 số và bắt đầu bằng 0"
    }
    
    fun getEmailErrorMessage(): String {
        return "Email không hợp lệ"
    }
    
    fun getCMNDErrorMessage(): String {
        return "CMND/CCCD phải có 9 hoặc 12 số"
    }
    
    fun getAmountErrorMessage(): String {
        return "Số tiền phải lớn hơn 0"
    }
    
    fun getDateRangeErrorMessage(): String {
        return "Ngày kết thúc phải sau ngày bắt đầu"
    }
}

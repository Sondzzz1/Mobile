package com.example.btl_mobile_son.utils

import android.util.Patterns
import java.util.regex.Pattern

object ValidationHelper {

    /**
     * Kiểm tra số điện thoại Việt Nam (10-11 số)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.isBlank()) return false
        val phonePattern = Pattern.compile("^(0[3|5|7|8|9])[0-9]{8,9}$")
        return phonePattern.matcher(phone.trim()).matches()
    }

    /**
     * Kiểm tra email hợp lệ
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    /**
     * Kiểm tra CMND/CCCD (9 hoặc 12 số)
     */
    fun isValidCMND(cmnd: String): Boolean {
        if (cmnd.isBlank()) return false
        val cmndPattern = Pattern.compile("^[0-9]{9}$|^[0-9]{12}$")
        return cmndPattern.matcher(cmnd.trim()).matches()
    }

    /**
     * Kiểm tra ngày kết thúc > ngày bắt đầu
     */
    fun isValidDateRange(startDate: Long, endDate: Long): Boolean {
        return endDate > startDate
    }

    /**
     * Kiểm tra số tiền > 0
     */
    fun isValidAmount(amount: Double): Boolean {
        return amount > 0
    }

    /**
     * Kiểm tra chuỗi không rỗng
     */
    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    /**
     * Lấy thông báo lỗi cho số điện thoại
     */
    fun getPhoneErrorMessage(): String {
        return "Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 03, 05, 07, 08, 09)"
    }

    /**
     * Lấy thông báo lỗi cho email
     */
    fun getEmailErrorMessage(): String {
        return "Email không hợp lệ"
    }

    /**
     * Lấy thông báo lỗi cho CMND
     */
    fun getCMNDErrorMessage(): String {
        return "CMND/CCCD không hợp lệ (9 hoặc 12 số)"
    }

    /**
     * Lấy thông báo lỗi cho khoảng ngày
     */
    fun getDateRangeErrorMessage(): String {
        return "Ngày kết thúc phải sau ngày bắt đầu"
    }

    /**
     * Lấy thông báo lỗi cho số tiền
     */
    fun getAmountErrorMessage(): String {
        return "Số tiền phải lớn hơn 0"
    }
}

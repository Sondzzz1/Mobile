package com.example.btl_mobile_son

import android.content.Context
import android.content.SharedPreferences
import com.example.btl_mobile_son.data.model.NguoiDung

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "QuanLyNhaTroSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_USER_TYPE = "userType" // "admin" hoặc "tenant"
    }

    fun createLoginSession(nguoiDung: NguoiDung) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, nguoiDung.maNguoiDung)
            putString(KEY_USERNAME, nguoiDung.tenDangNhap)
            putString(KEY_FULL_NAME, nguoiDung.hoTen)
            putString(KEY_USER_TYPE, "admin")
            apply()
        }
    }

    fun createTenantLoginSession(maKhach: Long, hoTen: String, tenDangNhap: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, maKhach.toInt())
            putString(KEY_USERNAME, tenDangNhap)
            putString(KEY_FULL_NAME, hoTen)
            putString(KEY_USER_TYPE, "tenant")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, 0)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getFullName(): String? {
        return prefs.getString(KEY_FULL_NAME, null)
    }

    fun getUserType(): String? {
        return prefs.getString(KEY_USER_TYPE, null)
    }

    fun isAdmin(): Boolean {
        return getUserType() == "admin"
    }

    fun isTenant(): Boolean {
        return getUserType() == "tenant"
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}

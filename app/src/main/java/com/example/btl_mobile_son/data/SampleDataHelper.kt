package com.example.btl_mobile_son.data

import android.content.Context
import android.content.SharedPreferences
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class SampleDataHelper(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val KEY_SAMPLE_DATA_LOADED = "sample_data_loaded"

    suspend fun loadSampleDataIfNeeded() {
        if (prefs.getBoolean(KEY_SAMPLE_DATA_LOADED, false)) {
            return // Đã load rồi
        }

        withContext(Dispatchers.IO) {
            // Không thêm dữ liệu mẫu nữa theo yêu cầu của người dùng
            
            // Đánh dấu đã load sample data
            prefs.edit().putBoolean(KEY_SAMPLE_DATA_LOADED, true).apply()
        }
    }

    fun resetSampleData() {
        prefs.edit().putBoolean(KEY_SAMPLE_DATA_LOADED, false).apply()
    }
}

package com.example.btl_mobile_son.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.dao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class QuanLyCoSoDuLieu private constructor(context: Context) {
    
    private val dbHelper: HoTroCoSoDuLieu = HoTroCoSoDuLieu(context)
    private var _database: SQLiteDatabase? = null
    private val mutex = Mutex()

    /**
     * Hàm lấy database an toàn. Luôn gọi hàm này trước khi sử dụng các DAO
     * để đảm bảo database đã được mở ở background thread.
     */
    suspend fun getDatabase(): SQLiteDatabase = withContext(Dispatchers.IO) {
        _database?.let { if (it.isOpen) return@withContext it }
        
        mutex.withLock {
            _database?.let { if (it.isOpen) return@withContext it }
            android.util.Log.d("QuanLyCoSoDuLieu", "Opening database safely on IO thread...")
            val db = dbHelper.writableDatabase
            _database = db
            db
        }
    }

    /**
     * Lấy instance database hiện tại. 
     * CẢNH BÁO: Phải đảm bảo getDatabase() đã được gọi thành công trước đó.
     */
    private fun getDb(): SQLiteDatabase {
        val db = _database
        if (db == null || !db.isOpen) {
            // Nếu chưa có, vẫn phải mở nhưng log cảnh báo vì đây là nguy cơ gây treo máy
            android.util.Log.w("QuanLyCoSoDuLieu", "DATABASE ACCESS BEFORE INITIALIZATION! This may cause ANR.")
            return dbHelper.writableDatabase.also { _database = it }
        }
        return db
    }

    // Các DAO sử dụng instance database đã mở
    val nhaTroDao by lazy { NhaTroDao(getDb()) }
    val phongDao by lazy { PhongDao(getDb()) }
    val khachThueDao by lazy { KhachThueDao(getDb()) }
    val hopDongDao by lazy { HopDongDao(getDb()) }
    val hopDongThanhVienDao by lazy { HopDongThanhVienDao(getDb()) }
    val dichVuDao by lazy { DichVuDao(getDb()) }
    val phongDichVuDao by lazy { PhongDichVuDao(getDb()) }
    val datCocDao by lazy { DatCocDao(getDb()) }
    val chiSoDienNuocDao by lazy { ChiSoDienNuocDao(getDb()) }
    val hoaDonDao by lazy { HoaDonDao(getDb()) }
    val chiTietHoaDonDao by lazy { ChiTietHoaDonDao(getDb()) }
    val giaoDichDao by lazy { GiaoDichDao(getDb()) }
    val suCoDao by lazy { SuCoDao(getDb()) }
    val nguoiDungDao by lazy { NguoiDungDao(getDb()) }
    
    // Alias để tránh lỗi compile nếu code cũ dùng getDbInternal
    private fun getDbInternal() = getDb()

    /**
     * Kích hoạt việc mở database ở background thread để tránh delay khi sử dụng lần đầu.
     */
    fun triggerInitialization() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getDatabase()
            } catch (e: Exception) {
                android.util.Log.e("QuanLyCoSoDuLieu", "Error triggering initialization", e)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: QuanLyCoSoDuLieu? = null

        fun getInstance(context: Context): QuanLyCoSoDuLieu {
            return INSTANCE ?: synchronized(this) {
                val instance = QuanLyCoSoDuLieu(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun close() {
        _database?.close()
        dbHelper.close()
    }
}

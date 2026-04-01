package com.example.btl_mobile_son.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.btl_mobile_son.data.dao.*

class DatabaseManager private constructor(context: Context) {
    
    private val dbHelper: DatabaseHelper = DatabaseHelper(context)
    private val database: SQLiteDatabase = dbHelper.writableDatabase

    // Các DAO
    val nhaTroDao: NhaTroDao by lazy { NhaTroDao(database) }
    val phongDao: PhongDao by lazy { PhongDao(database) }
    val khachThueDao: KhachThueDao by lazy { KhachThueDao(database) }
    val hopDongDao: HopDongDao by lazy { HopDongDao(database) }
    val hopDongThanhVienDao: HopDongThanhVienDao by lazy { HopDongThanhVienDao(database) }
    val dichVuDao: DichVuDao by lazy { DichVuDao(database) }
    val phongDichVuDao: PhongDichVuDao by lazy { PhongDichVuDao(database) } // VĐ1: DAO mới
    val datCocDao: DatCocDao by lazy { DatCocDao(database) }
    val chiSoDienNuocDao: ChiSoDienNuocDao by lazy { ChiSoDienNuocDao(database) }
    val hoaDonDao: HoaDonDao by lazy { HoaDonDao(database) }
    val chiTietHoaDonDao: ChiTietHoaDonDao by lazy { ChiTietHoaDonDao(database) }
    val giaoDichDao: GiaoDichDao by lazy { GiaoDichDao(database) }

    companion object {
        @Volatile
        private var INSTANCE: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun close() {
        database.close()
        dbHelper.close()
    }
}

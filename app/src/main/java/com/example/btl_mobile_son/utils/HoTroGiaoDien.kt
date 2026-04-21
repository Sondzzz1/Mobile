package com.example.btl_mobile_son.utils

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

object HoTroGiaoDien {
    
    /**
     * Hiển thị dialog xác nhận xóa
     */
    fun showDeleteConfirmation(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Xóa") { _, _ -> onConfirm() }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    /**
     * Hiển thị thông báo thành công
     */
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, "✓ $message", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Hiển thị thông báo lỗi
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, "✗ $message", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Hiển thị thông báo thông thường
     */
    fun showValue(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Hiển thị loading
     */
    fun showLoading(progressBar: ProgressBar?) {
        progressBar?.visibility = View.VISIBLE
    }
    
    /**
     * Ẩn loading
     */
    fun hideLoading(progressBar: ProgressBar?) {
        progressBar?.visibility = View.GONE
    }
    
    /**
     * Hiển thị empty state
     */
    fun showEmptyState(emptyView: View?, recyclerView: View?) {
        emptyView?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
    }
    
    /**
     * Ẩn empty state
     */
    fun hideEmptyState(emptyView: View?, recyclerView: View?) {
        emptyView?.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
    }
}

package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.HoaDonAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantInvoiceListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: HoaDonAdapter
    private var maKhach: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tenant_invoice_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dbManager = DatabaseManager.getInstance(requireContext())
            sessionManager = SessionManager(requireContext())
            maKhach = sessionManager.getUserId().toLong()

            val recyclerView = view.findViewById<RecyclerView>(R.id.rvTenantInvoices)
            recyclerView.layoutManager = LinearLayoutManager(context)

            adapter = HoaDonAdapter(
                onItemClick = { hoaDon ->
                    // Show invoice details
                    android.widget.Toast.makeText(context, "Chi tiết hóa đơn #${hoaDon.maHoaDon}", android.widget.Toast.LENGTH_SHORT).show()
                },
                onItemLongClick = { hoaDon ->
                    // Tenant cannot edit
                    false
                }
            )
            recyclerView.adapter = adapter

            loadInvoices()
        } catch (e: Exception) {
            android.util.Log.e("TenantInvoiceList", "Error in onViewCreated", e)
            android.widget.Toast.makeText(context, "Lỗi khởi tạo: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun loadInvoices() {
        lifecycleScope.launch {
            try {
                val invoices = withContext(Dispatchers.IO) {
                    try {
                        // Get active contract
                        val hopDong = dbManager.hopDongDao.layHopDongHienTaiCuaKhach(maKhach)
                        
                        if (hopDong != null) {
                            // Get all invoices for this contract
                            dbManager.hoaDonDao.layTatCa().filter { it.maHopDong == hopDong.maHopDong }
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("TenantInvoiceList", "Error loading invoices", e)
                        emptyList()
                    }
                }

                adapter.capNhatDanhSach(invoices)

                if (invoices.isEmpty()) {
                    view?.findViewById<android.widget.TextView>(R.id.tvEmptyInvoices)?.visibility = View.VISIBLE
                } else {
                    view?.findViewById<android.widget.TextView>(R.id.tvEmptyInvoices)?.visibility = View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("TenantInvoiceList", "Error in loadInvoices", e)
                android.widget.Toast.makeText(context, "Lỗi tải hóa đơn: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

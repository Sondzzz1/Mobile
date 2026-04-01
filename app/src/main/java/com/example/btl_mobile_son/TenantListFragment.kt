package com.example.btl_mobile_son

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.KhachThueAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: KhachThueAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tenant_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("TenantListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val rvTenantList = view.findViewById<RecyclerView>(R.id.rvTenantList)
        adapter = KhachThueAdapter(
            danhSach = emptyList(),
            onItemClick = { khach ->
                val fragment = CreateTenantFragment().apply {
                    arguments = Bundle().apply { putLong("maKhach", khach.maKhach) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onItemLongClick = { khach ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa khách thuê")
                    .setMessage("Xóa \"${khach.hoTen}\"?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.khachThueDao.xoa(khach.maKhach)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                taiDuLieu(view)
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null).show()
            }
        )
        rvTenantList.layoutManager = LinearLayoutManager(requireContext())
        rvTenantList.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<View>(R.id.btnAddTenant).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateTenantFragment())
                .addToBackStack(null).commit()
        }

        // Tìm kiếm
        view.findViewById<EditText>(R.id.etSearchTenant)?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val tuKhoa = s.toString().trim()
                CoroutineScope(Dispatchers.IO).launch {
                    val ds = if (tuKhoa.isEmpty()) dbManager.khachThueDao.layTatCa()
                             else dbManager.khachThueDao.timKiem(tuKhoa)
                    withContext(Dispatchers.Main) { adapter.capNhatDanhSach(ds) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        taiDuLieu(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { taiDuLieu(it) }
    }

    private fun taiDuLieu(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val danhSach = dbManager.khachThueDao.layTatCa()
                withContext(Dispatchers.Main) {
                    adapter.capNhatDanhSach(danhSach)
                    view.findViewById<TextView>(R.id.tvTenantCount)?.text = "${danhSach.size}"
                }
            } catch (e: Exception) {
                android.util.Log.e("TenantListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

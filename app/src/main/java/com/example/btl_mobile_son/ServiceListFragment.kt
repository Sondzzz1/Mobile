package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.DichVuAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.DichVu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServiceListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: DichVuAdapter
    private val danhSach = mutableListOf<DichVu>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_service_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("ServiceListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerService)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = DichVuAdapter(danhSach,
            onEdit = { dichVu ->
                val fragment = CreateServiceFragment().apply {
                    arguments = Bundle().apply { putLong("maDichVu", dichVu.maDichVu) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onDelete = { dichVu ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa dịch vụ")
                    .setMessage("Xóa dịch vụ '${dichVu.tenDichVu}' (${dichVu.donGia.toLong()}đ)?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.dichVuDao.xoa(dichVu.maDichVu)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "✓ Đã xóa", Toast.LENGTH_SHORT).show()
                                taiDuLieu(recyclerView, tvEmpty)
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null).show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnAddService).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateServiceFragment())
                .addToBackStack(null).commit()
        }

        taiDuLieu(recyclerView, tvEmpty)
    }

    override fun onResume() {
        super.onResume()
        val rv = view?.findViewById<RecyclerView>(R.id.recyclerService) ?: return
        val tvEmpty = view?.findViewById<TextView>(R.id.tvEmpty) ?: return
        taiDuLieu(rv, tvEmpty)
    }

    private fun taiDuLieu(recyclerView: RecyclerView, tvEmpty: TextView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ds = dbManager.dichVuDao.layTatCa()
                withContext(Dispatchers.Main) {
                    danhSach.clear()
                    danhSach.addAll(ds)
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (ds.isEmpty()) View.GONE else View.VISIBLE
                }
            } catch (e: Exception) {
                android.util.Log.e("ServiceListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

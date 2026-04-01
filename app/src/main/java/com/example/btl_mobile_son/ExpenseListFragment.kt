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
import com.example.btl_mobile_son.adapter.GiaoDichAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.GiaoDich
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ExpenseListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: GiaoDichAdapter
    private val danhSach = mutableListOf<GiaoDich>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_expense_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("ExpenseListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerExpense)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        val tvTongChi = view.findViewById<TextView>(R.id.tvTongChi)

        adapter = GiaoDichAdapter(danhSach,
            onEdit = { gd ->
                val fragment = CreateExpenseFragment().apply {
                    arguments = Bundle().apply { putLong("maGiaoDich", gd.maGiaoDich) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onDelete = { gd ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa khoản chi")
                    .setMessage("Xóa khoản chi \"${gd.noiDung}\"?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.giaoDichDao.xoa(gd.maGiaoDich)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                taiDuLieu(recyclerView, tvEmpty, tvTongChi)
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

        view.findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateExpenseFragment())
                .addToBackStack(null).commit()
        }

        taiDuLieu(recyclerView, tvEmpty, tvTongChi)
    }

    override fun onResume() {
        super.onResume()
        val rv = view?.findViewById<RecyclerView>(R.id.recyclerExpense) ?: return
        val tvEmpty = view?.findViewById<TextView>(R.id.tvEmpty) ?: return
        val tvTong = view?.findViewById<TextView>(R.id.tvTongChi) ?: return
        taiDuLieu(rv, tvEmpty, tvTong)
    }

    private fun taiDuLieu(recyclerView: RecyclerView, tvEmpty: TextView, tvTong: TextView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ds = dbManager.giaoDichDao.layTheoLoai("chi")
                val tong = dbManager.giaoDichDao.tinhTongTheoLoai("chi")
                withContext(Dispatchers.Main) {
                    danhSach.clear()
                    danhSach.addAll(ds)
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (ds.isEmpty()) View.GONE else View.VISIBLE
                    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                    tvTong.text = "Tổng chi: ${fmt.format(tong)} đ"
                }
            } catch (e: Exception) {
                android.util.Log.e("ExpenseListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

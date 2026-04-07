package com.example.btl_mobile_son

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    private var danhSachDayDu = listOf<GiaoDich>()
    private var categoryFilter = "all"

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

        // Category filter
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategoryFilter)
        val categories = arrayOf("Tất cả", "Sửa chữa", "Điện", "Nước", "Vệ sinh", "Bảo trì", "Lương", "Khác")
        spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoryFilter = when (position) {
                    1 -> "Sửa chữa"
                    2 -> "Điện"
                    3 -> "Nước"
                    4 -> "Vệ sinh"
                    5 -> "Bảo trì"
                    6 -> "Lương"
                    7 -> "Khác"
                    else -> "all"
                }
                applyFilters(recyclerView, tvEmpty, tvTongChi)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Search
        view.findViewById<EditText>(R.id.etSearchExpense)?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilters(recyclerView, tvEmpty, tvTongChi)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        taiDuLieu(recyclerView, tvEmpty, tvTongChi)
    }

    private fun applyFilters(recyclerView: RecyclerView, tvEmpty: TextView, tvTong: TextView) {
        val tuKhoa = view?.findViewById<EditText>(R.id.etSearchExpense)?.text.toString().trim().lowercase()
        
        var filtered = danhSachDayDu
        
        // Filter by category
        if (categoryFilter != "all") {
            filtered = filtered.filter { it.danhMuc == categoryFilter }
        }
        
        // Filter by search keyword
        if (tuKhoa.isNotEmpty()) {
            filtered = filtered.filter { gd ->
                gd.noiDung.lowercase().contains(tuKhoa) ||
                gd.danhMuc.lowercase().contains(tuKhoa) ||
                gd.tenNguoi.lowercase().contains(tuKhoa)
            }
        }
        
        danhSach.clear()
        danhSach.addAll(filtered)
        adapter.notifyDataSetChanged()
        
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        
        val tong = filtered.sumOf { it.soTien }
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        tvTong.text = "Tổng chi: ${fmt.format(tong)} đ"
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
                    danhSachDayDu = ds
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

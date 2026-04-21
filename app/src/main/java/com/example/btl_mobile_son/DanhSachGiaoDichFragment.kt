package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.GiaoDichAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.GiaoDich
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DanhSachGiaoDichFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: GiaoDichAdapter
    private val danhSach = mutableListOf<GiaoDich>()
    private var currentFilter = "all" // all, thu, chi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Lỗi khởi tạo database", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTransactions)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = GiaoDichAdapter(danhSach,
            onEdit = { gd ->
                val fragment = TaoGiaoDichFragment().apply {
                    arguments = Bundle().apply { putLong("maGiaoDich", gd.maGiaoDich) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onDelete = { gd ->
                com.example.btl_mobile_son.utils.UIHelper.showDeleteConfirmation(
                    requireContext(),
                    "Xóa giao dịch này?",
                    "Bạn có chắc chắn muốn xóa giao dịch này không?"
                ) {
                    deleteTransaction(gd.maGiaoDich)
                }
            }
        )
        recyclerView.adapter = adapter

        // Filter spinner
        val spinnerFilter = view.findViewById<Spinner>(R.id.spinnerTransactionFilter)
        val filterOptions = arrayOf("Tất cả", "Thu", "Chi")
        spinnerFilter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterOptions)
        
        spinnerFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, position: Int, id: Long) {
                currentFilter = when (position) {
                    1 -> "thu"
                    2 -> "chi"
                    else -> "all"
                }
                loadData()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        view.findViewById<Button>(R.id.btnAddTransaction).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TaoGiaoDichFragment())
                .addToBackStack(null).commit()
        }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    val all = dbManager.giaoDichDao.layTatCa()
                    when (currentFilter) {
                        "thu" -> all.filter { it.loai == "thu" }
                        "chi" -> all.filter { it.loai == "chi" }
                        else -> all
                    }
                }

                danhSach.clear()
                danhSach.addAll(data)
                adapter.notifyDataSetChanged()

                view?.findViewById<android.widget.TextView>(R.id.tvEmptyTransactions)?.visibility =
                    if (danhSach.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Lỗi tải dữ liệu", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTransaction(maGiaoDich: Long) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dbManager.giaoDichDao.xoa(maGiaoDich)
                }
                android.widget.Toast.makeText(context, "✓ Đã xóa giao dịch", android.widget.Toast.LENGTH_SHORT).show()
                loadData()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "✗ Lỗi xóa giao dịch", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

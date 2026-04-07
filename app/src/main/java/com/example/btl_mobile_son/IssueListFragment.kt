package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.SuCoAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.SuCo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IssueListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SuCoAdapter
    private lateinit var spinnerFilter: Spinner
    private var danhSachSuCo = mutableListOf<SuCo>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        recyclerView = view.findViewById(R.id.rvIssueList)
        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        val btnAdd = view.findViewById<Button>(R.id.btnAddIssue)
        val layoutEmpty = view.findViewById<View>(R.id.layoutEmpty)

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Setup filter spinner
        val filterOptions = arrayOf("Tất cả", "Chưa xử lý", "Đang xử lý", "Đã xử lý")
        spinnerFilter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterOptions)
        
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup RecyclerView
        adapter = SuCoAdapter(danhSachSuCo, dbManager) { suCo ->
            // Click to edit
            val fragment = CreateIssueFragment.newInstance(suCo.maSuCo)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateIssueFragment())
                .addToBackStack(null)
                .commit()
        }

        loadData()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filterPosition = spinnerFilter.selectedItemPosition
                val allIssues = dbManager.suCoDao.layTatCa()
                
                val filteredList = when (filterPosition) {
                    1 -> allIssues.filter { it.trangThai == "chua_xu_ly" }
                    2 -> allIssues.filter { it.trangThai == "dang_xu_ly" }
                    3 -> allIssues.filter { it.trangThai == "da_xu_ly" }
                    else -> allIssues
                }

                withContext(Dispatchers.Main) {
                    danhSachSuCo.clear()
                    danhSachSuCo.addAll(filteredList)
                    adapter.notifyDataSetChanged()
                    
                    if (danhSachSuCo.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        view?.findViewById<View>(R.id.layoutEmpty)?.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        view?.findViewById<View>(R.id.layoutEmpty)?.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}

package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.ChiSoAdapter
import com.example.btl_mobile_son.adapter.ChiSoDisplay
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.ChiSoDienNuoc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class UtilityListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: ChiSoAdapter
    private val danhSach = mutableListOf<ChiSoDisplay>()
    private var thangHienTai = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var namHienTai = Calendar.getInstance().get(Calendar.YEAR)
    private var xemTatCa = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_utility_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("UtilityListFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerUtility)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        
        // CẢI TIẾN 3: Bộ lọc tháng/năm
        val spinnerThang = view.findViewById<Spinner>(R.id.spinnerThang)
        val spinnerNam = view.findViewById<Spinner>(R.id.spinnerNam)
        val btnXemTatCa = view.findViewById<Button>(R.id.btnXemTatCa)

        // Setup spinner tháng
        val thangList = (1..12).map { "Tháng $it" }
        spinnerThang?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, thangList)
        spinnerThang?.setSelection(thangHienTai - 1)
        
        // Setup spinner năm
        val namList = (2020..2030).toList()
        spinnerNam?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, namList)
        spinnerNam?.setSelection(namList.indexOf(namHienTai))

        adapter = ChiSoAdapter(danhSach,
            onEdit = { chiSo ->
                val fragment = CreateUtilityFragment().apply {
                    arguments = Bundle().apply { putLong("maChiSo", chiSo.maChiSo) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
            },
            onDelete = { chiSo ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa chỉ số")
                    .setMessage("Xóa chỉ số tháng ${chiSo.thang}/${chiSo.nam}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dbManager.chiSoDienNuocDao.xoa(chiSo.maChiSo)
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

        view.findViewById<Button>(R.id.btnLock).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateUtilityFragment())
                .addToBackStack(null).commit()
        }
        
        // Listener cho bộ lọc
        spinnerThang?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (!xemTatCa) {
                    thangHienTai = pos + 1
                    taiDuLieu(recyclerView, tvEmpty)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        spinnerNam?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (!xemTatCa) {
                    namHienTai = namList[pos]
                    taiDuLieu(recyclerView, tvEmpty)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        btnXemTatCa?.setOnClickListener {
            xemTatCa = !xemTatCa
            btnXemTatCa.text = if (xemTatCa) "Lọc" else "Tất cả"
            spinnerThang?.isEnabled = !xemTatCa
            spinnerNam?.isEnabled = !xemTatCa
            taiDuLieu(recyclerView, tvEmpty)
        }

        taiDuLieu(recyclerView, tvEmpty)
    }

    override fun onResume() {
        super.onResume()
        val rv = view?.findViewById<RecyclerView>(R.id.recyclerUtility) ?: return
        val tvEmpty = view?.findViewById<TextView>(R.id.tvEmpty) ?: return
        taiDuLieu(rv, tvEmpty)
    }

    private fun taiDuLieu(recyclerView: RecyclerView, tvEmpty: TextView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lấy dữ liệu theo bộ lọc
                val ds = if (xemTatCa) {
                    // Lấy tất cả chỉ số, sắp xếp theo năm, tháng giảm dần
                    val allChiSo = mutableListOf<ChiSoDienNuoc>()
                    for (y in 2030 downTo 2020) {
                        for (m in 12 downTo 1) {
                            allChiSo.addAll(dbManager.chiSoDienNuocDao.layTheoThangNam(m, y))
                        }
                    }
                    allChiSo
                } else {
                    dbManager.chiSoDienNuocDao.layTheoThangNam(thangHienTai, namHienTai)
                }
                
                // CẢI TIẾN 2: Map sang ChiSoDisplay với tên phòng đầy đủ
                val dsHienThi = ds.map { chiSo ->
                    val phong = dbManager.phongDao.layTheoMa(chiSo.maPhong)
                    val nha = phong?.let { dbManager.nhaTroDao.layTheoMa(it.maNha) }
                    ChiSoDisplay(
                        chiSo = chiSo,
                        tenPhong = phong?.tenPhong ?: "Phòng ${chiSo.maPhong}",
                        tenNha = nha?.tenNha ?: "?"
                    )
                }
                
                withContext(Dispatchers.Main) {
                    danhSach.clear()
                    danhSach.addAll(dsHienThi)
                    adapter.notifyDataSetChanged()
                    val emptyView = view?.findViewById<View>(R.id.emptyStateView)
                    val dataView = view?.findViewById<View>(R.id.dataTableView)
                    if (dsHienThi.isEmpty()) {
                        emptyView?.visibility = View.VISIBLE
                        dataView?.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyView?.visibility = View.GONE
                        dataView?.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("UtilityListFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

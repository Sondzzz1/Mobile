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

class DanhSachChiSoFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: ChiSoAdapter
    private val danhSach = mutableListOf<ChiSoDisplay>()
    private var thangHienTai = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var namHienTai = Calendar.getInstance().get(Calendar.YEAR)
    private var xemTatCa = false
    
    private var danhSachNha = listOf<com.example.btl_mobile_son.data.model.NhaTro>()
    private var danhSachPhong = listOf<com.example.btl_mobile_son.data.model.Phong>()
    private var maNhaChon: Long = -1L
    private var maPhongChon: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_utility_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("DanhSachChiSoFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerUtility)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        
        // Bộ lọc nhà/phòng/tháng/năm
        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)
        val spinnerRoom = view.findViewById<Spinner>(R.id.spinnerRoom)
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
        
        // Load danh sách nhà
        CoroutineScope(Dispatchers.IO).launch {
            danhSachNha = dbManager.nhaTroDao.layTatCa()
            
            withContext(Dispatchers.Main) {
                if (danhSachNha.isEmpty()) {
                    Toast.makeText(context, "Chưa có nhà trọ nào!", Toast.LENGTH_SHORT).show()
                    return@withContext
                }
                
                // Tạo danh sách với "Tất cả nhà" ở đầu
                val tenNhaList = mutableListOf("Tất cả nhà")
                tenNhaList.addAll(danhSachNha.map { it.tenNha })
                
                spinnerHouse.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    tenNhaList
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                
                // Listener chọn nhà → load phòng
                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                        if (position == 0) {
                            // Tất cả nhà
                            maNhaChon = -1L
                            maPhongChon = -1L
                            
                            // Load tất cả phòng
                            CoroutineScope(Dispatchers.IO).launch {
                                danhSachPhong = dbManager.phongDao.layTatCa()
                                withContext(Dispatchers.Main) {
                                    val tenPhongList = mutableListOf("Tất cả phòng")
                                    tenPhongList.addAll(danhSachPhong.map { "${it.tenPhong} - ${danhSachNha.find { n -> n.maNha == it.maNha }?.tenNha ?: ""}" })
                                    
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        tenPhongList
                                    ).apply {
                                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    }
                                    taiDuLieu(recyclerView, tvEmpty)
                                }
                            }
                        } else {
                            // Nhà cụ thể
                            maNhaChon = danhSachNha[position - 1].maNha
                            maPhongChon = -1L
                            
                            // Load phòng của nhà này
                            CoroutineScope(Dispatchers.IO).launch {
                                danhSachPhong = dbManager.phongDao.layTheoNha(maNhaChon)
                                withContext(Dispatchers.Main) {
                                    val tenPhongList = mutableListOf("Tất cả phòng")
                                    tenPhongList.addAll(danhSachPhong.map { it.tenPhong })
                                    
                                    spinnerRoom.adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        tenPhongList
                                    ).apply {
                                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    }
                                    taiDuLieu(recyclerView, tvEmpty)
                                }
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                
                // Listener chọn phòng
                spinnerRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                        if (position == 0) {
                            maPhongChon = -1L
                        } else {
                            maPhongChon = danhSachPhong[position - 1].maPhong
                        }
                        taiDuLieu(recyclerView, tvEmpty)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        adapter = ChiSoAdapter(danhSach,
            onEdit = { chiSo ->
                val fragment = TaoChiSoFragment().apply {
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
                .replace(R.id.fragment_container, TaoChiSoFragment())
                .addToBackStack(null).commit()
        }
        
        // Listener cho bộ lọc tháng/năm
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
            spinnerHouse?.isEnabled = !xemTatCa
            spinnerRoom?.isEnabled = !xemTatCa
            taiDuLieu(recyclerView, tvEmpty)
        }
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
                var ds = if (xemTatCa) {
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
                
                // Lọc theo nhà và phòng
                if (maNhaChon > 0) {
                    ds = ds.filter { chiSo ->
                        val phong = dbManager.phongDao.layTheoMa(chiSo.maPhong)
                        phong?.maNha == maNhaChon
                    }
                }
                
                if (maPhongChon > 0) {
                    ds = ds.filter { it.maPhong == maPhongChon }
                }
                
                // Map sang ChiSoDisplay với tên phòng đầy đủ
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
                android.util.Log.e("DanhSachChiSoFragment", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

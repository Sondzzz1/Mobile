package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.DichVuAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.DichVu
import com.example.btl_mobile_son.utils.UIHelper
import com.example.btl_mobile_son.utils.CurrencyHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServiceListFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var adapter: DichVuAdapter
    private val danhSach = mutableListOf<DichVu>()
    private var danhSachNha = listOf<com.example.btl_mobile_son.data.model.NhaTro>()
    private var maNhaChon: Long = -1L

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
        val spinnerHouse = view.findViewById<Spinner>(R.id.spinnerHouse)

        // Load danh sách nhà và setup spinner
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
                
                // Listener khi chọn nhà
                spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                        if (position == 0) {
                            // Chọn "Tất cả nhà"
                            maNhaChon = -1L
                        } else {
                            // Chọn nhà cụ thể
                            maNhaChon = danhSachNha[position - 1].maNha
                        }
                        taiDuLieu(recyclerView, tvEmpty)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

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
                UIHelper.showDeleteConfirmation(
                    requireContext(),
                    "Xóa dịch vụ",
                    "Bạn có chắc muốn xóa dịch vụ '${dichVu.tenDichVu}' (${CurrencyHelper.format(dichVu.donGia.toLong())})?"
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dbManager.dichVuDao.xoa(dichVu.maDichVu)
                        withContext(Dispatchers.Main) {
                            UIHelper.showSuccess(requireContext(), "Đã xóa dịch vụ")
                            taiDuLieu(recyclerView, tvEmpty)
                        }
                    }
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.btnAddService).setOnClickListener {
            // Truyền maNha nếu đang chọn nhà cụ thể
            val fragment = CreateServiceFragment().apply {
                if (maNhaChon > 0) {
                    arguments = Bundle().apply { putLong("maNha", maNhaChon) }
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit()
        }

        // Không gọi taiDuLieu ở đây vì spinner listener sẽ tự động gọi
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
                // Lọc theo nhà nếu có chọn
                val ds = if (maNhaChon > 0) {
                    dbManager.dichVuDao.layTatCa().filter { it.maNha == maNhaChon }
                } else {
                    dbManager.dichVuDao.layTatCa()
                }
                
                // Lấy tên nhà để hiển thị
                val tenNha = if (maNhaChon > 0) {
                    dbManager.nhaTroDao.layTheoMa(maNhaChon)?.tenNha ?: "Nhà trọ"
                } else {
                    "Tất cả nhà"
                }
                
                withContext(Dispatchers.Main) {
                    danhSach.clear()
                    danhSach.addAll(ds)
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (ds.isEmpty()) View.GONE else View.VISIBLE
                    
                    view?.findViewById<TextView>(R.id.tvServiceCount)?.text = "$tenNha - ${ds.size} dịch vụ"
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

package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.adapter.SuCoAdapter
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.SuCo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantIssueReportFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: SuCoAdapter
    private var maKhach: Long = 0
    private var maPhong: Long = 0
    private var issueList = mutableListOf<SuCo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tenant_issue_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbManager = DatabaseManager.getInstance(requireContext())
        sessionManager = SessionManager(requireContext())
        maKhach = sessionManager.getUserId().toLong()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTenantIssues)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = SuCoAdapter(
            danhSach = issueList,
            dbManager = dbManager,
            onItemClick = { suco ->
                android.widget.Toast.makeText(context, "Sự cố: ${suco.moTa}", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btnReportIssue).setOnClickListener {
            reportIssue()
        }

        loadRoomAndIssues()
    }

    private fun loadRoomAndIssues() {
        lifecycleScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    try {
                        val hopDong = dbManager.hopDongDao.layHopDongHienTaiCuaKhach(maKhach)
                        val phongId = hopDong?.maPhong?.toInt() ?: 0
                        val issues = if (phongId > 0) {
                            dbManager.suCoDao.layTheoPhong(phongId)
                        } else {
                            emptyList()
                        }
                        Pair(phongId, issues)
                    } catch (e: Exception) {
                        android.util.Log.e("TenantIssueReport", "Error loading data", e)
                        Pair(0, emptyList())
                    }
                }

                maPhong = data.first.toLong()
                issueList.clear()
                issueList.addAll(data.second)
                adapter.notifyDataSetChanged()

                if (data.second.isEmpty()) {
                    view?.findViewById<android.widget.TextView>(R.id.tvEmptyIssues)?.visibility = View.VISIBLE
                } else {
                    view?.findViewById<android.widget.TextView>(R.id.tvEmptyIssues)?.visibility = View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("TenantIssueReport", "Error in loadRoomAndIssues", e)
                android.widget.Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reportIssue() {
        val etDescription = view?.findViewById<EditText>(R.id.etIssueDescription)
        val description = etDescription?.text.toString().trim()

        if (description.isEmpty()) {
            android.widget.Toast.makeText(context, "Vui lòng nhập mô tả sự cố", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (maPhong == 0L) {
            android.widget.Toast.makeText(context, "Không tìm thấy thông tin phòng", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val suco = SuCo(
                        maSuCo = 0,
                        maPhong = maPhong.toInt(),
                        loaiSuCo = "Khác",
                        moTa = description,
                        trangThai = "chua_xu_ly",
                        nguoiBaoGao = sessionManager.getFullName() ?: "",
                        nguoiXuLy = "",
                        ngayBaoGao = System.currentTimeMillis(),
                        ngayXuLy = null,
                        chiPhi = 0.0,
                        ghiChu = ""
                    )
                    dbManager.suCoDao.them(suco)
                }

                android.widget.Toast.makeText(context, "✓ Đã gửi báo cáo sự cố", android.widget.Toast.LENGTH_SHORT).show()
                etDescription?.setText("")
                loadRoomAndIssues()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "✗ Lỗi: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

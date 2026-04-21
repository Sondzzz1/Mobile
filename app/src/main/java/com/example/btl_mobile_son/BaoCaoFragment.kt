package com.example.btl_mobile_son

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.GiaoDich
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BaoCaoFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var thangChon = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var namChon = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        val etMonth = view.findViewById<EditText>(R.id.etMonth)
        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Set tháng hiện tại
        etMonth.setText("$thangChon/$namChon")

        // Chọn tháng
        etMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, _ ->
                    thangChon = month + 1
                    namChon = year
                    etMonth.setText("$thangChon/$namChon")
                },
                namChon,
                thangChon - 1,
                1
            ).show()
        }

        // Nút lọc
        btnFilter.setOnClickListener {
            loadReport(view)
        }

        // Load báo cáo tháng hiện tại
        loadReport(view)
    }

    private fun loadReport(view: View) {
        val tvTongThu = view.findViewById<TextView>(R.id.tvTongThu)
        val tvTongChi = view.findViewById<TextView>(R.id.tvTongChi)
        val tvLoiNhuan = view.findViewById<TextView>(R.id.tvLoiNhuan)
        val rvThuList = view.findViewById<RecyclerView>(R.id.rvThuList)
        val rvChiList = view.findViewById<RecyclerView>(R.id.rvChiList)
        val layoutEmptyThu = view.findViewById<View>(R.id.layoutEmptyThu)
        val layoutEmptyChi = view.findViewById<View>(R.id.layoutEmptyChi)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lấy giao dịch theo tháng
                val giaoDichThang = dbManager.giaoDichDao.layTatCa().filter { gd ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = gd.ngayGiaoDich
                    cal.get(Calendar.MONTH) + 1 == thangChon && cal.get(Calendar.YEAR) == namChon
                }

                val danhSachThu = giaoDichThang.filter { it.loai == "thu" }.sortedByDescending { it.ngayGiaoDich }
                val danhSachChi = giaoDichThang.filter { it.loai == "chi" }.sortedByDescending { it.ngayGiaoDich }

                val tongThu = danhSachThu.sumOf { it.soTien }
                val tongChi = danhSachChi.sumOf { it.soTien }
                val loiNhuan = tongThu - tongChi

                withContext(Dispatchers.Main) {
                    // Hiển thị tổng
                    tvTongThu?.text = "${formatter.format(tongThu)}đ"
                    tvTongChi?.text = "${formatter.format(tongChi)}đ"
                    tvLoiNhuan?.text = "${formatter.format(loiNhuan)}đ"
                    tvLoiNhuan?.setTextColor(
                        if (loiNhuan >= 0) android.graphics.Color.parseColor("#4CAF50") 
                        else android.graphics.Color.parseColor("#F44336")
                    )

                    // Hiển thị danh sách thu
                    if (danhSachThu.isEmpty()) {
                        rvThuList?.visibility = View.GONE
                        layoutEmptyThu?.visibility = View.VISIBLE
                    } else {
                        rvThuList?.visibility = View.VISIBLE
                        layoutEmptyThu?.visibility = View.GONE
                        rvThuList?.layoutManager = LinearLayoutManager(requireContext())
                        rvThuList?.adapter = GiaoDichReportAdapter(danhSachThu, formatter, sdf)
                    }

                    // Hiển thị danh sách chi
                    if (danhSachChi.isEmpty()) {
                        rvChiList?.visibility = View.GONE
                        layoutEmptyChi?.visibility = View.VISIBLE
                    } else {
                        rvChiList?.visibility = View.VISIBLE
                        layoutEmptyChi?.visibility = View.GONE
                        rvChiList?.layoutManager = LinearLayoutManager(requireContext())
                        rvChiList?.adapter = GiaoDichReportAdapter(danhSachChi, formatter, sdf)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BaoCaoFragment", "Error loading report", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi tải báo cáo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Adapter đơn giản cho danh sách giao dịch
class GiaoDichReportAdapter(
    private val danhSach: List<GiaoDich>,
    private val formatter: NumberFormat,
    private val sdf: SimpleDateFormat
) : RecyclerView.Adapter<GiaoDichReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNgay: TextView = view.findViewById(R.id.tvNgay)
        val tvDanhMuc: TextView = view.findViewById(R.id.tvDanhMuc)
        val tvNoiDung: TextView = view.findViewById(R.id.tvNoiDung)
        val tvSoTien: TextView = view.findViewById(R.id.tvSoTien)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_giao_dich_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gd = danhSach[position]
        holder.tvNgay.text = sdf.format(Date(gd.ngayGiaoDich))
        holder.tvDanhMuc.text = gd.danhMuc
        holder.tvNoiDung.text = if (gd.noiDung.isNotEmpty()) gd.noiDung else "Không có ghi chú"
        holder.tvSoTien.text = "${formatter.format(gd.soTien)}đ"
        
        // Màu sắc theo loại
        val color = if (gd.loai == "thu") 
            android.graphics.Color.parseColor("#4CAF50") 
        else 
            android.graphics.Color.parseColor("#F44336")
        holder.tvSoTien.setTextColor(color)
    }

    override fun getItemCount() = danhSach.size
}

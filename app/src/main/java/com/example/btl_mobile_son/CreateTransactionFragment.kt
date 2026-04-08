package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.GiaoDich
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTransactionFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maGiaoDich: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }

        maGiaoDich = arguments?.getLong("maGiaoDich", 0) ?: 0

        val spinnerType = view.findViewById<Spinner>(R.id.spinnerTransactionType)
        val types = arrayOf("Thu", "Chi")
        spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

        view.findViewById<Button>(R.id.btnSaveTransaction).setOnClickListener {
            saveTransaction()
        }

        view.findViewById<Button>(R.id.btnCancelTransaction).setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (maGiaoDich > 0) {
            loadTransaction()
        }
    }

    private fun loadTransaction() {
        lifecycleScope.launch {
            try {
                val gd = withContext(Dispatchers.IO) {
                    dbManager.giaoDichDao.layTheoMa(maGiaoDich)
                }

                gd?.let {
                    view?.findViewById<Spinner>(R.id.spinnerTransactionType)?.setSelection(
                        if (it.loai == "thu") 0 else 1
                    )
                    view?.findViewById<EditText>(R.id.etTransactionAmount)?.setText(it.soTien.toString())
                    view?.findViewById<EditText>(R.id.etTransactionDescription)?.setText(it.noiDung)
                    view?.findViewById<EditText>(R.id.etTransactionNote)?.setText(it.ghiChu)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTransaction() {
        val type = if (view?.findViewById<Spinner>(R.id.spinnerTransactionType)?.selectedItemPosition == 0) "thu" else "chi"
        val amountStr = view?.findViewById<EditText>(R.id.etTransactionAmount)?.text.toString()
        val description = view?.findViewById<EditText>(R.id.etTransactionDescription)?.text.toString()
        val note = view?.findViewById<EditText>(R.id.etTransactionNote)?.text.toString()

        if (amountStr.isEmpty() || amountStr.toDoubleOrNull() == null || amountStr.toDouble() <= 0) {
            Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val gd = GiaoDich(
                        maGiaoDich = maGiaoDich,
                        loai = type,
                        soTien = amountStr.toDouble().toLong(),
                        noiDung = description,
                        ngayGiaoDich = System.currentTimeMillis(),
                        ghiChu = note
                    )

                    if (maGiaoDich > 0) {
                        dbManager.giaoDichDao.capNhat(gd)
                    } else {
                        dbManager.giaoDichDao.them(gd)
                    }
                }

                Toast.makeText(context, "✓ Lưu thành công", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } catch (e: Exception) {
                Toast.makeText(context, "✗ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

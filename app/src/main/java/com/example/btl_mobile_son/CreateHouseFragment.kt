package com.example.btl_mobile_son

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.btl_mobile_son.data.db.DatabaseManager
import com.example.btl_mobile_son.data.model.NhaTro
import com.example.btl_mobile_son.utils.ValidationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateHouseFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private var maNhaEdit: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_house, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbManager = DatabaseManager.getInstance(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("CreateHouseFragment", "Error initializing database", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show()
            return
        }
        maNhaEdit = arguments?.getLong("maNha", -1L) ?: -1L

        // Các trường nhập liệu
        val etHouseName = view.findViewById<EditText>(R.id.etHouseName)
        val etOwnerName = view.findViewById<EditText>(R.id.etOwnerName)
        val etOwnerPhone = view.findViewById<EditText>(R.id.etOwnerPhone)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etWard = view.findViewById<EditText>(R.id.etWard)
        val etDistrict = view.findViewById<EditText>(R.id.etDistrict)
        val etProvince = view.findViewById<EditText>(R.id.etProvince)
        val etNote = view.findViewById<EditText>(R.id.etNote)

        // Load dữ liệu khi edit
        if (maNhaEdit > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val nha = dbManager.nhaTroDao.layTheoMa(maNhaEdit)
                withContext(Dispatchers.Main) {
                    nha?.let {
                        etHouseName.setText(it.tenNha)
                        etOwnerName.setText(it.tenChuNha)
                        etOwnerPhone.setText(it.soDienThoai)
                        etNote.setText(it.ghiChu)
                        
                        // Tách địa chỉ thành 4 phần
                        val addressParts = it.diaChi.split(",").map { part -> part.trim() }
                        when (addressParts.size) {
                            4 -> {
                                etAddress.setText(addressParts[0])
                                etWard.setText(addressParts[1])
                                etDistrict.setText(addressParts[2])
                                etProvince.setText(addressParts[3])
                            }
                            3 -> {
                                etAddress.setText(addressParts[0])
                                etDistrict.setText(addressParts[1])
                                etProvince.setText(addressParts[2])
                            }
                            2 -> {
                                etAddress.setText(addressParts[0])
                                etProvince.setText(addressParts[1])
                            }
                            1 -> {
                                etAddress.setText(addressParts[0])
                            }
                            else -> {
                                etAddress.setText(it.diaChi)
                            }
                        }
                    }
                }
            }
        }

        // Nút quay về
        view.findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Nút lưu
        view.findViewById<Button>(R.id.btnSubmit)?.setOnClickListener {
            val tenNha = etHouseName.text.toString().trim()
            val tenChuNha = etOwnerName.text.toString().trim()
            val sdt = etOwnerPhone.text.toString().trim()
            val diaChiCuThe = etAddress.text.toString().trim()
            val xa = etWard.text.toString().trim()
            val huyen = etDistrict.text.toString().trim()
            val tinh = etProvince.text.toString().trim()
            val ghiChu = etNote.text.toString().trim()

            // Validation
            if (!ValidationHelper.isNotEmpty(tenNha)) {
                Toast.makeText(context, "Vui lòng nhập tên nhà trọ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sdt.isNotEmpty() && !ValidationHelper.isValidPhoneNumber(sdt)) {
                Toast.makeText(context, ValidationHelper.getPhoneErrorMessage(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ghép địa chỉ
            val diaChiParts = listOf(diaChiCuThe, xa, huyen, tinh).filter { it.isNotEmpty() }
            val diaChi = diaChiParts.joinToString(", ")

            CoroutineScope(Dispatchers.IO).launch {
                val nha = NhaTro(
                    maNha = if (maNhaEdit > 0) maNhaEdit else 0,
                    tenNha = tenNha,
                    diaChi = diaChi,
                    tenChuNha = tenChuNha,
                    soDienThoai = sdt,
                    ghiChu = ghiChu
                )

                if (maNhaEdit > 0) {
                    dbManager.nhaTroDao.capNhat(nha)
                } else {
                    dbManager.nhaTroDao.them(nha)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Đã lưu nhà trọ", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}

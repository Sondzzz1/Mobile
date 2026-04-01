-- DỮ LIỆU MẪU - ỨNG DỤNG QUẢN LÝ NHÀ TRỌ

-- Nhà trọ mẫu
INSERT INTO nha_tro (ten_nha, dia_chi, ten_chu_nha, so_dien_thoai, ghi_chu) VALUES
('Nhà trọ Bình Minh', '123 Đường Lê Lợi, Quận 1, TP.HCM', 'Nguyễn Văn An', '0901234567', 'Nhà 3 tầng, 12 phòng'),
('Nhà trọ Hòa Bình', '456 Đường Nguyễn Huệ, Quận 3, TP.HCM', 'Trần Thị Bình', '0912345678', 'Nhà 2 tầng, 8 phòng');

-- Phòng mẫu (nhà 1)
INSERT INTO phong (ma_nha, ten_phong, dien_tich_m2, gia_co_ban, trang_thai, so_nguoi_toi_da, ghi_chu) VALUES
(1, 'P101', 25.0, 3000000, 'da_thue', 2, 'Phòng có ban công'),
(1, 'P102', 20.0, 2500000, 'da_thue', 2, 'Phòng hướng Đông'),
(1, 'P103', 30.0, 3500000, 'trong', 3, 'Phòng rộng, có bếp'),
(1, 'P201', 25.0, 3000000, 'da_thue', 2, 'Tầng 2'),
(1, 'P202', 20.0, 2500000, 'trong', 2, 'Tầng 2, hướng Tây');

-- Phòng mẫu (nhà 2)
INSERT INTO phong (ma_nha, ten_phong, dien_tich_m2, gia_co_ban, trang_thai, so_nguoi_toi_da, ghi_chu) VALUES
(2, 'A01', 22.0, 2800000, 'da_thue', 2, 'Phòng tầng trệt'),
(2, 'A02', 22.0, 2800000, 'trong', 2, 'Phòng tầng trệt'),
(2, 'B01', 28.0, 3200000, 'da_thue', 3, 'Phòng tầng 1');

-- Khách thuê mẫu
INSERT INTO khach_thue (ho_ten, so_dien_thoai, email, so_cmnd, ghi_chu, ngay_tao) VALUES
('Nguyễn Văn Hùng', '0933111222', 'hung@gmail.com', '079201001234', '', 1700000000000),
('Trần Thị Mai', '0944222333', 'mai@gmail.com', '079202005678', '', 1700000000000),
('Lê Văn Tuấn', '0955333444', 'tuan@gmail.com', '079203009012', '', 1700000000000),
('Phạm Thị Lan', '0966444555', 'lan@gmail.com', '079204003456', '', 1700000000000);

-- Hợp đồng mẫu
INSERT INTO hop_dong (ma_phong, ma_khach, ngay_bat_dau, ngay_ket_thuc, gia_thue_thang, tien_dat_coc, trang_thai) VALUES
(1, 1, 1696118400000, 1727654400000, 3000000, 6000000, 'dang_thue'),
(2, 2, 1696118400000, 1727654400000, 2500000, 5000000, 'dang_thue'),
(4, 3, 1698710400000, 1730246400000, 3000000, 6000000, 'dang_thue'),
(6, 4, 1699315200000, 1730851200000, 2800000, 5600000, 'dang_thue');

-- Dịch vụ mẫu
INSERT INTO dich_vu (ma_nha, ten_dich_vu, don_vi, don_gia, loai_dich_vu) VALUES
(1, 'Điện', 'kWh', 3500, 'dien'),
(1, 'Nước', 'm3', 15000, 'nuoc'),
(1, 'Internet', 'tháng', 100000, 'khac'),
(1, 'Rác', 'tháng', 20000, 'khac'),
(2, 'Điện', 'kWh', 3500, 'dien'),
(2, 'Nước', 'm3', 15000, 'nuoc');

-- Chỉ số điện nước mẫu (tháng 11/2024)
INSERT INTO chi_so_dien_nuoc (ma_phong, loai, thang, nam, chi_so_cu, chi_so_moi, don_gia, ghi_chu) VALUES
(1, 'dien', 11, 2024, 1200, 1285, 3500, ''),
(1, 'nuoc', 11, 2024, 45, 52, 15000, ''),
(2, 'dien', 11, 2024, 980, 1050, 3500, ''),
(2, 'nuoc', 11, 2024, 30, 36, 15000, '');

-- Hóa đơn mẫu (tháng 11/2024)
INSERT INTO hoa_don (ma_hop_dong, thang, nam, tien_phong, tong_tien_dich_vu, giam_gia, tong_tien, da_thanh_toan, ghi_chu, ngay_tao) VALUES
(1, 11, 2024, 3000000, 417500, 0, 3417500, 1, '', 1701388800000),
(2, 11, 2024, 2500000, 332000, 0, 2832000, 0, '', 1701388800000);

-- Giao dịch mẫu
INSERT INTO giao_dich (loai, ma_phong, so_tien, danh_muc, ngay_giao_dich, noi_dung, ten_nguoi, phuong_thuc_thanh_toan, ghi_chu, ngay_tao) VALUES
('thu', 1, 3417500, 'Tiền thuê phòng', 1701388800000, 'Thu tiền tháng 11/2024 - P101', 'Nguyễn Văn Hùng', 'chuyen_khoan', '', 1701388800000),
('chi', NULL, 500000, 'Sửa chữa', 1701302400000, 'Sửa vòi nước tầng 1', 'Thợ sửa chữa', 'tien_mat', '', 1701302400000),
('chi', NULL, 200000, 'Vệ sinh', 1701216000000, 'Dọn vệ sinh chung', '', 'tien_mat', '', 1701216000000);

-- Đặt cọc mẫu
INSERT INTO dat_coc (ma_phong, ten_khach, so_dien_thoai, so_cmnd, email, tien_dat_coc, gia_phong, ngay_du_kien_vao, ghi_chu, ngay_tao) VALUES
(3, 'Võ Thị Hoa', '0977555666', '079205007890', 'hoa@gmail.com', 3500000, 3500000, 1703980800000, 'Dự kiến vào tháng 1/2025', 1701388800000);

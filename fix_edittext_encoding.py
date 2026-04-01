#!/usr/bin/env python3
"""
Script để tự động thêm inputType cho tất cả EditText trong layout XML
để hỗ trợ tiếng Việt đầy đủ
"""

import os
import re
from pathlib import Path

def fix_edittext_in_file(file_path):
    """Sửa các EditText trong một file XML"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    changes_made = False
    
    # Pattern để tìm EditText không có inputType
    # Tìm các EditText block
    edittext_pattern = r'(<EditText[^>]*?)(/?>)'
    
    def add_input_type(match):
        nonlocal changes_made
        edittext_tag = match.group(1)
        closing = match.group(2)
        
        # Kiểm tra xem đã có inputType chưa
        if 'android:inputType=' in edittext_tag:
            return match.group(0)  # Đã có inputType, không thay đổi
        
        # Xác định inputType phù hợp dựa trên hint hoặc id
        input_type = 'textCapSentences'
        
        # Số điện thoại
        if any(x in edittext_tag.lower() for x in ['phone', 'dien_thoai', 'sodienthoai', 'etphone', 'etownerphone']):
            input_type = 'phone'
        # Email
        elif any(x in edittext_tag.lower() for x in ['email', 'etemail']):
            input_type = 'textEmailAddress'
        # Số tiền, giá
        elif any(x in edittext_tag.lower() for x in ['price', 'gia', 'tien', 'sotien', 'dongia', 'etroomprice', 'etsotien', 'etdongia']):
            input_type = 'numberDecimal'
        # Diện tích, số lượng
        elif any(x in edittext_tag.lower() for x in ['area', 'dientich', 'soluong', 'chiso', 'etroomarea', 'etmaxtenant', 'etchisocu', 'etchisomoi', 'etthang', 'etnam']):
            input_type = 'numberDecimal'
        # CMND/CCCD
        elif any(x in edittext_tag.lower() for x in ['cmnd', 'cccd', 'idcard', 'etidcard', 'socmnd']):
            input_type = 'number'
        # Tên người (họ tên)
        elif any(x in edittext_tag.lower() for x in ['hoten', 'fullname', 'tennguoi', 'ownername', 'etfullname', 'etownername', 'ettennguoi']):
            input_type = 'textCapWords'
        # Ghi chú, nội dung (multiline)
        elif any(x in edittext_tag.lower() for x in ['note', 'ghichu', 'noidung', 'etnote', 'etghichu', 'etnoidung']):
            # Kiểm tra xem có phải multiline không (height > 60dp)
            if 'android:layout_height="80dp"' in edittext_tag or 'android:layout_height="100dp"' in edittext_tag:
                input_type = 'textMultiLine|textCapSentences'
            else:
                input_type = 'textCapSentences'
        # Tìm kiếm
        elif any(x in edittext_tag.lower() for x in ['search', 'timkiem', 'etsearch']):
            input_type = 'text'
        # Ngày tháng (nếu có date picker thì để text)
        elif any(x in edittext_tag.lower() for x in ['ngay', 'date', 'etngay', 'etmonth']):
            input_type = 'text'
        
        # Thêm inputType vào trước closing tag
        changes_made = True
        return f'{edittext_tag}\n                android:inputType="{input_type}"{closing}'
    
    # Thay thế tất cả EditText
    content = re.sub(edittext_pattern, add_input_type, content, flags=re.DOTALL)
    
    # Chỉ ghi file nếu có thay đổi
    if changes_made and content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    """Main function"""
    layout_dir = Path('app/src/main/res/layout')
    
    if not layout_dir.exists():
        print(f"Không tìm thấy thư mục: {layout_dir}")
        return
    
    fixed_files = []
    
    # Duyệt qua tất cả file XML trong thư mục layout
    for xml_file in layout_dir.glob('*.xml'):
        if fix_edittext_in_file(xml_file):
            fixed_files.append(xml_file.name)
            print(f"✓ Đã sửa: {xml_file.name}")
    
    print(f"\n{'='*50}")
    print(f"Tổng số file đã sửa: {len(fixed_files)}")
    if fixed_files:
        print("\nDanh sách file đã sửa:")
        for f in fixed_files:
            print(f"  - {f}")

if __name__ == '__main__':
    main()

"""
Danh sách Phường thuộc Thành phố Cần Thơ (province_code = '92', city_code = '92').
Extract từ 2026-06-13_normalized_vietnam_regions.sql — chỉ giữ các row prefix "Phường"
(bỏ "Xã" vì user chỉ muốn đô thị).
"""

PROVINCE_CODE = "92"
PROVINCE_NAME = "Thành phố Cần Thơ"
CITY_CODE = "92"
CITY_NAME = "Cần Thơ"

CANTHO_WARDS = [
    ("31120", "Phường Cái Khế"),
    ("31135", "Phường Ninh Kiều"),
    ("31147", "Phường Tân An"),
    ("31150", "Phường An Bình"),
    ("31153", "Phường Ô Môn"),
    ("31157", "Phường Thới Long"),
    ("31162", "Phường Phước Thới"),
    ("31168", "Phường Bình Thủy"),
    ("31174", "Phường Thới An Đông"),
    ("31183", "Phường Long Tuyền"),
    ("31186", "Phường Cái Răng"),
    ("31201", "Phường Hưng Phú"),
    ("31207", "Phường Thốt Nốt"),
    ("31213", "Phường Tân Lộc"),
    ("31217", "Phường Trung Nhứt"),
    ("31228", "Phường Thuận Hưng"),
    ("31321", "Phường Vị Thanh"),
    ("31333", "Phường Vị Tân"),
    ("31340", "Phường Ngã Bảy"),
    ("31411", "Phường Đại Thành"),
    ("31471", "Phường Long Mỹ"),
    ("31473", "Phường Long Bình"),
    ("31480", "Phường Long Phú 1"),
    ("31507", "Phường Sóc Trăng"),
    ("31510", "Phường Phú Lợi"),
    ("31684", "Phường Mỹ Xuyên"),
    ("31732", "Phường Ngã Năm"),
    ("31753", "Phường Mỹ Quới"),
    ("31783", "Phường Vĩnh Châu"),
    ("31789", "Phường Khánh Hòa"),
    ("31804", "Phường Vĩnh Phước"),
]

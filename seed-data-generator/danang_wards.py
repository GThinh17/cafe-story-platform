"""
Danh sách Phường thuộc Thành phố Đà Nẵng (province_code = '48', city_code = '48').
Extract từ 2026-06-13_normalized_vietnam_regions.sql — chỉ giữ các row prefix "Phường"
(bỏ "Xã" vì user chỉ muốn đô thị).
"""

PROVINCE_CODE = "48"
PROVINCE_NAME = "Thành phố Đà Nẵng"
CITY_CODE = "48"
CITY_NAME = "Đà Nẵng"

DANANG_WARDS = [
    ("20194", "Phường Hải Vân"),
    ("20197", "Phường Liên Chiểu"),
    ("20200", "Phường Hòa Khánh"),
    ("20209", "Phường Thanh Khê"),
    ("20242", "Phường Hải Châu"),
    ("20257", "Phường Hòa Cường"),
    ("20260", "Phường Cẩm Lệ"),
    ("20263", "Phường Sơn Trà"),
    ("20275", "Phường An Hải"),
    ("20285", "Phường Ngũ Hành Sơn"),
    ("20305", "Phường An Khê"),
    ("20314", "Phường Hòa Xuân"),
    ("20335", "Phường Bàn Thạch"),
    ("20341", "Phường Tam Kỳ"),
    ("20350", "Phường Hương Trà"),
    ("20356", "Phường Quảng Phú"),
    ("20401", "Phường Hội An Tây"),
    ("20410", "Phường Hội An"),
    ("20413", "Phường Hội An Đông"),
    ("20551", "Phường Điện Bàn"),
    ("20557", "Phường Điện Bàn Bắc"),
    ("20575", "Phường An Thắng"),
    ("20579", "Phường Điện Bàn Đông"),
]

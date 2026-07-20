"""
Danh sách họ + tên đệm + tên riêng phổ biến ở Việt Nam.
Nguồn: các bảng xếp hạng họ Việt Nam thông dụng (Nguyễn ~38%, Trần ~11%, ...) +
tên riêng phổ biến qua các thế hệ. Tự tổ hợp trong script sinh, không dùng list
tên người thật.
"""

FAMILY_NAMES = [
    "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng",
    "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý", "Trịnh", "Đoàn", "Tô", "Trương",
    "Đinh", "Cao", "Chu", "Đào", "Mai", "Lâm", "Tăng", "Kiều", "La", "Đàm",
]

MALE_MIDDLE_NAMES = [
    "Văn", "Đức", "Minh", "Hoàng", "Anh", "Quốc", "Tuấn", "Ngọc", "Xuân", "Chí",
    "Công", "Duy", "Hữu", "Khắc", "Hải", "Đình", "Bá", "Trọng", "Bảo", "Thành",
    "Nhật", "Gia", "Thanh", "Việt", "Trung",
]

FEMALE_MIDDLE_NAMES = [
    "Thị", "Ngọc", "Thanh", "Thu", "Kim", "Minh", "Hoài", "Bảo", "Mỹ", "Diễm",
    "Bích", "Diệu", "Phương", "Hồng", "Ánh", "Ngân", "Yến", "Xuân", "Thúy", "Uyên",
    "Khánh", "Nhật", "Hà", "Vân", "Lan",
]

MALE_FIRST_NAMES = [
    "Vũ", "Thịnh", "Nam", "Tuấn", "Bảo", "Khôi", "Long", "Hùng", "Đức", "Minh",
    "Huy", "Dũng", "Sơn", "Hải", "Cường", "Thắng", "Trung", "Kiên", "Duy", "Phong",
    "Quang", "Việt", "Đạt", "Kiệt", "Phúc", "Hoàng", "Tùng", "Khánh", "Toàn", "Lâm",
    "Nghĩa", "Phú", "Tiến", "Chương", "Danh", "Đạo", "Đông", "Hào", "Hòa", "Khoa",
    "Lộc", "Nhân", "Nhật", "Phát", "Sang", "Thái", "Tín", "Vinh", "Vượng", "An",
]

FEMALE_FIRST_NAMES = [
    "Linh", "Hương", "Mai", "Trang", "Ngọc", "Anh", "Thảo", "My", "Ngân", "Quỳnh",
    "Yến", "Hà", "Nhi", "Vy", "Trâm", "Huyền", "Phương", "Uyên", "Diễm", "Bình",
    "Châu", "Dung", "Duyên", "Hạnh", "Hiền", "Hoa", "Hòa", "Lan", "Loan", "Nga",
    "Nhung", "Oanh", "Phượng", "Quyên", "Thanh", "Thu", "Thúy", "Trinh", "Tú",
    "Tuyết", "Vân", "Xuân", "Ý", "Ánh", "Kim", "Bích", "Hồng", "Diệu", "Khánh",
    "Lâm",
]

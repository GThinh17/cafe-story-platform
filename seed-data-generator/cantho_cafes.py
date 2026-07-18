"""
20 cafe thật ở Cần Thơ dùng để seed cafe_pages + payments + page_members.

Cấu trúc mỗi record: (name, street, ward_code, ward_name, package_key, description).
- package_key = "6mo" hoặc "3mo" — map sang extra_fees.name ('Cafe Page 6 tháng' / 'Cafe Page 3 tháng')
- name phải UNIQUE trong bảng cafe_pages (migration abort nếu đã tồn tại)
- ward_code phải tồn tại trong region_wards; tất cả province_code='92', city_code='92' (Cần Thơ)
- description: 1-2 câu tiếng Việt mô tả không gian / phong cách quán

Nguồn: web search 2026-07-16 — xem chi tiết trong plan file.
"""

# Ward info (Cần Thơ, post-2025 merger)
PROVINCE_CODE = "92"
PROVINCE_NAME = "Thành phố Cần Thơ"
CITY_CODE = "92"
CITY_NAME = "Cần Thơ"

WARD_NINH_KIEU = ("31135", "Phường Ninh Kiều")
WARD_AN_BINH = ("31150", "Phường An Bình")
WARD_CAI_RANG = ("31186", "Phường Cái Răng")

# 20 cafes: 10 gói 6mo trước, 10 gói 3mo sau (thứ tự này quyết định index → owner)
CANTHO_CAFES = [
    # ── Gói 6 tháng (199k) ─────────────────────────────────────────────
    ("Phúc Long Coffee & Tea",         "209 Đường 30 Tháng 4",                       *WARD_NINH_KIEU, "6mo",
     "Chuỗi trà và cà phê Việt Nam nổi tiếng, kết hợp hương vị trà truyền thống với cà phê và các món bánh ngọt tinh tế."),
    ("Highlands Coffee Vincom Cần Thơ","Vincom Plaza, 2 Hùng Vương",                 *WARD_NINH_KIEU, "6mo",
     "Không gian hiện đại tại Vincom Plaza Cần Thơ, mặt bằng rộng cả indoor và outdoor, phù hợp gặp gỡ bạn bè và làm việc."),
    ("Trung Nguyên Legend Cà Phê",     "259 Đường 30 Tháng 4",                       *WARD_NINH_KIEU, "6mo",
     "Cà phê Việt trong không gian sang trọng, sáng tạo, đậm bản sắc văn hóa cà phê Tây Đô với diện tích rộng rãi."),
    ("Katinat Saigon Kafe",            "48 Xô Viết Nghệ Tĩnh",                        *WARD_NINH_KIEU, "6mo",
     "Cafe hiện đại giới trẻ yêu thích, thiết kế sang-sáng, nhiều góc check-in đẹp ngay trung tâm Ninh Kiều sầm uất."),
    ("The 80's iCafe",                 "6/46 Mạc Thiên Tích",                         *WARD_NINH_KIEU, "6mo",
     "Quán quen của dân chạy deadline 24/7, không gian rộng rãi và ít ồn ào, gần Đại học Cần Thơ, có thể đặt bàn trước."),
    ("Đậu Ơi Coffee & Tea",            "372B Nguyễn Văn Cừ",                          *WARD_NINH_KIEU, "6mo",
     "Quán học bài và làm việc xuyên đêm, không gian minimalism sáng thoáng, thức uống ngon với mức giá học sinh - sinh viên."),
    ("Trầm Coffee & Tea",              "321-323 Nguyễn Văn Cừ nối dài",               *WARD_AN_BINH,   "6mo",
     "Cafe & trà mở 24/7, không gian trẻ trung, phục vụ đông sinh viên khu vực Nguyễn Văn Cừ nối dài."),
    ("Cà phê Nhà Phạm",                "Đường 3 Tháng 2",                             *WARD_NINH_KIEU, "6mo",
     "Không gian mộc mạc pha vintage, decor nhiều cây xanh và ánh sáng vàng, mang lại cảm giác 'về nhà' giữa lòng thành phố."),
    ("Nhà Phạm trong rừng",            "219 Đường số 3, khu Giảng viên ĐH Cần Thơ",   *WARD_NINH_KIEU, "6mo",
     "Chi nhánh nằm trong khu Giảng viên ĐH Cần Thơ, không gian xanh mát như 'ốc đảo' giữa thành phố, thích hợp học bài, làm việc."),
    ("Nhà Phạm bên cầu",               "Cầu Đầu Sấu",                                 *WARD_CAI_RANG,  "6mo",
     "Cafe view sông ngay chân cầu Đầu Sấu, sáng sớm và chiều mát view rất chill, phù hợp uống cà phê ngắm cảnh."),

    # ── Gói 3 tháng (100k) ─────────────────────────────────────────────
    ("Là Cafe",                        "122/6 Mạc Thiên Tích",                        *WARD_NINH_KIEU, "3mo",
     "Không gian ấm cúng, chỗ ngồi rộng rãi, thực đơn đa dạng với cacao và espresso được đánh giá cao ở khu vực Ninh Kiều."),
    ("Raw Coffee",                     "224 Đường 30 Tháng 4",                        *WARD_NINH_KIEU, "3mo",
     "Không gian gần gũi thiên nhiên độc đáo, view cầu Quang Trung, phong cách phục vụ và decor riêng biệt."),
    ("Tiệm Trà Cỏ Ngọt",               "139/105 Đường 30 Tháng 4",                    *WARD_NINH_KIEU, "3mo",
     "Không gian phong cách Nhật Bản mộc mạc, chủ yếu trang trí gỗ, phù hợp uống trà chiều và thư giãn."),
    ("Time Cafe",                      "89A Võ Văn Tần",                              *WARD_NINH_KIEU, "3mo",
     "Cafe bình dân trên đường Võ Văn Tần, mức giá 20k-50k, không gian đơn giản dễ chịu cho học sinh sinh viên."),
    ("Sky Bar Iris",                   "224 Đường 30 Tháng 4",                        *WARD_NINH_KIEU, "3mo",
     "Sân thượng có view sông và cầu Quang Trung, đồ uống và không gian lên hình đẹp, chill về đêm."),
    ("Aurora Coffee",                  "419 Đường 30 Tháng 4",                        *WARD_NINH_KIEU, "3mo",
     "Cafe view đẹp, wifi mạnh, không gian sạch sẽ không khói thuốc, nhân viên phục vụ thân thiện gần Cao đẳng Cần Thơ."),
    ("HiHi Onigiri",                   "172/16B Lê Bình",                             *WARD_CAI_RANG,  "3mo",
     "Phong cách bánh bèo hường phấn đáng yêu, ngoài đồ uống còn phục vụ bánh ngọt và đồ ăn vặt nhẹ."),
    ("Mật Ngọt Coffee",                "132x12 Hẻm Liên Tổ 1-2, Nguyễn Văn Cừ",       *WARD_NINH_KIEU, "3mo",
     "Không gian ngọt ngào ấm cúng, decor tone pastel, phục vụ nước ngọt và đồ ăn nhẹ, mới mở gần đây."),
    ("Chịn Cà Phê",                    "82 Đường Số 3, KDC Đại Ngân",                 *WARD_NINH_KIEU, "3mo",
     "Cafe KDC Đại Ngân, không gian yên tĩnh phù hợp học tập và làm việc lâu, mức giá bình dân."),
    ("Tiệm Cà Phê Nhà Có Khách",       "359/19B Nguyễn Văn Cừ",                       *WARD_NINH_KIEU, "3mo",
     "Cafe mới mở phong cách 'về nhà', không gian ấm cúng phục vụ đông khách sinh viên và người ghé nghỉ chân."),
]


def sanity_check() -> None:
    """Assert cấu trúc data đúng — chạy lúc import."""
    names = [row[0] for row in CANTHO_CAFES]
    assert len(CANTHO_CAFES) == 20, f"Expect 20 cafes, got {len(CANTHO_CAFES)}"
    assert len(set(names)) == 20, "Cafe names must be unique"
    packages = [row[4] for row in CANTHO_CAFES]
    assert packages.count("6mo") == 10, "Must have 10 x 6mo"
    assert packages.count("3mo") == 10, "Must have 10 x 3mo"
    for row in CANTHO_CAFES:
        assert len(row) == 6, f"Row phải có 6 field (name, street, ward_code, ward_name, package, description): {row[0]}"
        assert row[5], f"Description trống ở cafe: {row[0]}"


sanity_check()

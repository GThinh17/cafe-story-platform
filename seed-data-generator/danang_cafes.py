"""
20 cafe THẬT ở Đà Nẵng dùng để seed cafe_pages + payments + page_members.

Toàn bộ tên, số nhà, đường và phường được cross-check qua web search 2026-07-18
(Foody, Highlands/Phúc Long/Katinat store locator, Cốc Cốc Map, Tripadvisor).
Địa chỉ hành chính đã map theo hệ thống phường mới (post-merger 7/2025) trong
danang_wards.py — quận cũ (Hải Châu / Sơn Trà / Ngũ Hành Sơn ...) đã được
gộp/đổi tên theo bảng phường mới của TP Đà Nẵng.

Cấu trúc mỗi record: (name, street, ward_code, ward_name, package_key, description).
- package_key = "6mo" hoặc "3mo" — map sang extra_fees.name
- name UNIQUE (khác 20 cafe Cần Thơ đã seed)
- ward_code phải tồn tại trong danang_wards.DANANG_WARDS
- description: 1-2 câu tiếng Việt mô tả không gian / phong cách quán
"""

PROVINCE_CODE = "48"
PROVINCE_NAME = "Thành phố Đà Nẵng"
CITY_CODE = "48"
CITY_NAME = "Đà Nẵng"

# Wards new-system (post-2025 merger) — từ danang_wards.py
WARD_HAI_CHAU = ("20242", "Phường Hải Châu")
WARD_THANH_KHE = ("20209", "Phường Thanh Khê")
WARD_SON_TRA = ("20263", "Phường Sơn Trà")
WARD_AN_HAI = ("20275", "Phường An Hải")
WARD_NGU_HANH_SON = ("20285", "Phường Ngũ Hành Sơn")
WARD_HOI_AN = ("20410", "Phường Hội An")

# 20 cafe verified — 10 gói 6mo (chuỗi F&B lớn) + 10 gói 3mo (local & specialty nhỏ)
DANANG_CAFES = [
    # ── Gói 6 tháng (199k) — chuỗi F&B lớn ────────────────────────────
    ("Highlands Coffee Vincom Đà Nẵng",  "Tầng Trệt, Vincom Center, 910A Ngô Quyền",  *WARD_AN_HAI,       "6mo",
     "Chi nhánh Highlands tại tầng trệt Vincom Center Ngô Quyền, không gian rộng máy lạnh, tiện cho khách mua sắm ghé nghỉ chân."),
    ("Phúc Long Đà Nẵng",                "61 Nguyễn Văn Linh",                        *WARD_HAI_CHAU,     "6mo",
     "Phúc Long chi nhánh Đà Nẵng — trục đường sầm uất Nguyễn Văn Linh, không gian 2 tầng, trà và cà phê Phúc Long đặc trưng."),
    ("Trung Nguyên Legend Đà Nẵng",      "102-104 Đống Đa",                           *WARD_HAI_CHAU,     "6mo",
     "Trung Nguyên Legend giữa lòng Hải Châu, không gian sang trọng đậm bản sắc cà phê Buôn Ma Thuột, khách doanh nhân nhiều."),
    ("Cộng Cà Phê Đà Nẵng",              "96-98 Bạch Đằng",                           *WARD_HAI_CHAU,     "6mo",
     "Cộng Cà Phê phong cách bao cấp retro, view thẳng sông Hàn và cầu Rồng, signature cốt dừa Cộng bán chạy nhất chi nhánh."),
    ("Katinat Đà Nẵng",                  "34 Bạch Đằng",                              *WARD_HAI_CHAU,     "6mo",
     "Katinat mặt tiền Bạch Đằng, decor xanh-vàng bắt mắt, giới trẻ Đà Nẵng ưa thích check-in ngay từ khi khai trương."),
    ("Starbucks Đà Nẵng",                "50 Bạch Đằng",                              *WARD_HAI_CHAU,     "6mo",
     "Cửa hàng Starbucks đầu tiên tại Đà Nẵng, mặt tiền sông Hàn, không gian 2 tầng view trực diện cầu Rồng."),
    ("The Coffee House Đà Nẵng",         "Lô A2, Nguyễn Văn Linh, Bình Hiên",         *WARD_HAI_CHAU,     "6mo",
     "The Coffee House chi nhánh Đà Nẵng gần cầu Rồng, không gian 2 tầng rộng, wifi mạnh — dân freelancer thường ghé."),
    ("Phê La Đà Nẵng",                   "36 Bạch Đằng, Thạch Thang",                 *WARD_HAI_CHAU,     "6mo",
     "Phê La — thương hiệu trà ô long sương từ Đà Lạt, chi nhánh Đà Nẵng view sông Hàn, decor gỗ trầm ấm."),
    ("43 Factory Coffee Roaster",        "422 Ngô Thì Sỹ, Mỹ An",                     *WARD_NGU_HANH_SON, "6mo",
     "Specialty coffee roaster nổi tiếng nhất Đà Nẵng, không gian công nghiệp minimalism, hạt rang mộc từ Ethiopia đến Việt Nam."),
    ("XLIII Coffee Đà Nẵng",             "258 Bạch Đằng, Phước Ninh",                 *WARD_HAI_CHAU,     "6mo",
     "XLIII (Bốn Ba) Coffee chi nhánh Đà Nẵng — không gian đô thị hiện đại, menu cà phê specialty từ hạt rang tại 43 Factory."),

    # ── Gói 3 tháng (100k) — local & boutique cafe ─────────────────────
    ("The Local Beans Đà Nẵng",          "186 Phan Châu Trinh",                       *WARD_HAI_CHAU,     "3mo",
     "The Local Beans — quán specialty rang xay tại chỗ, không gian 2 tầng minimalism với gỗ và cây xanh, chill cho dân yêu cà phê thuần vị."),
    ("Wonderlust Coffee & Souvenir",     "96 Trần Phú",                               *WARD_HAI_CHAU,     "3mo",
     "Wonderlust decor minimalism đen-trắng kết hợp cây xanh, ánh sáng tự nhiên tràn ngập, phục vụ cả cà phê và đồ lưu niệm."),
    ("Boulevard Gelato & Coffee",        "77 Trần Quốc Toản, Phước Ninh",             *WARD_HAI_CHAU,     "3mo",
     "Boulevard phong cách châu Âu hiện đại, chuyên gelato Ý kết hợp cà phê, gần biển và cầu Rồng, khách du lịch nhiều."),
    ("Nối Cafe",                         "113/18 Nguyễn Chí Thanh",                   *WARD_HAI_CHAU,     "3mo",
     "Nối Cafe — chuỗi cafe local Đà Nẵng, 3 cơ sở liền nhau trong hẻm Nguyễn Chí Thanh, decor vintage ấm cúng."),
    ("Gé Cafe",                          "60 Nguyễn Chí Thanh",                       *WARD_HAI_CHAU,     "3mo",
     "Gé Cafe — quán local Đà Nẵng khu Nguyễn Chí Thanh, không gian nhỏ xinh giá bình dân, khách sinh viên và dân văn phòng."),
    ("Faifo Coffee Hội An",              "130 Trần Phú, phố cổ Hội An",               *WARD_HOI_AN,       "3mo",
     "Faifo Coffee giữa phố cổ Hội An, rooftop 3 tầng ngắm toàn mái ngói phố cổ, món egg coffee và cốt dừa nổi tiếng."),
    ("Nối - The Cabin",                  "118 Chu Huy Mân, Nại Hiên Đông",            *WARD_SON_TRA,      "3mo",
     "Nối - The Cabin view cảng cá Thọ Quang, style vintage với vật liệu tái chế từ thuyền cũ, không gian hoài niệm."),
    ("Sơn Trà Marina",                   "Hồ Xanh, Thọ Quang",                        *WARD_SON_TRA,      "3mo",
     "Sơn Trà Marina được mệnh danh 'Santorini thu nhỏ' với kiến trúc Địa Trung Hải xanh-trắng bên hồ Xanh, chân bán đảo Sơn Trà."),
    ("OM Herbal Tea & Coffee",           "116 Thích Thiện Chiếu",                     *WARD_SON_TRA,      "3mo",
     "OM Herbal Tea & Coffee — quán trà thảo mộc và cà phê phong cách zen, decor gỗ và cây xanh, hợp cho ai muốn detox và tĩnh lặng."),
    ("Mirko Coffee & Chill Beer",        "100 Nguyễn Hữu An, Nại Hiên Đông",          *WARD_SON_TRA,      "3mo",
     "Mirko Coffee & Chill Beer kết hợp cafe ban ngày và craft beer buổi tối, không gian outdoor gần biển Sơn Trà."),
]

assert len({c[0] for c in DANANG_CAFES}) == 20, "Cafe names phải UNIQUE"
assert sum(1 for c in DANANG_CAFES if c[4] == "6mo") == 10
assert sum(1 for c in DANANG_CAFES if c[4] == "3mo") == 10

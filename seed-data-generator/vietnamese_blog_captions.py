"""
Pool caption tiếng Việt cho blog cafe.

10 nhóm theme (khớp thứ tự BLOG_IMAGE_THEMES trong download_blog_images.py):
  1. coffee,espresso           → Espresso review / morning coffee
  2. cafe,interior             → Không gian / decor
  3. latte-art,coffee-cup      → Latte art / hình art
  4. coffee-beans,roasting     → Nguồn hạt / rang
  5. cappuccino,foam           → Cappuccino
  6. cafe,chill,relax          → Chill vibes
  7. coffee,book,study         → Học bài / làm việc
  8. barista,coffee-machine    → Barista skills
  9. coffee-shop,vintage       → Không gian vintage
  10. coffee,dessert,cake      → Bánh ngọt kèm

Mỗi blog dùng theme của img1 (đầu tiên) để pick caption pool phù hợp.
"""

# 5 caption/theme × 10 theme = 50 caption pool
CAPTIONS_BY_THEME = {
    # Theme 1 (blog 1) - espresso morning
    "espresso": [
        "Khởi đầu ngày mới bằng ly espresso đậm đà tại {cafe} — hương vị đánh thức mọi giác quan.",
        "Buổi sáng ở {cafe}: espresso nguyên chất, thơm nồng, chất lượng ổn định qua từng ly.",
        "Ly espresso đầu ngày tại {cafe} — vị đắng vừa, hậu ngọt, đúng chuẩn coffee thuần Việt.",
        "Ghé {cafe} sáng nay uống espresso, thấy tinh thần tỉnh táo hẳn để chinh phục cả tuần.",
        "Espresso ở {cafe} không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.",
    ],
    # Theme 2 (blog 1 img 2) - interior
    "interior": [
        "Không gian {cafe} được thiết kế tinh tế, ánh sáng ấm áp — chỗ ngồi nào cũng thấy dễ chịu.",
        "Decor {cafe} rất chỉn chu, đầu tư từng góc — không lạ khi các bạn trẻ hay tới check-in.",
        "Ghé {cafe}, ấn tượng ngay với cách bố trí không gian — vừa hiện đại vừa ấm cúng.",
        "Interior của {cafe} có gu, đơn giản mà sang, chụp góc nào lên hình cũng đẹp.",
        "{cafe} có không gian rộng, thoáng — đến đây cảm giác thư giãn ngay lập tức.",
    ],
    # Theme 3 (blog 2) - latte art
    "latte-art": [
        "Latte art tại {cafe} đỉnh thật — mỗi ly là một tác phẩm, tiếc là uống xong mất luôn.",
        "Barista {cafe} vẽ hình lá trên latte cực khéo — chưa uống đã thấy được chăm sóc.",
        "Ly latte hôm nay ở {cafe}: art đẹp, milk vừa vặn, hương vị hoàn hảo cho buổi chiều.",
        "Rất mê latte art của {cafe} — cứ đến là order thêm ly nữa chỉ để chụp ảnh.",
        "Latte tại {cafe} không chỉ ngon mà còn đẹp mắt — điểm cộng lớn cho trải nghiệm.",
    ],
    # Theme 4 (blog 2 img 2) - coffee beans
    "coffee-beans": [
        "Hạt cà phê tại {cafe} được rang mộc, mùi thơm lan tỏa khắp quán — dấu hiệu của cafe chất.",
        "Nguồn hạt của {cafe} tuyển kỹ, rang đúng độ — vị cà phê đậm và trong, không đắng gắt.",
        "Ghé {cafe} coi barista pha, mới thấy trân trọng từng hạt cà phê được chọn kỹ.",
        "Cà phê ở {cafe} có hậu vị rõ ràng — chắc là chất lượng hạt được kiểm soát tốt.",
        "Không phải quán nào cũng dám phô nguồn hạt như {cafe} — điểm cộng cho sự minh bạch.",
    ],
    # Theme 5 (blog 3) - cappuccino
    "cappuccino": [
        "Cappuccino ở {cafe} có lớp foam mịn, tỉ lệ hoàn hảo — uống xong nhẹ nhàng cả buổi.",
        "Ly cappuccino {cafe} vừa đủ ngọt, foam đứng, hương cafe không bị át bởi sữa.",
        "Buổi chiều nhẹ nhàng với cappuccino tại {cafe} — foam đẹp, vị cân bằng.",
        "{cafe} pha cappuccino chuẩn ratio, uống xong không thấy ngấy, dư âm dễ chịu.",
        "Cappuccino {cafe} là món ruột của mình mỗi khi ghé — chưa lần nào thất vọng.",
    ],
    # Theme 6 (blog 3 img 2) - chill vibes
    "chill": [
        "Chiều nay ngồi {cafe} nghe nhạc chill — thời gian trôi mà không muốn về.",
        "Cần một chỗ để thả lỏng sau tuần dài? {cafe} là lựa chọn không sai.",
        "Không gian {cafe} có thứ gì đó khiến mình quên hết stress — chill đúng chuẩn.",
        "Ngày mệt, ghé {cafe} một mình, cầm ly cà phê nghe playlist là đủ rồi.",
        "{cafe} có vibes riêng — không quá sôi động, cũng không quá tĩnh, vừa vặn để suy nghĩ.",
    ],
    # Theme 7 (blog 4) - study/work
    "study": [
        "Chỗ ngồi ở {cafe} rất hợp học bài — wifi mạnh, ổ cắm nhiều, không gian yên tĩnh.",
        "Deadline dồn dập, may có {cafe} cho ngồi cả buổi mà không bị thúc dọn dẹp.",
        "Học nhóm ở {cafe} — bàn rộng, chỗ đủ cho 4 người, đồ uống giá sinh viên.",
        "Ngày làm việc remote ở {cafe} rất năng suất — mọi thứ đều setup sẵn cho dân văn phòng.",
        "{cafe} thành base để chạy dự án cuối kỳ của mình — vibes làm việc rất tốt.",
    ],
    # Theme 8 (blog 4 img 2) - barista
    "barista": [
        "Barista {cafe} rất tận tâm — hỏi khẩu vị, gợi ý theo mood, cảm giác được chăm sóc.",
        "Xem barista {cafe} pha máy chuyên nghiệp — mỗi thao tác đều chuẩn, không có gì thừa.",
        "Đội ngũ pha chế của {cafe} có tay nghề, mỗi ly ra đều đồng đều chất lượng.",
        "Chưa gọi đủ menu {cafe} vì mỗi lần barista đều recommend một món mới đáng thử.",
        "Barista {cafe} thân thiện, giới thiệu tận tình — trải nghiệm không chỉ là đồ uống.",
    ],
    # Theme 9 (blog 5) - vintage
    "vintage": [
        "Không khí vintage ở {cafe} đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.",
        "{cafe} giữ nét cổ điển giữa lòng phố hiện đại — góc nào cũng đủ chất kể chuyện.",
        "Ghé {cafe} như bước vào một bộ phim retro — âm nhạc, không gian, tất cả rất có gu.",
        "Fan của phong cách vintage nhất định phải ghé {cafe} — mỗi vật dụng đều được chọn kỹ.",
        "{cafe} là nơi để chậm lại — không có gì vội, chỉ có vibes hoài niệm dịu dàng.",
    ],
    # Theme 10 (blog 5 img 2) - dessert
    "dessert": [
        "Bánh ngọt ở {cafe} homemade, mềm ẩm, ăn kèm cà phê là combo hoàn hảo.",
        "Menu tráng miệng {cafe} đa dạng — mỗi lần ghé thử một món, chưa thất vọng lần nào.",
        "Chiều nay ở {cafe}: cà phê + tiramisu — đơn giản mà đủ để chữa lành cả tuần.",
        "Bánh ở {cafe} không quá ngọt, phù hợp gu người lớn — điểm cộng đáng kể.",
        "Ai ghé {cafe} nhớ order bánh — dessert ở đây làm mạnh tay không kém đồ uống.",
    ],
}


def caption_for(blog_index: int, cafe_name: str) -> str:
    """Trả về caption phù hợp theme của blog N (1-5), inject cafe name."""
    # Blog 1 → theme 'espresso' + 'interior' → dùng theme 1 (espresso) làm chính
    # Blog 2 → 'latte-art'
    # Blog 3 → 'cappuccino'
    # Blog 4 → 'study'
    # Blog 5 → 'vintage'
    theme_by_blog = {
        1: "espresso",
        2: "latte-art",
        3: "cappuccino",
        4: "study",
        5: "vintage",
    }
    theme = theme_by_blog[blog_index]
    pool = CAPTIONS_BY_THEME[theme]
    # Deterministic: cafe name hash + blog_index → pick from 5 captions
    seed = (sum(ord(c) for c in cafe_name) * blog_index) % len(pool)
    return pool[seed].format(cafe=cafe_name)


def sanity_check() -> None:
    """Assert đủ 10 theme × 5 caption."""
    assert len(CAPTIONS_BY_THEME) == 10, f"Expect 10 themes, got {len(CAPTIONS_BY_THEME)}"
    for theme, pool in CAPTIONS_BY_THEME.items():
        assert len(pool) == 5, f"Theme {theme} phải có 5 captions, got {len(pool)}"
        for cap in pool:
            assert "{cafe}" in cap, f"Caption thiếu placeholder {{cafe}}: {cap}"


sanity_check()

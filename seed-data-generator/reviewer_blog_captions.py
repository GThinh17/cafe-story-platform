"""
Pool caption tiếng Việt cho blog cá nhân của reviewer.

Khác blog cafe (mang góc nhìn quán): đây là góc nhìn reviewer/khách hàng — chia sẻ
trải nghiệm, đánh giá đồ uống, cafe hopping, khám phá quán mới, gu cá nhân.

10 nhóm theme (khớp thứ tự REVIEWER_BLOG_THEMES trong download_reviewer_blog_images.py).
Mỗi blog chọn theme dựa trên blog_index (1-3), inject reviewer full name để cá nhân hoá.
"""

# Placeholder {name} sẽ inject reviewer full name
CAPTIONS_BY_THEME = {
    # Theme 1: personal review / gu cá nhân
    "review": [
        "Tuần này {name} khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.",
        "Mình là {name}, chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.",
        "{name} vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.",
        "Chia sẻ nhanh: {name} thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.",
        "{name} đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.",
        "Tiêu chí chọn cafe của {name}: yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.",
    ],
    # Theme 2: coffee tasting notes
    "tasting": [
        "Note tasting hôm nay của {name}: hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.",
        "{name} thử pour-over lần đầu ở Cần Thơ — hương hoa nhài nhẹ, không đắng gắt như espresso thường.",
        "Cold brew ở quán này {name} chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.",
        "{name} đang tập quen với cà phê Ethiopia — vị trái cây rõ hơn hạt Việt, cần thời gian để thích.",
        "Latte 1 shot vs 2 shot: {name} thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.",
        "Espresso của quán này {name} note: crema dày, vị chocolate đậm, ít chua — chuẩn gu Việt Nam.",
    ],
    # Theme 3: cafe hopping story
    "hopping": [
        "Chủ nhật của {name}: cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.",
        "{name} vừa hoàn thành challenge 'thử 10 quán mới trong tháng' — thấy Cần Thơ có nhiều gem hơn tưởng.",
        "Route cafe của {name} sáng nay: Đường 30/4 → Mậu Thân → Nguyễn Văn Cừ. 4 tiếng, 3 ly, 1 tim thổn thức.",
        "Đi cafe cùng bạn của {name}: mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.",
        "{name} thấy Cần Thơ tháng này bùng nổ quán mới — không kịp thử hết, phải chọn lọc kỹ.",
        "Cafe hopping trong Ninh Kiều: {name} khuyên bắt đầu từ trung tâm, đi ra ngoại vi dần theo view sông.",
    ],
    # Theme 4: cozy discovery / space vibes
    "discovery": [
        "Tình cờ {name} rẽ vào hẻm, gặp một quán cafe nhỏ — không ngờ decor và đồ uống đều 10 điểm.",
        "{name} phát hiện một góc chill mới trong Cần Thơ — không đông, wifi mạnh, sẽ giấu tên chút xíu.",
        "Điểm cộng lớn của quán hôm nay theo {name}: ánh sáng tự nhiên tràn ngập, chụp hình siêu đẹp.",
        "Ai như {name} thích không gian nhỏ, ấm cúng — thấy quán này là mê ngay từ lần đầu bước vào.",
        "Quán ẩn trong sâu, khó tìm nhưng đáng — {name} nghĩ đó là dấu hiệu của một chỗ đáng giá.",
        "{name} recommend quán này cho ai muốn tìm không gian tĩnh để đọc sách hoặc journaling cuối tuần.",
    ],
    # Theme 5: dessert / food review
    "dessert": [
        "Tráng miệng ở quán mới của {name} hôm nay: tiramisu homemade, mềm ẩm, không ngọt gắt.",
        "{name} thử combo cafe + croissant — bánh giòn tan, cafe đậm, kiểu breakfast hoàn hảo cuối tuần.",
        "Bánh mì Cần Thơ vs bánh mì Sài Gòn: {name} vote Cần Thơ ngon hơn — patê đậm đà, giá cực rẻ.",
        "Ai đi cafe với {name} biết mình lười order đồ ăn, nhưng bánh flan quán này thay đổi mọi suy nghĩ.",
        "Menu tráng miệng của quán này quá dài — {name} thử được 3 món, tất cả đều above expectation.",
        "{name} không phải fan sweets nhưng chocolate cake ở đây làm mình đổi ý — recommend thử 1 lần.",
    ],
}


def caption_template_for(blog_index: int, seed_str: str) -> str:
    """Return raw caption template với placeholder {name} chưa resolve.

    Caller (SQL generator) split around {name} rồi concat với v_user_full_name
    trong DO block, để inject tên thật của reviewer runtime.

    blog_index 1 → review, 2 → tasting, 3 → hopping, 4 → discovery, 5 → dessert.
    seed_str: chuỗi deterministic (thường f"rev{index}_blog{n}") để pick caption
    khác nhau cho từng (reviewer, blog).
    """
    theme_by_blog = {
        1: "review",
        2: "tasting",
        3: "hopping",
        4: "discovery",
        5: "dessert",
    }
    theme = theme_by_blog[blog_index]
    pool = CAPTIONS_BY_THEME[theme]
    seed = (sum(ord(c) for c in seed_str) * blog_index) % len(pool)
    return pool[seed]  # template có {name} placeholder


def sanity_check() -> None:
    assert len(CAPTIONS_BY_THEME) == 5, f"Expect 5 themes, got {len(CAPTIONS_BY_THEME)}"
    for theme, pool in CAPTIONS_BY_THEME.items():
        assert len(pool) == 6, f"Theme {theme} phải có 6 captions, got {len(pool)}"
        for cap in pool:
            assert "{name}" in cap, f"Caption thiếu {{name}}: {cap}"


sanity_check()

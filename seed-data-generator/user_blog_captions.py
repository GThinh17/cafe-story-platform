"""
Pool caption tiếng Việt cho blog cá nhân của user thường.

Khác blog cafe (góc nhìn quán) và reviewer (góc nhìn chuyên nghiệp): đây là góc
nhìn khách hàng phổ thông — chia sẻ ly nước vừa uống, chill cuối tuần, checkin
quán quen. KHÔNG chèn tên vào caption (author đã hiển thị ở phần user profile).

20 nhóm theme khớp thứ tự THEMES trong download_pexels_user_blog_images.py.
Mỗi slot dùng theme của mình để pick caption pool tương ứng.
"""

from __future__ import annotations

import hashlib
from typing import List

# 5 caption/theme × 20 theme = 100 caption pool
CAPTIONS_BY_THEME = {
    # 1. vietnamese coffee
    "vietnamese coffee": [
        "Sáng nay làm ly cà phê sữa đá — vẫn là chân ái mỗi ngày làm việc.",
        "Cà phê phin Việt truyền thống, đậm đà đến giọt cuối cùng, không có gì thay thế được.",
        "Đơn giản mà chuẩn: cà phê sữa đá, chút đá lạnh, một buổi sáng chill.",
        "Đi làm sớm, ghé quán quen làm ly cà phê Việt cho tỉnh táo cả ngày.",
        "Cà phê phin nhỏ giọt chậm rãi — đúng chất Sài thành cho một sáng thứ Hai.",
    ],
    # 2. coffee cup
    "coffee cup": [
        "Ly cà phê buổi sáng — đôi khi chỉ cần vậy để bắt đầu ngày mới.",
        "Cầm ly cà phê ấm trên tay, ngồi ngắm phố phường qua ô cửa — cảm giác rất chill.",
        "Ly nhỏ, hương thơm lớn — cảm ơn barista đã pha một ly hoàn hảo.",
        "Không có gì bằng ly cà phê nóng vào buổi sáng se lạnh của Cần Thơ.",
        "Chỉ một ly cà phê thôi cũng đủ khiến cả buổi sáng trở nên dễ chịu.",
    ],
    # 3. iced coffee
    "iced coffee": [
        "Trưa nắng Cần Thơ, một ly cà phê đá là cứu tinh đúng nghĩa.",
        "Cà phê đá xay mát lạnh — giải nhiệt tuyệt vời cho ngày oi bức.",
        "Đá viên tan chậm, cà phê giữ vị nguyên — quán này chuẩn từng chi tiết.",
        "Ly cà phê đá buổi trưa — vừa tỉnh táo vừa mát lòng để tiếp tục công việc.",
        "Iced coffee đúng bài: đậm, không loãng, hậu ngọt nhẹ, uống bao nhiêu cũng được.",
    ],
    # 4. espresso
    "espresso": [
        "Shot espresso buổi sáng, cả người tỉnh táo hẳn ra trong 5 phút.",
        "Espresso đắng vừa phải, hậu ngọt — đúng gu cho ai thích cà phê nguyên chất.",
        "Ly nhỏ mà lực, tinh thần lên ngay sau ngụm đầu tiên.",
        "Espresso ở quán này ổn định qua nhiều lần ghé — điểm cộng lớn.",
        "Chỉ 30ml nhưng đủ đánh thức mọi giác quan trước cuộc họp sáng.",
    ],
    # 5. latte art
    "latte art": [
        "Latte hôm nay đẹp quá — tiếc là uống xong là mất luôn hình.",
        "Barista vẽ hình tim trên ly latte làm mình cười tít cả buổi sáng.",
        "Vị latte mượt, sữa vừa vặn, art thì khỏi bàn — đáng đồng tiền.",
        "Ghé đây chỉ để coi barista biểu diễn latte art thôi cũng đáng.",
        "Ly latte hôm nay đẹp đến mức chưa dám uống, chỉ chụp ảnh thôi.",
    ],
    # 6. cappuccino
    "cappuccino": [
        "Cappuccino sáng nay foam mịn, cà phê đậm — chuẩn tỷ lệ 1-1-1.",
        "Foam sữa dày, mềm mượt — cappuccino ở đây làm rất kỹ.",
        "Ly cappuccino ấm nóng cho một buổi sáng dễ chịu tại Cần Thơ.",
        "Rắc thêm chút bột quế lên foam, hương thơm cả bàn.",
        "Cappuccino cân bằng vị đắng và ngọt, uống hoài không chán.",
    ],
    # 7. cold brew coffee
    "cold brew coffee": [
        "Cold brew ở đây ủ 12 tiếng — vị mượt, ít chua, mê ngay từ ly đầu.",
        "Ngày nóng như hôm nay, cold brew là lựa chọn không thể tuyệt hơn.",
        "Cold brew đá viên trong veo, không loãng, hậu vị lưu mãi.",
        "Không đắng gắt như espresso, cold brew hợp cho cả buổi chiều dài.",
        "Ly cold brew đen tuyền, uống chậm để cảm nhận từng lớp hương.",
    ],
    # 8. milk tea
    "milk tea": [
        "Trà sữa chiều nay ngọt vừa, trân châu dai — chuẩn gu.",
        "Cuối tuần chill với ly trà sữa quen — đơn giản mà vui.",
        "Trà sữa ở đây thơm mùi trà thật, không bị át bởi sữa — điểm cộng.",
        "Order size L, ít đường, nhiều đá — công thức bất bại của mình.",
        "Ly trà sữa mát lạnh cho một buổi chiều mưa Cần Thơ.",
    ],
    # 9. bubble tea
    "bubble tea": [
        "Trân châu đen dai vừa, không quá dẻo, không quá cứng — chuẩn.",
        "Bubble tea buổi chiều: một ly để ngồi tám chuyện với bạn cả tiếng.",
        "Trân châu đường đen, thêm phô mai kem — combo bất bại cho ai mê ngọt.",
        "Ly trân châu size lớn cho ngày làm việc dài — vừa uống vừa nhai vui.",
        "Trà sữa trân châu ở đây làm mình quay lại lần thứ ba trong tuần.",
    ],
    # 10. matcha latte
    "matcha latte": [
        "Ly matcha latte chiều nay — xanh mướt và thơm mùi trà Nhật.",
        "Matcha vị đậm, không bị đắng gắt, hòa quyện hoàn hảo với sữa.",
        "Nghiện matcha nặng — ngày nào cũng phải ghé làm 1 ly để yên tâm.",
        "Foam sữa mịn, bột matcha rắc trên cùng đẹp mắt và thơm.",
        "Matcha latte đá — vừa mát vừa healthy, hợp cho ngày nóng.",
    ],
    # 11. smoothie drink
    "smoothie drink": [
        "Ly sinh tố xoài chiều nay — tươi mát, không bị đá loãng.",
        "Sinh tố dâu nguyên chất, ngọt tự nhiên, không có đường thêm.",
        "Sinh tố bơ béo mịn — một ly bằng cả bữa nhẹ.",
        "Smoothie trái cây theo mùa — quán này chọn nguyên liệu rất tươi.",
        "Sinh tố xanh cải bó xôi + chuối — nghe lạ mà thử mới thấy ngon.",
    ],
    # 12. fruit tea
    "fruit tea": [
        "Trà đào cam sả cho ngày oi — chua ngọt cân bằng, uống mãi không ngán.",
        "Trà trái cây nhiều tầng vị: chua, ngọt, thanh — đúng gu mùa hè.",
        "Ly trà vải nhiệt đới lạnh ngắt — giải nhiệt tức thì cho buổi trưa.",
        "Trà đào miếng đào tươi thật, không phải syrup — chất lượng ổn.",
        "Fruit tea của quán này màu đẹp, vị tự nhiên, không quá ngọt.",
    ],
    # 13. lemonade
    "lemonade": [
        "Nước chanh muối buổi trưa — chua nhẹ, mặn nhẹ, giải nhiệt tức thì.",
        "Ly lemonade ga chiều nay — mát rượi, uống là quên hết mệt.",
        "Chanh tươi vắt tay, không dùng syrup — vị chua tự nhiên khó cưỡng.",
        "Lemonade bạc hà — thơm cay nhẹ, refresh cả người trong 3 giây.",
        "Đơn giản mà đỉnh: nước chanh mật ong đá — công thức nhà mình vẫn hay làm.",
    ],
    # 14. iced tea
    "iced tea": [
        "Trà đá miễn phí Việt Nam vẫn là quốc bảo — bàn nào cũng đầy bình.",
        "Trà đá lipton chanh — combo huyền thoại cho những chiều nóng.",
        "Ly trà đá không đường, nhâm nhi cả tiếng vẫn không thấy loãng.",
        "Trà đá quán này pha đậm, uống là đã khát ngay.",
        "Đơn giản nhất trần đời: trà đá, một cuốn sách, một buổi chiều rảnh.",
    ],
    # 15. cocktail bar
    "cocktail bar": [
        "Cocktail tối nay: Mojito bạc hà đá xay, thơm chanh và rum nhẹ nhàng.",
        "Bar tối cuối tuần, ly Margarita muối viền — không thể chill hơn.",
        "Bartender ở đây pha cocktail rất tinh tế, mỗi ly là một câu chuyện.",
        "Old Fashioned kiểu cổ điển — whisky đắng nhẹ, đường và cam, đúng gu.",
        "Cocktail signature của quán này chưa thấy chỗ nào làm giống.",
    ],
    # 16. bar counter
    "bar counter": [
        "Ngồi quầy bar tối nay, ngắm bartender pha chế — như coi biểu diễn nghệ thuật.",
        "Quầy bar ấm cúng, ánh đèn vàng dịu — chỗ chill lý tưởng sau giờ làm.",
        "Bar counter dài, ghế cao — kiểu quán cổ điển mình thích.",
        "Ngồi quầy nói chuyện với bartender, học được vài công thức mới.",
        "Ánh đèn quầy bar phản chiếu qua chai rượu — không gian ăn ảnh cực.",
    ],
    # 17. bartender
    "bartender": [
        "Bartender kể chuyện trong lúc pha chế — mỗi ly kèm một câu chuyện.",
        "Kỹ thuật lắc shaker mượt mà, chỉ nhìn thôi đã thấy tay nghề.",
        "Bartender ở đây tư vấn cocktail rất chuẩn khẩu vị khách.",
        "Đi bar mà gặp bartender giỏi là buổi tối trọn vẹn — đêm nay vậy đấy.",
        "Xin công thức xong về nhà tự thử — bartender siêu nhiệt tình.",
    ],
    # 18. coffee bar
    "coffee bar": [
        "Ngồi quầy pha chế ngắm barista làm việc — nhìn thôi đã thấy chill.",
        "Coffee bar nhỏ mà đầy đủ dụng cụ — dấu hiệu của cafe chuyên nghiệp.",
        "Espresso machine nổ ầm ầm, mùi cà phê thơm lừng cả quán.",
        "Quầy pha chế gọn gàng, sạch sẽ — đánh giá cao chất lượng vệ sinh.",
        "Ngồi bar nhấp espresso, nói chuyện với barista về hạt cà phê.",
    ],
    # 19. juice bar
    "juice bar": [
        "Ly nước ép cam tươi buổi sáng — vitamin C liều cao cho ngày mới.",
        "Nước ép cà rốt táo — vị ngọt tự nhiên, không thêm đường.",
        "Juice bar này ép trực tiếp trước mặt, tươi 100%, không đóng chai sẵn.",
        "Nước ép dứa gừng — cay ấm nhẹ, tốt cho tiêu hoá.",
        "Ly nước ép rau xanh detox — cuối tuần thanh lọc cơ thể.",
    ],
    # 20. cafe drinks
    "cafe drinks": [
        "Menu đồ uống ở quán này phong phú thật — không biết chọn gì cho hết.",
        "Đủ loại đồ uống từ cà phê đến trà, không có ly nào là dở.",
        "Order combo đồ uống + bánh — chill cả buổi chiều rồi.",
        "Đồ uống ở đây trình bày đẹp, chất lượng đồng đều — quay lại nhiều lần.",
        "Menu update theo mùa — mỗi lần ghé lại có món mới để thử.",
    ],
}


def _theme_for_slot(slot_idx: int, theme_list: List[str]) -> str:
    return theme_list[slot_idx % len(theme_list)]


def caption_for_slot(slot_idx: int, theme_list: List[str], seed: str) -> str:
    """Pick 1 caption từ pool của theme tương ứng slot, deterministic theo seed."""
    theme = _theme_for_slot(slot_idx, theme_list)
    pool = CAPTIONS_BY_THEME.get(theme)
    if not pool:
        raise KeyError(f"Không có caption pool cho theme '{theme}'")
    digest = hashlib.md5(seed.encode("utf-8")).hexdigest()
    idx = int(digest[:8], 16) % len(pool)
    return pool[idx]

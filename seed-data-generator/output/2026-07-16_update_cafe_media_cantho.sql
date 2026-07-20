-- UPDATE 20 cafe đã seed: thêm description + avatar_url + cover_url.
-- Match theo cafe_pages.name (unique trong batch này).
-- Preflight check: 20 quán phải tồn tại — nếu thiếu, abort.
-- Idempotent: chạy lại được, giá trị cuối cùng thắng.

BEGIN;

DO $$
DECLARE
    v_existing_count integer;
BEGIN
    -- Preflight: 20 cafe name phải có sẵn trong DB
    SELECT count(*) INTO v_existing_count
    FROM cafe_pages
    WHERE name IN (
        'Phúc Long Coffee & Tea',
        'Highlands Coffee Vincom Cần Thơ',
        'Trung Nguyên Legend Cà Phê',
        'Katinat Saigon Kafe',
        'The 80''s iCafe',
        'Đậu Ơi Coffee & Tea',
        'Trầm Coffee & Tea',
        'Cà phê Nhà Phạm',
        'Nhà Phạm trong rừng',
        'Nhà Phạm bên cầu',
        'Là Cafe',
        'Raw Coffee',
        'Tiệm Trà Cỏ Ngọt',
        'Time Cafe',
        'Sky Bar Iris',
        'Aurora Coffee',
        'HiHi Onigiri',
        'Mật Ngọt Coffee',
        'Chịn Cà Phê',
        'Tiệm Cà Phê Nhà Có Khách'
    );
    IF v_existing_count <> 20 THEN
        RAISE EXCEPTION 'Chỉ tìm được %/20 cafe theo tên — abort. Có thể chưa apply seed cafe.', v_existing_count;
    END IF;
END $$;

-- Cafe #01: Phúc Long Coffee & Tea
UPDATE cafe_pages SET
    description = 'Chuỗi trà và cà phê Việt Nam nổi tiếng, kết hợp hương vị trà truyền thống với cà phê và các món bánh ngọt tinh tế.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291018/cafestory/cafes/mmubymqyja7opniy6wzb.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291057/cafestory/cafes/rn4uhwqzqqw3tl2v3jpz.jpg',
    updated_at  = now()
WHERE name = 'Phúc Long Coffee & Tea';

-- Cafe #02: Highlands Coffee Vincom Cần Thơ
UPDATE cafe_pages SET
    description = 'Không gian hiện đại tại Vincom Plaza Cần Thơ, mặt bằng rộng cả indoor và outdoor, phù hợp gặp gỡ bạn bè và làm việc.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291020/cafestory/cafes/sekxebzyycbv0taqdsx0.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291060/cafestory/cafes/awfmxcgqhqkildnrtbfv.jpg',
    updated_at  = now()
WHERE name = 'Highlands Coffee Vincom Cần Thơ';

-- Cafe #03: Trung Nguyên Legend Cà Phê
UPDATE cafe_pages SET
    description = 'Cà phê Việt trong không gian sang trọng, sáng tạo, đậm bản sắc văn hóa cà phê Tây Đô với diện tích rộng rãi.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291022/cafestory/cafes/fl4nq7wvoudo07bpyh0u.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291062/cafestory/cafes/h5xakyim9x8dfbdctan3.png',
    updated_at  = now()
WHERE name = 'Trung Nguyên Legend Cà Phê';

-- Cafe #04: Katinat Saigon Kafe
UPDATE cafe_pages SET
    description = 'Cafe hiện đại giới trẻ yêu thích, thiết kế sang-sáng, nhiều góc check-in đẹp ngay trung tâm Ninh Kiều sầm uất.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291024/cafestory/cafes/wgmqoq8klsr0hgrwxydn.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291066/cafestory/cafes/rjugbihwccrv3vjtgwm1.png',
    updated_at  = now()
WHERE name = 'Katinat Saigon Kafe';

-- Cafe #05: The 80's iCafe
UPDATE cafe_pages SET
    description = 'Quán quen của dân chạy deadline 24/7, không gian rộng rãi và ít ồn ào, gần Đại học Cần Thơ, có thể đặt bàn trước.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291026/cafestory/cafes/d8ahq97rc2ynsxlsq3lr.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291068/cafestory/cafes/ejenjrdrekiwpejpqypx.jpg',
    updated_at  = now()
WHERE name = 'The 80''s iCafe';

-- Cafe #06: Đậu Ơi Coffee & Tea
UPDATE cafe_pages SET
    description = 'Quán học bài và làm việc xuyên đêm, không gian minimalism sáng thoáng, thức uống ngon với mức giá học sinh - sinh viên.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291029/cafestory/cafes/rsltdsjx8rlff0flz7ss.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291070/cafestory/cafes/hunu0ssi3pvt4qdkfyoh.jpg',
    updated_at  = now()
WHERE name = 'Đậu Ơi Coffee & Tea';

-- Cafe #07: Trầm Coffee & Tea
UPDATE cafe_pages SET
    description = 'Cafe & trà mở 24/7, không gian trẻ trung, phục vụ đông sinh viên khu vực Nguyễn Văn Cừ nối dài.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291031/cafestory/cafes/zhh7deqe3wc900y41yoa.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291072/cafestory/cafes/mic9mksat24evxqkb8lj.jpg',
    updated_at  = now()
WHERE name = 'Trầm Coffee & Tea';

-- Cafe #08: Cà phê Nhà Phạm
UPDATE cafe_pages SET
    description = 'Không gian mộc mạc pha vintage, decor nhiều cây xanh và ánh sáng vàng, mang lại cảm giác ''về nhà'' giữa lòng thành phố.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291032/cafestory/cafes/grtiidpxfujju5jgrbf0.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291075/cafestory/cafes/ucftikktx2egmemzxdtc.jpg',
    updated_at  = now()
WHERE name = 'Cà phê Nhà Phạm';

-- Cafe #09: Nhà Phạm trong rừng
UPDATE cafe_pages SET
    description = 'Chi nhánh nằm trong khu Giảng viên ĐH Cần Thơ, không gian xanh mát như ''ốc đảo'' giữa thành phố, thích hợp học bài, làm việc.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291034/cafestory/cafes/k72enlm7vz1ttijhqd6g.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291078/cafestory/cafes/awbf3ejniv9iaqjwlydc.jpg',
    updated_at  = now()
WHERE name = 'Nhà Phạm trong rừng';

-- Cafe #10: Nhà Phạm bên cầu
UPDATE cafe_pages SET
    description = 'Cafe view sông ngay chân cầu Đầu Sấu, sáng sớm và chiều mát view rất chill, phù hợp uống cà phê ngắm cảnh.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291035/cafestory/cafes/k5xpep0bbakd1qlfcs2x.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291081/cafestory/cafes/gayv5tyupow4nvwg932p.jpg',
    updated_at  = now()
WHERE name = 'Nhà Phạm bên cầu';

-- Cafe #11: Là Cafe
UPDATE cafe_pages SET
    description = 'Không gian ấm cúng, chỗ ngồi rộng rãi, thực đơn đa dạng với cacao và espresso được đánh giá cao ở khu vực Ninh Kiều.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291037/cafestory/cafes/zh3wtfn7sew6hdtpfu5u.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291084/cafestory/cafes/y7yqcj0hl3umziznmzf3.jpg',
    updated_at  = now()
WHERE name = 'Là Cafe';

-- Cafe #12: Raw Coffee
UPDATE cafe_pages SET
    description = 'Không gian gần gũi thiên nhiên độc đáo, view cầu Quang Trung, phong cách phục vụ và decor riêng biệt.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291038/cafestory/cafes/cbqbvzmoqeycnnxqwejg.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291087/cafestory/cafes/xulejov2hdu9fu6vbbhw.jpg',
    updated_at  = now()
WHERE name = 'Raw Coffee';

-- Cafe #13: Tiệm Trà Cỏ Ngọt
UPDATE cafe_pages SET
    description = 'Không gian phong cách Nhật Bản mộc mạc, chủ yếu trang trí gỗ, phù hợp uống trà chiều và thư giãn.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291041/cafestory/cafes/f1h5bbdxiwm4arugd0md.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291089/cafestory/cafes/tjotbn8fbtzzi3zhhfrg.jpg',
    updated_at  = now()
WHERE name = 'Tiệm Trà Cỏ Ngọt';

-- Cafe #14: Time Cafe
UPDATE cafe_pages SET
    description = 'Cafe bình dân trên đường Võ Văn Tần, mức giá 20k-50k, không gian đơn giản dễ chịu cho học sinh sinh viên.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291042/cafestory/cafes/xpmbfhharnuomlrigz0f.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291090/cafestory/cafes/q6vglth27dbdtp9qo4wf.jpg',
    updated_at  = now()
WHERE name = 'Time Cafe';

-- Cafe #15: Sky Bar Iris
UPDATE cafe_pages SET
    description = 'Sân thượng có view sông và cầu Quang Trung, đồ uống và không gian lên hình đẹp, chill về đêm.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291044/cafestory/cafes/j8v1ruvem7jxaecsxq7g.webp',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291094/cafestory/cafes/deaytkql4mio9vy1weii.jpg',
    updated_at  = now()
WHERE name = 'Sky Bar Iris';

-- Cafe #16: Aurora Coffee
UPDATE cafe_pages SET
    description = 'Cafe view đẹp, wifi mạnh, không gian sạch sẽ không khói thuốc, nhân viên phục vụ thân thiện gần Cao đẳng Cần Thơ.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291046/cafestory/cafes/xlbbj9dwc4c7yenftoan.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291095/cafestory/cafes/u7mrumijxldehyv9a13j.jpg',
    updated_at  = now()
WHERE name = 'Aurora Coffee';

-- Cafe #17: HiHi Onigiri
UPDATE cafe_pages SET
    description = 'Phong cách bánh bèo hường phấn đáng yêu, ngoài đồ uống còn phục vụ bánh ngọt và đồ ăn vặt nhẹ.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291048/cafestory/cafes/bzveir77mtbfj9pqdczw.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291098/cafestory/cafes/mllzt50iotdrubwygorx.jpg',
    updated_at  = now()
WHERE name = 'HiHi Onigiri';

-- Cafe #18: Mật Ngọt Coffee
UPDATE cafe_pages SET
    description = 'Không gian ngọt ngào ấm cúng, decor tone pastel, phục vụ nước ngọt và đồ ăn nhẹ, mới mở gần đây.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291051/cafestory/cafes/yviiamnyzocspyfhnx4z.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291100/cafestory/cafes/jleds9m6rhaatcitt2dt.jpg',
    updated_at  = now()
WHERE name = 'Mật Ngọt Coffee';

-- Cafe #19: Chịn Cà Phê
UPDATE cafe_pages SET
    description = 'Cafe KDC Đại Ngân, không gian yên tĩnh phù hợp học tập và làm việc lâu, mức giá bình dân.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291053/cafestory/cafes/lacqttlo7nszcr4wgccw.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291102/cafestory/cafes/wa60vaspklf6qa6wowiw.jpg',
    updated_at  = now()
WHERE name = 'Chịn Cà Phê';

-- Cafe #20: Tiệm Cà Phê Nhà Có Khách
UPDATE cafe_pages SET
    description = 'Cafe mới mở phong cách ''về nhà'', không gian ấm cúng phục vụ đông khách sinh viên và người ghé nghỉ chân.',
    avatar_url  = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291055/cafestory/cafes/fy3ofercywtkiofhk1jt.jpg',
    cover_url   = 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291105/cafestory/cafes/vkneugcqehbbzk1b73el.jpg',
    updated_at  = now()
WHERE name = 'Tiệm Cà Phê Nhà Có Khách';

COMMIT;

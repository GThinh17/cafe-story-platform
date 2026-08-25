-- Seed 40 blog cá nhân cho 40 user Cần Thơ CHỈ có role USER.
--   Loại: admin, cafe owner (CAFE_PAGE), reviewer (REVIEWER).
--   Mỗi user 1 blog cá nhân (page_id NULL), 1 ảnh Cloudinary/blog.
--   Ảnh Pexels chủ đề quán nước / thức uống / cà phê / quầy bar (folder cafestory/user_blogs).
-- Follow rule BE:
--   * author_user_id = user
--   * page_id = NULL (blog cá nhân → hiện ở user profile, KHÔNG hiện ở cafe page)
--   * region_id = user.region_id (BE require)
--   * content tone khách hàng thường (không chèn tên vì đã hiển thị author)
-- User pool: 40 user Cần Thơ (province_code='92') non-admin, không có role
-- CAFE_PAGE và không có role REVIEWER — deterministic ORDER BY md5(user_id) LIMIT 40.
-- created_at rải interleaved 60 ngày qua (2160 min/slot).

BEGIN;

DO $$
DECLARE
    v_user_ids   uuid[];
    v_user_id    uuid;
    v_region_id  uuid;
    v_blog_id    uuid;
BEGIN
    -- Pick 40 user Cần Thơ chỉ có role USER (skip admin/CAFE_PAGE/REVIEWER),
    -- deterministic theo md5(user_id)
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_user_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '92'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
          AND NOT EXISTS (
              SELECT 1
              FROM user_roles ur
              JOIN roles ro ON ro.id = ur.role_id
              WHERE ur.user_id = u.user_id
                AND ro.name IN ('ADMIN', 'CAFE_PAGE', 'REVIEWER')
          )
        ORDER BY md5(u.user_id::text)
        LIMIT 40
    ) sub;
    IF v_user_ids IS NULL OR array_length(v_user_ids, 1) < 40 THEN
        RAISE EXCEPTION 'Chỉ pick được %/40 user chỉ role USER, cần đủ 40.',
                        coalesce(array_length(v_user_ids, 1), 0);
    END IF;

    -- === User #001 (slot 0, theme 'vietnamese coffee') ===
    v_user_id := v_user_ids[1];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cà phê phin Việt truyền thống, đậm đà đến giọt cuối cùng, không có gì thay thế được.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '0 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344666/cafestory/user_blogs/moixflyzgc5awabsajcb.jpg');

    -- === User #002 (slot 1, theme 'coffee cup') ===
    v_user_id := v_user_ids[2];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ly nhỏ, hương thơm lớn — cảm ơn barista đã pha một ly hoàn hảo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '2160 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344667/cafestory/user_blogs/lvtheckdjoydfvsgdgi8.jpg');

    -- === User #003 (slot 2, theme 'iced coffee') ===
    v_user_id := v_user_ids[3];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Trưa nắng Cần Thơ, một ly cà phê đá là cứu tinh đúng nghĩa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '4320 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344669/cafestory/user_blogs/vxs0cuycg8ztsdyd0iak.jpg');

    -- === User #004 (slot 3, theme 'espresso') ===
    v_user_id := v_user_ids[4];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ly nhỏ mà lực, tinh thần lên ngay sau ngụm đầu tiên.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '6480 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344671/cafestory/user_blogs/upwpreaafuc0e0ljb92k.jpg');

    -- === User #005 (slot 4, theme 'latte art') ===
    v_user_id := v_user_ids[5];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Barista vẽ hình tim trên ly latte làm mình cười tít cả buổi sáng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '8640 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344673/cafestory/user_blogs/bamqvw4mf9knuvkxsl8x.jpg');

    -- === User #006 (slot 5, theme 'cappuccino') ===
    v_user_id := v_user_ids[6];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Foam sữa dày, mềm mượt — cappuccino ở đây làm rất kỹ.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '10800 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344675/cafestory/user_blogs/ra1zpj1ral0o4gwwzi0d.jpg');

    -- === User #007 (slot 6, theme 'cold brew coffee') ===
    v_user_id := v_user_ids[7];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew đá viên trong veo, không loãng, hậu vị lưu mãi.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '12960 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344677/cafestory/user_blogs/hqp5jrfranvvaqrcjz6m.jpg');

    -- === User #008 (slot 7, theme 'milk tea') ===
    v_user_id := v_user_ids[8];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Order size L, ít đường, nhiều đá — công thức bất bại của mình.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '15120 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344678/cafestory/user_blogs/vz7gd4qj9eonnwsjj9rj.jpg');

    -- === User #009 (slot 8, theme 'bubble tea') ===
    v_user_id := v_user_ids[9];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Trà sữa trân châu ở đây làm mình quay lại lần thứ ba trong tuần.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '17280 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344680/cafestory/user_blogs/d39jxg8o0wxman9kcomo.jpg');

    -- === User #010 (slot 9, theme 'matcha latte') ===
    v_user_id := v_user_ids[10];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Foam sữa mịn, bột matcha rắc trên cùng đẹp mắt và thơm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '19440 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344682/cafestory/user_blogs/xtmx5bxsh9obhnrlqywi.jpg');

    -- === User #011 (slot 10, theme 'smoothie drink') ===
    v_user_id := v_user_ids[11];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ly sinh tố xoài chiều nay — tươi mát, không bị đá loãng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '21600 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344684/cafestory/user_blogs/qzwdf7j38cyehtsvgg23.jpg');

    -- === User #012 (slot 11, theme 'fruit tea') ===
    v_user_id := v_user_ids[12];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ly trà vải nhiệt đới lạnh ngắt — giải nhiệt tức thì cho buổi trưa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '23760 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344686/cafestory/user_blogs/mj8duvat4gk2y5vxtbms.jpg');

    -- === User #013 (slot 12, theme 'lemonade') ===
    v_user_id := v_user_ids[13];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chanh tươi vắt tay, không dùng syrup — vị chua tự nhiên khó cưỡng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '25920 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344688/cafestory/user_blogs/bqckgxqh1832fpvmnslx.jpg');

    -- === User #014 (slot 13, theme 'iced tea') ===
    v_user_id := v_user_ids[14];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Trà đá lipton chanh — combo huyền thoại cho những chiều nóng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '28080 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344690/cafestory/user_blogs/vqenwewfholhdobczdac.jpg');

    -- === User #015 (slot 14, theme 'cocktail bar') ===
    v_user_id := v_user_ids[15];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Bartender ở đây pha cocktail rất tinh tế, mỗi ly là một câu chuyện.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '30240 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344692/cafestory/user_blogs/jjpvtdyuwx1llf9tapqi.jpg');

    -- === User #016 (slot 15, theme 'bar counter') ===
    v_user_id := v_user_ids[16];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Quầy bar ấm cúng, ánh đèn vàng dịu — chỗ chill lý tưởng sau giờ làm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '32400 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344694/cafestory/user_blogs/l6ykobjpe9zcdsehyzfj.jpg');

    -- === User #017 (slot 16, theme 'bartender') ===
    v_user_id := v_user_ids[17];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Bartender kể chuyện trong lúc pha chế — mỗi ly kèm một câu chuyện.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '34560 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344696/cafestory/user_blogs/g8bnhxcc4pzlte86vcoc.jpg');

    -- === User #018 (slot 17, theme 'coffee bar') ===
    v_user_id := v_user_ids[18];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Espresso machine nổ ầm ầm, mùi cà phê thơm lừng cả quán.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '36720 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344698/cafestory/user_blogs/fsj2bx5bcdlceecixhgj.jpg');

    -- === User #019 (slot 18, theme 'juice bar') ===
    v_user_id := v_user_ids[19];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Nước ép cà rốt táo — vị ngọt tự nhiên, không thêm đường.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '38880 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344701/cafestory/user_blogs/sab96ulglqvarhz8xcjl.jpg');

    -- === User #020 (slot 19, theme 'cafe drinks') ===
    v_user_id := v_user_ids[20];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đủ loại đồ uống từ cà phê đến trà, không có ly nào là dở.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '41040 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344704/cafestory/user_blogs/pu1anuwj8rni1hbhxwkz.jpg');

    -- === User #021 (slot 20, theme 'vietnamese coffee') ===
    v_user_id := v_user_ids[21];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi làm sớm, ghé quán quen làm ly cà phê Việt cho tỉnh táo cả ngày.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '43200 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344706/cafestory/user_blogs/rkrmtiphg4v3osmjmldf.jpg');

    -- === User #022 (slot 21, theme 'coffee cup') ===
    v_user_id := v_user_ids[22];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chỉ một ly cà phê thôi cũng đủ khiến cả buổi sáng trở nên dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '45360 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344708/cafestory/user_blogs/ow0hkzxw0a3kic2g8xug.jpg');

    -- === User #023 (slot 22, theme 'iced coffee') ===
    v_user_id := v_user_ids[23];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cà phê đá xay mát lạnh — giải nhiệt tuyệt vời cho ngày oi bức.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '47520 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344709/cafestory/user_blogs/nsqoeznnnsk7wz9mtf0p.jpg');

    -- === User #024 (slot 23, theme 'espresso') ===
    v_user_id := v_user_ids[24];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Espresso ở quán này ổn định qua nhiều lần ghé — điểm cộng lớn.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '49680 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344711/cafestory/user_blogs/p96ggk9efckyrzfcsnen.jpg');

    -- === User #025 (slot 24, theme 'latte art') ===
    v_user_id := v_user_ids[25];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ghé đây chỉ để coi barista biểu diễn latte art thôi cũng đáng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '51840 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344713/cafestory/user_blogs/dururatrbesuniucs7tp.jpg');

    -- === User #026 (slot 25, theme 'cappuccino') ===
    v_user_id := v_user_ids[26];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cappuccino sáng nay foam mịn, cà phê đậm — chuẩn tỷ lệ 1-1-1.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '54000 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344714/cafestory/user_blogs/f4y8bmtotuh63ing9vyw.jpg');

    -- === User #027 (slot 26, theme 'cold brew coffee') ===
    v_user_id := v_user_ids[27];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Không đắng gắt như espresso, cold brew hợp cho cả buổi chiều dài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '56160 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344717/cafestory/user_blogs/rbbtepvtpv8vdxmlohia.png');

    -- === User #028 (slot 27, theme 'milk tea') ===
    v_user_id := v_user_ids[28];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cuối tuần chill với ly trà sữa quen — đơn giản mà vui.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '58320 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344719/cafestory/user_blogs/o80kytm73ulyekc2cbqw.jpg');

    -- === User #029 (slot 28, theme 'bubble tea') ===
    v_user_id := v_user_ids[29];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Trân châu đen dai vừa, không quá dẻo, không quá cứng — chuẩn.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '60480 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344721/cafestory/user_blogs/dqx0wv434rzzooybldtz.jpg');

    -- === User #030 (slot 29, theme 'matcha latte') ===
    v_user_id := v_user_ids[30];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Foam sữa mịn, bột matcha rắc trên cùng đẹp mắt và thơm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '62640 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344723/cafestory/user_blogs/lbriajsguo5i9tsmnwwa.jpg');

    -- === User #031 (slot 30, theme 'smoothie drink') ===
    v_user_id := v_user_ids[31];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Smoothie trái cây theo mùa — quán này chọn nguyên liệu rất tươi.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '64800 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344725/cafestory/user_blogs/w5zdlbrn5tqu9i67nbm9.jpg');

    -- === User #032 (slot 31, theme 'fruit tea') ===
    v_user_id := v_user_ids[32];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ly trà vải nhiệt đới lạnh ngắt — giải nhiệt tức thì cho buổi trưa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '66960 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344727/cafestory/user_blogs/sgktewbt1qs4l8ettmxl.jpg');

    -- === User #033 (slot 32, theme 'lemonade') ===
    v_user_id := v_user_ids[33];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chanh tươi vắt tay, không dùng syrup — vị chua tự nhiên khó cưỡng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '69120 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344729/cafestory/user_blogs/obo6pxuwntrzcgodkyo8.jpg');

    -- === User #034 (slot 33, theme 'iced tea') ===
    v_user_id := v_user_ids[34];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Trà đá quán này pha đậm, uống là đã khát ngay.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '71280 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344730/cafestory/user_blogs/afeaax5zu49k0nwiif8j.jpg');

    -- === User #035 (slot 34, theme 'cocktail bar') ===
    v_user_id := v_user_ids[35];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Bartender ở đây pha cocktail rất tinh tế, mỗi ly là một câu chuyện.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '73440 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344732/cafestory/user_blogs/xzdjqmvt9lzcsuaymryd.jpg');

    -- === User #036 (slot 35, theme 'bar counter') ===
    v_user_id := v_user_ids[36];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ngồi quầy nói chuyện với bartender, học được vài công thức mới.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '75600 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344734/cafestory/user_blogs/ktywo5cx3vdvqjpyba5a.jpg');

    -- === User #037 (slot 36, theme 'bartender') ===
    v_user_id := v_user_ids[37];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Bartender kể chuyện trong lúc pha chế — mỗi ly kèm một câu chuyện.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '77760 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344736/cafestory/user_blogs/s5h2w9xa3cudeqpl3jji.jpg');

    -- === User #038 (slot 37, theme 'coffee bar') ===
    v_user_id := v_user_ids[38];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Espresso machine nổ ầm ầm, mùi cà phê thơm lừng cả quán.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '79920 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344738/cafestory/user_blogs/zkvnegxuiyo45fph0mgs.jpg');

    -- === User #039 (slot 38, theme 'juice bar') ===
    v_user_id := v_user_ids[39];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Ly nước ép cam tươi buổi sáng — vitamin C liều cao cho ngày mới.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '82080 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344740/cafestory/user_blogs/ifxpbzkedv8llajw5koc.jpg');

    -- === User #040 (slot 39, theme 'cafe drinks') ===
    v_user_id := v_user_ids[40];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;
    END IF;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đủ loại đồ uống từ cà phê đến trà, không có ly nào là dở.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '84240 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784344741/cafestory/user_blogs/c9wku4q1rd62zr6m8h1n.jpg');

END $$;

COMMIT;

-- Seed 100 blog cho 20 cafe Cần Thơ (5 blog/cafe, 2 ảnh/blog = 200 blog_images).
-- Follow rule BE:
--   * author_user_id = cafe.owner_user_id (query runtime)
--   * page_id = cafe.id (blog thuộc cafe page)
--   * region_id = cafe.region_id (BE require không null)
--   * status = PUBLISHED, defaults cho pin/comment/counts
-- Caption tiếng Việt về cà phê, theme rotate theo blog_index (1-5).
-- created_at rải interleaved trong 60 ngày gần đây
-- (864 phút/slot × 100 blog).
-- Ảnh Cloudinary từ blog_image_urls.json (đã upload trước đó).

BEGIN;

DO $$
DECLARE
    v_cafe_id   uuid;
    v_owner_id  uuid;
    v_region_id uuid;
    v_blog_id   uuid;
BEGIN
    -- === Cafe #01: Phúc Long Coffee & Tea ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Phúc Long Coffee & Tea';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Phúc Long Coffee & Tea';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 0, 0 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Khởi đầu ngày mới bằng ly espresso đậm đà tại Phúc Long Coffee & Tea — hương vị đánh thức mọi giác quan.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '0 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294159/cafestory/blogs/kpae125rmn3qqz6q8wkv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294161/cafestory/blogs/rvuayqewv4ai8xjhzuos.jpg');

    -- Blog #2 (slot 20, 17280 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte art tại Phúc Long Coffee & Tea đỉnh thật — mỗi ly là một tác phẩm, tiếc là uống xong mất luôn.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '17280 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294164/cafestory/blogs/haqderldfvb2hwnpfjyg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294166/cafestory/blogs/snjoz57qhvj2qkolb70k.jpg');

    -- Blog #3 (slot 40, 34560 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Cappuccino ở Phúc Long Coffee & Tea có lớp foam mịn, tỉ lệ hoàn hảo — uống xong nhẹ nhàng cả buổi.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '34560 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294168/cafestory/blogs/nesjy0bbx3rtsf26eogv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294170/cafestory/blogs/tqav83m41pys2ckqnl9b.jpg');

    -- Blog #4 (slot 60, 51840 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Chỗ ngồi ở Phúc Long Coffee & Tea rất hợp học bài — wifi mạnh, ổ cắm nhiều, không gian yên tĩnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '51840 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294172/cafestory/blogs/uzyfoisjwthewnu9tdgn.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294174/cafestory/blogs/eua8rb2bta6skefay9f6.jpg');

    -- Blog #5 (slot 80, 69120 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Phúc Long Coffee & Tea đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '69120 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294176/cafestory/blogs/l30a00twba17zfs4zo33.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294178/cafestory/blogs/liwrsfwehzywfmsr5yhy.jpg');

    -- === Cafe #02: Highlands Coffee Vincom Cần Thơ ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Highlands Coffee Vincom Cần Thơ';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Highlands Coffee Vincom Cần Thơ';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 1, 864 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Khởi đầu ngày mới bằng ly espresso đậm đà tại Highlands Coffee Vincom Cần Thơ — hương vị đánh thức mọi giác quan.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '864 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294179/cafestory/blogs/ryowyu4upmkiyesdfz79.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294181/cafestory/blogs/rv4zbxuecvulgijbaczg.jpg');

    -- Blog #2 (slot 21, 18144 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte art tại Highlands Coffee Vincom Cần Thơ đỉnh thật — mỗi ly là một tác phẩm, tiếc là uống xong mất luôn.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '18144 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294183/cafestory/blogs/jixk9zf4oyhhg86lmta1.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294185/cafestory/blogs/dtqhg4netkvljs24zy0i.jpg');

    -- Blog #3 (slot 41, 35424 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Cappuccino ở Highlands Coffee Vincom Cần Thơ có lớp foam mịn, tỉ lệ hoàn hảo — uống xong nhẹ nhàng cả buổi.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '35424 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294187/cafestory/blogs/ltbyvzgw5qijmqu1rir5.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294189/cafestory/blogs/prhqytnqgiqghjzjpg98.jpg');

    -- Blog #4 (slot 61, 52704 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Chỗ ngồi ở Highlands Coffee Vincom Cần Thơ rất hợp học bài — wifi mạnh, ổ cắm nhiều, không gian yên tĩnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '52704 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294191/cafestory/blogs/radiyytnsya1cqt6soes.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294193/cafestory/blogs/wi9jp5wjxt6p85dlvwqm.jpg');

    -- Blog #5 (slot 81, 69984 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Highlands Coffee Vincom Cần Thơ đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '69984 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294195/cafestory/blogs/wl1klqwoabvfnzcxt83j.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294197/cafestory/blogs/qczam3kfa2j5emsgoysn.jpg');

    -- === Cafe #03: Trung Nguyên Legend Cà Phê ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Trung Nguyên Legend Cà Phê';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Trung Nguyên Legend Cà Phê';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 2, 1728 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Trung Nguyên Legend Cà Phê không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '1728 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294200/cafestory/blogs/vkp5ubg4qhhkdlik06ni.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294202/cafestory/blogs/ghmlwuhrmunuxbpvatew.jpg');

    -- Blog #2 (slot 22, 19008 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Trung Nguyên Legend Cà Phê — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '19008 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294204/cafestory/blogs/nhthyhgnknczaepdkiec.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294207/cafestory/blogs/a5k3kam0smcy2ca0qpc2.jpg');

    -- Blog #3 (slot 42, 36288 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Trung Nguyên Legend Cà Phê — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '36288 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294209/cafestory/blogs/zddzgg8daduxhbvowtbs.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294212/cafestory/blogs/ofsublpuywc2vavekb9h.jpg');

    -- Blog #4 (slot 62, 53568 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Trung Nguyên Legend Cà Phê cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '53568 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294214/cafestory/blogs/wokmecc7olk3xnmiurxr.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294216/cafestory/blogs/mumavxpnndfwldgzlvxh.jpg');

    -- Blog #5 (slot 82, 70848 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Trung Nguyên Legend Cà Phê đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '70848 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294218/cafestory/blogs/hswan1w42bngb1f1fsur.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294220/cafestory/blogs/h3nwe4gbzslb8dttjtdd.jpg');

    -- === Cafe #04: Katinat Saigon Kafe ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Katinat Saigon Kafe';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Katinat Saigon Kafe';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 3, 2592 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Katinat Saigon Kafe không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '2592 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294222/cafestory/blogs/lksorbu8pztfda7bh8k5.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294224/cafestory/blogs/laev9q1sqb0qmijref31.jpg');

    -- Blog #2 (slot 23, 19872 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Katinat Saigon Kafe — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '19872 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294227/cafestory/blogs/rbdy3nkxmh9k5oskumj5.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294229/cafestory/blogs/imtgilgf7dn4aimb3maa.jpg');

    -- Blog #3 (slot 43, 37152 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Katinat Saigon Kafe — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '37152 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294231/cafestory/blogs/bgqmwjprufwq9rdbvipv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294233/cafestory/blogs/tggxa59jb28l1fofodrf.jpg');

    -- Blog #4 (slot 63, 54432 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Katinat Saigon Kafe cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '54432 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294237/cafestory/blogs/mbs7m2uecnqpzmpxjoem.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294239/cafestory/blogs/ewb8au0n61bj50zw5stc.jpg');

    -- Blog #5 (slot 83, 71712 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Katinat Saigon Kafe đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '71712 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294241/cafestory/blogs/x9n7gnxkut3xcjvbrk2u.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294243/cafestory/blogs/iantv9emjysnfwjy0sp7.jpg');

    -- === Cafe #05: The 80's iCafe ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'The 80''s iCafe';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: The 80''s iCafe';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 4, 3456 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ghé The 80''s iCafe sáng nay uống espresso, thấy tinh thần tỉnh táo hẳn để chinh phục cả tuần.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '3456 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294246/cafestory/blogs/lkhvataqdzdkhuxtuctf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294247/cafestory/blogs/klczkybpzrpswwdeipdn.jpg');

    -- Blog #2 (slot 24, 20736 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Barista The 80''s iCafe vẽ hình lá trên latte cực khéo — chưa uống đã thấy được chăm sóc.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '20736 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294250/cafestory/blogs/pw8hfwoxqtl3ylxvlpba.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294252/cafestory/blogs/kqidrsvrjkmddngrisx8.jpg');

    -- Blog #3 (slot 44, 38016 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Cappuccino The 80''s iCafe là món ruột của mình mỗi khi ghé — chưa lần nào thất vọng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '38016 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294254/cafestory/blogs/q84iyx06ouua3u7fjabc.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294257/cafestory/blogs/n7iphjwp6e6dk5ovauvh.jpg');

    -- Blog #4 (slot 64, 55296 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Học nhóm ở The 80''s iCafe — bàn rộng, chỗ đủ cho 4 người, đồ uống giá sinh viên.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '55296 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294258/cafestory/blogs/wvfbhx1w7eqi57mqftdz.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294261/cafestory/blogs/nwpj6ccb4bzv0zx42l8t.jpg');

    -- Blog #5 (slot 84, 72576 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở The 80''s iCafe đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '72576 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294263/cafestory/blogs/vi9ubee9jg5e3mt48t97.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294265/cafestory/blogs/yk5y8zi0viz1sa3tkyr0.jpg');

    -- === Cafe #06: Đậu Ơi Coffee & Tea ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Đậu Ơi Coffee & Tea';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Đậu Ơi Coffee & Tea';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 5, 4320 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Khởi đầu ngày mới bằng ly espresso đậm đà tại Đậu Ơi Coffee & Tea — hương vị đánh thức mọi giác quan.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '4320 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294267/cafestory/blogs/hpwxlh3igcjrgzyrjq03.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294269/cafestory/blogs/gwauw5j5awgof4kdtuk0.jpg');

    -- Blog #2 (slot 25, 21600 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte art tại Đậu Ơi Coffee & Tea đỉnh thật — mỗi ly là một tác phẩm, tiếc là uống xong mất luôn.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '21600 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294271/cafestory/blogs/mb3kkc1fyucd97gp8dxg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294272/cafestory/blogs/sgggenzt7nfbxbbtfhwl.jpg');

    -- Blog #3 (slot 45, 38880 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Cappuccino ở Đậu Ơi Coffee & Tea có lớp foam mịn, tỉ lệ hoàn hảo — uống xong nhẹ nhàng cả buổi.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '38880 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294274/cafestory/blogs/lu2owulaujqrym1essxe.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294277/cafestory/blogs/ky10o25wh1dxxud1ja32.jpg');

    -- Blog #4 (slot 65, 56160 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Chỗ ngồi ở Đậu Ơi Coffee & Tea rất hợp học bài — wifi mạnh, ổ cắm nhiều, không gian yên tĩnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '56160 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294279/cafestory/blogs/duwcersdupyxvmigmoju.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294280/cafestory/blogs/pnv64t8f7qqifr4mt7al.jpg');

    -- Blog #5 (slot 85, 73440 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Đậu Ơi Coffee & Tea đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '73440 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294283/cafestory/blogs/ufozfy0yqzwjhi6340nb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294285/cafestory/blogs/q9sjihiq8xyk1jdkbak6.jpg');

    -- === Cafe #07: Trầm Coffee & Tea ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Trầm Coffee & Tea';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Trầm Coffee & Tea';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 6, 5184 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Trầm Coffee & Tea không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '5184 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294287/cafestory/blogs/mddsvag05vylfrupdwtk.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294289/cafestory/blogs/alnapickoyqdrv6axn3q.jpg');

    -- Blog #2 (slot 26, 22464 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Trầm Coffee & Tea — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '22464 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294291/cafestory/blogs/o1xiiv2yj38xw4c8vrqv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294293/cafestory/blogs/xmqhqdml4uysyp6ap3b7.jpg');

    -- Blog #3 (slot 46, 39744 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Trầm Coffee & Tea — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '39744 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294295/cafestory/blogs/julxsgl2s3rin3ujlwh4.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294297/cafestory/blogs/z3abstkaxhkaf6ab5eac.jpg');

    -- Blog #4 (slot 66, 57024 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Trầm Coffee & Tea cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '57024 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294299/cafestory/blogs/pmy4s21dawvbomcpvyqr.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294301/cafestory/blogs/ndcbjix0fr33mckqf4lk.jpg');

    -- Blog #5 (slot 86, 74304 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Trầm Coffee & Tea đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '74304 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294303/cafestory/blogs/ltqzhcwkdbh0ltphd9jx.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294305/cafestory/blogs/h5d1vnzqwbdbqmbbrgjl.jpg');

    -- === Cafe #08: Cà phê Nhà Phạm ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Cà phê Nhà Phạm';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Cà phê Nhà Phạm';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 7, 6048 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly espresso đầu ngày tại Cà phê Nhà Phạm — vị đắng vừa, hậu ngọt, đúng chuẩn coffee thuần Việt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '6048 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294307/cafestory/blogs/vlmqm701fp6ccdr42xyb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294309/cafestory/blogs/fb8q9s8eq0ljrhfrptql.jpg');

    -- Blog #2 (slot 27, 23328 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte tại Cà phê Nhà Phạm không chỉ ngon mà còn đẹp mắt — điểm cộng lớn cho trải nghiệm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '23328 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294311/cafestory/blogs/wjjgo5arscmvjbcqjpls.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294313/cafestory/blogs/s4kgyiilw2gdazx5i6oo.jpg');

    -- Blog #3 (slot 47, 40608 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly cappuccino Cà phê Nhà Phạm vừa đủ ngọt, foam đứng, hương cafe không bị át bởi sữa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '40608 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294315/cafestory/blogs/ctqwkuogqgcbca3caulu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294317/cafestory/blogs/cp6xc211sqanyylknavb.jpg');

    -- Blog #4 (slot 67, 57888 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ngày làm việc remote ở Cà phê Nhà Phạm rất năng suất — mọi thứ đều setup sẵn cho dân văn phòng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '57888 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294319/cafestory/blogs/xypcc7n0fplimyww9n3g.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294321/cafestory/blogs/jdrijluan1jvtr4uanw9.jpg');

    -- Blog #5 (slot 87, 75168 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Cà phê Nhà Phạm đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '75168 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294323/cafestory/blogs/kvwz5ad5ye5l3vocpllv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294325/cafestory/blogs/vfqae5x7q4skznt72oqp.jpg');

    -- === Cafe #09: Nhà Phạm trong rừng ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Nhà Phạm trong rừng';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Nhà Phạm trong rừng';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 8, 6912 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly espresso đầu ngày tại Nhà Phạm trong rừng — vị đắng vừa, hậu ngọt, đúng chuẩn coffee thuần Việt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '6912 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294327/cafestory/blogs/sc0ihxxm7ujftjnambza.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294329/cafestory/blogs/l5rd7zx6f1dfmksemfyp.jpg');

    -- Blog #2 (slot 28, 24192 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte tại Nhà Phạm trong rừng không chỉ ngon mà còn đẹp mắt — điểm cộng lớn cho trải nghiệm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '24192 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294331/cafestory/blogs/gnmohskk11il1ktohdew.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294333/cafestory/blogs/nguqjiux2pm7u0w0qihx.jpg');

    -- Blog #3 (slot 48, 41472 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly cappuccino Nhà Phạm trong rừng vừa đủ ngọt, foam đứng, hương cafe không bị át bởi sữa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '41472 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294335/cafestory/blogs/a4kkcd1ix2mbf58ocktt.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294337/cafestory/blogs/qgudjw8ej27quodnjpn6.jpg');

    -- Blog #4 (slot 68, 58752 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ngày làm việc remote ở Nhà Phạm trong rừng rất năng suất — mọi thứ đều setup sẵn cho dân văn phòng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '58752 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294339/cafestory/blogs/lpiqw0yikdgi7srij1ip.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294341/cafestory/blogs/hgvn2wnhtkbbcm9yaylb.jpg');

    -- Blog #5 (slot 88, 76032 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Nhà Phạm trong rừng đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '76032 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294343/cafestory/blogs/zaesca3lwhiuzywijtio.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294345/cafestory/blogs/hiizfrfirwugpcxpn6cy.jpg');

    -- === Cafe #10: Nhà Phạm bên cầu ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Nhà Phạm bên cầu';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Nhà Phạm bên cầu';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 9, 7776 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi sáng ở Nhà Phạm bên cầu: espresso nguyên chất, thơm nồng, chất lượng ổn định qua từng ly.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '7776 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294347/cafestory/blogs/kdoa1agy0saqxflqe0z4.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294350/cafestory/blogs/pn59ofh77hzf0weqxfji.jpg');

    -- Blog #2 (slot 29, 25056 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly latte hôm nay ở Nhà Phạm bên cầu: art đẹp, milk vừa vặn, hương vị hoàn hảo cho buổi chiều.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '25056 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294352/cafestory/blogs/wxt9tx5f2hjxkioh2ynf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294355/cafestory/blogs/lsonnkllpa4lst4fllsn.jpg');

    -- Blog #3 (slot 49, 42336 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Nhà Phạm bên cầu pha cappuccino chuẩn ratio, uống xong không thấy ngấy, dư âm dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '42336 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294357/cafestory/blogs/ijqgxpoz26oifk3zdntj.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294359/cafestory/blogs/p1yafiiqhqliyhyhaten.jpg');

    -- Blog #4 (slot 69, 59616 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Nhà Phạm bên cầu thành base để chạy dự án cuối kỳ của mình — vibes làm việc rất tốt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '59616 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294362/cafestory/blogs/cebiofjxaejlom2msqed.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294363/cafestory/blogs/zke9tt6q1kbzqwmzw0wo.jpg');

    -- Blog #5 (slot 89, 76896 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Nhà Phạm bên cầu đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '76896 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294365/cafestory/blogs/a3um4uezbplbe136slou.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294368/cafestory/blogs/pgcalcvner5pgc9d1ubs.jpg');

    -- === Cafe #11: Là Cafe ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Là Cafe';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Là Cafe';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 10, 8640 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Là Cafe không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '8640 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294369/cafestory/blogs/bibtahu3szzdhjatz9em.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294372/cafestory/blogs/ymqh8uubvglazpgwywf3.jpg');

    -- Blog #2 (slot 30, 25920 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Là Cafe — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '25920 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294373/cafestory/blogs/aph4i15isafpfmsde30k.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294376/cafestory/blogs/jew310eguclmczgkzhqh.jpg');

    -- Blog #3 (slot 50, 43200 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Là Cafe — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '43200 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294378/cafestory/blogs/vhrvw7quntifvmlnq3s3.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294380/cafestory/blogs/qecbk31by1ht8c1qw6bz.jpg');

    -- Blog #4 (slot 70, 60480 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Là Cafe cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '60480 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294381/cafestory/blogs/t1wdt4ez3yk4harqjkgi.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294383/cafestory/blogs/q40jfjjh4uiq1etxfdmj.jpg');

    -- Blog #5 (slot 90, 77760 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Là Cafe đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '77760 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294385/cafestory/blogs/vhj2lgzaewrld2r7v9q1.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294387/cafestory/blogs/c6cnxqh6ty6cgvwmkymr.jpg');

    -- === Cafe #12: Raw Coffee ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Raw Coffee';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Raw Coffee';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 11, 9504 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Raw Coffee không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '9504 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294389/cafestory/blogs/rflchae5tuhjubmzvbkg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294391/cafestory/blogs/omks6awvcc9c0u5ndnnj.jpg');

    -- Blog #2 (slot 31, 26784 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Raw Coffee — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '26784 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294393/cafestory/blogs/bu6eowkgy43xv8p3s5aa.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294395/cafestory/blogs/yf9uh173bbt3qbl3dgl5.jpg');

    -- Blog #3 (slot 51, 44064 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Raw Coffee — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '44064 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294397/cafestory/blogs/og8wpyulqjw11gdnl4ui.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294399/cafestory/blogs/ry2buwtwpletfufe3hsm.jpg');

    -- Blog #4 (slot 71, 61344 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Raw Coffee cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '61344 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294401/cafestory/blogs/ok33lftdtgcixlxxugcp.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294403/cafestory/blogs/n4alvvxqxw3cehdfhnne.jpg');

    -- Blog #5 (slot 91, 78624 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Raw Coffee đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '78624 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294405/cafestory/blogs/fbdulvdbbnwtbgddrez4.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294408/cafestory/blogs/lxmiwbb8ohbeqcr31oob.jpg');

    -- === Cafe #13: Tiệm Trà Cỏ Ngọt ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Tiệm Trà Cỏ Ngọt';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Tiệm Trà Cỏ Ngọt';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 12, 10368 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi sáng ở Tiệm Trà Cỏ Ngọt: espresso nguyên chất, thơm nồng, chất lượng ổn định qua từng ly.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '10368 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294409/cafestory/blogs/i2dod78zrk2iyqssb8xh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294411/cafestory/blogs/nyfwsgx7oiyanvy7ihin.jpg');

    -- Blog #2 (slot 32, 27648 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly latte hôm nay ở Tiệm Trà Cỏ Ngọt: art đẹp, milk vừa vặn, hương vị hoàn hảo cho buổi chiều.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '27648 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294414/cafestory/blogs/ssvtrubtes4pqpcwjbsp.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294417/cafestory/blogs/psr3p1h3bfg4blf0cmoh.jpg');

    -- Blog #3 (slot 52, 44928 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Tiệm Trà Cỏ Ngọt pha cappuccino chuẩn ratio, uống xong không thấy ngấy, dư âm dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '44928 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294419/cafestory/blogs/rmeyhjoaq1igij9vqpza.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294421/cafestory/blogs/kbvclxteogzbpvtbtwzl.jpg');

    -- Blog #4 (slot 72, 62208 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Tiệm Trà Cỏ Ngọt thành base để chạy dự án cuối kỳ của mình — vibes làm việc rất tốt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '62208 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294423/cafestory/blogs/t5kdke5e5ils3cslcksk.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294424/cafestory/blogs/tthz03kovhwvsskgqdra.jpg');

    -- Blog #5 (slot 92, 79488 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Tiệm Trà Cỏ Ngọt đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '79488 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294427/cafestory/blogs/f8sgwslyc0mecx7yec5z.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294429/cafestory/blogs/boarjng8rdllvpxk5ynu.jpg');

    -- === Cafe #14: Time Cafe ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Time Cafe';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Time Cafe';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 13, 11232 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ghé Time Cafe sáng nay uống espresso, thấy tinh thần tỉnh táo hẳn để chinh phục cả tuần.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '11232 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294430/cafestory/blogs/xtfqefevq1v8gefdbfui.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294432/cafestory/blogs/zgfpp3wdch45brx2jhje.jpg');

    -- Blog #2 (slot 33, 28512 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Barista Time Cafe vẽ hình lá trên latte cực khéo — chưa uống đã thấy được chăm sóc.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '28512 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294434/cafestory/blogs/bjnhp45ct6wrgem4stq5.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294436/cafestory/blogs/dv09siodghjxq47yzsft.jpg');

    -- Blog #3 (slot 53, 45792 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Cappuccino Time Cafe là món ruột của mình mỗi khi ghé — chưa lần nào thất vọng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '45792 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294438/cafestory/blogs/dhwueitlojxjvfbpbymr.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294440/cafestory/blogs/ktolysm9fojlpt9k6psi.jpg');

    -- Blog #4 (slot 73, 63072 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Học nhóm ở Time Cafe — bàn rộng, chỗ đủ cho 4 người, đồ uống giá sinh viên.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '63072 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294443/cafestory/blogs/h5uk8vceui2per2dejms.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294445/cafestory/blogs/f0jmsomrf8o95x2tlsf9.jpg');

    -- Blog #5 (slot 93, 80352 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Time Cafe đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '80352 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294447/cafestory/blogs/uj5yuppv9mhdrala6gk4.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294450/cafestory/blogs/lc2c1kiayjewwzckw5bf.jpg');

    -- === Cafe #15: Sky Bar Iris ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Sky Bar Iris';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Sky Bar Iris';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 14, 12096 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Sky Bar Iris không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '12096 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294452/cafestory/blogs/p63f6kiwcvcsrqnd7csg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294454/cafestory/blogs/cyxtbdac7smjzvbzlwzl.jpg');

    -- Blog #2 (slot 34, 29376 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Sky Bar Iris — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '29376 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294455/cafestory/blogs/ez7qnedymw37qh5hubqg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294458/cafestory/blogs/yegl0lzvufbilompossh.jpg');

    -- Blog #3 (slot 54, 46656 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Sky Bar Iris — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '46656 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294460/cafestory/blogs/dfkaqrsusdmmmpphr7qu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294462/cafestory/blogs/rao7xgaqbjtnyoar8bij.jpg');

    -- Blog #4 (slot 74, 63936 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Sky Bar Iris cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '63936 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294463/cafestory/blogs/jlvhpe3dosanxp2cx1bp.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294465/cafestory/blogs/cafhiwgbu5rlr6pp8xte.jpg');

    -- Blog #5 (slot 94, 81216 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Sky Bar Iris đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '81216 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294468/cafestory/blogs/vaefcqqjnzpdwlhapwgg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294470/cafestory/blogs/vbrxuahjqojypjfmwdax.jpg');

    -- === Cafe #16: Aurora Coffee ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Aurora Coffee';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Aurora Coffee';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 15, 12960 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Aurora Coffee không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '12960 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294472/cafestory/blogs/p7z1riffdfvds0gedxfe.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294474/cafestory/blogs/ahexwqlk9fvrzwtoqkfn.jpg');

    -- Blog #2 (slot 35, 30240 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Aurora Coffee — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '30240 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294476/cafestory/blogs/ikx1okbdoxuzik916pgc.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294479/cafestory/blogs/kvbxuwjxign6fi3yjp1p.jpg');

    -- Blog #3 (slot 55, 47520 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Aurora Coffee — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '47520 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294481/cafestory/blogs/osv121zyqa2tihdwawtn.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294483/cafestory/blogs/w2ckkz1wsyufzn5rthad.jpg');

    -- Blog #4 (slot 75, 64800 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Aurora Coffee cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '64800 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294485/cafestory/blogs/veoei5yegeywh8hfos2j.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294488/cafestory/blogs/wowlgjjubzw89ogvomub.jpg');

    -- Blog #5 (slot 95, 82080 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Aurora Coffee đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '82080 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294490/cafestory/blogs/adygd88sfpgzld3afqwh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294493/cafestory/blogs/enp8tptbxoywmi2inrro.jpg');

    -- === Cafe #17: HiHi Onigiri ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'HiHi Onigiri';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: HiHi Onigiri';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 16, 13824 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly espresso đầu ngày tại HiHi Onigiri — vị đắng vừa, hậu ngọt, đúng chuẩn coffee thuần Việt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '13824 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294495/cafestory/blogs/pm29lbgoq7znldwamqte.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294497/cafestory/blogs/oqo4lfdvxwtgaudoacmb.jpg');

    -- Blog #2 (slot 36, 31104 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte tại HiHi Onigiri không chỉ ngon mà còn đẹp mắt — điểm cộng lớn cho trải nghiệm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '31104 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294500/cafestory/blogs/opqwyssghcekxaeutdrc.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294502/cafestory/blogs/d4zwlallhkew0lcmf3rb.jpg');

    -- Blog #3 (slot 56, 48384 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly cappuccino HiHi Onigiri vừa đủ ngọt, foam đứng, hương cafe không bị át bởi sữa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '48384 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294504/cafestory/blogs/sphqivjjpxjo9ewdklhu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294506/cafestory/blogs/puwxzhyf8djb6cu64ryu.jpg');

    -- Blog #4 (slot 76, 65664 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ngày làm việc remote ở HiHi Onigiri rất năng suất — mọi thứ đều setup sẵn cho dân văn phòng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '65664 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294508/cafestory/blogs/cf5fifxsifwku82tqxib.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294511/cafestory/blogs/lgkbhxezdnj6g1ewbklo.jpg');

    -- Blog #5 (slot 96, 82944 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở HiHi Onigiri đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '82944 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294512/cafestory/blogs/pocpd1xxjdknuqilzc1v.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294515/cafestory/blogs/ldjr2teoyfdzhbfkjms9.jpg');

    -- === Cafe #18: Mật Ngọt Coffee ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Mật Ngọt Coffee';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Mật Ngọt Coffee';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 17, 14688 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi sáng ở Mật Ngọt Coffee: espresso nguyên chất, thơm nồng, chất lượng ổn định qua từng ly.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '14688 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294517/cafestory/blogs/pcv0ukg0hd6r86cyscle.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294518/cafestory/blogs/s0l44cshjhanplun8wvb.jpg');

    -- Blog #2 (slot 37, 31968 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly latte hôm nay ở Mật Ngọt Coffee: art đẹp, milk vừa vặn, hương vị hoàn hảo cho buổi chiều.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '31968 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294520/cafestory/blogs/dacfz2rw7kfn3wtmu1jg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294522/cafestory/blogs/kfzmbi0zncdon5sktvzt.jpg');

    -- Blog #3 (slot 57, 49248 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Mật Ngọt Coffee pha cappuccino chuẩn ratio, uống xong không thấy ngấy, dư âm dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '49248 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294524/cafestory/blogs/aziwjhty8vmvgvr0avc5.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294526/cafestory/blogs/pm4zra5npx34dkhaf4im.jpg');

    -- Blog #4 (slot 77, 66528 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Mật Ngọt Coffee thành base để chạy dự án cuối kỳ của mình — vibes làm việc rất tốt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '66528 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294528/cafestory/blogs/mc1llcaqp66qg3xam2ej.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294530/cafestory/blogs/x9ndvntfo3tzpcjdowix.jpg');

    -- Blog #5 (slot 97, 83808 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Mật Ngọt Coffee đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '83808 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294531/cafestory/blogs/xmq8ksotaqbqcaxxcirl.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294534/cafestory/blogs/zqats66aees8cxfippss.jpg');

    -- === Cafe #19: Chịn Cà Phê ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Chịn Cà Phê';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Chịn Cà Phê';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 18, 15552 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly espresso đầu ngày tại Chịn Cà Phê — vị đắng vừa, hậu ngọt, đúng chuẩn coffee thuần Việt.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '15552 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294536/cafestory/blogs/defmq81qmcshcnypj0by.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294538/cafestory/blogs/tdhxnsbykuijgvyodoct.jpg');

    -- Blog #2 (slot 38, 32832 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Latte tại Chịn Cà Phê không chỉ ngon mà còn đẹp mắt — điểm cộng lớn cho trải nghiệm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '32832 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294540/cafestory/blogs/v1bxjt7iggpdxt0zx78h.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294543/cafestory/blogs/x1frbeemq6z4r6mxmhkh.jpg');

    -- Blog #3 (slot 58, 50112 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ly cappuccino Chịn Cà Phê vừa đủ ngọt, foam đứng, hương cafe không bị át bởi sữa.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '50112 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294545/cafestory/blogs/tfdnqa5jdx6sy0zxl7cs.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294547/cafestory/blogs/kvdorldstrerurbabmx2.jpg');

    -- Blog #4 (slot 78, 67392 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Ngày làm việc remote ở Chịn Cà Phê rất năng suất — mọi thứ đều setup sẵn cho dân văn phòng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '67392 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294549/cafestory/blogs/gb31bzi6fzg9ctbrnwzc.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294551/cafestory/blogs/gdfqiekocdxyzi0frzqx.jpg');

    -- Blog #5 (slot 98, 84672 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Chịn Cà Phê đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '84672 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294553/cafestory/blogs/tiia3thhgfcgcqkkulrf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294555/cafestory/blogs/ik7gsxmyzzvcarxgzt8r.jpg');

    -- === Cafe #20: Tiệm Cà Phê Nhà Có Khách ===
    SELECT cp.id, cp.owner_user_id, cp.region_id
    INTO v_cafe_id, v_owner_id, v_region_id
    FROM cafe_pages cp WHERE cp.name = 'Tiệm Cà Phê Nhà Có Khách';
    IF v_cafe_id IS NULL THEN
        RAISE EXCEPTION 'Cafe not found: Tiệm Cà Phê Nhà Có Khách';
    END IF;
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;
    END IF;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;
    END IF;

    -- Blog #1 (slot 19, 16416 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Espresso ở Tiệm Cà Phê Nhà Có Khách không quá đắng, không quá gắt — vừa vặn cho một buổi sáng dễ chịu.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '16416 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294557/cafestory/blogs/dmqrdqhcslv4bojbtgkx.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294559/cafestory/blogs/zmyu0f5asilieltd3wjy.jpg');

    -- Blog #2 (slot 39, 33696 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Rất mê latte art của Tiệm Cà Phê Nhà Có Khách — cứ đến là order thêm ly nữa chỉ để chụp ảnh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '33696 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294561/cafestory/blogs/qjbdjjawhw08sbhbzkkt.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294563/cafestory/blogs/ior2ixa2aeorhqrtbmkz.jpg');

    -- Blog #3 (slot 59, 50976 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Buổi chiều nhẹ nhàng với cappuccino tại Tiệm Cà Phê Nhà Có Khách — foam đẹp, vị cân bằng.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '50976 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294566/cafestory/blogs/nxazk0kp0whewcoy6nnd.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294568/cafestory/blogs/wotuhclmwsdslc3qcnko.jpg');

    -- Blog #4 (slot 79, 68256 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Deadline dồn dập, may có Tiệm Cà Phê Nhà Có Khách cho ngồi cả buổi mà không bị thúc dọn dẹp.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '68256 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294569/cafestory/blogs/i0qybgowfrbdt2n1akue.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294571/cafestory/blogs/yhpz2t6p9t9ptntqeb2m.jpg');

    -- Blog #5 (slot 99, 85536 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,
            'Không khí vintage ở Tiệm Cà Phê Nhà Có Khách đưa mình về thời sinh viên — decor gỗ, đèn vàng ấm.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '85536 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294574/cafestory/blogs/igzqfwylptyycspl5t6e.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784294575/cafestory/blogs/p42ounte1h9b7uhbumri.jpg');

END $$;

COMMIT;

-- Seed 50 blog cá nhân cho 20 reviewer Đà Nẵng.
--   10 reviewer × 2 blog + 10 reviewer × 3 blog = 50 blog.
--   100 blog_images (2 ảnh/blog) Pexels themes barista / quán nước / thức uống tại quán,
--   distinct photo_id trong batch (dedup khi download).
-- Follow rule BE:
--   * author_user_id = reviewer user
--   * page_id = NULL (blog cá nhân → hiện ở user profile)
--   * region_id = user.region_id (BE require)
--   * content dùng tên thật reviewer (query user_full_name runtime)
-- Reviewer pool: cùng logic seed_20_reviewers_danang — province_code='48',
-- ORDER BY md5(user_id) LIMIT 20.
-- created_at rải interleaved 60 ngày qua (1728 min/slot).

BEGIN;

DO $$
DECLARE
    v_reviewer_ids     uuid[];
    v_user_id          uuid;
    v_region_id        uuid;
    v_blog_id          uuid;
    v_user_full_name   text;
BEGIN
    -- Pick 20 reviewer user_ids Đà Nẵng — cùng thứ tự với seed reviewer step
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_reviewer_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '48'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        LIMIT 20
    ) sub;
    IF v_reviewer_ids IS NULL OR array_length(v_reviewer_ids, 1) < 20 THEN
        RAISE EXCEPTION 'Chỉ pick được %/20 reviewer Đà Nẵng, cần đủ 20.',
                        coalesce(array_length(v_reviewer_ids, 1), 0);
    END IF;

    -- === Reviewer Đà Nẵng #01 (2 blog) ===
    v_user_id := v_reviewer_ids[1];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 0, 0 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '0 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346623/cafestory/reviewer_blogs/vgc2fyc1n2j1wbccqa7r.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346624/cafestory/reviewer_blogs/z0eab3zxdjj4pjbp2cvq.jpg');

    -- Blog #2 (slot 1, 1728 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '1728 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346626/cafestory/reviewer_blogs/zuut01oxqvtg3udzsztb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346628/cafestory/reviewer_blogs/medot4qqmysurxkx1wyd.jpg');

    -- === Reviewer Đà Nẵng #02 (2 blog) ===
    v_user_id := v_reviewer_ids[2];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 2, 3456 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '3456 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346631/cafestory/reviewer_blogs/bnyo3v8ffjp5lldvgx1h.png'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346633/cafestory/reviewer_blogs/phumgasfayrpht3xllms.jpg');

    -- Blog #2 (slot 3, 5184 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '5184 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346634/cafestory/reviewer_blogs/vt3x8ha07pyxlio99sry.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346636/cafestory/reviewer_blogs/se6hwksdsln2tnnbhosv.jpg');

    -- === Reviewer Đà Nẵng #03 (2 blog) ===
    v_user_id := v_reviewer_ids[3];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 4, 6912 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '6912 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346638/cafestory/reviewer_blogs/ls083a6vh6ztlryqezhk.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346640/cafestory/reviewer_blogs/tikhpxhkkqwbcxnzgkct.jpg');

    -- Blog #2 (slot 5, 8640 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '8640 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346642/cafestory/reviewer_blogs/wiajtpjjsyk3m2emzthl.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346644/cafestory/reviewer_blogs/jj9rzme4n5lxunt2p8wn.jpg');

    -- === Reviewer Đà Nẵng #04 (2 blog) ===
    v_user_id := v_reviewer_ids[4];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 6, 10368 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '10368 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346645/cafestory/reviewer_blogs/emnzjqaznbgsa9rtljbf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346647/cafestory/reviewer_blogs/ex9zsrqqekaswupdyi87.jpg');

    -- Blog #2 (slot 7, 12096 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '12096 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346648/cafestory/reviewer_blogs/ejf0ng1ushj7ysbfl0lh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346650/cafestory/reviewer_blogs/lkk5sws8fquwxum630ut.jpg');

    -- === Reviewer Đà Nẵng #05 (2 blog) ===
    v_user_id := v_reviewer_ids[5];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 8, 13824 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '13824 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346652/cafestory/reviewer_blogs/zgspypav3wc01qtv6v4f.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346654/cafestory/reviewer_blogs/hpbc2vcss5uknocu1rdi.jpg');

    -- Blog #2 (slot 9, 15552 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '15552 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346655/cafestory/reviewer_blogs/msdw6e7zmlycjslniglb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346657/cafestory/reviewer_blogs/wji9jfksvt4slmjfji7y.jpg');

    -- === Reviewer Đà Nẵng #06 (2 blog) ===
    v_user_id := v_reviewer_ids[6];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 10, 17280 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '17280 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346659/cafestory/reviewer_blogs/i3gehpiux807yoz66md7.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346661/cafestory/reviewer_blogs/t208i1bdegkmvwhlhlyx.jpg');

    -- Blog #2 (slot 11, 19008 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '19008 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346663/cafestory/reviewer_blogs/bc5mqeje8hsxgswtm8v4.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346665/cafestory/reviewer_blogs/sgw7cjozdlhyfhpykaud.jpg');

    -- === Reviewer Đà Nẵng #07 (2 blog) ===
    v_user_id := v_reviewer_ids[7];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 12, 20736 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '20736 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346667/cafestory/reviewer_blogs/aiiesucbbeukhgzgjkr2.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346669/cafestory/reviewer_blogs/lfiz2emctqgdbwjjuhqv.jpg');

    -- Blog #2 (slot 13, 22464 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '22464 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346671/cafestory/reviewer_blogs/yc2uwgfu7swwd0hzb9p1.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346673/cafestory/reviewer_blogs/auhbmi8wuvwlctzfedqj.jpg');

    -- === Reviewer Đà Nẵng #08 (2 blog) ===
    v_user_id := v_reviewer_ids[8];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 14, 24192 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '24192 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346675/cafestory/reviewer_blogs/jadesymj2am0hwrkkydn.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346677/cafestory/reviewer_blogs/ssmnv9pk25betinxcm6i.jpg');

    -- Blog #2 (slot 15, 25920 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '25920 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346679/cafestory/reviewer_blogs/i4zjkta7expsvalhprdh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346681/cafestory/reviewer_blogs/nzniiqaqc1msrrnwoylx.jpg');

    -- === Reviewer Đà Nẵng #09 (2 blog) ===
    v_user_id := v_reviewer_ids[9];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 16, 27648 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '27648 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346683/cafestory/reviewer_blogs/fgfwbneuwiuftmlx9r3d.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346685/cafestory/reviewer_blogs/i1b48uxamwfibxwe7f1y.jpg');

    -- Blog #2 (slot 17, 29376 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '29376 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346686/cafestory/reviewer_blogs/a1ogzbde5s1kwcjcwayh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346688/cafestory/reviewer_blogs/bsvykmisd0agdoritdls.jpg');

    -- === Reviewer Đà Nẵng #10 (2 blog) ===
    v_user_id := v_reviewer_ids[10];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 18, 31104 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '31104 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346691/cafestory/reviewer_blogs/g3ens6g1umkajbeeg69z.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346693/cafestory/reviewer_blogs/y9rqq5vyk5sbbxdi9nj5.jpg');

    -- Blog #2 (slot 19, 32832 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '32832 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346695/cafestory/reviewer_blogs/gkak3k0verkbbppegmm9.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346697/cafestory/reviewer_blogs/norx00oazzabmxaxe5q4.jpg');

    -- === Reviewer Đà Nẵng #11 (3 blog) ===
    v_user_id := v_reviewer_ids[11];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 20, 34560 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '34560 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346698/cafestory/reviewer_blogs/paqaevjw1zlasr2qqvh3.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346700/cafestory/reviewer_blogs/bfc1joatvsbgtpctbiun.jpg');

    -- Blog #2 (slot 21, 36288 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '36288 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346702/cafestory/reviewer_blogs/wesxtzvzac72mc6i7gfl.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346704/cafestory/reviewer_blogs/ru5bsieh82d9y7clcipa.jpg');

    -- Blog #3 (slot 22, 38016 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '38016 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346705/cafestory/reviewer_blogs/au2oieb0cfzjsje3egyr.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346707/cafestory/reviewer_blogs/moc1aytya0iyuwecy0nt.jpg');

    -- === Reviewer Đà Nẵng #12 (3 blog) ===
    v_user_id := v_reviewer_ids[12];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 23, 39744 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '39744 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346709/cafestory/reviewer_blogs/w87aursed7o6ibmijc23.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346712/cafestory/reviewer_blogs/qs3rlfzpzfcv5tlh3lgg.jpg');

    -- Blog #2 (slot 24, 41472 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '41472 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346714/cafestory/reviewer_blogs/lyajlcvfkpghfo8nljfp.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346716/cafestory/reviewer_blogs/btr2fqqb3w5ryf5hzk2q.jpg');

    -- Blog #3 (slot 25, 43200 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '43200 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346717/cafestory/reviewer_blogs/gliona3egnm9wzw0vfto.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346719/cafestory/reviewer_blogs/y4ikbwn4gjxp1qtvigar.jpg');

    -- === Reviewer Đà Nẵng #13 (3 blog) ===
    v_user_id := v_reviewer_ids[13];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 26, 44928 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '44928 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346722/cafestory/reviewer_blogs/atnizjhkqolzvor8s6vy.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346724/cafestory/reviewer_blogs/rebcyqfemaq9ii5rfl61.jpg');

    -- Blog #2 (slot 27, 46656 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '46656 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346726/cafestory/reviewer_blogs/wteab8wcct3jf0apsfhb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346728/cafestory/reviewer_blogs/p5pnlqrhjvrw7iupbkkg.jpg');

    -- Blog #3 (slot 28, 48384 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '48384 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346730/cafestory/reviewer_blogs/idzhdme0i5kdntuyc42e.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346732/cafestory/reviewer_blogs/rueb49y9ernz8x371khi.jpg');

    -- === Reviewer Đà Nẵng #14 (3 blog) ===
    v_user_id := v_reviewer_ids[14];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 29, 50112 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '50112 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346735/cafestory/reviewer_blogs/mjqmzapafyvhbgwtlnxa.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346737/cafestory/reviewer_blogs/eedbtengb9cmwuvicyf4.jpg');

    -- Blog #2 (slot 30, 51840 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '51840 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346738/cafestory/reviewer_blogs/fxrbzo4vmrnclkykkw0w.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346740/cafestory/reviewer_blogs/grgvqgptgmgmn03tsci5.jpg');

    -- Blog #3 (slot 31, 53568 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '53568 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346742/cafestory/reviewer_blogs/a7kwvy9hq7nqif92brhv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346744/cafestory/reviewer_blogs/afg4o7hnqa1hfmfr8iw4.jpg');

    -- === Reviewer Đà Nẵng #15 (3 blog) ===
    v_user_id := v_reviewer_ids[15];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 32, 55296 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '55296 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346746/cafestory/reviewer_blogs/ovaiqfi5zmazvldryntl.png'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346748/cafestory/reviewer_blogs/fx6i75wdwwmtnsx97unx.jpg');

    -- Blog #2 (slot 33, 57024 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '57024 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346750/cafestory/reviewer_blogs/pzxwfgtbdwzzb9jfv9mw.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346752/cafestory/reviewer_blogs/fqxhsyodqnpiv6uoqvij.jpg');

    -- Blog #3 (slot 34, 58752 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '58752 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346754/cafestory/reviewer_blogs/oseqffdbab3icm1bzxnl.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346756/cafestory/reviewer_blogs/yamshbg2su6aerh5a24j.jpg');

    -- === Reviewer Đà Nẵng #16 (3 blog) ===
    v_user_id := v_reviewer_ids[16];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 35, 60480 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '60480 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346758/cafestory/reviewer_blogs/oilj6h2lua2aeypdgosf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346760/cafestory/reviewer_blogs/ithhtvwdnnqqtxodnoau.jpg');

    -- Blog #2 (slot 36, 62208 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '62208 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346762/cafestory/reviewer_blogs/bvuqznqy1hdo5s4ktlwu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346763/cafestory/reviewer_blogs/ot6atnkfyljxmuwje8a7.jpg');

    -- Blog #3 (slot 37, 63936 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '63936 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346765/cafestory/reviewer_blogs/ulagkt43jumvijfoiedv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346767/cafestory/reviewer_blogs/y33msrahai7bkqfdzrz3.jpg');

    -- === Reviewer Đà Nẵng #17 (3 blog) ===
    v_user_id := v_reviewer_ids[17];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 38, 65664 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '65664 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346768/cafestory/reviewer_blogs/noj81ywsztnogo3yud6a.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346770/cafestory/reviewer_blogs/dlzrbfq8o3w1fubon3mq.jpg');

    -- Blog #2 (slot 39, 67392 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '67392 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346772/cafestory/reviewer_blogs/cyhcri1xaascmegb6t6n.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346774/cafestory/reviewer_blogs/ps3n8hqp5grh9ekotsov.jpg');

    -- Blog #3 (slot 40, 69120 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '69120 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346776/cafestory/reviewer_blogs/mwpaozilxg24r3ualx17.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346778/cafestory/reviewer_blogs/jvkbioyueom6cvhux27q.jpg');

    -- === Reviewer Đà Nẵng #18 (3 blog) ===
    v_user_id := v_reviewer_ids[18];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 41, 70848 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '70848 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346780/cafestory/reviewer_blogs/x2retslfexm7vvaup8bt.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346782/cafestory/reviewer_blogs/zuegvopmug1uaew6dt9z.jpg');

    -- Blog #2 (slot 42, 72576 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '72576 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346784/cafestory/reviewer_blogs/hq4u7cmxfxwlvhkbovak.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346786/cafestory/reviewer_blogs/fz4bgvtqxecat5i2mrwq.jpg');

    -- Blog #3 (slot 43, 74304 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '74304 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346788/cafestory/reviewer_blogs/calggtjiryf0ecj25oip.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346790/cafestory/reviewer_blogs/tjqui34bdlkllwfsu94y.jpg');

    -- === Reviewer Đà Nẵng #19 (3 blog) ===
    v_user_id := v_reviewer_ids[19];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 44, 76032 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '76032 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346792/cafestory/reviewer_blogs/w3yrgfgnbvta1ob3uj5b.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346793/cafestory/reviewer_blogs/bsm8bcqlxunnxedmkori.jpg');

    -- Blog #2 (slot 45, 77760 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '77760 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346795/cafestory/reviewer_blogs/ln0h9fkghb4opxx5un7f.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346797/cafestory/reviewer_blogs/dyvbxbwtposwtukzbjeu.jpg');

    -- Blog #3 (slot 46, 79488 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '79488 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346800/cafestory/reviewer_blogs/bn419jdjzbkusifoqdys.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346802/cafestory/reviewer_blogs/xyfa6dsabvelquuyg4js.jpg');

    -- === Reviewer Đà Nẵng #20 (3 blog) ===
    v_user_id := v_reviewer_ids[20];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 47, 81216 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '81216 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346803/cafestory/reviewer_blogs/mdfbfaaczhyqdxduxodz.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346806/cafestory/reviewer_blogs/mbnjlo9pw00hh0jp2sta.jpg');

    -- Blog #2 (slot 48, 82944 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '82944 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346808/cafestory/reviewer_blogs/lhuo7e5bfaamzjox6oai.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346810/cafestory/reviewer_blogs/i6rmmalfpgtrj1ti2h9y.jpg');

    -- Blog #3 (slot 49, 84672 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '84672 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346812/cafestory/reviewer_blogs/qcwkixxg9zahjee1fksh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346814/cafestory/reviewer_blogs/bupm6hhbzoatx94tyeqq.jpg');

END $$;

COMMIT;

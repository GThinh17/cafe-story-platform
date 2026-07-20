-- Seed 100 blog cá nhân cho 40 reviewer Cần Thơ.
--   20 reviewer × 2 blog + 20 reviewer × 3 blog = 100 blog.
--   200 blog_images (2 ảnh/blog) từ Cloudinary folder cafestory/reviewer_blogs.
-- Follow rule BE:
--   * author_user_id = reviewer user
--   * page_id = NULL (blog cá nhân → hiện ở user profile, KHÔNG hiện ở cafe page)
--   * region_id = user.region_id (BE require)
--   * content dùng tên thật của reviewer (query user_full_name runtime)
-- Reviewer pool: dùng lại logic seed reviewer (ORDER BY md5 OFFSET 20 LIMIT 40).
-- created_at rải interleaved 60 ngày qua (864 min/slot).

BEGIN;

DO $$
DECLARE
    v_reviewer_ids     uuid[];
    v_user_id          uuid;
    v_region_id        uuid;
    v_blog_id          uuid;
    v_user_full_name   text;
BEGIN
    -- Pick 40 reviewer user_ids — cùng thứ tự với migration seed_40_reviewers
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_reviewer_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '92'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        OFFSET 20 LIMIT 40
    ) sub;
    IF v_reviewer_ids IS NULL OR array_length(v_reviewer_ids, 1) < 40 THEN
        RAISE EXCEPTION 'Chỉ pick được %/40 reviewer, cần đủ 40.',
                        coalesce(array_length(v_reviewer_ids, 1), 0);
    END IF;

    -- === Reviewer #01 (2 blog) ===
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
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '0 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297285/cafestory/reviewer_blogs/ox9iw50czhtuqarq2kpv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297287/cafestory/reviewer_blogs/mxt4ohej35u2di0jjyje.jpg');

    -- Blog #2 (slot 1, 864 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '864 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297290/cafestory/reviewer_blogs/bibkwr7jdqhy6x7fftcf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297292/cafestory/reviewer_blogs/fopjfa0qg0bxemlburvt.jpg');

    -- === Reviewer #02 (2 blog) ===
    v_user_id := v_reviewer_ids[2];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 2, 1728 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '1728 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297293/cafestory/reviewer_blogs/ipzlzex83uw8dn1hdejb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297295/cafestory/reviewer_blogs/wcaoy7eneoicnr2js0lj.jpg');

    -- Blog #2 (slot 3, 2592 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '2592 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297297/cafestory/reviewer_blogs/ea32llbogyxjjp9p7iy8.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297299/cafestory/reviewer_blogs/l0ziajsr2fh47trroq5h.jpg');

    -- === Reviewer #03 (2 blog) ===
    v_user_id := v_reviewer_ids[3];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 4, 3456 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '3456 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297301/cafestory/reviewer_blogs/swc8qt0xksktho2b6jle.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297303/cafestory/reviewer_blogs/cinyboru7eufmfkcxfsg.jpg');

    -- Blog #2 (slot 5, 4320 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '4320 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297306/cafestory/reviewer_blogs/u7nzk2neupleuwsehhyk.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297308/cafestory/reviewer_blogs/tvzfvw3wumqjebqmdfdw.jpg');

    -- === Reviewer #04 (2 blog) ===
    v_user_id := v_reviewer_ids[4];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 6, 5184 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '5184 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297310/cafestory/reviewer_blogs/qceoquz6wvjs96jfzyuh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297312/cafestory/reviewer_blogs/ihjqjegtnf8re4bk5bxs.jpg');

    -- Blog #2 (slot 7, 6048 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '6048 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297314/cafestory/reviewer_blogs/rel8wulctvqnybv1pnu0.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297316/cafestory/reviewer_blogs/zse5u4scrkgt9vtkjfdw.jpg');

    -- === Reviewer #05 (2 blog) ===
    v_user_id := v_reviewer_ids[5];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 8, 6912 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '6912 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297318/cafestory/reviewer_blogs/cx1etnubp53lbwlbjypi.png'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297320/cafestory/reviewer_blogs/i39djjya3wskxzqd0x4l.jpg');

    -- Blog #2 (slot 9, 7776 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '7776 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297321/cafestory/reviewer_blogs/pbmxdgr8zowcbynm1eay.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297323/cafestory/reviewer_blogs/lslwrrwd7xdfeuwhq0oa.jpg');

    -- === Reviewer #06 (2 blog) ===
    v_user_id := v_reviewer_ids[6];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 10, 8640 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '8640 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297325/cafestory/reviewer_blogs/ykqv4c20fjt5ntcue3qd.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297327/cafestory/reviewer_blogs/arh3arh6zxfjnaq1sqma.jpg');

    -- Blog #2 (slot 11, 9504 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '9504 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297329/cafestory/reviewer_blogs/ufcueou6hxmwoklcuvgg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297331/cafestory/reviewer_blogs/payqqqlpbewwdgrmjyf1.jpg');

    -- === Reviewer #07 (2 blog) ===
    v_user_id := v_reviewer_ids[7];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 12, 10368 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '10368 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297333/cafestory/reviewer_blogs/kikfirmtadyzbyau1yzi.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297335/cafestory/reviewer_blogs/vdnnsgb4dwvvacunlwvu.jpg');

    -- Blog #2 (slot 13, 11232 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '11232 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297337/cafestory/reviewer_blogs/vk1vjrofovpkrgd1o67m.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297339/cafestory/reviewer_blogs/maeambcyi0vccjsqlwcz.jpg');

    -- === Reviewer #08 (2 blog) ===
    v_user_id := v_reviewer_ids[8];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 14, 12096 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '12096 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297340/cafestory/reviewer_blogs/da6sdrnshzlian6rsncc.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297343/cafestory/reviewer_blogs/yvkidwxmnjqel4d0pzi9.png');

    -- Blog #2 (slot 15, 12960 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '12960 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297345/cafestory/reviewer_blogs/qgkatgidkuun9a2c1kjl.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297346/cafestory/reviewer_blogs/nm53j4umoo6ouyczws2o.jpg');

    -- === Reviewer #09 (2 blog) ===
    v_user_id := v_reviewer_ids[9];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 16, 13824 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '13824 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297348/cafestory/reviewer_blogs/gikivyfdzmabutjebral.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297350/cafestory/reviewer_blogs/rzqfso9ckeefwzdez5fj.jpg');

    -- Blog #2 (slot 17, 14688 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '14688 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297352/cafestory/reviewer_blogs/aquukfodhiz4nec2ilwo.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297354/cafestory/reviewer_blogs/ws2wirm6qh4gg2rqepmv.jpg');

    -- === Reviewer #10 (2 blog) ===
    v_user_id := v_reviewer_ids[10];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 18, 15552 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '15552 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297355/cafestory/reviewer_blogs/dlzv3gmlec35s8znbbd7.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297359/cafestory/reviewer_blogs/b2qdbioigjlmojiuvvu5.jpg');

    -- Blog #2 (slot 19, 16416 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '16416 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297360/cafestory/reviewer_blogs/sfabqetalibhsme3kiua.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297362/cafestory/reviewer_blogs/w13tlzjqdokntf5bd6sc.jpg');

    -- === Reviewer #11 (2 blog) ===
    v_user_id := v_reviewer_ids[11];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 20, 17280 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '17280 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297364/cafestory/reviewer_blogs/vqrixywecbrv6cbnpe1r.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297366/cafestory/reviewer_blogs/zpl0kl6rfneerrzw4wx4.jpg');

    -- Blog #2 (slot 21, 18144 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '18144 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297368/cafestory/reviewer_blogs/ibjzphyber996iu2zf9a.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297370/cafestory/reviewer_blogs/h7gt03aib3wgdb7wlusv.jpg');

    -- === Reviewer #12 (2 blog) ===
    v_user_id := v_reviewer_ids[12];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 22, 19008 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '19008 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297372/cafestory/reviewer_blogs/h6bz8veamkdgwcbadwsc.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297375/cafestory/reviewer_blogs/ue77ikgmwz8edrusljbg.jpg');

    -- Blog #2 (slot 23, 19872 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '19872 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297377/cafestory/reviewer_blogs/rto2zlqsjkouf2vz335b.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297379/cafestory/reviewer_blogs/crncp2cdzzuo1vlf8ylj.jpg');

    -- === Reviewer #13 (2 blog) ===
    v_user_id := v_reviewer_ids[13];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 24, 20736 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '20736 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297381/cafestory/reviewer_blogs/o44l8ac6gfqtzlpqpzmv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297383/cafestory/reviewer_blogs/ppzkgcnjj2s0tzmidfqm.jpg');

    -- Blog #2 (slot 25, 21600 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '21600 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297385/cafestory/reviewer_blogs/oohtrrzlgnvqytsfavgb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297386/cafestory/reviewer_blogs/f3qmojaaqhtbuxxp2zjp.jpg');

    -- === Reviewer #14 (2 blog) ===
    v_user_id := v_reviewer_ids[14];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 26, 22464 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '22464 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297388/cafestory/reviewer_blogs/g4ofgiekwzzpsffl4is7.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297390/cafestory/reviewer_blogs/hfsm9llgsljnolygzmjj.jpg');

    -- Blog #2 (slot 27, 23328 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '23328 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297392/cafestory/reviewer_blogs/oxxg3m9rsxkzhj8oboal.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297394/cafestory/reviewer_blogs/pckhw4fhfkmskuhavalo.jpg');

    -- === Reviewer #15 (2 blog) ===
    v_user_id := v_reviewer_ids[15];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 28, 24192 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '24192 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297396/cafestory/reviewer_blogs/kbmgpmb1dx9nqdzrdwhm.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297397/cafestory/reviewer_blogs/ziwu5jmk0u1v5xwhisqi.jpg');

    -- Blog #2 (slot 29, 25056 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '25056 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297399/cafestory/reviewer_blogs/s7fhretzkv09sgserp1v.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297401/cafestory/reviewer_blogs/vp0t7v4bgr4ys3y1ah6r.jpg');

    -- === Reviewer #16 (2 blog) ===
    v_user_id := v_reviewer_ids[16];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 30, 25920 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '25920 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297403/cafestory/reviewer_blogs/if716ypbhbhhppndigo6.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297405/cafestory/reviewer_blogs/f3mxfgqzdgwzjctrqsbd.jpg');

    -- Blog #2 (slot 31, 26784 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '26784 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297406/cafestory/reviewer_blogs/ihocx9dwdtsoz067pkyx.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297408/cafestory/reviewer_blogs/w5wookjqqa9e8u5kqkti.jpg');

    -- === Reviewer #17 (2 blog) ===
    v_user_id := v_reviewer_ids[17];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 32, 27648 min ago)
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
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297410/cafestory/reviewer_blogs/rqcnznqmrcnpvfovzhry.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297412/cafestory/reviewer_blogs/bneakb0cxoo6ah2h8zbm.jpg');

    -- Blog #2 (slot 33, 28512 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '28512 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297414/cafestory/reviewer_blogs/j7sxrkcpp3nxseterlnd.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297416/cafestory/reviewer_blogs/dt4izlgulc9y8emswo4g.jpg');

    -- === Reviewer #18 (2 blog) ===
    v_user_id := v_reviewer_ids[18];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 34, 29376 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '29376 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297418/cafestory/reviewer_blogs/i4d42oivfcgc6jl8gls6.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297420/cafestory/reviewer_blogs/tbrssnj7rtosnyv50qk6.jpg');

    -- Blog #2 (slot 35, 30240 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '30240 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297422/cafestory/reviewer_blogs/oeh0cnxejrcn2bvenvbs.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297424/cafestory/reviewer_blogs/cq0eleev3ym4safhn0ss.jpg');

    -- === Reviewer #19 (2 blog) ===
    v_user_id := v_reviewer_ids[19];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 36, 31104 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '31104 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297426/cafestory/reviewer_blogs/npgpvpspysra4dgkc5hg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297428/cafestory/reviewer_blogs/yi24uy1xghdsjwsvzfzf.jpg');

    -- Blog #2 (slot 37, 31968 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '31968 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297430/cafestory/reviewer_blogs/hvt4bzilmvg6s91lpzgf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297432/cafestory/reviewer_blogs/ztb4ir0o1ayh7yvboruq.jpg');

    -- === Reviewer #20 (2 blog) ===
    v_user_id := v_reviewer_ids[20];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 38, 32832 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '32832 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297434/cafestory/reviewer_blogs/m1h8qxnttwamotlmxsls.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297436/cafestory/reviewer_blogs/me61hozhptwkfadwnpvi.jpg');

    -- Blog #2 (slot 39, 33696 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '33696 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297439/cafestory/reviewer_blogs/xpdavwnzcnmftxyfnhbj.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297441/cafestory/reviewer_blogs/jqqaeuhfxs6o7dchnq6h.jpg');

    -- === Reviewer #21 (3 blog) ===
    v_user_id := v_reviewer_ids[21];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 40, 34560 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '34560 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297443/cafestory/reviewer_blogs/iat8cf9multp5j1prhga.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297445/cafestory/reviewer_blogs/f6mzrp8tvgm1pjildrpv.jpg');

    -- Blog #2 (slot 41, 35424 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '35424 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297446/cafestory/reviewer_blogs/els4ryd2ksbwiuyf6dne.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297448/cafestory/reviewer_blogs/sifugoc9ff0lgn2krt8i.jpg');

    -- Blog #3 (slot 42, 36288 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '36288 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297450/cafestory/reviewer_blogs/qkj6wrsn7zfb3sy3bp6a.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297452/cafestory/reviewer_blogs/lupuf0fmfxbhbddg30hn.jpg');

    -- === Reviewer #22 (3 blog) ===
    v_user_id := v_reviewer_ids[22];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 43, 37152 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '37152 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297454/cafestory/reviewer_blogs/cqs8ffetxbnsfpppiuhr.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297455/cafestory/reviewer_blogs/y7x9hqbpkmtxy0cn0bgp.jpg');

    -- Blog #2 (slot 44, 38016 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '38016 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297457/cafestory/reviewer_blogs/h9fk3zto7vncmmr1bfzm.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297459/cafestory/reviewer_blogs/vpirvrn5rzp0h4vdpa0g.jpg');

    -- Blog #3 (slot 45, 38880 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '38880 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297461/cafestory/reviewer_blogs/r0zrb6akna9ybkbnobyp.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297468/cafestory/reviewer_blogs/wuz5dvlyx6boja0ww4py.jpg');

    -- === Reviewer #23 (3 blog) ===
    v_user_id := v_reviewer_ids[23];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 46, 39744 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '39744 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297470/cafestory/reviewer_blogs/ghklc4ccvid4sy7stj6j.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297472/cafestory/reviewer_blogs/eqqruou3rkqokizqaknb.jpg');

    -- Blog #2 (slot 47, 40608 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '40608 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297474/cafestory/reviewer_blogs/o36kk8qxvmgou6evdmqv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297476/cafestory/reviewer_blogs/nxzbjlbg3vy4z3rd43xa.jpg');

    -- Blog #3 (slot 48, 41472 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '41472 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297478/cafestory/reviewer_blogs/b9l5qjod9h7xdhxrx53w.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297479/cafestory/reviewer_blogs/zrtjj7ggzi0xd3lifqjt.jpg');

    -- === Reviewer #24 (3 blog) ===
    v_user_id := v_reviewer_ids[24];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 49, 42336 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '42336 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297482/cafestory/reviewer_blogs/zhqeoamoia3rfetauoic.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297484/cafestory/reviewer_blogs/pi4qvc7blpmgg7ugalak.jpg');

    -- Blog #2 (slot 50, 43200 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '43200 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297487/cafestory/reviewer_blogs/rjqs2ak33kracb89vssv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297489/cafestory/reviewer_blogs/wtufyuhjlnygz3vmb3gj.jpg');

    -- Blog #3 (slot 51, 44064 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '44064 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297490/cafestory/reviewer_blogs/tvmu5wmb407arvxm460o.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297492/cafestory/reviewer_blogs/wnrdw4ahodhbmj3dwyne.jpg');

    -- === Reviewer #25 (3 blog) ===
    v_user_id := v_reviewer_ids[25];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 52, 44928 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '44928 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297494/cafestory/reviewer_blogs/mqstdxtzkhpz8gx2bjcr.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297496/cafestory/reviewer_blogs/mmblssvmr6oonujmxbar.jpg');

    -- Blog #2 (slot 53, 45792 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '45792 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297498/cafestory/reviewer_blogs/qhozfgdzujy1grzpdeya.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297500/cafestory/reviewer_blogs/cw4tw6x202us9hrofhfp.jpg');

    -- Blog #3 (slot 54, 46656 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '46656 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297502/cafestory/reviewer_blogs/hdlpgjruzcmsyd3yvvdf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297504/cafestory/reviewer_blogs/btbzerrsmuzcdergzurl.jpg');

    -- === Reviewer #26 (3 blog) ===
    v_user_id := v_reviewer_ids[26];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 55, 47520 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tiêu chí chọn cafe của ' || v_user_full_name || ': yên tĩnh, đồ uống chất, giá hợp lý. Quán hôm nay đạt cả 3.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '47520 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297505/cafestory/reviewer_blogs/nvy3y8ld0x8ubobmzpbe.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297507/cafestory/reviewer_blogs/u2lwbfghexslntr9fdco.jpg');

    -- Blog #2 (slot 56, 48384 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '48384 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297509/cafestory/reviewer_blogs/it1c0t2iielnf1gkdfsh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297511/cafestory/reviewer_blogs/uvl76vyul3oxydfgqi9x.jpg');

    -- Blog #3 (slot 57, 49248 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '49248 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297513/cafestory/reviewer_blogs/kw4jena599hhksytgqxy.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297515/cafestory/reviewer_blogs/kmrqbpeqql9eiiptuznd.jpg');

    -- === Reviewer #27 (3 blog) ===
    v_user_id := v_reviewer_ids[27];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 58, 50112 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '50112 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297517/cafestory/reviewer_blogs/ywuuttgenyhaqr63vzgh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297519/cafestory/reviewer_blogs/sh5lcehfyfldwq0f6zhv.jpg');

    -- Blog #2 (slot 59, 50976 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '50976 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297521/cafestory/reviewer_blogs/bo6kve0afpib1abzsysx.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297523/cafestory/reviewer_blogs/viqljax78k0xyzoq6xis.jpg');

    -- Blog #3 (slot 60, 51840 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '51840 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297525/cafestory/reviewer_blogs/nzmydmfp5kpqcryo11eu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297527/cafestory/reviewer_blogs/eyyczybi9457mukkl2me.jpg');

    -- === Reviewer #28 (3 blog) ===
    v_user_id := v_reviewer_ids[28];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 61, 52704 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '52704 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297529/cafestory/reviewer_blogs/ifb0ww9j655fgfkjtopo.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297531/cafestory/reviewer_blogs/bcmqwvmbk2mo8ug7pnjp.png');

    -- Blog #2 (slot 62, 53568 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '53568 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297533/cafestory/reviewer_blogs/j9fu1pzxgrhxqipczqzn.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297535/cafestory/reviewer_blogs/wo8n6munareysxcg9hre.jpg');

    -- Blog #3 (slot 63, 54432 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '54432 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297537/cafestory/reviewer_blogs/me3pnyr0b4d2ayy1oumu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297539/cafestory/reviewer_blogs/drbhnwrbcoan0v8m7wlr.jpg');

    -- === Reviewer #29 (3 blog) ===
    v_user_id := v_reviewer_ids[29];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 64, 55296 min ago)
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
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297540/cafestory/reviewer_blogs/cjohx40nw8pc9v0ldmt9.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297542/cafestory/reviewer_blogs/yxqb90jqjwr1gmt8irtb.jpg');

    -- Blog #2 (slot 65, 56160 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '56160 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297545/cafestory/reviewer_blogs/aro0lzvgjkjpswawguay.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297546/cafestory/reviewer_blogs/hli8oisgkgqpjlrlotiu.jpg');

    -- Blog #3 (slot 66, 57024 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '57024 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297548/cafestory/reviewer_blogs/uia31scagz0fx1ry1gvh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297550/cafestory/reviewer_blogs/rzz82uhsjheu5ktnd8vd.jpg');

    -- === Reviewer #30 (3 blog) ===
    v_user_id := v_reviewer_ids[30];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 67, 57888 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '57888 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297551/cafestory/reviewer_blogs/ihwiz2xy1u7rqvj2k8eo.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297553/cafestory/reviewer_blogs/ekhkd3kqqqs4cny65qzj.jpg');

    -- Blog #2 (slot 68, 58752 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '58752 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297555/cafestory/reviewer_blogs/vglf44zwac9vfrpbh2vd.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297557/cafestory/reviewer_blogs/djpdhqwvnglyeg8edykl.jpg');

    -- Blog #3 (slot 69, 59616 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '59616 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297559/cafestory/reviewer_blogs/rwjrfkdeuotroacpjgfv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297561/cafestory/reviewer_blogs/sqluchn9abkiruwnqgjk.jpg');

    -- === Reviewer #31 (3 blog) ===
    v_user_id := v_reviewer_ids[31];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 70, 60480 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '60480 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297563/cafestory/reviewer_blogs/tnjiideyxldhvperuqdx.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297565/cafestory/reviewer_blogs/hqitqoz61kuy81kabsr3.jpg');

    -- Blog #2 (slot 71, 61344 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '61344 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297567/cafestory/reviewer_blogs/zodde8hrz1w6besxerv7.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297568/cafestory/reviewer_blogs/rjsdqso2cha6ewrncfaq.jpg');

    -- Blog #3 (slot 72, 62208 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '62208 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297570/cafestory/reviewer_blogs/i6ivwxst5jfp480kv3d0.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297572/cafestory/reviewer_blogs/ugl8ruckhk5ciapcvq8x.jpg');

    -- === Reviewer #32 (3 blog) ===
    v_user_id := v_reviewer_ids[32];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 73, 63072 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '63072 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297574/cafestory/reviewer_blogs/r9yir5lwx8jvs1smljd1.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297576/cafestory/reviewer_blogs/owplagrkyvmplwyoxj6v.jpg');

    -- Blog #2 (slot 74, 63936 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '63936 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297578/cafestory/reviewer_blogs/ru9is8ipde8xcl9dzbx3.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297580/cafestory/reviewer_blogs/vzmrh1js0uqq0k7x9xrl.jpg');

    -- Blog #3 (slot 75, 64800 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '64800 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297582/cafestory/reviewer_blogs/draowsc66cjm0ypncur4.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297584/cafestory/reviewer_blogs/cfvzkgxcdvefrihtiksc.jpg');

    -- === Reviewer #33 (3 blog) ===
    v_user_id := v_reviewer_ids[33];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 76, 65664 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '65664 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297586/cafestory/reviewer_blogs/qeasnkfci0c5evcp8o3d.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297588/cafestory/reviewer_blogs/wwdg0tyirto93uspyzra.jpg');

    -- Blog #2 (slot 77, 66528 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '66528 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297590/cafestory/reviewer_blogs/x0gupjgjnxfzpnllsrnf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297593/cafestory/reviewer_blogs/ix9w0wvayjhopjbv80kv.jpg');

    -- Blog #3 (slot 78, 67392 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '67392 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297594/cafestory/reviewer_blogs/benicjahkslkh3lplano.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297596/cafestory/reviewer_blogs/iwnidr6ratgvuo0byieb.jpg');

    -- === Reviewer #34 (3 blog) ===
    v_user_id := v_reviewer_ids[34];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 79, 68256 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' đã đi thử 3 quán tuần này, đây là quán duy nhất mình muốn quay lại — sẽ giải thích trong bài.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '68256 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297598/cafestory/reviewer_blogs/fvzh1sxxwig2sa0tqm29.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297600/cafestory/reviewer_blogs/qk52fzf8gt3fkjjtxeuk.jpg');

    -- Blog #2 (slot 80, 69120 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '69120 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297602/cafestory/reviewer_blogs/syu06bft0ajunelqxr7o.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297603/cafestory/reviewer_blogs/ervtrc5tt6ppi9k0ewky.jpg');

    -- Blog #3 (slot 81, 69984 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '69984 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297605/cafestory/reviewer_blogs/fvfat53svbtziphflhxf.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297607/cafestory/reviewer_blogs/bxtnwybvlp7u9l9rnwfb.jpg');

    -- === Reviewer #35 (3 blog) ===
    v_user_id := v_reviewer_ids[35];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 82, 70848 min ago)
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
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297609/cafestory/reviewer_blogs/dwfbnnsi47rwtber20rz.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297611/cafestory/reviewer_blogs/ljcztbmeqkfkia5mynuq.jpg');

    -- Blog #2 (slot 83, 71712 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '71712 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297613/cafestory/reviewer_blogs/kzxfrefgp995igkexnxb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297615/cafestory/reviewer_blogs/h2twbcqf7oifpotuvtwp.jpg');

    -- Blog #3 (slot 84, 72576 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '72576 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297617/cafestory/reviewer_blogs/jaopforqawn5lstst8ex.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297619/cafestory/reviewer_blogs/vkoq2x1q6slyflq7ckcn.jpg');

    -- === Reviewer #36 (3 blog) ===
    v_user_id := v_reviewer_ids[36];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 85, 73440 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Tuần này ' || v_user_full_name || ' khám phá được vài quán mới ở Cần Thơ — sẽ update review chi tiết ở comment.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '73440 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297621/cafestory/reviewer_blogs/ig2til8dv5c5axknzuou.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297623/cafestory/reviewer_blogs/jab4ey5ouovcvrgieuis.jpg');

    -- Blog #2 (slot 86, 74304 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '74304 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297625/cafestory/reviewer_blogs/s4zewosxmt6a2mfmzxhb.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297627/cafestory/reviewer_blogs/mdhnax1asqjnmtsugwez.jpg');

    -- Blog #3 (slot 87, 75168 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '75168 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297629/cafestory/reviewer_blogs/xnatjjmrcpa7klgnzljz.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297631/cafestory/reviewer_blogs/i8xptaa8qws9gjilkbth.jpg');

    -- === Reviewer #37 (3 blog) ===
    v_user_id := v_reviewer_ids[37];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 88, 76032 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '76032 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297633/cafestory/reviewer_blogs/gwvchn3wemvwnng445cz.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297635/cafestory/reviewer_blogs/wpd5j8prme98jcwlyle9.jpg');

    -- Blog #2 (slot 89, 76896 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '76896 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297636/cafestory/reviewer_blogs/auyyqrvpbiqgtweedagu.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297638/cafestory/reviewer_blogs/pw920stvwj4o4pg1nzlq.jpg');

    -- Blog #3 (slot 90, 77760 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '77760 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297640/cafestory/reviewer_blogs/p034fuy4vh7ftzxdltza.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297642/cafestory/reviewer_blogs/qtvgjsximrpzwdrkoqko.jpg');

    -- === Reviewer #38 (3 blog) ===
    v_user_id := v_reviewer_ids[38];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 91, 78624 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            '' || v_user_full_name || ' vừa thử một quán khá lạ ở Ninh Kiều — không đông, nhưng đồ uống thật sự impress.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '78624 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297644/cafestory/reviewer_blogs/p5xkupixklqk35h4jc7z.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297645/cafestory/reviewer_blogs/z1n3nr6n2oalrneoyyht.jpg');

    -- Blog #2 (slot 92, 79488 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Note tasting hôm nay của ' || v_user_full_name || ': hạt Robusta rang mộc, vị đậm, hậu ngọt kéo dài đến 30 giây.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '79488 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297648/cafestory/reviewer_blogs/ef0jgfzzm9hvmtkkirhd.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297650/cafestory/reviewer_blogs/ocr9jr9j3ygbkfuyqani.jpg');

    -- Blog #3 (slot 93, 80352 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chủ nhật của ' || v_user_full_name || ': cafe hopping 3 quán Ninh Kiều. Sẽ ranking trong story tiếp theo.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '80352 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297652/cafestory/reviewer_blogs/bdkn2o4tvji9xoaoqfiv.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297654/cafestory/reviewer_blogs/whab94e7ycdrpynlxxhh.jpg');

    -- === Reviewer #39 (3 blog) ===
    v_user_id := v_reviewer_ids[39];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 94, 81216 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Chia sẻ nhanh: ' || v_user_full_name || ' thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '81216 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297656/cafestory/reviewer_blogs/sg2neyy8avfjilgp4k05.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297657/cafestory/reviewer_blogs/ulr5rlfwuiplocs8ju5g.jpg');

    -- Blog #2 (slot 95, 82080 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Cold brew ở quán này ' || v_user_full_name || ' chấm 8/10 — mượt, ít chua, hợp cho ai mới bắt đầu uống cà phê nguyên chất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '82080 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297660/cafestory/reviewer_blogs/ras2quwvgzc7fokfptht.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297661/cafestory/reviewer_blogs/uri1s2uctfnmj9oqtueb.jpg');

    -- Blog #3 (slot 96, 82944 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '82944 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297663/cafestory/reviewer_blogs/a8jola9an5ubkx2qaq4r.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297665/cafestory/reviewer_blogs/lb3ok9qqsiky4tliofow.jpg');

    -- === Reviewer #40 (3 blog) ===
    v_user_id := v_reviewer_ids[40];
    SELECT u.region_id INTO v_region_id
    FROM users u WHERE u.user_id = v_user_id;
    IF v_region_id IS NULL THEN
        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;
    END IF;

    -- Blog #1 (slot 97, 83808 min ago)
    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Mình là ' || v_user_full_name || ', chuyên đi tìm cafe ẩn giữa lòng thành phố. Bài này giới thiệu 1 chỗ mới toanh.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '83808 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297667/cafestory/reviewer_blogs/zmm1rxnynxf1karvsoy3.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297669/cafestory/reviewer_blogs/idgympdcgs7hqardold0.jpg');

    -- Blog #2 (slot 98, 84672 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Latte 1 shot vs 2 shot: ' || v_user_full_name || ' thấy 2 shot mới đủ cân với sữa. 1 shot bị nhạt quá.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '84672 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297671/cafestory/reviewer_blogs/y1yt5al3pgtsuzjtkjzg.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297673/cafestory/reviewer_blogs/ch0kngtfo8iev9qbqej9.jpg');

    -- Blog #3 (slot 99, 85536 min ago)
    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,
                       is_pinned, allow_comment, like_count, share_count, comment_count,
                       created_at)
    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,
            'Đi cafe cùng bạn của ' || v_user_full_name || ': mỗi đứa order 1 món rồi share — kiểu đánh giá menu nhanh nhất.',
            'PUBLISHED', false, true, 0, 0, 0,
            now() - interval '85536 minutes')
    RETURNING id INTO v_blog_id;
    INSERT INTO blog_images (blog_id, image_url) VALUES
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297674/cafestory/reviewer_blogs/lepku4mvasypydrkytrh.jpg'),
        (v_blog_id, 'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784297676/cafestory/reviewer_blogs/rlkcdjthwy7eugmhidqp.jpg');

END $$;

COMMIT;

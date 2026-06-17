CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blog_recommendation_scores_latest_cache
    ON blog_recommendation_scores (user_id, window_type, context_region_id, computed_at DESC, rank_position ASC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blogs_status_created_id
    ON blogs (status, created_at DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blogs_page_status_created_id
    ON blogs (page_id, status, created_at DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blogs_author_created_id
    ON blogs (author_user_id, created_at DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_comments_blog_created_id
    ON comments (blog_id, created_at DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_comments_parent_created_id
    ON comments (parent_comment_id, created_at ASC, id ASC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_follows_following_created
    ON user_follows (following_user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_follows_follower_created
    ON user_follows (follower_user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_page_follows_user_page
    ON page_follows (user_id, page_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_page_likes_user_page
    ON page_likes (user_id, page_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blog_saves_user_created
    ON blog_saves (user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blog_shares_user_created
    ON blog_shares (user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_blog_tagged_users_user_created
    ON blog_tagged_users (tagged_user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_chat_members_user_conversation
    ON chat_members (user_id, conversation_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_chat_messages_conversation_created
    ON chat_messages (conversation_id, created_at DESC, id DESC);

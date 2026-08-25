package com.cafestory.repository;

import java.util.UUID;

/**
 * Tổng lượt tương tác trên các blog PUBLISHED, gom theo tác giả.
 *
 * <p>Dùng chung cho like/share/comment/save để bảng xếp hạng reviewer không phải
 * quét toàn bảng rồi lọc bằng Java. Mỗi repository trả về cùng một shape:
 * tổng toàn thời gian và tổng trong khoảng "gần đây" của cùng một lượt quét.
 */
public interface AuthorEngagementCountRow {

    UUID getAuthorUserId();

    Long getTotalCount();

    Long getRecentCount();
}

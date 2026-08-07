package com.cafestory.repository;

import java.util.UUID;

/**
 * Số lượt tương tác mà blog của một tác giả NHẬN ĐƯỢC trong một khoảng thời gian.
 *
 * <p>Khác {@link AuthorEngagementCountRow}: bản kia quét toàn lịch sử để trả cả
 * tổng toàn thời gian lẫn tổng gần đây, dùng cho bảng xếp hạng khám phá. Bản này
 * chỉ đếm trong khoảng [startAt, endAt) nên các job chạy hằng ngày không phải
 * quét toàn bảng.
 *
 * <p>Dùng chung cho like/share/comment: chiều đo là engagement tác giả nhận về,
 * không phải engagement người dùng đi thả cho người khác.
 */
public interface AuthorInteractionCountRow {

    UUID getAuthorUserId();

    Long getEventCount();
}

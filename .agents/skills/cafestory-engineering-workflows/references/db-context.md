# CafeStory Database Context

Load this reference for entity design, repository work, migrations, API contracts, joins, source-of-truth questions, or schema reviews.

For exact table/column/enum definitions, read `cafestory-schema.dbml`.

## Database

- Type: PostgreSQL.
- Schema style: UUID primary keys for domain tables, integer IDs for lookup tables such as `roles` and `payment_methods`.
- Table names use snake_case.
- Do not expose entities directly through APIs; map entities to response DTOs.
- Prefer deriving counts from relation tables instead of storing counters as source of truth.

## Core Source-Of-Truth Rules

- User likes and followers must be derived from `blog_likes`, `comment_likes`, `page_likes`, `user_follows`, and `page_follows`.
- Current backend stores `users.user_like` and `users.user_follower` as cached counters. Services must update them consistently, while relation tables remain the source of truth for recalculation.
- Blog ownership is `blogs.author_user_id`; if `blogs.page_id` is set, the post belongs to a cafe page but still has a real user author.
- Comment replies use `comments.parent_comment_id`.
- Blog and comment images use element collection tables: `blog_images` and `comment_images`.
- Blog shares use `blog_shares.share_type` with `PUBLIC`, `PRIVATE`, and `PAGE_ONLY`.
- Reports target either a blog or a comment; exactly one of `content_reports.blog_id` or `content_reports.comment_id` should be set by application validation.
- User follow must reject self-follow: `follower_user_id <> following_user_id`.
- Page ownership starts with `cafe_pages.owner_user_id`; this primary owner/creator is unique, so one user can create only one cafe page.
- `page_members` stores page owners/co-owners/members with `status`: `PENDING`, `ACTIVE`, `REJECTED`.
- User self-registration for a page creates `page_members.role_name=MEMBER` and `status=PENDING`.
- Page managers can approve/reject pending membership, or add a page member directly with `status=ACTIVE`.
- A user can create a blog for a cafe page only when they are `cafe_pages.owner_user_id` or an `ACTIVE` `OWNER`/`CO_OWNER` in `page_members` for that page.

## Domain Groups

Identity and access:
- `users`, `roles`, `user_roles`.
- Default role names: `USER`, `REVIEWER`, `ADMIN`, `CAFE_PAGE`.

Cafe pages and regions:
- `regions`, `cafe_pages`, `page_members`.

Publishing:
- `blogs`, `blog_images`, `comments`, `comment_images`, `blog_shares`.

Reactions and follows:
- `blog_likes`, `comment_likes`, `page_likes`, `user_follows`, `page_follows`.

Messaging and notifications:
- `conversations`, `chat_members`, `chat_messages`, `chat_message_images`, `notifications`.

Moderation:
- `content_reports`, `ai_moderation_results`.

Reviewer and rewards:
- `reviewers`, `reviewer_badges`, `reviewer_payouts`.

Payments:
- `payment_methods`, `payments`, `extra_fees`, `payouts`.

## Enum Values

- `user_status`: `ACTIVE`, `INACTIVE`, `BANNED`
- `page_status`: `DRAFT`, `ACTIVE`, `SUSPENDED`
- `post_status`: `DRAFT`, `PUBLISHED`, `HIDDEN`, `REMOVED`
- `report_status`: `OPEN`, `REVIEWING`, `RESOLVED`, `REJECTED`
- `moderation_decision`: `SAFE`, `NEEDS_REVIEW`, `VIOLATION`
- `payment_status`: `PENDING`, `PAID`, `FAILED`, `EXPIRED`, `REFUNDED`
- `payout_status`: `PENDING`, `APPROVED`, `REJECTED`, `PAID`
- `share_type`: `PUBLIC`, `PRIVATE`, `PAGE_ONLY`
- `conversation_type`: `DIRECT`, `GROUP`
- `member_role`: `OWNER`, `CO_OWNER`, `ADMIN`, `MEMBER`
- `page_member_status`: `PENDING`, `ACTIVE`, `REJECTED`
- `message_type`: `TEXT`, `IMAGE`, `STICKER`, `MIXED`
- `message_status`: `SENT`, `FAILED`, `DELETED`
- `notification_type`: `LIKE`, `SHARE`, `COMMENT`, `MESSAGE`, `FOLLOW`
- `reviewer_badge`: `IRON`, `BRONZE`, `SILVER`, `GOLD`, `DIAMOND`
- `reviewer_payout_status`: `CALCULATED`, `PAID`, `CANCELLED`

## Backend Implementation Guidance

- Current backend user columns are `user_id`, `user_name`, `user_full_name`, `user_password`, `user_email`, `user_phone`, `user_avatar`, `user_like`, `user_follower`, and `account_status`.
- `account_status=false` means the user cannot perform action services such as like, share, follow, or unfollow.
- Use `@Enumerated(EnumType.STRING)` for enum columns.
- Use join entities or composite IDs for many-to-many relation tables with payload columns such as `created_at`.
- Add service-level validation for cross-column rules that DBML notes cannot express alone.
- For list endpoints over `blogs`, `comments`, `payments`, and reports, prefer pagination.

## Known Cleanup From Source DBML

The source DBML included two invalid trailing references and they are intentionally excluded from `cafestory-schema.dbml`:
- `Ref: "extra_fees"."id" < "extra_fees"."amount"`
- `Ref: "blogs"."id" < "blogs"."author_user_id"`

These are not valid relationships because they connect IDs to non-FK/non-compatible columns.

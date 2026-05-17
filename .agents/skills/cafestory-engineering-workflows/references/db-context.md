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
- Do not treat `users.user_like` or `users.user_follower` as authoritative. If counters are needed, cache them asynchronously or compute them.
- Blog ownership is `blogs.author_user_id`; if `blogs.page_id` is set, the post belongs to a cafe page but still has a real user author.
- Comment replies use `comments.parent_comment_id`.
- Reports target either a blog or a comment; exactly one of `content_reports.blog_id` or `content_reports.comment_id` should be set by application validation.
- User follow must reject self-follow: `follower_user_id <> following_user_id`.
- Page ownership starts with `cafe_pages.owner_user_id`; use `page_members` when a cafe page has multiple staff/admin users.

## Domain Groups

Identity and access:
- `users`, `roles`, `user_roles`.

Cafe pages and regions:
- `regions`, `cafe_pages`, `page_members`.

Publishing:
- `blogs`, `comments`, `shares`.

Reactions and follows:
- `blog_likes`, `comment_likes`, `page_likes`, `user_follows`, `page_follows`.

Messaging and notifications:
- `conversations`, `conversation_participants`, `messages`, `notifications`, `notification_recipients`.

Moderation:
- `content_reports`, `ai_moderation_results`.

Reviewer and rewards:
- `reviewer_profiles`, `reviewer_scores`, `reviewer_badges`, `reviewer_badge_awards`.

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

## Backend Implementation Guidance

- Align Java entity fields with DB column names rather than the older `userId`, `userName`, `userFollower` naming.
- For `users`, prefer fields like `id`, `username`, `fullName`, `passwordHash`, `email`, `phone`, `avatarUrl`, `status`, `createdAt`, `updatedAt`.
- Use `@Enumerated(EnumType.STRING)` for enum columns.
- Use join entities or composite IDs for many-to-many relation tables with payload columns such as `created_at`.
- Add service-level validation for cross-column rules that DBML notes cannot express alone.
- For list endpoints over `blogs`, `comments`, `payments`, and reports, prefer pagination.

## Known Cleanup From Source DBML

The source DBML included two invalid trailing references and they are intentionally excluded from `cafestory-schema.dbml`:
- `Ref: "extra_fees"."id" < "extra_fees"."amount"`
- `Ref: "blogs"."id" < "blogs"."author_user_id"`

These are not valid relationships because they connect IDs to non-FK/non-compatible columns.

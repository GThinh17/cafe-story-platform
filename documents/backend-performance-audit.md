# Backend Performance Audit Report

Audit date: 2026-06-18

Repository scope: `1-cafe-story-backend-javaspring`

Audit rule: read-only backend audit. No Java, configuration, database migration, or test code was changed.

## 1. Scope

| Module | Checked | Endpoint Found | Redis Cache | Complex Mapper | N+1 Risk | Response Time Test |
| --- | --- | --- | --- | --- | --- | --- |
| blogfeed `/api/feed`, `/api/feed/organic`, `/api/blogs/feed` | Yes | Yes | Yes: `organicFeed`, `personalizedFeedRankings` | Yes: manual feed DTO assembly | Critical | BLOCKED: requires JWT |
| profile / user profile `/api/users`, `/api/users/{userId}`, `/api/users/by-username/{username}` | Yes | Yes | Yes: `userProfilesById`, `userProfilesByUsername`, `userFollowingCounts` | Yes: `UserMapper` maps region refs | High | BLOCKED: requires JWT |
| page profile `/api/cafe-pages`, `/api/cafe-pages/{id}` | Yes | Yes | Yes: `cafePageDetails` | Yes: `CafePageMapper` maps owner and nested region refs | High | BLOCKED: requires JWT |
| page blogs `/api/cafe-pages/{id}/blogs` | Yes | Yes | Yes: `cafePageBlogs` | Yes: `BlogMapper` | Medium | BLOCKED: requires JWT |
| blog detail `/api/blogs/{blogId}` | Yes | Yes | Yes: `blogDetails` | Yes: `BlogMapper` plus detail enrichers | Medium | BLOCKED: requires JWT |
| comment `/api/comments/**` | Yes | Yes | Yes: `commentLists` | Yes: `CommentMapper` maps blog/user/parent comment | High | BLOCKED: requires JWT |
| like / save / rating / share `/api/blogs/{id}/...` | Yes | Yes | Eviction only for most mutations | Yes: `BlogInteractionMapper` maps user/blog | High | BLOCKED: requires JWT |
| notification `/notifications` | Yes | Yes | No service cache | Yes: `NotificationMapper` maps recipient/actor | High | BLOCKED: requires JWT |
| search | Partial | No public search API found; admin user search exists | No dedicated search cache found | Admin mapper risk separate | Medium | BLOCKED / endpoint not identified |
| trending `/api/blogs/trending` | Yes | Yes | Yes: `trendingBlogs` | Manual mapper touches lazy blog/author/page | High | BLOCKED: requires JWT |
| recommendations `/api/recommendations/**` | Yes | Yes | Yes: `recommendationCards` | Manual mapper | Medium | BLOCKED: requires JWT |
| payment / subscription `/api/payments` | Yes | Yes | No direct cache | Manual mapper touches lazy buyer/fee | High for admin list | BLOCKED: requires JWT |
| admin list APIs | Partial | Yes | Mostly no cache | Several mappers | Medium/High | Not prioritized for runtime timing |

## 2. Current Flow Analysis

| API | Method | Endpoint | Controller | Service | Repository | Mapper | DTO | Cache | Auth |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Mixed Feed | GET | `/api/feed?cursor=&size=` | `FeedController.getFeed` | `FeedServiceImpl.getFeed` -> `BlogFeedRankingServiceImpl.getOrganicFeed` + `SponsoredCafeCandidateServiceImpl.getCandidates` | `BlogRepository`, `AdCampaignRepository`, `AdImpressionRepository`, `CafePageRepository`, `RegionRepository` | manual `toFeedResponse`, `toSponsoredItem` | `FeedResponseDTO` | manual `organicFeed`; ads no cache | JWT required by `SecurityConfig.anyRequest().authenticated()` |
| Organic Feed | GET | `/api/feed/organic?cursor=&size=` | `FeedController.getOrganicFeed` | `BlogFeedRankingServiceImpl.getOrganicFeed` | `BlogRepository.findByStatus`, `AiModerationResultRepository`, `BlogEventRepository` | manual `toOrganicFeedResponses` -> `toFeedResponses` | `FeedResponseDTO` | `organicFeed` 15s | JWT required |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=&regionId=&page=&size=` | `BlogFeedRankingController.getFeed` | `BlogFeedRankingServiceImpl.getPersonalizedFeed` | `BlogRecommendationScoreRepository`, `BlogRepository`, `BlogTrendingScoreRepository`, follow/report/region repos | manual `toFeedResponses` | `BlogFeedResponse` | `personalizedFeedRankings` 15s | JWT required |
| Rebuild Feed Cache | POST | `/api/blogs/feed/rebuild` | `BlogFeedRankingController.rebuildRecommendationCache` | `BlogFeedRankingServiceImpl.rebuildRecommendationCache` | same as personalized feed plus upsert native query | manual `toFeedResponses` | `BlogFeedResponse` | no method cache; writes recommendation scores | JWT required |
| Blog List | GET | `/api/blogs` | `BlogController.getAllBlogs` | `BlogServiceImpl.getAllBlogs(viewerUserId)` | `BlogRepository.findAll`, batch image/like/save/rating/tag repos | manual list mapper `toBlogListResponseDTO` | `BlogResponseDTO` | no method cache | JWT required |
| User Profile Blogs | GET | `/api/blogs/users/{userId}` | `BlogController.getBlogsByAuthorId` | `BlogServiceImpl.getAllBlogsByUserId` | `BlogRepository.findByAuthorUserId`, batch enrichers | manual list mapper | `BlogResponseDTO` | `userProfileBlogs` 15s | JWT required |
| Saved/Shared/Tagged Blogs | GET | `/api/blogs/users/{userId}/saved`, `/shared`, `/tagged` | `BlogController` | `BlogServiceImpl.getSavedBlogsByUserId`, `getSharedBlogsByUserId`, `getTaggedBlogsByUserId` | `BlogRepository.findSavedBlogsByUserId`, `findSharedBlogsByUserId`, `findTaggedBlogsByUserId` | manual list mapper | `BlogResponseDTO` | `userProfileBlogs` 15s | JWT required |
| Blog Detail | GET | `/api/blogs/{blogId}` | `BlogController.getBlogById` | `BlogServiceImpl.getBlogById` | `BlogValidator` -> `BlogRepository.findById`; like/save/rating/tag/region repos | `BlogMapper.toBlogResponseDTO` plus detail enrichment | `BlogResponseDTO` | `blogDetails` 30s for anonymous condition/path | JWT required |
| Comment List By Blog | GET | `/api/comments/blogs/{blogId}` | `CommentController.getCommentsByBlogId` | `CommentServiceImpl.getCommentsByBlogId` | `CommentRepository.findByBlogId` | `CommentMapper.toCommentResponseDTO` | `CommentResponseDTO` | `commentLists` 10s | JWT required |
| Comment Replies | GET | `/api/comments/{commentId}/replies` | `CommentController.getRepliesByCommentId` | `CommentServiceImpl.getRepliesByCommentId` | `CommentRepository.findByParentCommentId` | `CommentMapper` | `CommentResponseDTO` | `commentLists` 10s | JWT required |
| Blog Likes | GET | `/api/blogs/{blogId}/likes` | `BlogLikeController.getLikesByBlogId` | `BlogLikeServiceImpl.getLikesByBlogId` | `BlogLikeRepository.findByBlogId` | `BlogInteractionMapper.toBlogLikeResponseDTO` | `BlogLikeResponseDTO` | no cache | JWT required |
| Blog Shares | GET | `/api/blogs/{blogId}/shares` | `BlogShareController.getSharesByBlogId` | `BlogShareServiceImpl.getSharesByBlogId` | `BlogShareRepository.findByBlogId` | `BlogInteractionMapper.toBlogShareResponseDTO` | `BlogShareResponseDTO` | no cache | JWT required |
| User Profile | GET | `/api/users/{userId}` | `UserController.getUserById` | `UserServiceImpl.getUserById` | `UserValidator` -> `UserRepository.findById`, `UserFollowRepository` | `UserMapper.toUserResponseDTO` | `UserResponseDTO` | `userProfilesById`, `userFollowingCounts` | JWT required |
| User By Username | GET | `/api/users/by-username/{username}` | `UserController.getUserByUsername` | `UserServiceImpl.getUserByUsername` -> `getUserById` | `UserRepository.findByUserName`, validator | `UserMapper` | `UserResponseDTO` | `userProfilesByUsername` only when viewer is null | JWT required |
| Cafe Page List | GET | `/api/cafe-pages` | `CafePageController.getCafePages` | `CafePageServiceImpl.getAllCafePages` or `getCafePagesByOwnerId` | `CafePageRepository.findAll` / `findByOwnerUserId`, page like/follow/rating repos | `CafePageMapper.toCafePageResponseDTO` | `CafePageResponseDTO` | `cafePageDetails` 15s | JWT required |
| Cafe Page Detail | GET | `/api/cafe-pages/{cafePageId}` | `CafePageController.getCafePageById` | `CafePageServiceImpl.getCafePageById` | `CafePageValidator` -> `CafePageRepository.findById`; interaction/rating repos | `CafePageMapper` | `CafePageResponseDTO` | `cafePageDetails` 15s | JWT required |
| Cafe Page Blogs | GET | `/api/cafe-pages/{cafePageId}/blogs` | `CafePageController.getBlogsByCafePageId` | `CafePageServiceImpl.getBlogsByCafePageId` | `BlogRepository.findPublishedCafePageBlogsFirstPage/AfterCursor` | `BlogMapper.toBlogResponseDTO` | `BlogCursorPageResponseDTO` | `cafePageBlogs` 15s | JWT required |
| Notifications | GET | `/notifications?page=&limit=&isRead=&type=` | `NotificationController.getNotifications` | `NotificationServiceImpl.getUserNotifications` | `NotificationRepository.findByRecipientUserId...` | `NotificationMapper.toNotificationResponseDTO` | `NotificationResponseDTO` | no cache | JWT required |
| Trending Blogs | GET | `/api/blogs/trending?windowType=&page=&size=` | `BlogTrendingController.getTrendingBlogs` | `BlogTrendingServiceImpl.getTrendingBlogs` | `BlogTrendingScoreRepository.findByWindowTypeAndComputedAtOrderByRankPositionAsc`, page/ranking repos | manual `toTrendingResponse` | `BlogTrendingResponse` | `trendingBlogs` 15s | JWT required |
| Recommendations | GET | `/api/recommendations/users`, `/reviewers`, `/cafe-pages`, `/mixed` | `RecommendationController` | `RecommendationServiceImpl` | `UserRepository`, `ReviewerRepository`, `CafePageRepository`, `ContentReportRepository` | manual response builder | `RecommendationCardResponseDTO` | `recommendationCards` 15s | JWT required |
| Payments List | GET | `/api/payments` | `PaymentController.getPayments` | `PaymentServiceImpl.getAllPayments` | `PaymentRepository.findAllByOrderByCreatedAtDesc`, `PaymentDetailRepository.findByPaymentPaymentId` | manual `toResponse` | `PaymentResponseDTO` | no cache | JWT required and admin validation in service |

## 3. N+1 Query Risk

| Severity | API | File | Method | Risk Location | Why N+1 Can Happen | How To Verify | Suggested Fix |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Critical | Organic Feed / Mixed Feed | `BlogFeedRankingServiceImpl.java:142`, `:144`, `:629` | `buildOrganicRankingPage`, `calculateOrganicReportPenalty` | `blogRepository.findByStatus(PUBLISHED)` then per-blog moderation/report count | For every published blog, code calls `existsByBlogIdAndDecision`; score calculation calls report count per blog. Query count grows with all published blogs, not page size. | Enable Hibernate statistics, request `/api/feed/organic?size=10` cold with 10/50/100 published blogs, compare query count. | Move ranking into SQL/projection using aggregate subqueries or precomputed metrics; batch moderation/report counts by blog IDs; page before heavy mapping if rule allows. |
| Critical | Personalized Feed Rebuild / stale personalized feed | `BlogFeedRankingServiceImpl.java:260`, `:262`, `:364-366`, `:498`, `:512`, `:524` | `buildRecommendationScores`, `createRecommendationScore` | Per-blog moderation, page follow, user follow, region lookup, report penalty | Rebuild computes every published blog and calls repositories inside score calculation. Query count grows linearly with blog count and may be much larger than returned page. | Force stale cache or call `/api/blogs/feed/rebuild`, compare SQL count for 10/50/100 blogs. | Batch followed page/user IDs, region city lookup, moderation decisions, report counts; use cached recommendation score table for request path. |
| Critical | Personalized Feed cached read | `BlogRecommendationScoreRepository.java:31`, `BlogFeedRankingServiceImpl.java:218`, `:396`, `:437` | `findLatestPage`, `toFeedResponses`, `toFeedResponse` | `BlogRecommendationScore.blog` and `Blog.author` are lazy and not fetched | Reading recommendation scores then accessing `score.getBlog()` and `blog.getAuthor()` can trigger one query per score and one per author. | Request `/api/blogs/feed?page=0&size=50` with Redis bypass; inspect SQL count. | Add `join fetch s.blog b join fetch b.author` or projection DTO for feed rows; fetch page/region/image data in batch as currently done. |
| High | Trending Blogs | `BlogTrendingScoreRepository.java:15`, `BlogTrendingServiceImpl.java:81`, `:208`, `:216`, `:221`, `:231` | `getTrendingBlogs`, `toTrendingResponse`, `findPageName` | Score query does not fetch `blog`/`author`; mapper calls lazy blog/author and does page lookup per item | Query count grows with trending page size. `findPageName` is explicitly one page lookup per page-backed blog. | Call `/api/blogs/trending?size=50` with SQL log; count score query + blog/author/page queries. | Fetch join `score.blog.author`; batch page names by page IDs or include page join/projection. |
| High | Comment lists | `CommentRepository.java:16-20`, `CommentServiceImpl.java:87-111`, `CommentMapper.java:12-16` | `getCommentsByBlogId`, `getCommentsByUserId`, `getRepliesByCommentId` | Repository returns comments without fetch join; mapper reads `blog.id`, `user.userId`, `user.userName`, `parentComment.id` | For many comments, each comment can lazy-load user/blog/parent comment. | Request comments for blog with 50 comments; compare SQL count and comment count. | Add `@EntityGraph(attributePaths={"blog","user","parentComment"})`, projection DTO, or fetch join queries for list endpoints. |
| High | Cafe Page list/profile list | `CafePageRepository.java:15`, `CafePageServiceImpl.java:165-185`, `CafePageMapper.java:12-21`, `CafePageServiceImpl.java:419-437` | `getAllCafePages`, `getCafePagesByOwnerId`, `toCafePageResponseDTO` | `findAll`/`findByOwnerUserId` do not fetch owner/region; mapper reads owner and deep region refs; service also calls follow/like/rating queries per page | Query count grows with cafe page count and viewer-specific flags. | Call `/api/cafe-pages` with size-equivalent data; observe owner/region/follow/like/rating queries. | Add page list projection or entity graph for owner/region/city/province/ward; batch viewer flags and rating summaries by page IDs. |
| High | User profile list | `UserServiceImpl.java:70-74`, `UserMapper.java:11-18`, `UserProfileCacheService.java:21`, `UserServiceImpl.java:198-202` | `getAllUsers`, `toUserResponseDTO` | `userRepository.findAll()` has no region fetch; mapper reads nested region refs; service calls following count cache/repository per user | Query count grows with user count; cache may hide following count query after warmup. | Call `/api/users` with Redis OFF/simple cache; compare query count for 10/50/100 users. | Add projection or `@EntityGraph` for region refs; batch following counts and viewer follow flags. |
| High | Notifications | `NotificationRepository.java:14-22`, `NotificationServiceImpl.java:133-141`, `NotificationMapper.java:18-19` | `getUserNotifications` | Page query does not fetch recipient/actor; mapper reads both lazy relations | For each notification, mapper may trigger actor and recipient queries. | Call `/notifications?limit=50`; count SQL vs notification count. | Add `@EntityGraph(attributePaths={"recipient","actor"})` or DTO projection containing actor/recipient IDs. |
| High | Blog likes/shares/saves/ratings lists | `BlogLikeRepository.java:28-30`, `BlogShareRepository.java:11-13`, `BlogInteractionMapper.java:17-35` | `getLikesByBlogId`, `getSharesByBlogId`, equivalent save/rating list methods | Repositories return interaction entities; mapper reads lazy `user` and `blog` | Query count grows with interaction count. | Call likes/shares list for a blog with many interactions; inspect SQL. | Use projection DTO queries or fetch join `user` and `blog`. |
| High | Payment admin list | `PaymentServiceImpl.java:165-172`, `PaymentServiceImpl.java:468-470`, `PaymentRepository.java:16-18`, `Payment.java:38-46` | `getAllPayments`, `toResponse` | For each payment, service calls `paymentDetailRepository.findByPaymentPaymentId`; mapper reads lazy buyer/extraFee/adFee | Query count grows with payments. No pagination on public service list. | Call `/api/payments` as admin with 50 payments and SQL log. | Add pagination; fetch join buyer/fees/detail or projection query. |
| Medium | Blog detail | `BlogServiceImpl.java:309-332`, `BlogMapper.java:14-35`, `BlogMapper.java:52-86` | `toBlogResponseDTO` | Detail mapper reads author/page lazy; service then calls separate repos for like/save/rating/tag/region | Detail endpoint has bounded extra queries, but cache can hide cold cost; if reused in list, it becomes high. | Request detail with Redis bypass; count queries. | Use `findById` query with `@EntityGraph(author,page)` and aggregate detail query/projection for counts/user flags. |
| Medium | Cafe Page blogs | `CafePageServiceImpl.java:235-249`, `BlogRepository.java:95-117`, `BlogMapper.java:14-35` | `getBlogsByCafePageId` | Blog repository fetches author only; `BlogMapper` reads page relation | Since query filters by pageId but `Blog.page` is not fetched, page data in mapper may trigger extra queries per item. | Request `/api/cafe-pages/{id}/blogs?size=50`; inspect page relation queries. | Fetch `page` too or manually set page fields from known cafe page; use same batch list mapper as `BlogServiceImpl`. |
| Medium | Recommendations | `RecommendationServiceImpl.java:56-107`, `:132-188` | recommendation methods | Repositories fetch some region refs, but report penalty uses `ContentReportRepository` per user/page | Query count grows with candidate count up to 100. | Call `/api/recommendations/mixed?size=50`; count content report queries. | Batch active report counts by user IDs/page IDs or materialize report counts. |
| Medium | Page members/follows | `PageMemberRepository.java:23-25`, `PageMemberMapper.java:12-13`, `UserFollowRepository.java:15-17`, `UserFollowMapper.java:12-13` | list members/followers/following | Mappers read lazy user/page relations from list entities | Query count grows with followers/members. | Request page members or user followers with many rows. | Fetch join relation IDs or return projection DTO. |

## 4. Redis Cache Impact

| API | Cache Type | Cache Name | Cache Key | TTL | User Specific | Risk | Suggested Fix |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Organic Feed | Manual `CacheManager` get/put | `organicFeed` | `ranking:{cursor-or-first}:{size}` | 15s | No | Medium: warm request can hide full-table ranking and per-blog report/moderation queries | Keep cache, but profile with cache bypass; consider key includes ranking algorithm version/window if rules change. |
| Personalized Feed | `@Cacheable` | `personalizedFeedRankings` | `userId:windowType:regionId:page:size` | 15s | Yes | Medium: key includes user/page/size/region, but warm cache hides N+1 from score read/rebuild path | Keep key shape; bypass cache when profiling; evict on follow/page follow/report/blog changes is partially present. |
| User Profile Blogs | `@Cacheable` | `userProfileBlogs` | `posts/saved/shared/tagged:userId:viewerUserId-or-anon` | 15s | Yes when viewer present | Medium: key includes viewer, good; allEntries eviction can be broad but avoids stale interaction flags | Keep viewer in key; add targeted evictions later if cache churn becomes high. |
| Blog Detail | `@Cacheable` | `blogDetails` | `blogId`; only caches viewer-specific overload when `viewerUserId == null` | 30s | Anonymous-only cache | Medium: anonymous cache can hide detail query cost; user-specific detail is not cached, avoiding `isLike/isSave` leak | Keep anonymous-only cache; profile first request or disable cache locally. |
| Comment Lists | `@Cacheable` | `commentLists` | `blog:{blogId}`, `user:{userId}`, `replies:{commentId}` | 10s | Mostly no | High: create evicts only exact blog/replies; update/delete clears all. Cache can hide comment mapper N+1 on second request | Keep short TTL; add fetch join/projection so cache is not masking N+1. |
| Cafe Page Details/List | `@Cacheable` | `cafePageDetails` | `all:{viewerId|anon}`, `owner:{ownerId}:{viewerId|anon}`, `detail:{pageId}:{viewerId|anon}`, `top:{region}:{city}:{size}` | 15s | Yes when viewer present | Medium: key includes viewer; warm cache hides per-page rating/follow/like queries | Keep viewer in key; batch page enrichers. |
| Cafe Page Blogs | `@Cacheable` | `cafePageBlogs` | `pageId:cursor-or-first:size` | 15s | No viewer flags in response | Medium: cache hides mapper lazy page lookups | Keep key; fetch page relation or avoid mapper page access. |
| Trending Blogs | `@Cacheable` | `trendingBlogs` | `windowType:page:size` | 15s | No | High: warm cache hides score->blog->author/page N+1 | Keep cache; fetch join/projection. |
| Recommendations | `@Cacheable` | `recommendationCards` | `users/reviewers/cafe-pages/mixed:userId:page:size` | 15s | Yes | Medium: key includes user/page/size; mixed calls cached submethods internally only if through Spring proxy? Self-invocation may bypass cache for submethods | Verify self-invocation behavior; consider extracting sub-recommendation cache service if needed. |
| User Follow Lists | `@Cacheable` | `userFollowLists` | `followers:{userId}`, `following:{userId}` | 15s | Public relation data | Medium: cache hides mapper lazy relation queries; eviction keys look reversed around follow/unfollow and should be reviewed | Verify cache invalidation key directions after follow/unfollow. |
| User Following Count | `@Cacheable` | `userFollowingCounts` | `userId` | 15s | No | Medium: warm cache hides per-user count N+1 in user lists | Batch counts for list APIs. |
| Region/Report reasons | `@Cacheable` | `regionProvinces`, `regionCities`, `regionWards`, `reportReasons` | fixed or region target keys | 24h / 30m | No | Low | Keep. |

Redis is configured as production/default cache provider in `application.properties:18-23`; test profile uses simple cache in `src/test/resources/application.properties:7`. Redis port `localhost:6379` was reachable during audit. API response profiling should be run with Redis ON and with a local/test cache-bypass profile. Do not disable Redis in production.

## 5. SQL / Hibernate Verification Plan

Existing SQL-related config:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Recommended local profiling additions, not applied in this audit:

```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

Recommended optional tools: datasource-proxy or p6spy for query counting in integration tests, if local logging is not enough.

| API | Expected Query Count | Actual Query Count If Measured | Redis ON/OFF | Risk | Note |
| --- | --- | --- | --- | --- | --- |
| `/api/feed/organic?size=10` | Target after fix: O(1) to O(few batch queries), independent of total published blogs | Not measured: no JWT/test token | Compare ON/OFF | Critical | Current code likely scans all published blogs and runs per-blog checks. |
| `/api/blogs/feed?page=0&size=10` | Target after fix: 1 score query with fetch/projection + batch image/page/region | Not measured | Compare ON/OFF | Critical | Cached score read still has lazy score.blog risk. |
| `/api/blogs/trending?size=10` | Target after fix: 1 projection/fetch query + optional batch page lookup | Not measured | Compare ON/OFF | High | Current mapper calls lazy blog/author and page lookup per item. |
| `/api/comments/blogs/{id}` | Target after fix: 1 fetch/projection query | Not measured | Compare ON/OFF | High | Current mapper touches comment user/blog/parent. |
| `/notifications?limit=50` | Target after fix: 1 page query with actor/recipient projection | Not measured | Redis not applicable | High | No cache; straightforward query count test. |
| `/api/cafe-pages` | Target after fix: list query + batch viewer flags/counts | Not measured | Compare ON/OFF | High | Current service likely per-page flags/counts. |
| `/api/users` | Target after fix: list query + batch following counts/follow flags | Not measured | Compare ON/OFF | High | Cache can hide following count. |
| `/api/payments` | Target after fix: paged query with detail/buyer/fee projection | Not measured | Redis not applicable | High | Current service calls detail repo per payment. |

Verification dataset recommendation: use at least 10, 50, and 100 records for blogs, comments, notifications, cafe pages, users, and payments. For list/feed APIs, verify query count does not grow linearly with returned item count except for expected bounded batch queries.

Index review notes from code/migrations:

| Area | Existing Index Evidence | Follow-up |
| --- | --- | --- |
| Blogs | `Blog` entity defines indexes on author/status/page/region | Verify DB has matching indexes via migrations and actual database metadata. |
| Blog recommendation/trending | Entity and migrations define latest/rank indexes | Verify query plans for `computed_at`, `rank_position`, `context_region_id`. |
| Comments | `Comment` entity defines blog/status/created, parent/status/created, user/created indexes | Verify actual DB index existence. |
| Payments | No entity indexes observed in `Payment` | Consider index on `payment_status`, `buyer_id`, `created_at` if admin list is slow. |
| Notifications | No entity indexes observed in `Notification` | Consider index on `recipient_id`, `is_read`, `type`, `created_at` if notification list/count is slow. |

## 6. API Response Time Test Plan

Runtime facts during audit:

| Item | Result |
| --- | --- |
| Backend port `localhost:8080` | Open |
| Redis port `localhost:6379` | Open |
| Swagger/OpenAPI | `/v3/api-docs` public and returned 200 |
| JWT/test account | Not available in repo/task context |
| Protected API timing | Measured unauthenticated requests; all priority APIs returned 401 and are marked BLOCKED |
| Data mutation | No test user or seed data was created |

## API Response Time Test Result

| API | Method | Endpoint | Auth Required | Test Case | Redis Status | Page Size | Attempt | Response Time | Status Code | Result Level | Note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Blog Feed | GET | `/api/feed?size=10` | Yes | unauthenticated cold/warm | ON/Unknown hit | 10 | 1 | 100 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Feed | GET | `/api/feed?size=10` | Yes | unauthenticated cold/warm | ON/Unknown hit | 10 | 2 | 5 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Feed | GET | `/api/feed?size=20` | Yes | unauthenticated cold/warm | ON/Unknown hit | 20 | 1 | 4 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Feed | GET | `/api/feed?size=20` | Yes | unauthenticated cold/warm | ON/Unknown hit | 20 | 2 | 4 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Feed | GET | `/api/feed?size=50` | Yes | unauthenticated cold/warm | ON/Unknown hit | 50 | 1 | 3 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Feed | GET | `/api/feed?size=50` | Yes | unauthenticated cold/warm | ON/Unknown hit | 50 | 2 | 4 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Organic Feed | GET | `/api/feed/organic?size=10` | Yes | unauthenticated | ON/Unknown hit | 10 | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Organic Feed | GET | `/api/feed/organic?size=10` | Yes | unauthenticated | ON/Unknown hit | 10 | 2 | 5 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Personalized Blog Feed | GET | `/api/blogs/feed?size=10&page=0` | Yes | unauthenticated | ON/Unknown hit | 10 | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Personalized Blog Feed | GET | `/api/blogs/feed?size=10&page=0` | Yes | unauthenticated | ON/Unknown hit | 10 | 2 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog List | GET | `/api/blogs` | Yes | unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog List | GET | `/api/blogs` | Yes | unauthenticated | ON/Unknown hit | N/A | 2 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Detail | GET | `/api/blogs/00000000-0000-0000-0000-000000000000` | Yes | dummy UUID unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Detail | GET | `/api/blogs/00000000-0000-0000-0000-000000000000` | Yes | dummy UUID unauthenticated | ON/Unknown hit | N/A | 2 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| User Profile | GET | `/api/users/00000000-0000-0000-0000-000000000000` | Yes | dummy UUID unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| User Profile | GET | `/api/users/00000000-0000-0000-0000-000000000000` | Yes | dummy UUID unauthenticated | ON/Unknown hit | N/A | 2 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| User Profile By Username | GET | `/api/users/by-username/test` | Yes | dummy username unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| User Profile By Username | GET | `/api/users/by-username/test` | Yes | dummy username unauthenticated | ON/Unknown hit | N/A | 2 | 3 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Page Profile | GET | `/api/cafe-pages/00000000-0000-0000-0000-000000000000` | Yes | dummy UUID unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Page Profile | GET | `/api/cafe-pages/00000000-0000-0000-0000-000000000000` | Yes | dummy UUID unauthenticated | ON/Unknown hit | N/A | 2 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Page Blogs | GET | `/api/cafe-pages/00000000-0000-0000-0000-000000000000/blogs?size=10` | Yes | dummy page UUID unauthenticated | ON/Unknown hit | 10 | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Page Blogs | GET | `/api/cafe-pages/00000000-0000-0000-0000-000000000000/blogs?size=10` | Yes | dummy page UUID unauthenticated | ON/Unknown hit | 10 | 2 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Comments By Blog | GET | `/api/comments/blogs/00000000-0000-0000-0000-000000000000` | Yes | dummy blog UUID unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Comments By Blog | GET | `/api/comments/blogs/00000000-0000-0000-0000-000000000000` | Yes | dummy blog UUID unauthenticated | ON/Unknown hit | N/A | 2 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Likes | GET | `/api/blogs/00000000-0000-0000-0000-000000000000/likes` | Yes | dummy blog UUID unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Likes | GET | `/api/blogs/00000000-0000-0000-0000-000000000000/likes` | Yes | dummy blog UUID unauthenticated | ON/Unknown hit | N/A | 2 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Shares | GET | `/api/blogs/00000000-0000-0000-0000-000000000000/shares` | Yes | dummy blog UUID unauthenticated | ON/Unknown hit | N/A | 1 | 2 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Blog Shares | GET | `/api/blogs/00000000-0000-0000-0000-000000000000/shares` | Yes | dummy blog UUID unauthenticated | ON/Unknown hit | N/A | 2 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Notifications | GET | `/notifications?page=0&limit=10` | Yes | unauthenticated | N/A | 10 | 1 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Notifications | GET | `/notifications?page=0&limit=10` | Yes | unauthenticated | N/A | 10 | 2 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=DAY_7&page=0&size=10` | Yes | unauthenticated | ON/Unknown hit | 10 | 1 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=DAY_7&page=0&size=10` | Yes | unauthenticated | ON/Unknown hit | 10 | 2 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=10` | Yes | unauthenticated | ON/Unknown hit | 10 | 1 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=10` | Yes | unauthenticated | ON/Unknown hit | 10 | 2 | 1 ms | 401 | BLOCKED | Missing JWT; service flow not executed. |
| Payments List | GET | `/api/payments` | Yes | unauthenticated | N/A | N/A | 1 | 1 ms | 401 | BLOCKED | Missing JWT/admin user; service flow not executed. |
| Payments List | GET | `/api/payments` | Yes | unauthenticated | N/A | N/A | 2 | 1 ms | 401 | BLOCKED | Missing JWT/admin user; service flow not executed. |
| Swagger Api Docs | GET | `/v3/api-docs` | No | public API docs health check | N/A | N/A | 1 | 1962 ms | 200 | PASS | App is reachable; first API-doc generation was cold. |
| Swagger Api Docs | GET | `/v3/api-docs` | No | public API docs health check | N/A | N/A | 2 | 19 ms | 200 | PASS | Warm API-doc request. |

### Slow API Summary

No protected business API could be measured through service/database flow because no JWT/test account was available. No measured protected API response time can be classified PASS/WARNING/CRITICAL. The code-level query risks above remain valid and should be verified with authenticated test data.

| Priority | API | Endpoint | Worst Response Time | Suspected Cause | Suggested Fix |
| --- | --- | --- | --- | --- | --- |
| P0 | Blog Feed | `/api/feed`, `/api/feed/organic`, `/api/blogs/feed` | BLOCKED | Missing JWT; code risk is full-table ranking plus per-blog queries | Provide test user/token, run Redis ON/OFF, then batch/projection ranking queries. |
| P1 | User/Profile/Page APIs | `/api/users/**`, `/api/cafe-pages/**` | BLOCKED | Missing JWT; code risk is per-item mapper enrichment | Provide test user/token and seed 50 users/pages. |
| P1 | Comments/Notifications | `/api/comments/**`, `/notifications` | BLOCKED | Missing JWT; code risk is list mapper lazy relation loading | Provide test user/token and seed 50 comments/notifications. |
| P2 | Payment List | `/api/payments` | BLOCKED | Missing JWT/admin token; code risk is per-payment detail lookup and lazy buyer/fee mapping | Provide admin token and payment dataset. |

## 7. Recommended Fix Plan

No fixes were applied in this audit.

| Priority | API | Problem | Suggested Fix | Files To Change | Risk | Need DB Migration | Need Test Update | Expected Impact |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P0 | Feed organic/personalized | Per-blog repository calls during ranking and stale recommendation rebuild | Batch moderation/report/follow/region lookups; prefer SQL projection for ranking inputs; avoid scoring all blogs on request path | `BlogFeedRankingServiceImpl.java`, repositories for blog events/follows/moderation/region | Medium | Maybe indexes only | Yes: service tests + query-count integration test | Large reduction in cold feed query count and latency |
| P0 | Personalized cached feed | Lazy `score.blog` and `blog.author` in score page mapping | Add fetch join/entity graph in `BlogRecommendationScoreRepository.findLatestPage` or projection DTO | `BlogRecommendationScoreRepository.java`, `BlogFeedRankingServiceImpl.java` | Low/Medium | No | Yes | Prevent query growth with page size |
| P1 | Trending blogs | Lazy `score.blog`, `blog.author`, page lookup per item | Fetch join score->blog->author and batch page names, or projection DTO | `BlogTrendingScoreRepository.java`, `BlogTrendingServiceImpl.java` | Low/Medium | No | Yes | Lower trending list query count |
| P1 | Comment lists | Mapper touches lazy blog/user/parent | Add `@EntityGraph` or fetch join/projection for comment list methods | `CommentRepository.java`, maybe `CommentMapper.java` | Low | No | Yes | Removes N+1 on comment lists |
| P1 | Cafe page list | Mapper/enrichment calls per page | Add list DTO projection or entity graph for owner/region refs; batch viewer flags/rating summaries | `CafePageRepository.java`, `CafePageServiceImpl.java`, `CafePageRatingRepository.java`, `PageLikeRepository.java`, `PageFollowRepository.java` | Medium | Maybe indexes | Yes | Stabilizes profile/page list APIs |
| P1 | User list/profile | Region lazy mapping and following count per user | Entity graph/projection for region refs; batch following counts/follow flags | `UserRepository.java`, `UserServiceImpl.java`, `UserFollowRepository.java` | Medium | Maybe indexes | Yes | Stabilizes user profile/list APIs |
| P1 | Notifications | Mapper lazy loads recipient/actor per row | Add entity graph or projection for `NotificationRepository` page methods | `NotificationRepository.java`, `NotificationMapper.java` | Low | Add index maybe | Yes | Reduces notification list queries |
| P2 | Blog interactions | Interaction list mapper lazy-loads user/blog | Fetch join or projection DTOs for like/share/save/rating lists | `BlogLikeRepository.java`, `BlogShareRepository.java`, `BlogSaveRepository.java`, `BlogRatingRepository.java`, `BlogInteractionMapper.java` | Low | No | Yes | Reduces interaction list queries |
| P2 | Payments | Admin list unpaged plus per-payment detail query and lazy mapping | Add pagination and fetch/projection query with buyer/fee/detail | `PaymentRepository.java`, `PaymentServiceImpl.java`, controller/service interface if pagination is exposed | Medium | Index maybe | Yes | Prevents payment list growth |
| P3 | SQL profiling | SQL log lacks statistics/query count | Add local/test profile only for Hibernate statistics or datasource-proxy/p6spy | `application-local.properties` or test config | Low | No | Yes if query-count tests added | Makes N+1 regression visible |

## 8. Fix Priority

| Priority | Severity | API | Main Problem | Why Fix First | Suggested Fix |
| --- | --- | --- | --- | --- | --- |
| 1 | Critical | Blog feed organic/personalized rebuild | Full published-blog scan plus per-blog repository calls | Highest user-facing blast radius; feed is core first-screen API | Batch/projection ranking inputs and avoid request-time full recomputation. |
| 2 | Critical | Personalized cached feed | Lazy `BlogRecommendationScore.blog`/`Blog.author` during page mapping | Affects normal cached feed read path and grows with page size | Fetch join or projection DTO. |
| 3 | High | Profile: user/page lists | Per-item mapper/enrichment queries | Profile is priority and cache can hide cold cost | Entity graph/projection plus batched viewer flags/counts. |
| 4 | High | Blog trending | Lazy score->blog/author/page mapping | Trending list can be page-size N+1 | Fetch join/projection and batch page names. |
| 5 | High | Comments/notifications | List mappers access lazy relations | Common list APIs with many rows | Entity graph/projection. |
| 6 | High | Interaction lists | Like/share/save/rating list mappers access lazy relations | Common social APIs; easy bounded fix | Fetch join/projection. |
| 7 | High | Payment list | Per-payment detail lookup and lazy buyer/fee | Admin/payment can become slow; less central to feed/profile | Pagination plus projection/fetch joins. |
| 8 | Medium | Recommendations | Per-candidate report count | Related to feed/profile discovery but cached short-term | Batch report counts. |

## 9. Proposed Patch List

No files below were modified in this audit.

| File | Change Type | Reason | Risk |
| --- | --- | --- | --- |
| `src/main/java/com/cafestory/repository/BlogRecommendationScoreRepository.java` | Add fetch join or projection | Avoid N+1 in personalized feed cached read | Low/Medium |
| `src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java` | Refactor ranking batches | Remove per-blog repository calls in feed ranking/rebuild | Medium |
| `src/main/java/com/cafestory/repository/BlogRepository.java` | Add projection/fetch methods | Feed/page blogs need author/page/media-safe loading | Medium |
| `src/main/java/com/cafestory/repository/BlogTrendingScoreRepository.java` | Add fetch join/projection | Avoid trending score -> blog -> author N+1 | Low |
| `src/main/java/com/cafestory/service/serviceImplement/BlogTrendingServiceImpl.java` | Refactor mapper batching | Batch page names and pinned overrides | Medium |
| `src/main/java/com/cafestory/repository/CommentRepository.java` | Add EntityGraph/projection | Avoid comment list lazy mapper N+1 | Low |
| `src/main/java/com/cafestory/repository/NotificationRepository.java` | Add EntityGraph/projection and indexes | Avoid notification actor/recipient N+1 | Low |
| `src/main/java/com/cafestory/repository/CafePageRepository.java` | Add EntityGraph/projection | Avoid owner/region lazy loads in page list/profile | Medium |
| `src/main/java/com/cafestory/service/serviceImplement/CafePageServiceImpl.java` | Batch viewer flags/counts | Avoid per-page like/follow/rating queries | Medium |
| `src/main/java/com/cafestory/repository/UserRepository.java` | Add EntityGraph/projection | Avoid user region lazy loads | Low |
| `src/main/java/com/cafestory/service/serviceImplement/UserServiceImpl.java` | Batch counts/flags | Avoid following count/follow flag per user | Medium |
| `src/main/java/com/cafestory/repository/UserFollowRepository.java` | Add batch count/status queries | Support user/profile batching | Low |
| `src/main/java/com/cafestory/repository/BlogLikeRepository.java` | Add fetch join/projection | Avoid like list mapper N+1 | Low |
| `src/main/java/com/cafestory/repository/BlogShareRepository.java` | Add fetch join/projection | Avoid share list mapper N+1 | Low |
| `src/main/java/com/cafestory/repository/BlogSaveRepository.java` | Add fetch join/projection | Avoid save list mapper N+1 | Low |
| `src/main/java/com/cafestory/repository/BlogRatingRepository.java` | Add fetch join/projection | Avoid rating list mapper N+1 | Low |
| `src/main/java/com/cafestory/repository/PaymentRepository.java` | Add pagination/fetch/projection | Avoid payment list N+1 | Medium |
| `src/main/java/com/cafestory/service/serviceImplement/PaymentServiceImpl.java` | Batch/fetch detail and lazy refs | Avoid per-payment detail lookup | Medium |
| `src/test/java/com/cafestory/...` | Add test/query-count coverage | Prevent regression | Medium |
| `src/main/resources/application-local.properties` or test profile | Add SQL logging/statistics config | Local-only profiling | Low |
| `docs/database/migrations/*.sql` | Add index | Only if query plan proves missing notification/payment indexes | Medium |

## 10. Questions / Assumptions

| Item | Assumption / Question | Impact |
| --- | --- | --- |
| JWT/test account | No test token or existing test account was provided. I did not create a new account or mutate DB data. | Protected API service/database response time is BLOCKED. |
| Redis ON/OFF | Redis port was reachable and app config uses Redis; no safe runtime toggle was used. | Cache may hide cold query cost; compare with local/test simple cache or bypass profile. |
| Test data volume | Dataset size in the running DB was not inspected because protected APIs require JWT and no DB console access was used. | Need seeded 10/50/100 records to verify performance. |
| Search endpoint | No general `/api/search` endpoint found. Admin user search exists in `AdminUserController`/`UserRepository.findAdminUsers`. | Search audit limited to code discovery. |
| Open Session In View | No explicit `spring.jpa.open-in-view` property found. Spring Boot default may apply unless disabled elsewhere. | Lazy loads in mapper may happen during request and hide architectural issue rather than throwing. |
| API docs | Swagger/OpenAPI is enabled and public; `/v3/api-docs` responded 200. | Can be used to drive authenticated Postman/curl tests. |
| Build/tests | Maven build/test was not run because task was audit/report first, and no code fix was applied. | No compile/test validation result is claimed. |
| Response time classification | 401/403 endpoints are recorded as BLOCKED, not PASS, even if HTTP response was fast. | Need JWT for real PASS/WARNING/CRITICAL. |
| Business rules | Conclusions are based on current controller/service/repository/mapper/entity code and project DB context only. | No business rule changes proposed. |

## 11. Authenticated API Response Time Supplement

Measured after receiving a real test account on 2026-06-18.

Important: password and JWT token are not stored in this report. Test identity recorded only as username `thinh.tran`; roles observed from login response: `USER`, `REVIEWER`, `ADMIN`, `CAFE_PAGE`.

Runtime context:

| Item | Result |
| --- | --- |
| Backend | `http://localhost:8080` reachable |
| Redis | Port `6379` reachable; app cache status treated as ON |
| Redis OFF test | Not executed because no safe runtime toggle/restart was used during read-only audit |
| Client timeout | 12s for first feed group, 15s for later authenticated group |
| Test account | `thinh.tran` |
| Test user ID | `0636b684-5cfd-4f31-aa5d-e6e5e91107db` |
| Blog ID used for detail/comment/like/share/save | `2638720f-3adc-4ca8-8fb0-db3cc1a61eae` |
| Cafe page ID used for detail/page blogs | `8fb5a67e-d1bd-4278-abd1-b379d46d49ea` |
| Payment ID used for detail | `00c047db-f564-43e3-abf4-5675f193d700` |

### Authenticated API Response Time Test Result

| API | Method | Endpoint | Auth Required | Test Case | Redis Status | Page Size | Attempt | Response Time | Status Code | Result Level | Note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Auth Login | POST | `/api/auth/login` | No | login as `thinh.tran` | N/A | N/A | 1 | 1172 ms | 200 | PASS | Token issued; password/token not stored in report. |
| Auth Me | GET | `/api/auth/me` | Yes | authenticated | ON | N/A | 1 | 873 ms | 200 | PASS | JWT principal validation. |
| Auth Me | GET | `/api/auth/me` | Yes | authenticated | ON | N/A | 2 | 861 ms | 200 | PASS | JWT principal validation. |
| Current User Profile | GET | `/api/users/me` | Yes | authenticated | ON | N/A | 1 | 334 ms | 200 | PASS | Current user profile mapper. |
| Current User Profile | GET | `/api/users/me` | Yes | authenticated | ON | N/A | 2 | 35 ms | 200 | PASS | Warm cache likely; do not use as SQL proof. |
| User Profile | GET | `/api/users/0636b684-5cfd-4f31-aa5d-e6e5e91107db` | Yes | authenticated | ON | N/A | 1 | 12 ms | 200 | PASS | Profile by ID. |
| User Profile | GET | `/api/users/0636b684-5cfd-4f31-aa5d-e6e5e91107db` | Yes | authenticated | ON | N/A | 2 | 9 ms | 200 | PASS | Warm cache likely. |
| User Profile By Username | GET | `/api/users/by-username/thinh.tran` | Yes | authenticated | ON | N/A | 1 | 347 ms | 200 | PASS | Profile by username. |
| User Profile By Username | GET | `/api/users/by-username/thinh.tran` | Yes | authenticated | ON | N/A | 2 | 329 ms | 200 | PASS | Profile by username. |
| User List | GET | `/api/users` | Yes | authenticated | ON | N/A | 1 | 4446 ms | 200 | PASS | Near 5s threshold; still has N+1 risk from mapper/region/following counts. |
| User List | GET | `/api/users` | Yes | authenticated | ON | N/A | 2 | 4468 ms | 200 | PASS | No meaningful warm-cache improvement observed. |
| Blog List | GET | `/api/blogs` | Yes | authenticated | ON | N/A | 1 | 1365 ms | 200 | PASS | Blog list. |
| Blog List | GET | `/api/blogs` | Yes | authenticated | ON | N/A | 2 | 1240 ms | 200 | PASS | Blog list. |
| Blog Detail | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae` | Yes | authenticated | ON | N/A | 1 | 1376 ms | 200 | PASS | Blog detail. |
| Blog Detail | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae` | Yes | authenticated | ON | N/A | 2 | 1297 ms | 200 | PASS | User-specific detail path remains non-trivial. |
| User Profile Blogs | GET | `/api/blogs/users/0636b684-5cfd-4f31-aa5d-e6e5e91107db` | Yes | authenticated | ON | N/A | 1 | 1219 ms | 200 | PASS | Blogs by author. |
| User Profile Blogs | GET | `/api/blogs/users/0636b684-5cfd-4f31-aa5d-e6e5e91107db` | Yes | authenticated | ON | N/A | 2 | 31 ms | 200 | PASS | Warm cache likely hides query/mapping cost. |
| Blog Feed | GET | `/api/feed?size=10` | Yes | authenticated | ON | 10 | 1 | 11650 ms | 200 | CRITICAL | Feed ranking/mapping path is above 10s. |
| Blog Feed | GET | `/api/feed?size=10` | Yes | authenticated | ON | 10 | 2 | 11415 ms | 200 | CRITICAL | Warm request remains above 10s. |
| Blog Feed | GET | `/api/feed?size=20` | Yes | authenticated | ON | 20 | 1 | 12018 ms | ERR | CRITICAL | Client timed out/canceled at 12s. |
| Blog Feed | GET | `/api/feed?size=20` | Yes | authenticated | ON | 20 | 2 | 12010 ms | ERR | CRITICAL | Client timed out/canceled at 12s. |
| Blog Feed | GET | `/api/feed?size=50` | Yes | authenticated | ON | 50 | 1 | 11898 ms | 200 | CRITICAL | Large payload plus ranking path. |
| Blog Feed | GET | `/api/feed?size=50` | Yes | authenticated | ON | 50 | 2 | 11049 ms | 200 | CRITICAL | Warm request remains above 10s. |
| Organic Feed | GET | `/api/feed/organic?size=10` | Yes | authenticated | ON | 10 | 1 | 11022 ms | 200 | CRITICAL | Organic ranking path above 10s. |
| Organic Feed | GET | `/api/feed/organic?size=10` | Yes | authenticated | ON | 10 | 2 | 10995 ms | 200 | CRITICAL | Warm request remains above 10s. |
| Organic Feed | GET | `/api/feed/organic?size=20` | Yes | authenticated | ON | 20 | 1 | 11227 ms | 200 | CRITICAL | Organic ranking path above 10s. |
| Organic Feed | GET | `/api/feed/organic?size=20` | Yes | authenticated | ON | 20 | 2 | 10904 ms | 200 | CRITICAL | Warm request remains above 10s. |
| Organic Feed | GET | `/api/feed/organic?size=50` | Yes | authenticated | ON | 50 | 1 | 10984 ms | 200 | CRITICAL | Organic ranking path above 10s. |
| Organic Feed | GET | `/api/feed/organic?size=50` | Yes | authenticated | ON | 50 | 2 | 11212 ms | 200 | CRITICAL | Warm request remains above 10s. |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=HOUR_24&page=0&size=10` | Yes | authenticated | ON | 10 | 1 | 2061 ms | 200 | PASS | Cached/personalized feed path. |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=HOUR_24&page=0&size=10` | Yes | authenticated | ON | 10 | 2 | 2659 ms | 200 | PASS | Cached/personalized feed path. |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=HOUR_24&page=0&size=20` | Yes | authenticated | ON | 20 | 1 | 3301 ms | 200 | PASS | Time increases with page size. |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=HOUR_24&page=0&size=20` | Yes | authenticated | ON | 20 | 2 | 3234 ms | 200 | PASS | Time increases with page size. |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=HOUR_24&page=0&size=50` | Yes | authenticated | ON | 50 | 1 | 7413 ms | 200 | WARNING | Above 5s at size 50. |
| Personalized Blog Feed | GET | `/api/blogs/feed?windowType=HOUR_24&page=0&size=50` | Yes | authenticated | ON | 50 | 2 | 6978 ms | 200 | WARNING | Above 5s at size 50. |
| Comments By Blog | GET | `/api/comments/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae` | Yes | authenticated | ON | N/A | 1 | 353 ms | 200 | PASS | Current blog returned small/empty list; still code-level N+1 risk for larger comment sets. |
| Comments By Blog | GET | `/api/comments/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae` | Yes | authenticated | ON | N/A | 2 | 335 ms | 200 | PASS | Current blog returned small/empty list. |
| Blog Likes | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae/likes` | Yes | authenticated | ON | N/A | 1 | 336 ms | 200 | PASS | Current blog returned small/empty list. |
| Blog Likes | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae/likes` | Yes | authenticated | ON | N/A | 2 | 328 ms | 200 | PASS | Current blog returned small/empty list. |
| Blog Shares | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae/shares` | Yes | authenticated | ON | N/A | 1 | 333 ms | 200 | PASS | Current blog returned small/empty list. |
| Blog Shares | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae/shares` | Yes | authenticated | ON | N/A | 2 | 332 ms | 200 | PASS | Current blog returned small/empty list. |
| Blog Saves | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae/saves` | Yes | authenticated | ON | N/A | 1 | 450 ms | 200 | PASS | Current blog returned small/empty list. |
| Blog Saves | GET | `/api/blogs/2638720f-3adc-4ca8-8fb0-db3cc1a61eae/saves` | Yes | authenticated | ON | N/A | 2 | 439 ms | 200 | PASS | Current blog returned small/empty list. |
| Cafe Page List | GET | `/api/cafe-pages` | Yes | authenticated | ON | N/A | 1 | 3240 ms | 200 | PASS | Page list mapper/enrichment still notable. |
| Cafe Page List | GET | `/api/cafe-pages` | Yes | authenticated | ON | N/A | 2 | 3243 ms | 200 | PASS | No meaningful warm-cache improvement observed. |
| Cafe Page Detail | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea` | Yes | authenticated | ON | N/A | 1 | 760 ms | 200 | PASS | Page detail. |
| Cafe Page Detail | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea` | Yes | authenticated | ON | N/A | 2 | 22 ms | 200 | PASS | Warm cache likely. |
| Cafe Page Blogs | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea/blogs?size=10` | Yes | authenticated | ON | 10 | 1 | 386 ms | 200 | PASS | Current page returned very small/empty result. |
| Cafe Page Blogs | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea/blogs?size=10` | Yes | authenticated | ON | 10 | 2 | 12 ms | 200 | PASS | Warm cache likely. |
| Cafe Page Blogs | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea/blogs?size=20` | Yes | authenticated | ON | 20 | 1 | 340 ms | 200 | PASS | Current page returned very small/empty result. |
| Cafe Page Blogs | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea/blogs?size=20` | Yes | authenticated | ON | 20 | 2 | 11 ms | 200 | PASS | Warm cache likely. |
| Cafe Page Blogs | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea/blogs?size=50` | Yes | authenticated | ON | 50 | 1 | 333 ms | 200 | PASS | Current page returned very small/empty result. |
| Cafe Page Blogs | GET | `/api/cafe-pages/8fb5a67e-d1bd-4278-abd1-b379d46d49ea/blogs?size=50` | Yes | authenticated | ON | 50 | 2 | 14 ms | 200 | PASS | Warm cache likely. |
| Notifications | GET | `/notifications?page=0&limit=10` | Yes | authenticated | ON | 10 | 1 | 368 ms | 200 | PASS | Notification list. |
| Notifications | GET | `/notifications?page=0&limit=10` | Yes | authenticated | ON | 10 | 2 | 350 ms | 200 | PASS | Notification list. |
| Notifications Unread Count | GET | `/notifications/unread-count` | Yes | authenticated | ON | N/A | 1 | 419 ms | 200 | PASS | Notification count. |
| Notifications Unread Count | GET | `/notifications/unread-count` | Yes | authenticated | ON | N/A | 2 | 326 ms | 200 | PASS | Notification count. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=HOUR_24&page=0&size=10` | Yes | authenticated | ON | 10 | 1 | 3120 ms | 200 | PASS | Trending list. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=HOUR_24&page=0&size=10` | Yes | authenticated | ON | 10 | 2 | 3116 ms | 200 | PASS | Trending list. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=HOUR_24&page=0&size=20` | Yes | authenticated | ON | 20 | 1 | 5760 ms | 200 | WARNING | Above 5s at size 20. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=HOUR_24&page=0&size=20` | Yes | authenticated | ON | 20 | 2 | 5847 ms | 200 | WARNING | Above 5s at size 20. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=HOUR_24&page=0&size=50` | Yes | authenticated | ON | 50 | 1 | 13347 ms | 200 | CRITICAL | Above 10s at size 50. |
| Trending Blogs | GET | `/api/blogs/trending?windowType=HOUR_24&page=0&size=50` | Yes | authenticated | ON | 50 | 2 | 13986 ms | 200 | CRITICAL | Above 10s at size 50. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=10` | Yes | authenticated | ON | 10 | 1 | 3682 ms | 200 | PASS | Mixed recommendations. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=10` | Yes | authenticated | ON | 10 | 2 | 3174 ms | 200 | PASS | Mixed recommendations. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=20` | Yes | authenticated | ON | 20 | 1 | 3154 ms | 200 | PASS | Mixed recommendations. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=20` | Yes | authenticated | ON | 20 | 2 | 3212 ms | 200 | PASS | Mixed recommendations. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=50` | Yes | authenticated | ON | 50 | 1 | 3135 ms | 200 | PASS | Size 50 returned only available recommendations; payload small. |
| Recommendations Mixed | GET | `/api/recommendations/mixed?page=0&size=50` | Yes | authenticated | ON | 50 | 2 | 3140 ms | 200 | PASS | Size 50 returned only available recommendations; payload small. |
| Recommendations Users | GET | `/api/recommendations/users?page=0&size=20` | Yes | authenticated | ON | 20 | 1 | 3326 ms | 200 | PASS | User recommendations. |
| Recommendations Users | GET | `/api/recommendations/users?page=0&size=20` | Yes | authenticated | ON | 20 | 2 | 3208 ms | 200 | PASS | User recommendations. |
| Recommendations Reviewers | GET | `/api/recommendations/reviewers?page=0&size=20` | Yes | authenticated | ON | 20 | 1 | 326 ms | 200 | PASS | Current result was small/empty. |
| Recommendations Reviewers | GET | `/api/recommendations/reviewers?page=0&size=20` | Yes | authenticated | ON | 20 | 2 | 322 ms | 200 | PASS | Current result was small/empty. |
| Recommendations Cafe Pages | GET | `/api/recommendations/cafe-pages?page=0&size=20` | Yes | authenticated | ON | 20 | 1 | 323 ms | 200 | PASS | Current result was small/empty. |
| Recommendations Cafe Pages | GET | `/api/recommendations/cafe-pages?page=0&size=20` | Yes | authenticated | ON | 20 | 2 | 328 ms | 200 | PASS | Current result was small/empty. |
| Payments List | GET | `/api/payments` | Yes | authenticated admin-capable account | ON | N/A | 1 | 6973 ms | 200 | WARNING | Unpaged payment list, suspected per-payment detail/lazy mapping cost. |
| Payments List | GET | `/api/payments` | Yes | authenticated admin-capable account | ON | N/A | 2 | 7057 ms | 200 | WARNING | No meaningful warm-cache improvement observed. |
| Payment Detail | GET | `/api/payments/00c047db-f564-43e3-abf4-5675f193d700` | Yes | authenticated admin-capable account | ON | N/A | 1 | 674 ms | 200 | PASS | Payment detail. |
| Payment Detail | GET | `/api/payments/00c047db-f564-43e3-abf4-5675f193d700` | Yes | authenticated admin-capable account | ON | N/A | 2 | 554 ms | 200 | PASS | Payment detail. |
| Admin Payments List | GET | `/api/admin/payments` | Yes | authenticated admin-capable account | ON | N/A | 1 | 4723 ms | 200 | PASS | Very close to 5s threshold. |
| Admin Payments List | GET | `/api/admin/payments` | Yes | authenticated admin-capable account | ON | N/A | 2 | 4539 ms | 200 | PASS | Very close to 5s threshold. |

### Authenticated Slow API Summary

| Priority | API | Endpoint | Worst Response Time | Suspected Cause | Suggested Fix |
| --- | --- | --- | --- | --- | --- |
| P0 | Blog Feed | `/api/feed?size=10/20/50` | 12018 ms / timeout at size 20 | `FeedServiceImpl` enters organic ranking path; `BlogFeedRankingServiceImpl` scans published blogs and performs per-blog moderation/report/ranking work; cache did not make warm request fast enough | Batch score inputs, avoid request-time full-table ranking, add SQL/projection path for feed summary, verify with query count Redis ON/OFF. |
| P0 | Organic Feed | `/api/feed/organic?size=10/20/50` | 11227 ms | Organic feed ranking remains above 10s even on repeated calls | Batch moderation/report counts, avoid scoring all published blogs per request, validate cache key/hit behavior. |
| P0 | Trending Blogs | `/api/blogs/trending?windowType=HOUR_24&page=0&size=50` | 13986 ms | Trending score page grows with page size; code audit found score->blog->author lazy access and per-item page lookup | Fetch join/projection for score/blog/author and batch cafe page names. |
| P1 | Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=50` | 7413 ms | Query/mapping cost grows with page size; code audit found lazy score->blog/author risk | Fetch join `BlogRecommendationScore.blog.author` or projection DTO; keep list mapper batched. |
| P1 | Payments List | `/api/payments` | 7057 ms | Unpaged list with per-payment detail lookup and lazy buyer/fee mapping | Add pagination and fetch/projection query containing buyer/fee/detail fields. |
| P2 | Trending Blogs | `/api/blogs/trending?windowType=HOUR_24&page=0&size=20` | 5847 ms | Same trend mapping issue appears before size 50 | Same as trending P0; verify query count by page size. |
| P2 | User List | `/api/users` | 4468 ms | Under 5s but close; code audit found region/following count mapper risks | Add projection/entity graph and batch following counts/follow flags. |
| P2 | Cafe Page List | `/api/cafe-pages` | 3243 ms | Under 5s but mapper/enrichment cost is visible | Entity graph/projection for owner/region and batch viewer flags/rating summaries. |
| P2 | Admin Payments List | `/api/admin/payments` | 4723 ms | Under 5s but close to threshold | Same payment pagination/projection direction. |

### Authenticated Test Notes

| Item | Observation | Impact |
| --- | --- | --- |
| Cache masking | Some endpoints improved sharply on attempt 2, e.g. `/api/users/me`, `/api/blogs/users/{userId}`, `/api/cafe-pages/{id}`, `/api/cafe-pages/{id}/blogs`. | Redis/cache can hide cold mapper or SQL cost; query-count verification still required. |
| Cache not enough | `/api/feed`, `/api/feed/organic`, `/api/blogs/trending` size 50, and `/api/payments` stayed slow on repeated calls. | These should be prioritized even before Redis OFF profiling. |
| Dataset limitation | Some comment/like/share/save/recommendation endpoints returned small or empty payloads. | Current PASS result does not disprove N+1 risk for high-volume data. Need seeded 10/50/100 comments/interactions/recommendations. |
| Redis OFF | Not measured in this audit to avoid config/code changes and app restart. | Recommended next verification: run same endpoint matrix with local/test profile bypassing cache. |
| SQL query count | Not measured because Hibernate statistics/logging was not enabled during read-only audit. | Response time identifies slow APIs; SQL log/statistics needed to prove exact N+1 query counts. |

## 12. Fix Applied

Fix date: 2026-06-18

Scope of this fix: P0/P1 feed performance only. No endpoint path, public DTO shape, JWT/security flow, Redis TTL, database schema, or business score formula was intentionally changed.

### Files Changed

| File | Change Type | Reason | Risk |
| --- | --- | --- | --- |
| `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java` | Refactor scoring data loading | Replace per-blog repository calls in organic/personalized feed scoring with preloaded sets/maps for moderation, report counts, follows, and region city lookup | Medium |
| `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/repository/AiModerationResultRepository.java` | Add batch query | Load all violation blog IDs for a feed candidate set in one query | Low |
| `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/repository/BlogEventRepository.java` | Add aggregate projection | Load report counts grouped by blog ID instead of counting one blog at a time | Low |
| `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/repository/PageFollowRepository.java` | Add batch query | Load followed cafe page IDs for candidate page IDs in one query | Low |
| `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/repository/UserFollowRepository.java` | Add batch query | Load followed author IDs for candidate author IDs in one query | Low |
| `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/repository/BlogRecommendationScoreRepository.java` | Add fetch join | Fetch `BlogRecommendationScore.blog.author` for personalized feed page reads | Low |
| `1-cafe-story-backend-javaspring/src/test/java/com/cafestory/service/BlogFeedRankingServiceImplTest.java` | Update tests | Align feed ranking tests with the new batch repository methods while preserving expected score behavior | Low |

### Fix Applied Table

| Fix No | API | Files Changed | What Changed | Why | Test Result | Response Time Before | Response Time After | Note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `/api/feed/organic` | `BlogFeedRankingServiceImpl`, `AiModerationResultRepository`, `BlogEventRepository` | Organic feed now preloads violation blog IDs and report counts by blog IDs before scoring. | Removed per-blog moderation exists query and per-blog report count query from organic ranking loop. | `mvn clean test` PASS, 420 tests | Worst measured 11227 ms, CRITICAL | Worst measured 838 ms, PASS | Tested on temporary port `18081` with Redis ON and user `thinh.tran`. |
| 2 | `/api/feed` | `BlogFeedRankingServiceImpl`, `AiModerationResultRepository`, `BlogEventRepository` | Mixed feed benefits from the optimized organic feed path before sponsored cafe insertion. | `/api/feed` delegates to organic feed and was inheriting the same per-blog query cost. | `mvn clean test` PASS, 420 tests | Worst measured 12018 ms / timeout, CRITICAL | Worst measured 1769 ms, PASS | Sponsored cafe merge logic and response format unchanged. |
| 3 | `/api/blogs/feed` | `BlogFeedRankingServiceImpl`, `BlogRecommendationScoreRepository`, `PageFollowRepository`, `UserFollowRepository`, `BlogEventRepository`, `AiModerationResultRepository` | Personalized feed score rebuild now preloads violation IDs, followed pages, followed users, region cities, and report counts; cached page read fetch-joins score -> blog -> author. | Removed query growth by candidate count in score rebuild/stale path and reduced lazy relation risk in cached read path. | `mvn clean test` PASS, 420 tests | Worst measured 7413 ms at size 50, WARNING | Worst measured 907 ms at size 50, PASS | Score formula and `BlogFeedResponse` fields unchanged. |

### Post-Fix Response Time Test Result

Test environment: backend started temporarily from current workspace on `http://localhost:18081`; Redis was ON; login used username `thinh.tran`; password/JWT not stored. Temporary test process was stopped after measurement.

| API | Endpoint | Page Size | Attempt | Response Time | Status Code | Result Level | Before Worst |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Blog Feed | `/api/feed?size=10` | 10 | 1 | 1769 ms | 200 | PASS | 11650 ms |
| Blog Feed | `/api/feed?size=10` | 10 | 2 | 1085 ms | 200 | PASS | 11415 ms |
| Blog Feed | `/api/feed?size=20` | 20 | 1 | 1004 ms | 200 | PASS | 12018 ms / timeout |
| Blog Feed | `/api/feed?size=20` | 20 | 2 | 997 ms | 200 | PASS | 12010 ms / timeout |
| Blog Feed | `/api/feed?size=50` | 50 | 1 | 1025 ms | 200 | PASS | 11898 ms |
| Blog Feed | `/api/feed?size=50` | 50 | 2 | 1112 ms | 200 | PASS | 11049 ms |
| Organic Feed | `/api/feed/organic?size=10` | 10 | 1 | 789 ms | 200 | PASS | 11022 ms |
| Organic Feed | `/api/feed/organic?size=10` | 10 | 2 | 788 ms | 200 | PASS | 10995 ms |
| Organic Feed | `/api/feed/organic?size=20` | 20 | 1 | 792 ms | 200 | PASS | 11227 ms |
| Organic Feed | `/api/feed/organic?size=20` | 20 | 2 | 812 ms | 200 | PASS | 10904 ms |
| Organic Feed | `/api/feed/organic?size=50` | 50 | 1 | 838 ms | 200 | PASS | 10984 ms |
| Organic Feed | `/api/feed/organic?size=50` | 50 | 2 | 824 ms | 200 | PASS | 11212 ms |
| Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=10` | 10 | 1 | 847 ms | 200 | PASS | 2061 ms |
| Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=10` | 10 | 2 | 784 ms | 200 | PASS | 2659 ms |
| Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=20` | 20 | 1 | 801 ms | 200 | PASS | 3301 ms |
| Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=20` | 20 | 2 | 805 ms | 200 | PASS | 3234 ms |
| Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=50` | 50 | 1 | 852 ms | 200 | PASS | 7413 ms |
| Personalized Blog Feed | `/api/blogs/feed?windowType=HOUR_24&page=0&size=50` | 50 | 2 | 907 ms | 200 | PASS | 6978 ms |

### Validation After Fix

| Command / Check | Result | Note |
| --- | --- | --- |
| `mvn -Dtest=BlogFeedRankingServiceImplTest test` | PASS | 9 feed ranking tests passed after updating mocks to batch methods. |
| `mvn test` | PASS | 420 tests passed before final cleanup. |
| `mvn clean test` | PASS | 420 tests passed after a clean rebuild. |
| Authenticated response-time smoke test | PASS | `/api/feed`, `/api/feed/organic`, and `/api/blogs/feed` all under 5s for page sizes 10/20/50. |

### Remaining Performance Work

| Priority | API | Remaining Issue | Suggested Next Fix |
| --- | --- | --- | --- |
| P0 | `/api/blogs/trending` | Still previously measured CRITICAL at size 50; not changed in this fix group. | Add fetch join/projection for trending score -> blog -> author and batch cafe page names. |
| P1 | `/api/payments` | Still previously measured WARNING around 7s; not changed in this fix group. | Add pagination and fetch/projection query for payment buyer/fee/detail. |
| P2 | `/api/users`, `/api/cafe-pages` | Under 5s but mapper/enrichment cost remains visible. | Add projection/entity graph and batch viewer flags/counts. |

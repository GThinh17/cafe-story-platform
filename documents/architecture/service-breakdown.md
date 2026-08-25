# CafeStory Service Breakdown

Tai lieu nay mo ta cach backend CafeStory dang chia cac service theo module lon, cac module lien ket voi nhau bang UUID/entity relation, va breakdown cac chuc nang nho ben trong tung module.

> Ket luan nhanh: code hien tai la modular monolith. Cac service nhu `UserService`, `ReviewerService`, `CafePageService`, `PageMemberService` chay trong cung mot Spring Boot app, dung chung PostgreSQL, va goi repository noi bo. Day chua phai microservice rieng biet.

## 1. So Do Module Lon

```mermaid
flowchart LR
    User["User module"]
    Region["Region module"]
    Reviewer["Reviewer module"]
    Page["Cafe Page module"]
    Blog["Blog module"]
    Social["Social interactions"]
    Payment["Payment module"]
    Chat["Chat module"]
    Notification["Notification module"]

    User -->|"users.region_id"| Region
    Reviewer -->|"reviewers.user_id"| User
    Page -->|"cafe_pages.owner_user_id"| User
    Page -->|"cafe_pages.region_id"| Region
    Blog -->|"blogs.author_user_id"| User
    Blog -->|"blogs.page_id"| Page
    Blog -->|"blogs.region_id"| Region
    Social -->|"like/follow/comment/share"| User
    Social -->|"page/blog targets"| Page
    Social -->|"page/blog targets"| Blog
    Payment -->|"payments.buyer_id"| User
    Payment -->|"upgrade reviewer"| Reviewer
    Chat -->|"chat_members.user_id"| User
    Notification -->|"recipient/actor"| User
```

## 2. Service Layer Hien Tai

Backend di theo luong:

```text
Controller -> Service -> Validator/Mapper -> Repository -> PostgreSQL
```

Moi module lon thuong co:

- `Controller`: nhan API request, lay `UUID` tu path/query/body.
- `Service`: xu ly business rule.
- `Validator`: kiem tra entity ton tai/quyen han/trang thai.
- `Repository`: query database bang Spring Data JPA.
- `Mapper`: doi Entity thanh DTO response.

## 3. User Module

### File chinh

- `UserController`
- `UserService`
- `UserServiceImpl`
- `UserRepository`
- `UserValidator`
- `UserMapper`
- Entity: `User`

### Chuc nang nho

- Tao user.
- Lay danh sach user.
- Lay user theo `userId`.
- Cap nhat thong tin user.
- Cap nhat region cua user.
- Xoa user.
- Kiem tra user ton tai.
- Kiem tra user active/inactive.

### Lien ket database

```mermaid
erDiagram
    USERS }o--|| REGIONS : "region_id"
    USER_ROLES }o--|| USERS : "user_id"
    USER_ROLES }o--|| ROLES : "role_id"
    USER_FOLLOWS }o--|| USERS : "follower_user_id"
    USER_FOLLOWS }o--|| USERS : "following_user_id"
```

### Cach service dung ID

`UserServiceImpl` nhan `UUID userId` hoac `regionId`, sau do goi:

```java
userValidator.validateUserExists(userId);
regionRepository.findById(regionId);
```

Day la truyen ID noi bo trong cung backend, khong phai goi microservice qua network.

## 4. Region Module

### File chinh

- `RegionRepository`
- Entity: `Region`
- DTO: `RegionRequestDTO`, `RegionResponseDTO`

Hien tai chua co `RegionService` rieng. Region dang duoc dung truc tiep trong:

- `UserServiceImpl`: cap nhat dia chi/khu vuc cua user.
- `CafePageServiceImpl`: gan region cho cafe page.
- `ReviewerServiceImpl`: thong ke reviewer theo `city`, `province`, `area`.

### Chuc nang nho dang co

- Luu thong tin region cho user.
- Gan region cho cafe page.
- Dung region de filter/ranking reviewer.
- Dung region de tinh feed/recommendation theo khu vuc.

### Khi nao nen tach `RegionService`

Nen tach khi co cac API rieng nhu:

- Quan ly danh sach tinh/thanh/quan/huyen.
- Tim region theo city/province/area.
- Chuan hoa dia chi.
- Reuse logic region o nhieu service.

## 5. Reviewer Module

### File chinh

- `ReviewerController`
- `ReviewerService`
- `ReviewerServiceImpl`
- `ReviewerRepository`
- `ReviewerPayoutRepository`
- `ReviewerBadgeHistoryRepository`
- Entity: `Reviewer`, `ReviewerPayout`, `ReviewerBadgeHistory`

### Chuc nang nho

- Tao reviewer tu user.
- Gan role `REVIEWER` cho user.
- Lay reviewer theo `userId`.
- Lay danh sach reviewer.
- Tinh stats theo ngay/tuan/thang/3 thang.
- Tinh score reviewer.
- Tinh payout.
- Tinh badge.
- Tinh segment.
- Tao monthly payout.
- Tao monthly badge.
- Lay lich su payout/badge.
- Ranking reviewer.
- Thong ke reviewer theo khu vuc.

### Lien ket database

```mermaid
erDiagram
    REVIEWERS ||--|| USERS : "user_id"
    REVIEWER_BADGES }o--|| REVIEWERS : "reviewer_id"
    REVIEWER_PAYOUTS }o--|| REVIEWERS : "reviewer_id"
    BLOG_LIKES }o--|| USERS : "user_id"
    BLOG_SHARES }o--|| USERS : "user_id"
    COMMENTS }o--|| USERS : "user_id"
```

### Service phu thuoc module nao

`ReviewerServiceImpl` doc du lieu tu:

- `UserRepository`, `UserValidator`: de biet reviewer la user nao.
- `RoleRepository`, `UserRoleAssignmentRepository`: de gan role reviewer/admin.
- `BlogLikeRepository`, `BlogShareRepository`, `CommentRepository`: de tinh diem reviewer.
- `UserFollowRepository`: de tinh thong tin follow.
- `ReviewerPayoutRepository`, `ReviewerBadgeHistoryRepository`: de luu ket qua thang.

## 6. Cafe Page Module

Day la module lon, ben trong dang co nhieu service nho.

```mermaid
flowchart TB
    Page["Cafe Page module"]
    PageCore["CafePageService"]
    Member["PageMemberService"]
    Like["PageLikeService"]
    Follow["PageFollowService"]
    BlogOnPage["BlogService: create blog on page"]

    Page --> PageCore
    Page --> Member
    Page --> Like
    Page --> Follow
    Page --> BlogOnPage

    PageCore -->|"create/update/delete page"| CafePageRepo["CafePageRepository"]
    Member -->|"join/add/approve/reject"| PageMemberRepo["PageMemberRepository"]
    Like -->|"like/unlike"| PageLikeRepo["PageLikeRepository"]
    Follow -->|"follow/unfollow"| PageFollowRepo["PageFollowRepository"]
    BlogOnPage -->|"validate page permission"| CafePageValidator["CafePageValidator"]
```

### 6.1 CafePageService

File:

- `CafePageController`
- `CafePageService`
- `CafePageServiceImpl`
- `CafePageRepository`
- `CafePageValidator`
- `CafePageMapper`
- Entity: `CafePage`

Chuc nang nho:

- Tao cafe page.
- Tao owner member mac dinh.
- Tao co-owner khi create page.
- Lay tat ca cafe page.
- Lay page theo owner.
- Lay page theo `cafePageId`.
- Lay blog cua page.
- Cap nhat page.
- Xoa page.

Lien ket:

```mermaid
erDiagram
    CAFE_PAGES }o--|| USERS : "owner_user_id"
    CAFE_PAGES }o--|| REGIONS : "region_id"
    PAGE_MEMBERS }o--|| CAFE_PAGES : "page_id"
    PAGE_MEMBERS }o--|| USERS : "user_id"
```

### 6.2 PageMemberService

File:

- `PageMemberController`
- `PageMemberService`
- `PageMemberServiceImpl`
- `PageMemberRepository`
- Entity: `PageMember`, `PageMemberId`

Chuc nang nho:

- User gui yeu cau tham gia page: `requestToJoinPage(cafePageId, userId)`.
- Owner/co-owner them thanh vien truc tiep: `addPageMember(cafePageId, actorUserId, userId, roleName)`.
- Owner/co-owner approve/reject request: `updatePageMemberStatus(...)`.
- Lay danh sach thanh vien page.
- Lay danh sach thanh vien dang pending.
- Normalize role: `OWNER`, `CO_OWNER`, `MEMBER`.
- Chan them primary owner thanh member lan nua.

Flow them thanh vien:

```mermaid
sequenceDiagram
    participant C as PageMemberController
    participant S as PageMemberServiceImpl
    participant V as CafePageValidator
    participant UV as UserValidator
    participant R as PageMemberRepository

    C->>S: addPageMember(cafePageId, actorUserId, userId, roleName)
    S->>V: validateUserCanManagePage(cafePageId, actorUserId)
    S->>V: validateCafePageExists(cafePageId)
    S->>UV: validateUserExists(userId)
    S->>UV: validateUserActive(user)
    S->>R: findByCafePageIdAndUserUserId(cafePageId, userId)
    alt member da ton tai
        S->>S: update role/status ACTIVE
    else member chua ton tai
        S->>S: create PageMember(pageId, userId)
    end
    S->>R: save(pageMember)
```

### 6.3 PageLikeService

Chuc nang nho:

- Like page.
- Unlike page.
- Lay likes theo page.
- Lay likes theo user.
- Cap nhat cached `like_count` cua page.

Lien ket:

```mermaid
erDiagram
    PAGE_LIKES }o--|| USERS : "user_id"
    PAGE_LIKES }o--|| CAFE_PAGES : "page_id"
```

### 6.4 PageFollowService

Chuc nang nho:

- Follow page.
- Unfollow page.
- Lay follower cua page.
- Lay danh sach page user dang follow.
- Cap nhat cached `follower_count` cua page.

Lien ket:

```mermaid
erDiagram
    PAGE_FOLLOWS }o--|| USERS : "user_id"
    PAGE_FOLLOWS }o--|| CAFE_PAGES : "page_id"
```

### 6.5 Blog tren Page

`BlogServiceImpl` cho phep tao blog gan voi page khi `BlogCreateDTO.pageId` co gia tri.

Rule quan trong nam trong `CafePageValidator.validateUserCanCreateBlogOnPage(...)`:

- User la primary owner cua page thi duoc dang bai.
- Hoac user la `ACTIVE` member voi role `OWNER`/`CO_OWNER`.
- Member role `MEMBER` khong duoc tao blog tren page.

## 7. Quan He User - Reviewer - Page - Region

```mermaid
flowchart LR
    U["User"]
    R["Reviewer"]
    P["CafePage"]
    G["Region"]
    PM["PageMember"]
    BL["BlogLike"]
    BS["BlogShare"]
    C["Comment"]

    U -->|"has region"| G
    R -->|"is upgraded from user"| U
    P -->|"owned by user"| U
    P -->|"located in region"| G
    PM -->|"page member user"| U
    PM -->|"member of page"| P
    R -->|"score from likes"| BL
    R -->|"score from shares"| BS
    R -->|"score from comments"| C
```

Doc theo ngon ngu domain:

- `User` la goc cua nhieu module.
- `Reviewer` la mot vai tro/nang cap cua `User`.
- `CafePage` duoc so huu boi `User`.
- `PageMember` noi `User` voi `CafePage` va quy dinh role trong page.
- `Region` la du lieu dia ly dung cho user, page, reviewer ranking/feed.
- `ReviewerService` khong tu tao interaction; no doc tu like/share/comment cua user de tinh diem.

## 8. Co Phai Nen Tach Thanh Microservice Khong?

Hien tai chua nen goi la microservice. Nen goi la cac bounded module trong modular monolith.

Neu sau nay muon tach microservice, co the tach theo huong:

```mermaid
flowchart LR
    IdentityService["Identity/User Service"]
    RegionService["Region Service"]
    PageService["Page Service"]
    ContentService["Blog/Comment Service"]
    ReviewerService["Reviewer/Reward Service"]
    PaymentService["Payment Service"]
    ChatService["Chat Service"]
    NotificationService["Notification Service"]

    PageService -->|"ownerUserId/memberUserId"| IdentityService
    PageService -->|"regionId"| RegionService
    ContentService -->|"authorUserId/pageId/regionId"| IdentityService
    ContentService --> PageService
    ContentService --> RegionService
    ReviewerService -->|"userId + interaction events"| IdentityService
    ReviewerService --> ContentService
    PaymentService -->|"buyerId, reviewer upgrade"| IdentityService
    PaymentService --> ReviewerService
    NotificationService -->|"recipientId/actorId"| IdentityService
```

Nhung neu tach that, moi service can them:

- API contract rieng.
- Database ownership rieng hoac schema ownership ro rang.
- Service-to-service communication: HTTP/gRPC/message broker.
- Event flow cho like/comment/share/payment.
- Error handling khi service khac down.
- Idempotency va retry.

Voi quy mo hien tai, cach tot hon la giu monolith nhung viet ro module boundary: controller/service/repository rieng, DTO rieng, validator rieng, va khong de service nay sua data cua service kia qua nhieu.

## 9. Goi Y Breakdown Code Theo Module

Neu muon lam dep lai kien truc ma chua tach microservice, co the gom theo package domain:

```text
com.cafestory.user
  controller
  service
  repository
  dto
  mapper
  validation

com.cafestory.region
  service
  repository
  dto

com.cafestory.page
  controller
  service
  repository
  dto
  mapper
  validation

com.cafestory.reviewer
  controller
  service
  repository
  dto

com.cafestory.blog
  controller
  service
  repository
  dto
```

Day van la mot app Spring Boot, nhung nhin giong cac service rieng hon va de tach microservice sau nay hon.

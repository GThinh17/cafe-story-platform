# Phase 03 - Cafe Page Posting Authorization

## Goal

Dam bao `POST /api/blogs` co the tao user blog hoac cafe page blog dung quyen.

## Existing API

```http
POST /api/blogs
```

User blog body:

```json
{
  "content": "Noi dung bai viet",
  "imageUrls": []
}
```

Cafe page blog body:

```json
{
  "pageId": "cafe-page-uuid",
  "content": "Noi dung bai viet",
  "imageUrls": []
}
```

## Authorization Rule

```text
pageId == null:
  user dang bai ca nhan

pageId != null:
  user phai co quyen dang duoi danh nghia cafe page
```

MVP permission:

```text
allowed if user is cafe_pages.owner_user_id
OR user is ACTIVE page_members with role OWNER or CO_OWNER
```

Khuyen nghi chua cho `MEMBER` dang bai trong MVP.

## Files Expected To Change

- `CafePageValidator.java`
- `BlogServiceImpl.java`
- `BlogCreateDTO.java` neu can validate them
- `BlogServiceImplTest.java`
- `BlogControllerTest.java`

## MVP Algorithm

```java
author = validate active user;

if (request.pageId != null) {
    cafePageValidator.validateUserCanCreateBlogOnPage(
        request.pageId,
        author.userId
    );
}

blog.author = author;
blog.page = cafePage if pageId exists;
save blog;
```

## Acceptance Criteria

- User tao bai ca nhan khong can page permission.
- Owner page tao page blog thanh cong.
- Active co-owner tao page blog thanh cong.
- Member/pending/rejected/unknown user khong tao page blog duoc.
- Response tra `displayAuthorType=CAFE_PAGE` khi co page.

## Tests

- Create personal blog success.
- Create cafe page blog as owner success.
- Create cafe page blog as co-owner success.
- Create cafe page blog without permission returns 403.


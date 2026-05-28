# Phase 01 - Blog Author Display Contract

## Goal

Chuan hoa blog response de backend noi ro bai dang hien thi duoi danh nghia user hay cafe page.

## Business Rule

```text
blog.pageId == null  -> USER blog
blog.pageId != null  -> CAFE_PAGE blog
```

Display rule:

```text
USER:
displayName = author.userFullName ?? author.userName
displayAvatarUrl = author.userAvatar

CAFE_PAGE:
displayName = cafePage.name
displayAvatarUrl = cafePage.avatarUrl
```

Van giu `authorUserId` trong response de audit nguoi that da tao bai.

## Data Model Impact

Khong can bang moi.

Khuyen nghi backend nen map quan he blog -> cafe page:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "page_id")
private CafePage page;
```

Neu chua muon doi entity ngay, co the giu `pageId` va batch-load cafe pages trong service. Tuy nhien cho feed dai han, quan he `Blog -> CafePage` sach hon va tranh query thu cong lap lai.

## DTO Contract

Them enum response-level:

```text
BlogDisplayAuthorType:
- USER
- CAFE_PAGE
```

Them field vao `BlogResponseDTO` va blog feed response dang dung:

```text
authorUserId
authorUserName
authorUserFullName
authorUserAvatar
pageId
pageName
pageAvatarUrl
displayAuthorType
displayName
displayAvatarUrl
```

## Files Expected To Change

- `Blog.java`
- `BlogResponseDTO.java`
- Blog feed response DTO hien co
- `BlogMapper.java`
- `BlogRepository.java` neu can join fetch/entity graph
- Service dang build blog feed response
- Blog service/controller tests

## MVP Algorithm

```java
if (blog.getPage() != null) {
    displayAuthorType = CAFE_PAGE;
    displayName = blog.getPage().getName();
    displayAvatarUrl = blog.getPage().getAvatarUrl();
} else {
    displayAuthorType = USER;
    displayName = firstNonBlank(
        blog.getAuthor().getUserFullName(),
        blog.getAuthor().getUserName()
    );
    displayAvatarUrl = blog.getAuthor().getUserAvatar();
}
```

## Acceptance Criteria

- User blog response co `displayAuthorType=USER`.
- Cafe page blog response co `displayAuthorType=CAFE_PAGE`.
- Cafe page blog hien `displayName` va `displayAvatarUrl` cua page.
- `authorUserId` van co trong response.
- Khong expose JPA entity truc tiep.

## Tests

- Blog mapper/service test cho user blog.
- Blog mapper/service test cho cafe page blog.
- Test page blog van giu author user id.


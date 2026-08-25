# Bao Cao Danh Gia Admin Report AI E2E

- Generated at: 2026-07-21T16:08:35.119Z
- Evidence folder: `E:\LuanVanToTNghiep\cafe-story-platform\documents\report-admin\ai-report-e2e\evidence\2026-07-21T16-04-33-780Z`
- Admin UI: `http://localhost:3636`
- Backend API: `http://localhost:8080`
- n8n health: `http://localhost:5678/healthz`
- n8n report AI webhook: `http://localhost:5678/webhook/cafestory-admin-report-ai-resolution`
- Total result: **85.33%** (128/150 criteria)
- Performance classification: No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold.

## Seed Data

```json
{
  "adminUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
  "targets": {
    "BLOG": {
      "targetType": "BLOG",
      "targetId": "555e9a98-998a-4dde-b02c-df1ce51cd743",
      "reason": {
        "id": "eb501d75-2579-45f5-b179-fec64d1de137",
        "code": "SCAM_FRAUD_OR_SPAM",
        "labelVi": "Lua dao, gian lan hoac spam",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 70,
        "createdAt": "2026-06-11T22:52:43.113463",
        "updatedAt": "2026-06-11T22:52:43.113463"
      },
      "source": {
        "id": "555e9a98-998a-4dde-b02c-df1ce51cd743",
        "authorUserId": "8743dd0c-ef70-4561-9c51-2db6e8b90609",
        "authorUserName": "truong.tha",
        "authorUserFullName": "Trương Trung Thái",
        "authorUserAvatar": "https://api.dicebear.com/9.x/avataaars/svg?seed=truong.tha",
        "pageId": null,
        "pageName": null,
        "pageAvatarUrl": null,
        "regionId": "b0c0ebd4-1999-4162-bfa6-48410247249d",
        "regionCity": null,
        "regionProvince": null,
        "content": "Chia sẻ nhanh: Trương Trung Thái thấy quán này đáng để ghé nhiều lần chứ không phải chỉ 1 lần cho check-in.",
        "imageUrls": [
          "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346623/cafestory/reviewer_blogs/vgc2fyc1n2j1wbccqa7r.jpg",
          "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784346624/cafestory/reviewer_blogs/z0eab3zxdjj4pjbp2cvq.jpg"
        ],
        "status": "PUBLISHED",
        "isPinned": false,
        "allowComment": true,
        "likeCount": 0,
        "shareCount": 0,
        "commentCount": 0,
        "isLike": null,
        "isSave": null,
        "isRating": null,
        "myRating": null,
        "ratingScore": null,
        "ratingCount": null,
        "saveCount": null,
        "taggedUsers": [],
        "displayAuthorType": "USER",
        "displayName": "truong.tha",
        "displayAvatarUrl": "https://api.dicebear.com/9.x/avataaars/svg?seed=truong.tha",
        "isAuthorFollowing": null,
        "isPageFollowing": null,
        "createdAt": "2026-07-18T03:53:49.967048",
        "updatedAt": null
      }
    },
    "USER": {
      "targetType": "USER",
      "targetId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "reason": {
        "id": "eb501d75-2579-45f5-b179-fec64d1de137",
        "code": "SCAM_FRAUD_OR_SPAM",
        "labelVi": "Lua dao, gian lan hoac spam",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 70,
        "createdAt": "2026-06-11T22:52:43.113463",
        "updatedAt": "2026-06-11T22:52:43.113463"
      },
      "source": {
        "userId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
        "userName": "bui.huy",
        "userFullName": "Bùi Tuấn Huy",
        "userEmail": "bu***@gmail.com",
        "userPhone": null,
        "userAvatar": "https://api.dicebear.com/9.x/avataaars/svg?seed=bui.huy",
        "userLike": 0,
        "userFollower": 0,
        "accountStatus": true,
        "regionId": "39ca40a5-b613-4622-b9b3-19d5dbc0704d",
        "roles": [
          "USER",
          "CAFE_PAGE"
        ]
      }
    },
    "CAFE_PAGE": {
      "targetType": "CAFE_PAGE",
      "targetId": "e17dcc33-beae-412a-854d-b6c284b7fa87",
      "reason": {
        "id": "eb501d75-2579-45f5-b179-fec64d1de137",
        "code": "SCAM_FRAUD_OR_SPAM",
        "labelVi": "Lua dao, gian lan hoac spam",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 70,
        "createdAt": "2026-06-11T22:52:43.113463",
        "updatedAt": "2026-06-11T22:52:43.113463"
      },
      "source": {
        "id": "e17dcc33-beae-412a-854d-b6c284b7fa87",
        "ownerUserId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
        "regionId": "84d9a3d8-4331-4438-bcb8-f7f229f7c7cd",
        "regionCityCode": "48",
        "regionCity": "Đà Nẵng",
        "regionProvinceCode": "48",
        "regionProvince": "Thành phố Đà Nẵng",
        "regionWardCode": "20275",
        "regionWard": "Phường An Hải",
        "regionArea": null,
        "regionStreet": "Tầng Trệt, Vincom Center, 910A Ngô Quyền",
        "name": "Highlands Coffee Vincom Đà Nẵng",
        "address": "Tầng Trệt, Vincom Center, 910A Ngô Quyền, Phường An Hải, Đà Nẵng",
        "description": "Chi nhánh Highlands tại tầng trệt Vincom Center Ngô Quyền, không gian rộng máy lạnh, tiện cho khách mua sắm ghé nghỉ chân.",
        "avatarUrl": null,
        "coverUrl": null,
        "status": "ACTIVE",
        "likeCount": 0,
        "followerCount": 0,
        "isFollowing": false,
        "isLiked": false,
        "isRating": false,
        "canManage": null,
        "myRating": null,
        "ratingScore": null,
        "ratingCount": 0,
        "maxMembers": 2,
        "pageActive": true,
        "pageExpiresAt": "2027-01-18T04:18:48.912716",
        "createdAt": "2026-07-18T04:18:48.912716",
        "updatedAt": "2026-07-18T04:18:48.912716"
      }
    }
  },
  "reports": {
    "BLOG": {
      "id": "77b8021d-f01b-4383-b529-a6459aa5ce81",
      "reporterUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
      "reporterUserName": "thinh.dev",
      "reporterUserAvatar": "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784280836/cafestory/avatars/mltw9gcjciktvkcgdepk.jpg",
      "targetType": "BLOG",
      "targetId": "555e9a98-998a-4dde-b02c-df1ce51cd743",
      "blogId": "555e9a98-998a-4dde-b02c-df1ce51cd743",
      "commentId": null,
      "reportedUserId": null,
      "cafePageId": null,
      "reasonId": "eb501d75-2579-45f5-b179-fec64d1de137",
      "reasonCode": "SCAM_FRAUD_OR_SPAM",
      "reason": "Lua dao, gian lan hoac spam",
      "reasonLabel": "Lua dao, gian lan hoac spam",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-01 BLOG 17***42",
      "status": "OPEN",
      "createdAt": "2026-07-21T23:06:02.40***03",
      "resolvedAt": null
    },
    "USER": {
      "id": "befeeac5-1104-4593-838f-91d40cb3d665",
      "reporterUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
      "reporterUserName": "thinh.dev",
      "reporterUserAvatar": "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784280836/cafestory/avatars/mltw9gcjciktvkcgdepk.jpg",
      "targetType": "USER",
      "targetId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "blogId": null,
      "commentId": null,
      "reportedUserId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "cafePageId": null,
      "reasonId": "eb501d75-2579-45f5-b179-fec64d1de137",
      "reasonCode": "SCAM_FRAUD_OR_SPAM",
      "reason": "Lua dao, gian lan hoac spam",
      "reasonLabel": "Lua dao, gian lan hoac spam",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-03 USER 17***45",
      "status": "OPEN",
      "createdAt": "2026-07-21T23:06:09.26***94",
      "resolvedAt": null
    },
    "CAFE_PAGE": {
      "id": "ad50d7b6-904c-4f8f-bd06-bc88ad6e99b2",
      "reporterUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
      "reporterUserName": "thinh.dev",
      "reporterUserAvatar": "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784280836/cafestory/avatars/mltw9gcjciktvkcgdepk.jpg",
      "targetType": "CAFE_PAGE",
      "targetId": "e17dcc33-beae-412a-854d-b6c284b7fa87",
      "blogId": null,
      "commentId": null,
      "reportedUserId": null,
      "cafePageId": "e17dcc33-beae-412a-854d-b6c284b7fa87",
      "reasonId": "eb501d75-2579-45f5-b179-fec64d1de137",
      "reasonCode": "SCAM_FRAUD_OR_SPAM",
      "reason": "Lua dao, gian lan hoac spam",
      "reasonLabel": "Lua dao, gian lan hoac spam",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-04 CAFE_PAGE 17***10",
      "status": "OPEN",
      "createdAt": "2026-07-21T23:06:13.60***51",
      "resolvedAt": null
    },
    "STATUS": {
      "id": "c6466ab7-613d-4477-9f74-9c8b6966b85f",
      "reporterUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
      "reporterUserName": "thinh.dev",
      "reporterUserAvatar": "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784280836/cafestory/avatars/mltw9gcjciktvkcgdepk.jpg",
      "targetType": "USER",
      "targetId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "blogId": null,
      "commentId": null,
      "reportedUserId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "cafePageId": null,
      "reasonId": "eb501d75-2579-45f5-b179-fec64d1de137",
      "reasonCode": "SCAM_FRAUD_OR_SPAM",
      "reason": "Lua dao, gian lan hoac spam",
      "reasonLabel": "Lua dao, gian lan hoac spam",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-20-23 STATUS 17***59",
      "status": "OPEN",
      "createdAt": "2026-07-21T23:07:17.06***85",
      "resolvedAt": null
    },
    "BULK_A": {
      "id": "09b878da-4045-421f-b638-a5b1a01a5c26",
      "reporterUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
      "reporterUserName": "thinh.dev",
      "reporterUserAvatar": "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784280836/cafestory/avatars/mltw9gcjciktvkcgdepk.jpg",
      "targetType": "BLOG",
      "targetId": "555e9a98-998a-4dde-b02c-df1ce51cd743",
      "blogId": "555e9a98-998a-4dde-b02c-df1ce51cd743",
      "commentId": null,
      "reportedUserId": null,
      "cafePageId": null,
      "reasonId": "eb501d75-2579-45f5-b179-fec64d1de137",
      "reasonCode": "SCAM_FRAUD_OR_SPAM",
      "reason": "Lua dao, gian lan hoac spam",
      "reasonLabel": "Lua dao, gian lan hoac spam",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI BULK_A 17***89 808d6b819c0f5",
      "status": "OPEN",
      "createdAt": "2026-07-21T23:07:23.36***15",
      "resolvedAt": null
    },
    "BULK_B": {
      "id": "98f5793b-4155-48b5-877d-5907747081d7",
      "reporterUserId": "efc94458-2220-453f-b581-c7d075cf27cd",
      "reporterUserName": "thinh.dev",
      "reporterUserAvatar": "https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784280836/cafestory/avatars/mltw9gcjciktvkcgdepk.jpg",
      "targetType": "USER",
      "targetId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "blogId": null,
      "commentId": null,
      "reportedUserId": "54fb02ff-8762-4e0a-ab38-e05369d3dbda",
      "cafePageId": null,
      "reasonId": "eb501d75-2579-45f5-b179-fec64d1de137",
      "reasonCode": "SCAM_FRAUD_OR_SPAM",
      "reason": "Lua dao, gian lan hoac spam",
      "reasonLabel": "Lua dao, gian lan hoac spam",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI BULK_B 17***86 223aaa0168f04",
      "status": "OPEN",
      "createdAt": "2026-07-21T23:07:24.76***12",
      "resolvedAt": null
    }
  }
}
```

## Scenario Results

| ID | Status | Score | Duration | Screenshots | Raw | Notes |
|---|---|---:|---:|---|---|---|
| RAI-01 | PASSED | 100% | 6802ms | `screenshots/RAI-01-blog-report-visible.png` | `raw/RAI-01-create-report.json` | - |
| RAI-02 | BLOCKED | 0% | 0ms | - | - | No COMMENT target is available that is safe to report with the admin test account.<br>Fix: Seed at least one COMMENT target not owned by the admin test account. |
| RAI-03 | PASSED | 100% | 4264ms | `screenshots/RAI-03-user-report-visible.png` | `raw/RAI-03-create-report.json` | - |
| RAI-04 | PASSED | 100% | 4465ms | `screenshots/RAI-04-cafe-page-report-visible.png` | `raw/RAI-04-create-report.json` | - |
| RAI-05 | PASSED | 100% | 819ms | - | `raw/RAI-05-duplicate-report.json` | Duplicate create returned HTTP 409. |
| RAI-06 | PASSED | 100% | 3574ms | `screenshots/RAI-06-report-detail.png` | - | - |
| RAI-07 | PASSED | 100% | 15565ms | `screenshots/RAI-07-ai-resolution-blog.png` | `raw/RAI-07-ai-resolution-blog.json` | pass: 14730ms |
| RAI-08 | BLOCKED | 0% | 0ms | - | - | No COMMENT report is available. |
| RAI-09 | PASSED | 100% | 8319ms | `screenshots/RAI-09-ai-resolution-user.png` | `raw/RAI-09-ai-resolution-user.json` | pass: 4270ms |
| RAI-10 | PASSED | 100% | 10232ms | `screenshots/RAI-10-ai-resolution-cafe-page.png` | `raw/RAI-10-ai-resolution-cafe-page.json` | pass: 6072ms |
| RAI-11 | PASSED | 100% | 5057ms | `screenshots/RAI-11-ai-history-refresh.png` | - | - |
| RAI-12 | PASSED | 100% | 0ms | - | `raw/RAI-12-contract-resolution.json` | - |
| RAI-13 | PASSED | 100% | 14730ms | - | - | Max AI duration: pass: 14730ms |
| RAI-14 | PASSED | 100% | 8285ms | `screenshots/RAI-14-auto-apply-ui.png` | `raw/RAI-14-auto-apply-resolution.json` | AI risk score is below the auto resolve threshold. |
| RAI-15 | BLOCKED | 0% | 0ms | `screenshots/RAI-14-auto-apply-ui.png` | `raw/RAI-14-auto-apply-resolution.json` | No scheduled job exists, so countdown cannot be verified. |
| RAI-16 | BLOCKED | 0% | 0ms | - | - | No scheduled job exists, so cancel behavior cannot be verified. |
| RAI-17 | PASSED | 100% | 0ms | - | `raw/RAI-17-auto-apply-safety.json` | AI risk score is below the auto resolve threshold. |
| RAI-18 | PASSED | 100% | 3891ms | - | `raw/RAI-18-replacement-auto-apply.json` | AI recommendation needs manual review; auto apply was not scheduled. |
| RAI-19 | PASSED | 100% | 537ms | - | `raw/RAI-19-auto-job-history.json` | - |
| RAI-20 | PASSED | 100% | 677ms | - | `raw/RAI-20-status-report-created.json` | - |
| RAI-21 | PASSED | 100% | 704ms | - | - | - |
| RAI-22 | PASSED | 100% | 544ms | - | - | - |
| RAI-23 | PASSED | 100% | 721ms | - | - | - |
| RAI-24 | PASSED | 100% | 1510ms | `screenshots/RAI-24-bulk-dialog-selected.png` | - | - |
| RAI-25 | WARN | 80% | 26741ms | `screenshots/RAI-25-bulk-run-selected.png` | - | pass: 26743ms |
| RAI-26 | PASSED | 100% | 0ms | `screenshots/RAI-26-bulk-auto-confirm-required.png` | - | - |
| RAI-27 | WARN | 80% | 26100ms | `screenshots/RAI-27-bulk-auto-run.png` | - | - |
| RAI-28 | PASSED | 100% | 0ms | - | - | - |
| RAI-29 | PASSED | 100% | 0ms | - | - | No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold. |
| RAI-30 | PASSED | 100% | 10446ms | - | `raw/RAI-30-cleanup-verification.json` | Closed report 77b8021d-f01b-4383-b529-a6459aa5ce81<br>Closed report befeeac5-1104-4593-838f-91d40cb3d665<br>Closed report ad50d7b6-904c-4f8f-bd06-bc88ad6e99b2<br>Closed report c6466ab7-613d-4477-9f74-9c8b6966b85f<br>Closed report 09b878da-4045-421f-b638-a5b1a01a5c26<br>Closed report 98f5793b-4155-48b5-877d-5907747081d7 |

## Fix Guidance

- RAI-02: Seed at least one COMMENT target not owned by the admin test account.

## Cleanup Verification

- The runner cancels SCHEDULED auto apply jobs in finally.
- The runner closes created reports by setting final status to REJECTED unless a scenario already closed it.
- The runner does not delete production rows and does not wait for real auto-apply execution.

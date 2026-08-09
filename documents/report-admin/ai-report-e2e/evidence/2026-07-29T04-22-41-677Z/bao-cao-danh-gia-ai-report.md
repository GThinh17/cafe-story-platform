# Bao Cao Danh Gia Admin Report AI E2E

- Generated at: 2026-07-29T04:23:33.369Z
- Evidence folder: `E:\LuanVanToTNghiep\cafe-story-platform\documents\report-admin\ai-report-e2e\evidence\2026-07-29T04-22-41-677Z`
- Admin UI: `http://localhost:3636`
- Backend API: `http://localhost:8080`
- n8n health: `http://localhost:5678/healthz`
- n8n report AI webhook: `http://localhost:5678/webhook/cafestory-admin-report-ai-resolution`
- Total result: **96.67%** (145/150 criteria)
- Performance classification: No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold.

## Seed Data

```json
{
  "adminUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
  "targets": {
    "BLOG": {
      "targetType": "BLOG",
      "targetId": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
      "reason": {
        "id": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
        "code": "RESTRICTED_GOODS",
        "labelVi": "Bán hoặc quảng bá mặt hàng bị hạn chế",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 50,
        "createdAt": "2026-07-29T11:08:31.54019",
        "updatedAt": "2026-07-29T11:08:31.54019"
      },
      "source": {
        "id": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
        "authorUserId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
        "authorUserName": "g012e_target",
        "authorUserFullName": "G0 12E Target",
        "authorUserAvatar": null,
        "pageId": null,
        "pageName": null,
        "pageAvatarUrl": null,
        "regionId": "11***11-1111-4111-8111-11***12",
        "regionCity": null,
        "regionProvince": null,
        "content": "G0-12E synthetic published blog evidence fixture",
        "imageUrls": [],
        "status": "PUBLISHED",
        "isPinned": false,
        "allowComment": true,
        "likeCount": 0,
        "shareCount": 0,
        "commentCount": 1,
        "isLike": null,
        "isSave": null,
        "isRating": null,
        "myRating": null,
        "ratingScore": null,
        "ratingCount": null,
        "saveCount": null,
        "taggedUsers": [],
        "displayAuthorType": "USER",
        "displayName": "g012e_target",
        "displayAvatarUrl": null,
        "isAuthorFollowing": null,
        "isPageFollowing": null,
        "createdAt": "2026-07-29T11:11:19.30612",
        "updatedAt": "2026-07-29T11:11:19.4764"
      }
    },
    "COMMENT": {
      "targetType": "COMMENT",
      "targetId": "c158cd15-5584-4852-9095-32e9d2012b0d",
      "reason": {
        "id": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
        "code": "RESTRICTED_GOODS",
        "labelVi": "Bán hoặc quảng bá mặt hàng bị hạn chế",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 50,
        "createdAt": "2026-07-29T11:08:31.54019",
        "updatedAt": "2026-07-29T11:08:31.54019"
      },
      "source": {
        "id": "c158cd15-5584-4852-9095-32e9d2012b0d",
        "blogId": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
        "userId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
        "authorUserName": "g012e_target",
        "authorUserAvatar": null,
        "actorContextType": "USER",
        "actorCafePageId": null,
        "actorDisplayName": "G0 12E Target",
        "actorAvatarUrl": null,
        "parentCommentId": null,
        "content": "G0-12E synthetic published comment evidence fixture",
        "imageUrls": [],
        "status": "PUBLISHED",
        "createdAt": "2026-07-29T11:11:19.473713",
        "updatedAt": null
      }
    },
    "USER": {
      "targetType": "USER",
      "targetId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
      "reason": {
        "id": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
        "code": "RESTRICTED_GOODS",
        "labelVi": "Bán hoặc quảng bá mặt hàng bị hạn chế",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 50,
        "createdAt": "2026-07-29T11:08:31.54019",
        "updatedAt": "2026-07-29T11:08:31.54019"
      },
      "source": {
        "userId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
        "userName": "g012e_target",
        "userFullName": "G0 12E Target",
        "userEmail": "g0***@example.invalid",
        "userPhone": null,
        "userAvatar": null,
        "userLike": 0,
        "userFollower": 0,
        "accountStatus": true,
        "regionId": null,
        "roles": [
          "USER"
        ]
      }
    },
    "CAFE_PAGE": {
      "targetType": "CAFE_PAGE",
      "targetId": "7ca0d5a8-ef9c-4703-87ab-547d10b7aac9",
      "reason": {
        "id": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
        "code": "RESTRICTED_GOODS",
        "labelVi": "Bán hoặc quảng bá mặt hàng bị hạn chế",
        "descriptionVi": null,
        "targetType": null,
        "severity": 4,
        "requiresDescription": false,
        "isActive": true,
        "sortOrder": 50,
        "createdAt": "2026-07-29T11:08:31.54019",
        "updatedAt": "2026-07-29T11:08:31.54019"
      },
      "source": {
        "id": "7ca0d5a8-ef9c-4703-87ab-547d10b7aac9",
        "ownerUserId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
        "regionId": null,
        "regionCityCode": null,
        "regionCity": null,
        "regionProvinceCode": null,
        "regionProvince": null,
        "regionWardCode": null,
        "regionWard": null,
        "regionArea": null,
        "regionStreet": null,
        "name": "G0-12E Synthetic Cafe",
        "address": "Local disposable database",
        "description": "Synthetic E2E fixture",
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
        "pageExpiresAt": null,
        "createdAt": "2026-07-29T11:10:05.113849",
        "updatedAt": "2026-07-29T11:10:05.513526"
      }
    }
  },
  "reports": {
    "BLOG": {
      "id": "1d503577-900c-4399-b83e-87236077d8dc",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "BLOG",
      "targetId": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
      "blogId": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
      "commentId": null,
      "reportedUserId": null,
      "cafePageId": null,
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-01 BLOG 17***21",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:22:48.88***28",
      "resolvedAt": null
    },
    "COMMENT": {
      "id": "769de633-0354-4d65-9e74-234ced29b3c1",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "COMMENT",
      "targetId": "c158cd15-5584-4852-9095-32e9d2012b0d",
      "blogId": null,
      "commentId": "c158cd15-5584-4852-9095-32e9d2012b0d",
      "reportedUserId": null,
      "cafePageId": null,
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-02 COMMENT 17***30",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:22:50.19***06",
      "resolvedAt": null
    },
    "USER": {
      "id": "56f5d9c7-be12-4379-8cab-1f3cbeb1f1b5",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "USER",
      "targetId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
      "blogId": null,
      "commentId": null,
      "reportedUserId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
      "cafePageId": null,
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-03 USER 17***09",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:22:51.48***73",
      "resolvedAt": null
    },
    "CAFE_PAGE": {
      "id": "8f07d582-c448-48c7-bf13-0f1e0e08ee26",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "CAFE_PAGE",
      "targetId": "7ca0d5a8-ef9c-4703-87ab-547d10b7aac9",
      "blogId": null,
      "commentId": null,
      "reportedUserId": null,
      "cafePageId": "7ca0d5a8-ef9c-4703-87ab-547d10b7aac9",
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-04 CAFE_PAGE 17***88",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:22:52.74***61",
      "resolvedAt": null
    },
    "STATUS": {
      "id": "56fcc963-3f83-4e6a-8ef8-0e3da109c25d",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "USER",
      "targetId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
      "blogId": null,
      "commentId": null,
      "reportedUserId": "b99f66c0-06bf-4769-b819-ed918a4fa0d0",
      "cafePageId": null,
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI RAI-20-23 STATUS 17***38",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:23:18.18***13",
      "resolvedAt": null
    },
    "BULK_A": {
      "id": "709a1091-dcb7-4e17-bafa-d2033767172d",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "BLOG",
      "targetId": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
      "blogId": "4f04a3f0-e3c5-492b-a65a-b387f1c1a6ec",
      "commentId": null,
      "reportedUserId": null,
      "cafePageId": null,
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI BULK_A 17***19 5176b3f55ace8",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:23:18.51***97",
      "resolvedAt": null
    },
    "BULK_B": {
      "id": "bd1dca9c-fb40-4d25-aa51-dabbb602d9b9",
      "reporterUserId": "a7150bf3-3312-422c-b028-08f05362eb83",
      "reporterUserName": "g012e_admin",
      "reporterUserAvatar": null,
      "targetType": "COMMENT",
      "targetId": "c158cd15-5584-4852-9095-32e9d2012b0d",
      "blogId": null,
      "commentId": "c158cd15-5584-4852-9095-32e9d2012b0d",
      "reportedUserId": null,
      "cafePageId": null,
      "reasonId": "d5ddb2c9-9200-461a-9a11-92b8fa0215a7",
      "reasonCode": "RESTRICTED_GOODS",
      "reason": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonLabel": "Bán hoặc quảng bá mặt hàng bị hạn chế",
      "reasonSeverity": 4,
      "description": "E2E Report Admin AI BULK_B 17***52 f0b1bbd1c4adb",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:23:18.72***19",
      "resolvedAt": null
    }
  }
}
```

## Scenario Results

| ID | Status | Score | Duration | Screenshots | Raw | Notes |
|---|---|---:|---:|---|---|---|
| RAI-01 | PASSED | 100% | 1306ms | `screenshots/RAI-01-blog-report-visible.png` | `raw/RAI-01-create-report.json` | - |
| RAI-02 | PASSED | 100% | 1279ms | `screenshots/RAI-02-comment-report-visible.png` | `raw/RAI-02-create-report.json` | - |
| RAI-03 | PASSED | 100% | 1279ms | `screenshots/RAI-03-user-report-visible.png` | `raw/RAI-03-create-report.json` | - |
| RAI-04 | PASSED | 100% | 1126ms | `screenshots/RAI-04-cafe-page-report-visible.png` | `raw/RAI-04-create-report.json` | - |
| RAI-05 | PASSED | 100% | 90ms | - | `raw/RAI-05-duplicate-report.json` | Duplicate create returned HTTP 409. |
| RAI-06 | PASSED | 100% | 1326ms | `screenshots/RAI-06-report-detail.png` | - | - |
| RAI-07 | PASSED | 100% | 8253ms | `screenshots/RAI-07-ai-resolution-blog.png` | `raw/RAI-07-ai-resolution-blog.json`<br>`raw/RAI-07-target-no-mutation-blog.json` | pass: 7698ms<br>Target unchanged: true. |
| RAI-08 | PASSED | 100% | 6162ms | `screenshots/RAI-08-ai-resolution-comment.png` | `raw/RAI-08-ai-resolution-comment.json`<br>`raw/RAI-08-target-no-mutation-comment.json` | pass: 4760ms<br>Target unchanged: true. |
| RAI-09 | PASSED | 100% | 1570ms | `screenshots/RAI-09-ai-resolution-user.png` | `raw/RAI-09-ai-resolution-user.json`<br>`raw/RAI-09-target-no-mutation-user.json` | pass: 59ms<br>Target unchanged: true. |
| RAI-10 | PASSED | 100% | 1452ms | `screenshots/RAI-10-ai-resolution-cafe-page.png` | `raw/RAI-10-ai-resolution-cafe-page.json`<br>`raw/RAI-10-target-no-mutation-cafe-page.json` | pass: 94ms<br>Target unchanged: true. |
| RAI-11 | PASSED | 100% | 1661ms | `screenshots/RAI-11-ai-history-refresh.png` | - | - |
| RAI-12 | PASSED | 100% | 0ms | - | `raw/RAI-12-contract-resolution.json` | - |
| RAI-13 | PASSED | 100% | 7698ms | - | - | Max AI duration: pass: 7698ms |
| RAI-14 | PASSED | 100% | 1784ms | `screenshots/RAI-14-a0-recommendation-only-ui.png` | - | - |
| RAI-15 | PASSED | 100% | 80ms | `screenshots/RAI-15-a0-blocked-auto-apply-ui.png` | `raw/RAI-15-a0-blocked-auto-apply.json` | Automation mode A0_RECOMMEND_ONLY is active; AI recommendations require an admin decision. |
| RAI-16 | BLOCKED | 0% | 0ms | - | - | No pre-existing SCHEDULED legacy job fixture exists; cancel endpoint remains covered by Backend tests. |
| RAI-17 | PASSED | 100% | 0ms | - | `raw/RAI-17-a0-safety-invariant.json` | - |
| RAI-18 | PASSED | 100% | 44ms | - | `raw/RAI-18-repeat-a0-request.json` | - |
| RAI-19 | PASSED | 100% | 0ms | - | `raw/RAI-19-legacy-auto-job-history.json` | - |
| RAI-20 | PASSED | 100% | 29ms | - | `raw/RAI-20-status-report-created.json` | - |
| RAI-21 | PASSED | 100% | 24ms | - | - | - |
| RAI-22 | PASSED | 100% | 34ms | - | - | - |
| RAI-23 | PASSED | 100% | 28ms | - | - | - |
| RAI-24 | PASSED | 100% | 635ms | `screenshots/RAI-24-bulk-dialog-selected.png` | - | - |
| RAI-25 | PASSED | 100% | 10515ms | `screenshots/RAI-25-bulk-run-selected.png` | - | pass: 10515ms |
| RAI-26 | PASSED | 100% | 0ms | `screenshots/RAI-26-bulk-a0-notice.png` | - | - |
| RAI-27 | PASSED | 100% | 1485ms | `screenshots/RAI-27-bulk-recommendation-only.png` | - | - |
| RAI-28 | PASSED | 100% | 0ms | - | - | - |
| RAI-29 | PASSED | 100% | 0ms | - | - | No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold. |
| RAI-30 | PASSED | 100% | 408ms | - | `raw/RAI-30-cleanup-verification.json` | Closed report 1d503577-900c-4399-b83e-87236077d8dc<br>Closed report 769de633-0354-4d65-9e74-234ced29b3c1<br>Closed report 56f5d9c7-be12-4379-8cab-1f3cbeb1f1b5<br>Closed report 8f07d582-c448-48c7-bf13-0f1e0e08ee26<br>Closed report 56fcc963-3f83-4e6a-8ef8-0e3da109c25d<br>Closed report 709a1091-dcb7-4e17-bafa-d2033767172d<br>Closed report bd1dca9c-fb40-4d25-aa51-dabbb602d9b9 |

## Fix Guidance

- No fix guidance generated.

## Cleanup Verification

- The runner cancels SCHEDULED auto apply jobs in finally.
- The runner closes created reports by setting final status to REJECTED unless a scenario already closed it.
- The runner does not delete production rows and does not wait for real auto-apply execution.

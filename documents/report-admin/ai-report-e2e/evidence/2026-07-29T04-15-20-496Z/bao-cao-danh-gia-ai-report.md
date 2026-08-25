# Bao Cao Danh Gia Admin Report AI E2E

- Generated at: 2026-07-29T04:16:13.355Z
- Evidence folder: `E:\LuanVanToTNghiep\cafe-story-platform\documents\report-admin\ai-report-e2e\evidence\2026-07-29T04-15-20-496Z`
- Admin UI: `http://localhost:3636`
- Backend API: `http://localhost:8080`
- n8n health: `http://localhost:5678/healthz`
- n8n report AI webhook: `http://localhost:5678/webhook/cafestory-admin-report-ai-resolution`
- Total result: **96%** (144/150 criteria)
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
      "id": "fc476a7b-de29-49e7-a5de-148ce766af51",
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
      "description": "E2E Report Admin AI RAI-01 BLOG 17***28",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:26.88***52",
      "resolvedAt": null
    },
    "COMMENT": {
      "id": "98f8cd57-9f14-4f80-a013-842cb7d2a6e4",
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
      "description": "E2E Report Admin AI RAI-02 COMMENT 17***11",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:27.84***24",
      "resolvedAt": null
    },
    "USER": {
      "id": "f0f94f21-59cf-4c92-a9e9-a1679b9ee506",
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
      "description": "E2E Report Admin AI RAI-03 USER 17***63",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:28.59***57",
      "resolvedAt": null
    },
    "CAFE_PAGE": {
      "id": "008c54b6-8396-4d73-98b4-0d98fb2a941f",
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
      "description": "E2E Report Admin AI RAI-04 CAFE_PAGE 17***60",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:29.28***47",
      "resolvedAt": null
    },
    "STATUS": {
      "id": "1a906352-6763-4ea8-918a-629be5af9a40",
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
      "description": "E2E Report Admin AI RAI-20-23 STATUS 17***71",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:55.62***44",
      "resolvedAt": null
    },
    "BULK_A": {
      "id": "97d17287-0f5f-4ac9-ba9a-9ea79ad00fcc",
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
      "description": "E2E Report Admin AI BULK_A 17***57 a5ee6c7596002",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:55.89***16",
      "resolvedAt": null
    },
    "BULK_B": {
      "id": "fd326fe5-f20c-4615-9489-5b17ae94b08c",
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
      "description": "E2E Report Admin AI BULK_B 17***33 3e699378696d2",
      "status": "OPEN",
      "createdAt": "2026-07-29T11:15:56.08***48",
      "resolvedAt": null
    }
  }
}
```

## Scenario Results

| ID | Status | Score | Duration | Screenshots | Raw | Notes |
|---|---|---:|---:|---|---|---|
| RAI-01 | PASSED | 100% | 981ms | `screenshots/RAI-01-blog-report-visible.png` | `raw/RAI-01-create-report.json` | - |
| RAI-02 | PASSED | 100% | 752ms | `screenshots/RAI-02-comment-report-visible.png` | `raw/RAI-02-create-report.json` | - |
| RAI-03 | PASSED | 100% | 697ms | `screenshots/RAI-03-user-report-visible.png` | `raw/RAI-03-create-report.json` | - |
| RAI-04 | PASSED | 100% | 806ms | `screenshots/RAI-04-cafe-page-report-visible.png` | `raw/RAI-04-create-report.json` | - |
| RAI-05 | PASSED | 100% | 127ms | - | `raw/RAI-05-duplicate-report.json` | Duplicate create returned HTTP 409. |
| RAI-06 | PASSED | 100% | 1164ms | `screenshots/RAI-06-report-detail.png` | - | - |
| RAI-07 | PASSED | 100% | 7752ms | `screenshots/RAI-07-ai-resolution-blog.png` | `raw/RAI-07-ai-resolution-blog.json` | pass: 7256ms |
| RAI-08 | PASSED | 100% | 7845ms | `screenshots/RAI-08-ai-resolution-comment.png` | `raw/RAI-08-ai-resolution-comment.json` | pass: 6598ms |
| RAI-09 | PASSED | 100% | 1253ms | `screenshots/RAI-09-ai-resolution-user.png` | `raw/RAI-09-ai-resolution-user.json` | pass: 55ms |
| RAI-10 | PASSED | 100% | 1943ms | `screenshots/RAI-10-ai-resolution-cafe-page.png` | `raw/RAI-10-ai-resolution-cafe-page.json` | pass: 61ms |
| RAI-11 | PASSED | 100% | 1526ms | `screenshots/RAI-11-ai-history-refresh.png` | - | - |
| RAI-12 | PASSED | 100% | 0ms | - | `raw/RAI-12-contract-resolution.json` | - |
| RAI-13 | PASSED | 100% | 7256ms | - | - | Max AI duration: pass: 7256ms |
| RAI-14 | PASSED | 100% | 1719ms | `screenshots/RAI-14-a0-recommendation-only-ui.png` | - | - |
| RAI-15 | WARN | 80% | 430ms | `screenshots/RAI-15-a0-blocked-auto-apply-ui.png` | `raw/RAI-15-a0-blocked-auto-apply.json` | No A0 warning returned. |
| RAI-16 | BLOCKED | 0% | 0ms | - | - | No pre-existing SCHEDULED legacy job fixture exists; cancel endpoint remains covered by Backend tests. |
| RAI-17 | PASSED | 100% | 0ms | - | `raw/RAI-17-a0-safety-invariant.json` | - |
| RAI-18 | PASSED | 100% | 36ms | - | `raw/RAI-18-repeat-a0-request.json` | - |
| RAI-19 | PASSED | 100% | 0ms | - | `raw/RAI-19-legacy-auto-job-history.json` | - |
| RAI-20 | PASSED | 100% | 31ms | - | `raw/RAI-20-status-report-created.json` | - |
| RAI-21 | PASSED | 100% | 23ms | - | - | - |
| RAI-22 | PASSED | 100% | 25ms | - | - | - |
| RAI-23 | PASSED | 100% | 26ms | - | - | - |
| RAI-24 | PASSED | 100% | 639ms | `screenshots/RAI-24-bulk-dialog-selected.png` | - | - |
| RAI-25 | PASSED | 100% | 11465ms | `screenshots/RAI-25-bulk-run-selected.png` | - | pass: 11465ms |
| RAI-26 | PASSED | 100% | 0ms | `screenshots/RAI-26-bulk-a0-notice.png` | - | - |
| RAI-27 | PASSED | 100% | 1736ms | `screenshots/RAI-27-bulk-recommendation-only.png` | - | - |
| RAI-28 | PASSED | 100% | 0ms | - | - | - |
| RAI-29 | PASSED | 100% | 0ms | - | - | No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold. |
| RAI-30 | PASSED | 100% | 506ms | - | `raw/RAI-30-cleanup-verification.json` | Closed report fc476a7b-de29-49e7-a5de-148ce766af51<br>Closed report 98f8cd57-9f14-4f80-a013-842cb7d2a6e4<br>Closed report f0f94f21-59cf-4c92-a9e9-a1679b9ee506<br>Closed report 008c54b6-8396-4d73-98b4-0d98fb2a941f<br>Closed report 1a906352-6763-4ea8-918a-629be5af9a40<br>Closed report 97d17287-0f5f-4ac9-ba9a-9ea79ad00fcc<br>Closed report fd326fe5-f20c-4615-9489-5b17ae94b08c |

## Fix Guidance

- No fix guidance generated.

## Cleanup Verification

- The runner cancels SCHEDULED auto apply jobs in finally.
- The runner closes created reports by setting final status to REJECTED unless a scenario already closed it.
- The runner does not delete production rows and does not wait for real auto-apply execution.

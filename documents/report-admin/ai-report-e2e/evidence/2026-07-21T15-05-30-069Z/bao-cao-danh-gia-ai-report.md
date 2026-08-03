# Bao Cao Danh Gia Admin Report AI E2E

- Generated at: 2026-07-21T15:07:48.709Z
- Evidence folder: `E:\LuanVanToTNghiep\cafe-story-platform\documents\report-admin\ai-report-e2e\evidence\2026-07-21T15-05-30-069Z`
- Admin UI: `http://localhost:3636`
- Backend API: `http://localhost:8080`
- n8n health: `http://localhost:5678/healthz`
- n8n report AI webhook: `http://localhost:5678/webhook/cafestory-admin-report-ai-resolution`
- Total result: **86%** (129/150 criteria)
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
      "id": "aee95da9-d56c-4789-8e00-f7d15275ea2f",
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
      "description": "E2E Report Admin AI RAI-01 BLOG 17***02",
      "status": "OPEN",
      "createdAt": "2026-07-21T22:06:34.49***66",
      "resolvedAt": null
    },
    "USER": {
      "id": "a9602e63-1800-49bd-98c8-75a22fd78627",
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
      "description": "E2E Report Admin AI RAI-03 USER 17***89",
      "status": "OPEN",
      "createdAt": "2026-07-21T22:06:38.488133",
      "resolvedAt": null
    },
    "CAFE_PAGE": {
      "id": "f3633c5f-8d0c-4f65-bf37-89c1c16c7a1e",
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
      "description": "E2E Report Admin AI RAI-04 CAFE_PAGE 17***21",
      "status": "OPEN",
      "createdAt": "2026-07-21T22:06:41.74***02",
      "resolvedAt": null
    },
    "STATUS": {
      "id": "dd66d9a2-5206-4983-9947-53946bee8925",
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
      "description": "E2E Report Admin AI RAI-20-23 STATUS 17***49",
      "status": "OPEN",
      "createdAt": "2026-07-21T22:07:10.96***73",
      "resolvedAt": null
    },
    "BULK_A": {
      "id": "41ff5e8e-6df0-4304-bb97-35dd01f73976",
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
      "description": "E2E Report Admin AI BULK_A 17***81 e54e1c617ab1e",
      "status": "OPEN",
      "createdAt": "2026-07-21T22:07:17.00***73",
      "resolvedAt": null
    },
    "BULK_B": {
      "id": "7b922f7f-f5de-49f0-be9d-a9b6814ee17f",
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
      "description": "E2E Report Admin AI BULK_B 17***95 2faea96f2acee",
      "status": "OPEN",
      "createdAt": "2026-07-21T22:07:18.257461",
      "resolvedAt": null
    }
  }
}
```

## Scenario Results

| ID | Status | Score | Duration | Screenshots | Raw | Notes |
|---|---|---:|---:|---|---|---|
| RAI-01 | PASSED | 100% | 3886ms | `screenshots/RAI-01-blog-report-visible.png` | `raw/RAI-01-create-report.json` | - |
| RAI-02 | BLOCKED | 0% | 0ms | - | - | No COMMENT target is available that is safe to report with the admin test account.<br>Fix: Seed at least one COMMENT target not owned by the admin test account. |
| RAI-03 | PASSED | 100% | 3331ms | `screenshots/RAI-03-user-report-visible.png` | `raw/RAI-03-create-report.json` | - |
| RAI-04 | PASSED | 100% | 3214ms | `screenshots/RAI-04-cafe-page-report-visible.png` | `raw/RAI-04-create-report.json` | - |
| RAI-05 | PASSED | 100% | 593ms | - | `raw/RAI-05-duplicate-report.json` | Duplicate create returned HTTP 409. |
| RAI-06 | PASSED | 100% | 2588ms | `screenshots/RAI-06-report-detail.png` | - | - |
| RAI-07 | WARN | 80% | 4794ms | `screenshots/RAI-07-ai-resolution-blog.png` | `raw/RAI-07-ai-resolution-blog.json` | pass: 4540ms<br>Fix: POST /api/admin/reports/{reportId}/ai-resolution returned 502; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n. |
| RAI-08 | BLOCKED | 0% | 0ms | - | - | No COMMENT report is available. |
| RAI-09 | WARN | 80% | 3356ms | `screenshots/RAI-09-ai-resolution-user.png` | `raw/RAI-09-ai-resolution-user.json` | pass: 759ms<br>Fix: POST /api/admin/reports/{reportId}/ai-resolution returned 502; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n. |
| RAI-10 | WARN | 80% | 3567ms | `screenshots/RAI-10-ai-resolution-cafe-page.png` | `raw/RAI-10-ai-resolution-cafe-page.json` | pass: 775ms<br>Fix: POST /api/admin/reports/{reportId}/ai-resolution returned 502; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n. |
| RAI-11 | WARN | 80% | 3665ms | `screenshots/RAI-11-ai-history-refresh.png` | - | - |
| RAI-12 | FAILED | 60% | 0ms | - | `raw/RAI-07-ai-resolution-blog.json`<br>`raw/RAI-09-ai-resolution-user.json`<br>`raw/RAI-10-ai-resolution-cafe-page.json` | Fix: No valid AI recommendation contract was returned; check n8n workflow publication, ADMIN_REPORT_AI_WEBHOOK_URL, OpenAI credentials, and AdminReportAiResolutionWebhookResponseDTO mapping. |
| RAI-13 | PASSED | 100% | 4540ms | - | - | Max AI duration: pass: 4540ms |
| RAI-14 | WARN | 80% | 3632ms | `screenshots/RAI-14-auto-apply-ui.png` | `raw/RAI-14-auto-apply-resolution.json` | No job returned.<br>Fix: POST /api/admin/reports/{reportId}/ai-resolution auto apply returned 502; fix admin report AI service before validating auto-apply scheduling. |
| RAI-15 | PASSED | 100% | 925ms | `screenshots/RAI-14-auto-apply-ui.png` | `raw/RAI-14-auto-apply-resolution.json` | No SCHEDULED job; AI returned safe warning or did not meet safety gate. |
| RAI-16 | WARN | 100% | 0ms | - | - | No scheduled job was created; cancel path is conditionally skipped. |
| RAI-17 | FAILED | 40% | 0ms | - | - | No auto apply response.<br>Fix: Auto apply safety gate cannot be evaluated until admin report AI recommendation endpoint returns a valid response. |
| RAI-18 | WARN | 80% | 768ms | - | `raw/RAI-18-replacement-auto-apply.json` | No replacement job created. |
| RAI-19 | PASSED | 100% | 447ms | - | `raw/RAI-19-auto-job-history.json` | - |
| RAI-20 | PASSED | 100% | 632ms | - | `raw/RAI-20-status-report-created.json` | - |
| RAI-21 | PASSED | 100% | 667ms | - | - | - |
| RAI-22 | PASSED | 100% | 628ms | - | - | - |
| RAI-23 | PASSED | 100% | 659ms | - | - | - |
| RAI-24 | PASSED | 100% | 475ms | `screenshots/RAI-24-bulk-dialog-selected.png` | - | - |
| RAI-25 | PASSED | 100% | 8407ms | `screenshots/RAI-25-bulk-run-selected.png` | - | pass: 8407ms |
| RAI-26 | PASSED | 100% | 0ms | `screenshots/RAI-26-bulk-auto-confirm-required.png` | - | - |
| RAI-27 | PASSED | 100% | 8578ms | `screenshots/RAI-27-bulk-auto-run.png` | - | - |
| RAI-28 | PASSED | 100% | 0ms | - | - | - |
| RAI-29 | PASSED | 100% | 0ms | - | - | No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold. |
| RAI-30 | PASSED | 100% | 9243ms | - | `raw/RAI-30-cleanup-verification.json` | Closed report aee95da9-d56c-4789-8e00-f7d15275ea2f<br>Closed report a9602e63-1800-49bd-98c8-75a22fd78627<br>Closed report f3633c5f-8d0c-4f65-bf37-89c1c16c7a1e<br>Closed report dd66d9a2-5206-4983-9947-53946bee8925<br>Closed report 41ff5e8e-6df0-4304-bb97-35dd01f73976<br>Closed report 7b922f7f-f5de-49f0-be9d-a9b6814ee17f |

## Fix Guidance

- Preflight: n8n report AI webhook is not active or published at http://localhost:5678/webhook/cafestory-admin-report-ai-resolution. Republish/activate existing workflow cafestory-admin-report-ai-resolution; do not create a new workflow.
- RAI-02: Seed at least one COMMENT target not owned by the admin test account.
- RAI-07: POST /api/admin/reports/{reportId}/ai-resolution returned 502; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n.
- RAI-09: POST /api/admin/reports/{reportId}/ai-resolution returned 502; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n.
- RAI-10: POST /api/admin/reports/{reportId}/ai-resolution returned 502; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n.
- RAI-12: No valid AI recommendation contract was returned; check n8n workflow publication, ADMIN_REPORT_AI_WEBHOOK_URL, OpenAI credentials, and AdminReportAiResolutionWebhookResponseDTO mapping.
- RAI-14: POST /api/admin/reports/{reportId}/ai-resolution auto apply returned 502; fix admin report AI service before validating auto-apply scheduling.
- RAI-17: Auto apply safety gate cannot be evaluated until admin report AI recommendation endpoint returns a valid response.

## Cleanup Verification

- The runner cancels SCHEDULED auto apply jobs in finally.
- The runner closes created reports by setting final status to REJECTED unless a scenario already closed it.
- The runner does not delete production rows and does not wait for real auto-apply execution.

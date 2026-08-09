# Issue Register — G0-06C

| ID | Classification | Mức | Evidence | Owner/proposed action | Auto-fix |
|---|---|---|---|---|---|
| `ISS-G0-06C-01` | `TEST_DATA` | Cao | 22 DB reasons vs 9 source reasons | Policy/data governance; reconcile sau approval | Không |
| `ISS-G0-06C-02` | `TEST_DATA` | Cao | `FALSE_INFORMATION`, `RESTRICTED_GOODS` field drift | Chốt canonical catalog trước migration/data repair | Không |
| `ISS-G0-06C-03` | `TEST_DATA` | Trung bình | Bảy label cũ không dấu | Chuẩn hóa label sau business approval | Không |
| `ISS-G0-06C-04` | `CODE_BUG` | Cao | 16/17 RESOLVE có risk dưới BE legacy threshold | Redesign score contract; không fix trước policy gate | Không trong G0 |
| `ISS-G0-06C-05` | `CODE_BUG` | Cao | Thiếu evidence/version/correlation columns | Thiết kế canonical contract/migration sau G0 | Không trong G0 |
| `ISS-G0-06C-06` | `TEST_DATA` | Cao | Không COMMENT/RESOLVED, chỉ dùng 2/22 reasons | Chuẩn bị safe fixture/evaluation dataset sau design | Không |
| `ISS-G0-06C-07` | `CODE_BUG` | Trung bình | AI score/decision không được DB constrain | Chốt validation ownership tại policy/DD | Không trong G0 |
| `ISS-G0-06C-08` | `TEST_DATA` | Trung bình | 14 free-form rule codes/23 rows | Rule catalog/version migration cần approval | Không |

Không auto-fix vì gate chỉ audit và mọi sửa catalog/schema/score cần policy/business decision trước.

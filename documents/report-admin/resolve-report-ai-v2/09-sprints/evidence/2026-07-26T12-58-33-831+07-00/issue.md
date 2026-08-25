# Issue Register — G0-12B

## Bound

- Gate: `G0-12B`.
- Scope: changed production Java classes có business/security logic trong Sprint 1.
- Hard gate: `100%` line và `>=85%` branch cho từng class trong scope.
- Ngoài phạm vi: migration/runtime (`G0-12C`), n8n/provider runtime (`G0-12D`), Admin UI/E2E (`G0-12E`).

## Baseline issues

| ID | Phân loại | Mức độ | Trạng thái ban đầu | Evidence |
|---|---|---|---|---|
| `G012B-COV-001` | `TEST_BUG` | High | `FIXED_VERIFIED` | `AdminReportAiAutoApplyJobServiceImpl`: từ `100% / 76.67%` lên `100% / 100%` |
| `G012B-COV-002` | `TEST_BUG` | High | `FIXED_VERIFIED` | `AdminReportAiResolutionServiceImpl`: từ `84.69% / 57.46%` lên `100% / 87.85%` |
| `G012B-COV-003` | `TEST_BUG` | High | `FIXED_VERIFIED` | `AdminReportAiPolicyCatalog`: từ `100% / 50%` lên `100% / 100%` |
| `G012B-COV-004` | `TEST_BUG` | High | `FIXED_VERIFIED` | `AdminReportAiSemanticValidator`: từ `90.28% / 61.73%` lên `100% / 98.75%` |
| `G012B-CODE-005` | `CODE_BUG` | Low | `FIXED_VERIFIED` | Loại dead enum case nhưng giữ nguyên ma trận decision/action; focused và full regression đều pass |
| `G012B-TEST-006` | `TEST_BUG` | Low | `FIXED_VERIFIED` | Đổi assertion sang lấy `Map<String,Object>` rõ kiểu; test compile và pass |

`AdminReportAiWebhookSigner` đã đạt `100%` line / `85.71%` branch nên không mở issue.

## Baseline command

```powershell
mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiAutoApplyJobServiceImplTest,AdminReportAiWebhookSignerTest,AdminContentReportControllerTest' test
```

Kết quả: `44/44 PASS`, nhưng coverage gate thất bại như bảng trên.

## Quy tắc sửa

- Bổ sung focused test cho nhánh hành vi còn thiếu.
- Không giảm threshold và không loại class khỏi JaCoCo để tạo pass giả.
- Chỉ sửa production nếu test chứng minh `CODE_BUG` hoặc nhánh thực sự không thể đạt.

## Kết luận

Không còn issue bắt buộc đang mở trong phạm vi G0-12B. PostgreSQL integration test bị skip thuộc
G0-12C và không được che giấu bằng source change.

# Issue register G0-12F

| ID | Mức | Phân loại | Mô tả | Trạng thái |
|---|---|---|---|---|
| `G012F-DOC-001` | High | Trong phạm vi | DD tổng vẫn dừng ở G0-12C và ghi sai rằng n8n/UI runtime chưa chạy | `FIXED_VERIFIED` |
| `G012F-GOV-002` | Medium | Governance | G0-12E chưa có approval; không được suy ra từ token triển khai G0-12F | `CLOSED_APPROVE_G0_12E` |
| `G012F-GOV-003` | Medium | Governance | Tại thời điểm implement/verify, G0-12F cần approval riêng | `CLOSED_APPROVE_G0_12F` |
| `G012F-POLICY-004` | High | Residual | Policy/rule runtime vẫn mang lifecycle `proposed` | `DEFERRED_BUSINESS_REVIEW` |
| `G012F-SEC-005` | High | Residual | Replay stores hiện local/process-scoped, chưa chứng minh multi-instance | `DEFERRED_HARDENING` |
| `G012F-N8N-006` | Medium | Residual | Negative n8n execution trả HTTP 200 body rỗng trên runtime đã test | `DEFERRED_HARDENING` |
| `G012F-EVIDENCE-007` | High | Product gap | Chưa fetch/OCR/vision media và chưa có authoritative external evidence | `DEFERRED_SPRINT_2_PLUS` |
| `G012F-DATA-008` | High | Environment | External/Supabase schema không được migrate hay chứng minh V2 trong các gate disposable | `NOT_AUTHORIZED` |
| `G012F-RELATED-009` | Medium | Ngoài phạm vi | Legacy `ReportModerationJobWorker` có lỗi lazy `imageUrls` với BLOG/COMMENT | `OPEN_SEPARATE_FIX` |
| `G012F-TEST-010` | Low | Test data | RAI-16 automated cần legacy scheduled fixture; hiện dùng supplemental disposable setup | `DEFERRED_FIXTURE` |

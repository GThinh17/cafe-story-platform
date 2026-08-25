# G0-10K — Sprint 1 Target-specific Depth

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-011` |
| Trạng thái | `APPROVED` |
| Approval chat | Ủy quyền hoàn thành toàn bộ `G0-10` theo phương án khuyến nghị, ngày `2026-07-23` |
| Targets | `BLOG`, `COMMENT`, `USER`, `CAFE_PAGE` |
| Selected option | `K1_GENERAL_CONTRACT_CONTENT_FIRST` |

## 2. Phạm vi Sprint 1

### Bắt buộc cho mọi target

- common immutable snapshot contract;
- target identity/type/state/version/hash;
- report-to-target association;
- sanitized observable fields và unavailable-field markers;
- provenance, capture time, freshness và access result;
- counter-evidence/missing-evidence representation;
- policy/rule/schema/prompt/workflow/model version context.

### Deep profile trong Sprint 1

| Target | Mức triển khai | Decision/action boundary |
|---|---|---|
| `BLOG` | Detailed minimum evidence + rule applicability | AI recommendation; human content action |
| `COMMENT` | Detailed minimum evidence + parent-context guard | AI recommendation; human content action |
| `USER` | Common snapshot + manual escalation only | Không AI `RESOLVE/SUSPEND_USER` |
| `CAFE_PAGE` | Common snapshot + manual escalation only | Không AI `RESOLVE/SUSPEND_PAGE` |

Một BLOG/COMMENT không được tự động suy thành actor/page violation. Aggregate behavior,
account history, coordinated abuse và page-level responsibility cần policy/evidence profile riêng.

## 3. Lý do chọn content-first

- BLOG/COMMENT có target content cụ thể, snapshot và action scope hẹp hơn.
- USER/CAFE_PAGE action có blast radius lớn và thường cần aggregation/context dài hạn.
- Rule Catalog hiện là general skeleton; triển khai sâu tất cả target ngay sẽ phát minh criteria
  khi chưa có evaluation dataset.
- C1 quorum vẫn là guard cho future/manual high-impact action, không phải lý do để AI đủ quyền
  đề xuất suspension trong Sprint 1.

## 4. Deferred roadmap

Ưu tiên sau Sprint 1:

1. USER repeated/coordinated behavior profile;
2. CAFE_PAGE ownership, merchant identity và page-level representation;
3. cross-target aggregation và temporal evidence;
4. actor/page action burden evaluation;
5. target-specific rule calibration và adversarial tests.

Các hạng mục deferred cần gate riêng; không được coi là bug của Sprint 1 content-first.

## 5. Acceptance record

```text
decisionId: BD-011
selectedOption: K1_GENERAL_CONTRACT_CONTENT_FIRST
commonContractTargets: [BLOG, COMMENT, USER, CAFE_PAGE]
deepSprint1Targets: [BLOG, COMMENT]
manualOnlySprint1Targets: [USER, CAFE_PAGE]
singleContentCanSubstantiateActorSuspension: false
targetDeepRoadmapIsFutureScope: true
```

## 6. Kết luận

Sprint 1 phải hoàn thiện general contract và BLOG/COMMENT evidence profile. USER/CAFE_PAGE deep
moderation là cải tiến sau, không nhồi vào lần sửa general hiện tại.

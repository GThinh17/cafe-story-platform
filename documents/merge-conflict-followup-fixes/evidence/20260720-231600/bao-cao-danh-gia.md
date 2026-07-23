# Báo cáo sửa lỗi hậu merge develop và current branch

## Kết luận

Các thay đổi P0-P3 đã qua focused test, full backend regression, web typecheck/build, API smoke và browser smoke. Không thay đổi ranking formula, cursor, feed size, ad pacing, schema, payment provider, mobile UI hoặc seed data.

## Khác nhau giữa hai nhánh trước khi resolve

Mốc so sánh:

- Current parent: `aa2bfb0bf630efc6f1247f3c90fce292c0f88534` (`fix mobile dicebear avatars`).
- Develop parent: `1036d39bbbcc99ca01a80f956740b245c1f33318`.
- Merge baseline: `83d7a59821a67ece0822af86c6620b786d641de7`.

| Khu vực | Develop trước merge | Current trước merge | Ảnh hưởng khi kết hợp |
|---|---|---|---|
| Feed viewer state | Có batch hydrate follow/like/save cho ranked posts; organic fallback vẫn gọi anonymous | Có mixed feed, sponsored items, cursor và SWR/cache; FE tải toàn bộ likes/saves để bù contract | Merge giữ cả viewer-state contract lẫn fallback/cache/workaround cũ, tạo state thiếu hoặc stale và request dư |
| Eager media | `index === 0` đúng vì mảng chỉ gồm post | Mảng mixed có cả post và ad, không cùng giả định index | Port logic index sang mixed array có thể eager nhầm ad và bỏ post đầu |
| Payment history | `PaymentService` phục vụ history, có `/api/payments/me` | Thêm `PaymentHistoryService` riêng cho `/api/payments` và Ads dashboard gọi client riêng | Merge tồn tại hai implementation cùng auth/filter/DTO, dễ drift |
| Ads campaign UX | Owner-only Feed Advertising Pack, cafe settings section và form `/cafes/campaigns/new` | `/ads` dashboard quản lý package/payment/campaign và static card “Open Ads dashboard” | Merge tạo hai card và hai campaign flow cạnh tranh nhau |

## Lỗi thuộc develop hay current?

Không nên gán toàn bộ cho một phía:

- **P0 là lỗi contract dùng chung/semantic merge.** Cả hai parent đều có internal organic fallback anonymous; cả hai cách cache personalized đều giữ hydrated response nên có thể stale. Develop cung cấp batch viewer-state tốt hơn, còn current cung cấp mixed-feed/cache và workaround FE. Lỗi xuất hiện rõ khi ghép các giả định này mà không chốt một contract duy nhất.
- **P1 là lỗi tại điểm tích hợp.** `index === 0` hợp lý trong list thuần post của develop; mixed array của current cũng hợp lý riêng. Ghép hai logic nguyên trạng mới sai.
- **P2 đến từ hai thiết kế song song.** Current thêm `PaymentHistoryService`; develop dùng `PaymentService`. Không implementation nào tự thân “sai”, nhưng giữ cả hai là không hợp lý.
- **P3 đến từ hai UX song song.** Develop gắn campaign vào cafe settings; current gom vào `/ads`. Vấn đề là merge giữ cả hai thay vì chọn canonical flow.

## Review lựa chọn resolve

- Viewer state được coi là request/viewer data, không phải ranking data. Vì vậy ranking cache giữ neutral payload và state được batch hydrate sau cache; cách này không ưu tiên code của nhánh nào mà tách đúng trách nhiệm.
- `PaymentService.getMyPayments` được chọn làm implementation chung vì endpoint `/me` đã dùng nó, DTO/filter đã có sẵn, và endpoint root chỉ cần compatibility cho mobile. Giữ service thứ hai không đem lại contract khác biệt có ích.
- `/ads` được chọn làm flow chuẩn vì nó bao phủ package, unused payment và campaign history trong một màn hình; payment return cũng đã trỏ tới `/ads`. Route develop được giữ dưới dạng redirect để không phá bookmark/link cũ.
- Eager media dựa trên semantic kind thay vì vị trí vật lý, nên đúng cho cả feed có hoặc không có ad mà không thay đổi ad placement.

## Ma trận kiểm chứng

| Tiêu chí | Phương pháp | Kết quả thực tế | Evidence |
|---|---|---|---|
| Viewer-aware fallback/cache, batch và self-follow | JUnit/Mockito focused tests | 87/87 pass | `coverage/changed-production-coverage.md` |
| Backend regression | `mvn test` | 580 tests, 0 failure/error, 1 skipped | `raw/validation-summary.txt` |
| Payment endpoints cùng contract | Controller/service tests + authenticated API smoke | Hai endpoint 200, data giống nhau | `raw/validation-summary.txt` |
| Web compile/type | `npm run typecheck`, `npm run build` | Pass; 23 pages | `raw/validation-summary.txt` |
| Like refresh + cleanup | Browser + DOM class/count + backend runtime | Persist sau refresh; unlike cleanup persist | `screenshots/01-home-feed-viewer-state.png` |
| Pricing không còn card trùng | Browser dialog snapshot | `Open Ads dashboard` = 0; non-owner không thấy owner pack | `screenshots/02-pricing-modal-single-options.png` |
| `/ads` canonical, không console error | Browser smoke | Dashboard empty state; 0 error | `screenshots/03-ads-dashboard.png` |
| Route cũ redirect và giữ `paymentId` | Browser navigation | Redirect đúng cả có/không có ID | `screenshots/04-legacy-route-redirect.png` |
| Changed coverage | Git diff + JaCoCo XML | Lines 100%, branches 100% | `coverage/changed-production-coverage.md` |

## Chưa kiểm chứng trực tiếp bằng seed runtime

Tài khoản test không sở hữu cafe page, không có paid Ads package/campaign và feed runtime không có ad ở raw index 0. Vì vậy owner purchase state, campaign submit thật và exact ad-first UI fixture không được tuyên bố đã smoke trực tiếp. Đây là `MCF-007 TEST_DATA`; seed và payment thật không được tạo vì ngoài bounds. Logic tương ứng đã qua typecheck/build, code audit và các trạng thái runtime có dữ liệu.

## Scope và cleanup

- Mobile source không đổi; `/api/payments` vẫn tồn tại.
- Không đổi DB/migration, ranking formula, cursor, feed size, ad slots, Stripe/VNPAY, admin hoặc seed.
- `PRODUCT.md` và `documents/backend-flyway-checksum-startup/` được giữ nguyên, không stage.
- Like test đã cleanup; runtime 8080/3000 đã dừng.

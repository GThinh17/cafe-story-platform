# S2-02 — Nhận xét và cải tiến workflow

## Cải tiến đã áp dụng ngay

1. Canonical schema là source of truth; embedded provider schema phải deep-equal canonical artifact.
2. n8n reject unknown property cả nested object, không chỉ outer request.
3. Backend giữ final semantic authority; n8n chỉ boundary validation, projection và orchestration.
4. Invalid Rule Context dừng trước semantic evaluation để bảo đảm fail-closed.
5. Provider categorical output được normalize trước persistence.
6. Coverage gate dùng line/branch thực tế; malformed-field variants trở thành regression suite.

## Khuyến nghị cho S2-03

1. Tạo dataset manifest versioned với immutable fixture ID, rule/catalog/schema versions và source
   snapshot fingerprint.
2. Mỗi record phải có expected per-rule outcome, expected blocked reason, evidence IDs và authority ceiling;
   không chỉ có expected final decision.
3. Tách hard safety slice khỏi quality slice. Safety phải `100%`; quality được báo theo rule family,
   evidence sufficiency và target type.
4. Tái sử dụng `SEM-S2-*`, schema boundaries và adversarial vectors làm baseline bắt buộc.
5. Ghi rõ synthetic/test-data status; không gọi fixture là production evidence.

## Khuyến nghị cho S2-04 và n8n runtime

1. Prompt clause phải sinh từ candidate rule metadata đã pin version, không copy policy catalog thứ hai vào n8n.
2. Trước publish phải backup workflow hiện hành, kiểm tra exact webhook, version pair và rollback path.
3. Provider call chỉ mở sau dataset/harness approval; lưu quality/latency/cost evidence tách biệt.
4. Không biến confidence thành violation probability hoặc action threshold.
5. Không dùng AI explanation làm evidence; explanation chỉ diễn giải finding đã trace bằng Evidence ID.

## Cải tiến dài hạn, chưa code trong S2-02

- external authoritative-source adapters;
- verified media/vision/OCR pipeline;
- cross-record/cross-target behavioral evidence;
- calibration dataset và threshold governance;
- deep evidence cho USER/CAFE_PAGE;
- production replay store và operational monitoring.


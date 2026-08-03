# S2-DONE-AUDIT — Workflow retrospective

## Quyết định sau review

Roadmap cũ quá nhỏ hạt và làm tăng context/token. Từ thời điểm
`APPROVE_S2_DONE_AUDIT`, dùng `CURRENT-LEAN-ROADMAP.md`: gộp remediation vào
một package, chạy functional gate một lần rồi đóng core; S2-05 mở rộng và
Sprint 3–4 chuyển thành backlog.

## Điều cần đưa lên sớm hơn

1. Mỗi package approval phải có machine-readable `summary.json`; audit không nên
   là lúc đầu tiên phát hiện artifact thiếu.
2. Coverage gate cần chạy bằng command cố định và lưu JaCoCo counters, không chỉ
   copy phần trăm từ lượt trước.
3. Security harness phải nằm trong S2 regression aggregator để schema evolution
   làm fail ngay tại S2-01/S2-02.
4. Trước full provider matrix phải có one-call schema probe và global circuit
   breaker.
5. `sk-` substring scan gây false positive với từ `risk-`; secret scan phải dùng
   exact env value và regex key đủ dài.

## Cải tiến gate

Đề xuất thêm một local aggregator:

```text
S2 contracts
→ security static
→ adversarial
→ prompt pilot
→ dataset hard gate
→ focused coverage parse
→ full Backend
→ evidence package completeness
```

Aggregator phải dừng ở gate đầu tiên fail và không gọi provider.

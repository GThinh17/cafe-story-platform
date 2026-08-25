# S2-01 — Workflow retrospective và cải tiến

## Điều đã cải thiện trong lượt này

1. Contract quan trọng không còn dùng raw map ở Backend boundary.
2. Lifecycle được kiểm tra trước semantic authority; null/unknown không được coi là active.
3. Evidence được phân biệt khỏi reporter claim và AI rationale.
4. n8n có explicit provider allowlist thay vì serialize toàn request.
5. Code node có source file reviewable và sync assertion để tránh inline workflow drift.
6. Coverage gate phát hiện test legacy constructor còn thiếu trước khi đóng package.

## Đề xuất cho S2-02

1. Chuyển rule metadata skeleton thành canonical per-rule requirement catalog được schema validate.
2. Chốt bounds cho candidate rules, evidence, findings, references và text.
3. Enforce rule version/status/applicable target và complete material scope ở Backend.
4. Dùng canonical request/provider-output schema và hash parity với n8n embedded schema.
5. Tách Missing Evidence ID khỏi Evidence ID thay vì dùng chung reference semantics.
6. Kiểm tra `collectedForRuleIds` là subset candidate Rule IDs.
7. Kiểm tra Evidence Kind/source/privacy/payload compatibility tại Backend và n8n.

## Đề xuất cho S2-03/S2-04

1. Tạo dataset có ground truth và adjudication record trước khi tối ưu prompt.
2. Đo unsupported assertion, unknown reference, manual clamp và complete-scope rate.
3. Chỉ pilot rule-family text-only sau khi S2-02 contract ổn định.
4. Không dùng confidence như violation probability hoặc action authority.

## Vận hành

Workflow S2-01 chưa được publish. Khi có gate runtime riêng:

1. backup workflow hiện hành;
2. import inactive;
3. parity source/inline;
4. exact webhook smoke;
5. verify signed response và no-mutation;
6. rollback nếu version/signature/projection mismatch.

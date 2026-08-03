# Domain Terminology — Contract 2.0

| Term | Meaning |
|---|---|
| Report claim | Điều reporter cáo buộc; không phải fact/evidence |
| Observation | Điều hệ thống/tool có provenance quan sát được |
| Evidence | Observation/reference có thể hỗ trợ hoặc phản bác claim/rule |
| Finding | Kết quả đánh giá một Rule ID dựa trên Evidence ID |
| Recommendation | AI-derived proposal, không phải decision/action authority |
| Report decision | `NEEDS_MANUAL_REVIEW`, `REJECT`, `RESOLVE` |
| Candidate action | Đề xuất hành động; chưa được thực thi |
| Execution | Mutation thực tế do Backend-authorized path |
| Evidence quality | Độ tin cậy/provenance của evidence |
| Evidence sufficiency | Evidence có đủ burden cho finding/decision không |
| Violation likelihood | Mức hỗ trợ finding; không phải calibrated probability |
| Harm severity | Mức harm nếu claim đúng |
| Action risk | Rủi ro của action, do Backend derive |
| AI rationale | Giải thích dẫn xuất; không phải evidence |
| Snapshot | Target observation bất biến tại một thời điểm |
| Operational failure | Lỗi network/provider/schema/auth; không phải content uncertainty |

Canonical names dùng ở FE/BE/n8n; legacy `APPROVE/NONE` chỉ là aliases đọc lịch sử.

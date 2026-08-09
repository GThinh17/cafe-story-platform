# S2-02 — Coverage gate

Nguồn: JaCoCo XML sinh bởi focused Maven test cuối cùng và xác nhận lại trong full regression.

| Production class | Line | Branch | Gate |
|---|---:|---:|---|
| `AdminReportAiSemanticValidator` | `434/434 = 100%` | `446/504 = 88.49%` | `PASS` |
| `AdminReportAiPolicyCatalog` | `109/109 = 100%` | `25/25 = 100%` | `PASS` |

Gate áp dụng: changed production line `100%`, branch `>=85%`.

Không loại branch khỏi báo cáo và không hạ threshold. Test bổ sung tập trung vào malformed Rule Context,
categorical normalization, non-material rules, bounds, reference, burden, aggregation và ceiling.


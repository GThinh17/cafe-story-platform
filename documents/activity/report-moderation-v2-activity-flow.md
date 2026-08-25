# Report Moderation V2 Activity Flow

    ```mermaid
    flowchart TD
        Start(["User bắt đầu report content"])

        Start --> A["POST /api/reports"]
        A --> B["BE validate report<br/>user active, target tồn tại, reason hợp lệ<br/>không tự report chính mình"]
        B --> C{"Duplicate OPEN/REVIEWING?"}

        C -->|"Có"| D["Return 409 Conflict"]
        C -->|"Không"| E["Save ContentReport<br/>status = OPEN"]

        E --> F{"Target là BLOG/COMMENT?"}
        F -->|"Không"| G["USER/CAFE_PAGE<br/>manual review only"]
        F -->|"Có"| H["Create ReportModerationJob<br/>status = PENDING"]

        G --> I["Return ContentReportResponseDTO nhanh"]
        H --> I

        I --> J["Scheduled worker claim due jobs<br/>PENDING/FAILED<br/>FOR UPDATE SKIP LOCKED"]
        J --> K["BE gọi n8n webhook<br/>/cafestory-report-moderation"]
        K --> L["n8n gọi OpenAI Moderation API<br/>normalize decision JSON"]
        L --> M{"n8n/OpenAI success?"}

        M -->|"Không"| N["Retry với backoff<br/>sau 3 lần -> DEAD_LETTER"]
        N --> J

        M -->|"Có"| O["BE lưu AiModerationResult<br/>compute riskScore + priorityScore"]
        O --> P{"AI decision?"}

        P -->|"SAFE"| Q["Result resolved = true<br/>report giữ OPEN"]
        P -->|"NEEDS_REVIEW"| R["Report -> REVIEWING<br/>chờ admin"]
        P -->|"VIOLATION"| S["Report -> REVIEWING<br/>priority cao"]

        Q --> T["Mark job SUCCEEDED"]
        R --> T
        S --> T

        T --> U["Admin xem reports / queue / jobs<br/>và resolve thủ công"]
        U --> End(["Kết thúc flow moderation"])
    ```

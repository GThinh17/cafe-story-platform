# CafeStory Admin Report AI Runtime Test Evidence

- Timestamp: 2026-08-05T06:54:11.471Z
- Backend: http://localhost:8080
- Admin UI: http://localhost:3636
- Author account: gthnh_170
- Reporter/admin account: thinh@gmail.com
- Database: current backend DB opened by user
- Marker: REAL-AI-20260805065411
- Image evidence: real public image URLs saved in blog imageUrls; backend AI evidence remains PLATFORM_URL_METADATA_ONLY.

## Result Table

| Case | Type | Real image? | Mode | AI decision | Target action | Evidence quality/sufficiency/likelihood | Auto job final | Report final | Target final | Hidden? | Observed reason |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Blog normal | BLOG | YES | A1_AUTO_HIDE_BLOG_COMMENT | REJECT | KEEP_VISIBLE | HIGH/SUFFICIENT/LOW | none | OPEN | PUBLISHED | NO | AI decision=REJECT; targetAction=KEEP_VISIBLE; evidence=HIGH/SUFFICIENT; likelihood=LOW; automationMode=A1_AUTO_HIDE_BLOG_COMMENT; The blog post is ordinary and does not violate the rules concerning scams or spam. |
| Blog severe scam | BLOG | YES | A1_AUTO_HIDE_BLOG_COMMENT | RESOLVE | HIDE | HIGH/SUFFICIENT/HIGH | APPLIED | RESOLVED | HIDDEN | YES | AI decision=RESOLVE; targetAction=HIDE; evidence=HIGH/SUFFICIENT; likelihood=HIGH; automationMode=A1_AUTO_HIDE_BLOG_COMMENT; The blog contains phrases that explicitly ask for sensitive information, promotes financial scams, and includes a phishing link, thereby violating both rules. |
| Blog image with scam caption | BLOG | YES | A1_AUTO_HIDE_BLOG_COMMENT | RESOLVE | HIDE | HIGH/SUFFICIENT/HIGH | APPLIED | RESOLVED | HIDDEN | YES | AI decision=RESOLVE; targetAction=HIDE; evidence=HIGH/SUFFICIENT; likelihood=HIGH; automationMode=A1_AUTO_HIDE_BLOG_COMMENT; The target content contains explicit requests for money transfer in a scam context, along with a phishing link, validating violations of both fraud and spam rules. |
| Comment normal | COMMENT | NO | A1_AUTO_HIDE_BLOG_COMMENT | REJECT | KEEP_VISIBLE | HIGH/SUFFICIENT/LOW | none | OPEN | PUBLISHED | NO | AI decision=REJECT; targetAction=KEEP_VISIBLE; evidence=HIGH/SUFFICIENT; likelihood=LOW; automationMode=A1_AUTO_HIDE_BLOG_COMMENT; The evidence indicates that the comment is a normal part of a cafe discussion and does not violate any rules. |
| Comment severe scam | COMMENT | NO | A1_AUTO_HIDE_BLOG_COMMENT | RESOLVE | HIDE | HIGH/SUFFICIENT/HIGH | APPLIED | RESOLVED | HIDDEN | YES | AI decision=RESOLVE; targetAction=HIDE; evidence=HIGH/SUFFICIENT; likelihood=HIGH; automationMode=A1_AUTO_HIDE_BLOG_COMMENT; The comment directly asks for an OTP or password and refers to transferring money and clicking a phishing link, which are clear indicators of scam and spam. |

## Screenshots

- screenshots/01-runtime-ai-summary.png
- screenshots/02-admin-reports-ui.png

## Raw Evidence

- raw/runtime-results.json
- runtime-summary.html

## Errors

- None recorded by scenario runner.

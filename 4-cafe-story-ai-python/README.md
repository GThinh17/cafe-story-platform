# CafeStory AI Moderation Backend

Python FastAPI service for Java Spring Boot to evaluate a saved blog caption and Cloudinary image URLs.

## Setup

```powershell
cd D:\cafe-story-platform\4-cafe-story-ai-python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

Set `GOOGLE_API_KEY` in `.env`.

## Run

```powershell
uvicorn app.main:app --reload --port 8036
```

On startup, the backend preloads:

- CLIP model `openai/clip-vit-base-patch32`
- Gemini text moderation client
- Gemini image classification client
- moderation rules from `app/config/moderation_rules.json`

Health check:

```http
GET http://localhost:8036/health
```

## Evaluate Blog API

```http
POST http://localhost:8036/api/ai/blogs/evaluate
Content-Type: application/json
```

Request:

```json
{
  "blogId": "8d3f...",
  "caption": "Quan ca phe yen tinh, do uong ngon.",
  "imageUrls": [
    "https://res.cloudinary.com/demo/image/upload/sample.jpg"
  ]
}
```

Response:

```json
{
  "blogId": "8d3f...",
  "captionScore": 12,
  "captionReason": "Noi dung lien quan den trai nghiem quan ca phe, khong co dau hieu vi pham.",
  "imageScore": 86,
  "imageReason": "1/1 images are cafe-related. imageScore=86. Main evidence: coffee shop space.",
  "tags": ["study cafe", "brunch cafe", "garden cafe"],
  "status": "approve"
}
```

`status` is always one of: `deny`, `send Admin`, `approve`.

## Decision Rules

Rules and prompts live in `app/config/moderation_rules.json`.

- `deny`: `captionScore >= 70`, caption is not coffee-related, or `imageScore <= 30`.
- `send Admin`: caption model error, image download/classification error, no image, `30 < imageScore < 50`, or tag classifier uncertainty.
- `approve`: caption passes, `imageScore >= 50`, no AI/image errors, and 3 reliable tags are available.

`imageScore` means `P(image cafe-related)` from CLIP on a 0-100 scale. For multiple images, the service uses the minimum image score so one clearly unrelated image lowers the whole post score.

## Java Spring Boot Integration

After Java saves the blog, call:

```http
POST http://localhost:8036/api/ai/blogs/evaluate
```

with `blogId`, `caption`, and `imageUrls`. Use the returned `blogId` to update the same blog row with `captionScore`, `captionReason`, `imageScore`, `imageReason`, `tags`, and moderation status.

## Reused Demo Configuration

- `cafe_img_detection.py`: CLIP model `openai/clip-vit-base-patch32`, cafe/non-cafe labels, confidence scoring.
- `image_classify_llm.py`: Gemini image model `gemini-2.5-flash`, strict image classifier prompt, exact 3-tag contract, CafeStory tag set.
- `text_llm.py`: Gemini text model `gemini-3.1-flash-lite`, JSON response config, Vietnamese moderation criteria and output schema.

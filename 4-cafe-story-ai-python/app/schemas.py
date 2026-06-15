from typing import Any, Literal

from pydantic import BaseModel, Field, field_validator


Status = Literal["deny", "send Admin", "approve"]


class BlogEvaluateRequest(BaseModel):
    blogId: str = Field(..., min_length=1)
    caption: str | None = ""
    imageUrls: list[str] | None = Field(default_factory=list)

    @field_validator("blogId")
    @classmethod
    def validate_blog_id(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("blogId must not be empty")
        return value

    @field_validator("caption", mode="before")
    @classmethod
    def default_caption(cls, value: Any) -> str:
        return "" if value is None else str(value)

    @field_validator("imageUrls", mode="before")
    @classmethod
    def default_image_urls(cls, value: Any) -> list[str]:
        return [] if value is None else value


class BlogEvaluateResponse(BaseModel):
    blogId: str
    captionScore: int
    captionReason: str
    imageScore: int
    imageReason: str
    tags: list[str] = Field(min_length=3, max_length=3)
    status: Status

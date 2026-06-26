from fastapi import APIRouter

from app.controllers.blog_controller import router as blog_router
from app.controllers.health_controller import router as health_router

router = APIRouter()
router.include_router(health_router)
router.include_router(blog_router)

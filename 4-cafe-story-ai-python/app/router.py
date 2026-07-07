from fastapi import APIRouter

from app.controllers.blog_controller import router as blog_router
from app.controllers.chat_controller import router as chat_router
from app.controllers.health_controller import router as health_router
from app.controllers.rag_admin_controller import router as rag_admin_router

router = APIRouter()
router.include_router(health_router)
router.include_router(blog_router)
router.include_router(chat_router)
router.include_router(rag_admin_router)

"""配置:从 env 读,带 demo 默认值(不在业务代码里硬编码密钥/口令)。
依赖:os / dataclasses。暴露:Config, load_config。"""
import os
from dataclasses import dataclass

_REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


@dataclass(frozen=True)
class Config:
    db_path: str
    uploads_dir: str
    host: str
    port: int
    secret_key: str = "dev-insecure-secret-change-via-env"
    admin_username: str = "admin"
    admin_password: str = ""  # 必须由 env 提供;为空 = 禁止登录(不把口令硬编码进代码)


def load_config() -> Config:
    return Config(
        db_path=os.environ.get("RIDEMALL_DB", os.path.join(_REPO_ROOT, "ridemall.db")),
        uploads_dir=os.environ.get("RIDEMALL_UPLOADS", os.path.join(_REPO_ROOT, "server", "uploads")),
        host=os.environ.get("RIDEMALL_HOST", "127.0.0.1"),
        port=int(os.environ.get("RIDEMALL_PORT", "8000")),
        secret_key=os.environ.get("RIDEMALL_SECRET_KEY", "dev-insecure-secret-change-via-env"),
        admin_username=os.environ.get("RIDEMALL_ADMIN_USER", "admin"),
        admin_password=os.environ.get("RIDEMALL_ADMIN_PASSWORD", ""),
    )

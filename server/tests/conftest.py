"""pytest 公共夹具:临时库 + 已 seed 的客户端 + 直连连接。"""
import pytest

from server import db
from server.app import create_app
from server.config import Config


@pytest.fixture
def cfg(tmp_path):
    return Config(
        db_path=str(tmp_path / "test.db"),
        uploads_dir=str(tmp_path / "uploads"),
        host="127.0.0.1",
        port=0,
        secret_key="test-secret",
        admin_username="admin",
        admin_password="test-pass",
    )


@pytest.fixture
def admin_client(client):
    """已登录的后台客户端。"""
    client.post("/admin/login", data={"username": "admin", "password": "test-pass"})
    return client


@pytest.fixture
def app(cfg):
    return create_app(cfg, seed=True)


@pytest.fixture
def client(app):
    return app.test_client()


@pytest.fixture
def unseeded_client(cfg):
    return create_app(cfg, seed=False).test_client()


@pytest.fixture
def conn(app, cfg):
    """直连同一 db 做断言/改数据;依赖 app 以保证已建库 + seed。"""
    c = db.connect(cfg.db_path)
    yield c
    c.close()

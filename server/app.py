"""Flask 应用工厂:组装配置 + 建库 + seed + 注册车机接口 + 后台 + 提供图片。
依赖:config, db, seed, api.routes, admin.views, rules.money。暴露:create_app。"""
from flask import Flask, g, send_from_directory

from server import db
from server.admin.views import bp as admin_bp
from server.api.routes import bp as api_bp
from server.config import Config, load_config
from server.rules import money
from server.seed import seed_if_empty


def create_app(config: Config | None = None, seed: bool = True) -> Flask:
    cfg = config or load_config()
    app = Flask(__name__)
    app.secret_key = cfg.secret_key
    app.config["DB_PATH"] = cfg.db_path
    app.config["UPLOADS_DIR"] = cfg.uploads_dir
    app.config["ADMIN_USERNAME"] = cfg.admin_username
    app.config["ADMIN_PASSWORD"] = cfg.admin_password
    app.config["STATUS_STEP_SECONDS"] = cfg.status_step_seconds
    app.config["RETURN_WINDOW_SECONDS"] = cfg.return_window_seconds
    app.config["LOGIN_ATTEMPTS"] = {}  # {ip: [epoch 秒]} 内存限速记录
    app.jinja_env.filters["yuan"] = money.cents_to_yuan

    conn = db.connect(cfg.db_path)
    db.init_schema(conn)
    if seed:
        seed_if_empty(conn, cfg.uploads_dir)
    conn.close()

    app.register_blueprint(api_bp)
    app.register_blueprint(admin_bp)

    @app.get("/uploads/<path:filename>")
    def uploads(filename: str):
        return send_from_directory(cfg.uploads_dir, filename)

    @app.teardown_appcontext
    def _close(_exc):
        conn = g.pop("conn", None)
        if conn is not None:
            conn.close()

    return app

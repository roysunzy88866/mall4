"""Flask 应用工厂:组装配置 + 建库 + seed + 注册接口 + 提供图片。
依赖:config, db, seed, api.routes。暴露:create_app。"""
from flask import Flask, g, send_from_directory

from server import db
from server.api.routes import bp as api_bp
from server.config import Config, load_config
from server.seed import seed_if_empty


def create_app(config: Config | None = None, seed: bool = True) -> Flask:
    cfg = config or load_config()
    app = Flask(__name__)
    app.config["DB_PATH"] = cfg.db_path
    app.config["UPLOADS_DIR"] = cfg.uploads_dir

    conn = db.connect(cfg.db_path)
    db.init_schema(conn)
    if seed:
        seed_if_empty(conn, cfg.uploads_dir)
    conn.close()

    app.register_blueprint(api_bp)

    @app.get("/uploads/<path:filename>")
    def uploads(filename: str):
        return send_from_directory(cfg.uploads_dir, filename)

    @app.teardown_appcontext
    def _close(_exc):
        conn = g.pop("conn", None)
        if conn is not None:
            conn.close()

    return app

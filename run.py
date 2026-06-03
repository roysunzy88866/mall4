"""本地启动后端:`python run.py`(端口/路径走 env,见 server/config.py)。"""
from server.app import create_app
from server.config import load_config

if __name__ == "__main__":
    cfg = load_config()
    create_app(cfg).run(host=cfg.host, port=cfg.port, debug=False)

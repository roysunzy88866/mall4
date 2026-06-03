"""后台视图蓝图:登录 + 限速 + 仪表盘。只收参/校验/调业务/渲染。"""
import time
from functools import wraps

from flask import (
    Blueprint, current_app, g, redirect, render_template, request, session, url_for,
)

from server import db
from server.rules import admin as admin_rules
from server.services import admin_service

bp = Blueprint("admin", __name__, url_prefix="/admin", template_folder="templates")


def _conn():
    if "conn" not in g:
        g.conn = db.connect(current_app.config["DB_PATH"])
    return g.conn


def login_required(view):
    @wraps(view)
    def wrapped(*args, **kwargs):
        if not session.get("admin"):
            return redirect(url_for("admin.login"))
        return view(*args, **kwargs)

    return wrapped


@bp.route("/login", methods=["GET", "POST"])
def login():
    attempts = current_app.config["LOGIN_ATTEMPTS"]
    ip = request.remote_addr or "?"
    if request.method == "POST":
        now = time.time()
        if admin_rules.is_throttled(attempts.get(ip, []), now):
            return render_template("login.html", error="尝试过于频繁,请 5 分钟后再试"), 429
        username = request.form.get("username", "")
        password = request.form.get("password", "")
        cfg_user = current_app.config["ADMIN_USERNAME"]
        cfg_pass = current_app.config["ADMIN_PASSWORD"]
        if cfg_pass and username == cfg_user and password == cfg_pass:
            session["admin"] = True
            return redirect(url_for("admin.dashboard"))
        attempts.setdefault(ip, []).append(now)
        return render_template("login.html", error="账号或口令错误"), 401
    return render_template("login.html", error=None)


@bp.route("/logout")
def logout():
    session.pop("admin", None)
    return redirect(url_for("admin.login"))


@bp.route("/")
@login_required
def dashboard():
    return render_template("dashboard.html", stats=admin_service.dashboard_stats(_conn()))

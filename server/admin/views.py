"""后台视图蓝图:登录 + 限速 + 仪表盘。只收参/校验/调业务/渲染。"""
import os
import time
from datetime import datetime
from functools import wraps
from uuid import uuid4

from flask import (
    Blueprint, current_app, g, redirect, render_template, request, session, url_for,
)

from server import db
from server.repositories import catalog_write_repo as cw
from server.rules import admin as admin_rules
from server.services import admin_catalog_service as cat_svc
from server.services import admin_service

bp = Blueprint("admin", __name__, url_prefix="/admin", template_folder="templates")


def _conn():
    if "conn" not in g:
        g.conn = db.connect(current_app.config["DB_PATH"])
    return g.conn


def _now() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


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


# ---- 分类管理 ----
@bp.get("/categories")
@login_required
def categories_page():
    conn = _conn()
    cats = cw.all_categories(conn)
    counts = {c.id: cw.count_products_in_category(conn, c.id) for c in cats}
    return render_template("categories.html", categories=cats, counts=counts, error=request.args.get("error"))


@bp.post("/categories/create")
@login_required
def category_create():
    name = (request.form.get("name") or "").strip()
    if name:
        cat_svc.create_category(_conn(), name, _now())
    return redirect(url_for("admin.categories_page"))


@bp.post("/categories/<int:category_id>/rename")
@login_required
def category_rename(category_id):
    name = (request.form.get("name") or "").strip()
    if name:
        cat_svc.rename_category(_conn(), category_id, name)
    return redirect(url_for("admin.categories_page"))


@bp.post("/categories/<int:category_id>/toggle")
@login_required
def category_toggle(category_id):
    cat_svc.toggle_category(_conn(), category_id, request.form.get("active") == "1")
    return redirect(url_for("admin.categories_page"))


@bp.post("/categories/<int:category_id>/move")
@login_required
def category_move(category_id):
    cat_svc.move_category(_conn(), category_id, request.form.get("direction", "up"))
    return redirect(url_for("admin.categories_page"))


@bp.post("/categories/<int:category_id>/delete")
@login_required
def category_delete(category_id):
    try:
        cat_svc.delete_category(_conn(), category_id)
    except cat_svc.CategoryNotEmpty:
        return redirect(url_for("admin.categories_page", error="请先清空该分类下的商品"))
    return redirect(url_for("admin.categories_page"))


# ---- 商品管理 ----
def _save_image(file_storage):
    """保存上传图片,返回 (url, 错误)。无文件返回 (None, None)。"""
    if not file_storage or not file_storage.filename:
        return None, None
    if not admin_rules.allowed_image_ext(file_storage.filename):
        return None, "图片格式不支持(仅 png/jpg/jpeg/gif/webp)"
    ext = file_storage.filename.rsplit(".", 1)[-1].lower()
    fname = f"{uuid4().hex}.{ext}"
    uploads = current_app.config["UPLOADS_DIR"]
    os.makedirs(uploads, exist_ok=True)
    file_storage.save(os.path.join(uploads, fname))
    return f"/uploads/{fname}", None


@bp.get("/products")
@login_required
def products_page():
    conn = _conn()
    return render_template(
        "products.html",
        products=cw.all_products_admin(conn),
        categories=cw.all_categories(conn),
        error=request.args.get("error"),
    )


@bp.post("/products/create")
@login_required
def product_create():
    image_url, img_err = _save_image(request.files.get("image"))
    if img_err:
        return redirect(url_for("admin.products_page", error=img_err))
    name = (request.form.get("name") or "").strip()
    category_id = request.form.get("category_id", type=int)
    if not name or not category_id:
        return redirect(url_for("admin.products_page", error="名称与分类必填"))
    try:
        cat_svc.create_product(
            _conn(), name=name, price_yuan=request.form.get("price", "0"),
            description=request.form.get("description", ""),
            stock=request.form.get("stock", default=0, type=int),
            category_id=category_id, now=_now(), image_url=image_url,
        )
    except cat_svc.InvalidPrice:
        return redirect(url_for("admin.products_page", error="价格非法"))
    return redirect(url_for("admin.products_page"))


@bp.post("/products/<int:product_id>/update")
@login_required
def product_update(product_id):
    name = (request.form.get("name") or "").strip()
    category_id = request.form.get("category_id", type=int)
    if not name or not category_id:
        return redirect(url_for("admin.products_page", error="名称与分类必填"))
    try:
        cat_svc.update_product(
            _conn(), product_id, name=name, price_yuan=request.form.get("price", "0"),
            description=request.form.get("description", ""),
            stock=request.form.get("stock", default=0, type=int), category_id=category_id,
        )
    except cat_svc.InvalidPrice:
        return redirect(url_for("admin.products_page", error="价格非法"))
    return redirect(url_for("admin.products_page"))


@bp.post("/products/<int:product_id>/delete")
@login_required
def product_delete(product_id):
    cat_svc.delete_product(_conn(), product_id)
    return redirect(url_for("admin.products_page"))

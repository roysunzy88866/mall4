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
from server.repositories import catalog_repo as cr
from server.repositories import catalog_write_repo as cw
from server.repositories import orders_repo
from server.rules import admin as admin_rules
from server.rules import lifecycle as lc
from server.services import admin_catalog_service as cat_svc
from server.services import admin_service
from server.services import lifecycle_service as life_svc

bp = Blueprint("admin", __name__, url_prefix="/admin", template_folder="templates")


def _conn():
    if "conn" not in g:
        g.conn = db.connect(current_app.config["DB_PATH"])
    return g.conn


def _now() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def _step() -> int:
    return current_app.config.get("STATUS_STEP_SECONDS", 30)


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
    conn = _conn()
    stats = admin_service.dashboard_stats(conn)
    # 订单状态分布(中文标签)
    dist = {}
    for o in orders_repo.all_orders(conn):
        dist[o.status] = dist.get(o.status, 0) + 1
    status_dist = [
        {"status": s, "label": lc.status_label(s), "count": c}
        for s, c in sorted(dist.items(), key=lambda kv: lc.stage_index(kv[0]))
    ]
    return render_template("dashboard.html", stats=stats, status_dist=status_dist)


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
    image_url, img_err = _save_image(request.files.get("image"))
    if img_err:
        return redirect(url_for("admin.products_page", error=img_err))
    name = (request.form.get("name") or "").strip()
    category_id = request.form.get("category_id", type=int)
    if not name or not category_id:
        return redirect(url_for("admin.products_page", error="名称与分类必填"))
    try:
        cat_svc.update_product(
            _conn(), product_id, name=name, price_yuan=request.form.get("price", "0"),
            description=request.form.get("description", ""),
            stock=request.form.get("stock", default=0, type=int), category_id=category_id,
            image_url=image_url,
        )
    except cat_svc.InvalidPrice:
        return redirect(url_for("admin.products_page", error="价格非法"))
    return redirect(url_for("admin.products_page"))


@bp.post("/products/<int:product_id>/delete")
@login_required
def product_delete(product_id):
    cat_svc.delete_product(_conn(), product_id)
    return redirect(url_for("admin.products_page"))


# ---- 推荐位管理 ----
@bp.get("/banners")
@login_required
def banners_page():
    conn = _conn()
    rows, banner_pids = [], set()
    for b in cw.all_banners(conn):
        p = cr.product_by_id(conn, b.product_id)
        banner_pids.add(b.product_id)
        rows.append({
            "id": b.id, "sort_order": b.sort_order,
            "name": p.name if p else "(已删商品)", "price_cents": p.price_cents if p else 0,
        })
    available = [p for p in cw.all_products_admin(conn) if p.id not in banner_pids]
    categories = cw.all_categories(conn)
    # 按分类分组可选商品(供「先选分类→再选商品」两级联动)
    products_by_category = {
        c.id: [{"id": p.id, "name": p.name} for p in available if p.category_id == c.id]
        for c in categories
    }
    return render_template(
        "banners.html", rows=rows, categories=categories,
        products_by_category=products_by_category, count=len(rows), error=request.args.get("error"),
    )


@bp.post("/banners/add")
@login_required
def banner_add():
    pid = request.form.get("product_id", type=int)
    if pid:
        try:
            cat_svc.add_banner(_conn(), pid)
        except cat_svc.BannerLimit as e:
            return redirect(url_for("admin.banners_page", error=e.msg))
    return redirect(url_for("admin.banners_page"))


@bp.post("/banners/<int:banner_id>/remove")
@login_required
def banner_remove(banner_id):
    try:
        cat_svc.remove_banner(_conn(), banner_id)
    except cat_svc.BannerLimit as e:
        return redirect(url_for("admin.banners_page", error=e.msg))
    return redirect(url_for("admin.banners_page"))


@bp.post("/banners/<int:banner_id>/move")
@login_required
def banner_move(banner_id):
    cat_svc.move_banner(_conn(), banner_id, request.form.get("direction", "up"))
    return redirect(url_for("admin.banners_page"))


# ---- 订单管理(状态机 + 物流) ----
def _status_options():
    return [{"status": s, "label": lc.STAGE_LABELS[s]} for s in lc.STAGES]


@bp.get("/orders")
@login_required
def orders_page():
    status_filter = request.args.get("status") or None
    rows = life_svc.admin_list(_conn(), _now(), _step(), status_filter)
    return render_template(
        "orders.html", rows=rows, labels=lc.STAGE_LABELS,
        status_options=_status_options(), current_status=status_filter, step=_step(),
    )


@bp.get("/orders/<int:order_id>")
@login_required
def order_detail_page(order_id):
    result = life_svc.order_detail(_conn(), order_id, _now(), _step())
    if result is None:
        return redirect(url_for("admin.orders_page"))
    ow, logistics = result
    return render_template(
        "order_detail.html", ow=ow, logistics=logistics, labels=lc.STAGE_LABELS,
        stages=lc.STAGES, status_options=_status_options(),
        stage_index=lc.stage_index(ow.order.status), step=_step(),
        status_text=lc.status_label(ow.order.status),
    )


@bp.post("/orders/<int:order_id>/advance")
@login_required
def order_advance(order_id):
    life_svc.admin_advance_one(_conn(), order_id, _step())
    return redirect(url_for("admin.order_detail_page", order_id=order_id))


@bp.post("/orders/<int:order_id>/deliver")
@login_required
def order_deliver(order_id):
    life_svc.admin_advance_delivered(_conn(), order_id, _now(), _step())
    return redirect(url_for("admin.order_detail_page", order_id=order_id))


@bp.post("/orders/<int:order_id>/status")
@login_required
def order_set_status(order_id):
    status = request.form.get("status", "")
    if status in lc.STAGES:
        life_svc.admin_set_status(_conn(), order_id, status, _now())
    return redirect(url_for("admin.order_detail_page", order_id=order_id))


def _window() -> int:
    return current_app.config.get("RETURN_WINDOW_SECONDS", 7 * 86400)


@bp.post("/orders/<int:order_id>/expire-return")
@login_required
def order_expire_return(order_id):
    life_svc.fast_forward_return_window(_conn(), order_id, _now(), _window())
    return redirect(url_for("admin.order_detail_page", order_id=order_id))


# ---- 退货审核 ----
@bp.get("/returns")
@login_required
def returns_page():
    return render_template(
        "returns.html", rows=life_svc.returns_list(_conn()), labels=lc.BRANCH_LABELS
    )


@bp.post("/returns/<int:order_id>/approve")
@login_required
def return_approve(order_id):
    life_svc.approve_return(_conn(), order_id, _now())
    return redirect(request.form.get("back") or url_for("admin.returns_page"))


@bp.post("/returns/<int:order_id>/reject")
@login_required
def return_reject(order_id):
    life_svc.reject_return(_conn(), order_id)
    return redirect(request.form.get("back") or url_for("admin.returns_page"))

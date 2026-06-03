## Context

后端读侧(catalog-browsing)+ 下单(ordering)已交付。数据表 6 张已建。本变更加**网页后台**(写侧 + 查看),复用现有 repositories/services 并补写侧。技术栈后台 = Flask + Jinja2 + Bootstrap 5(共识 §3)。

## Goals / Non-Goals

**Goals:** 单管理员登录 + 限速;分类 / 商品(含传图)/ 推荐位维护;订单查看;仪表盘。顺带把价格元↔分换算抽进 `rules`(闭合 DEBT-003)。
**Non-Goals:** 车机 `android/`、`deploy/`、操作日志、导出 Excel(共识 §7)、多管理员 / 角色。

## Decisions

- **服务端渲染**:Jinja2 模板 + Bootstrap 5(CDN),无前端构建。一个 `base.html` 布局 + 各页继承。
- **鉴权**:Flask `session`(密钥取自 `config`/env);`login_required` 装饰器保护 `/admin/*`;账号口令取自 config(不硬编码)。
- **登录限速**:**内存**实现的纯规则 `login_throttle`(IP→失败时间戳列表,5 次/5 分钟滑窗),纯函数 100% 单测;重启即清空(demo 可接受)。
- **价格换算**:新增纯规则 `money.yuan_to_cents` / `cents_to_yuan`,用 `Decimal(str(x))` 避免浮点,补 `X.X5` 单测——**闭合 [DEBT-003](../../../docs/tech-debt.md)**;seed 的换算也改用它。
- **推荐位 3~5**:纯规则 `banner_count_ok`(保存时校验),100% 单测。
- **传图**:`werkzeug.secure_filename` + uuid 命名,存 `uploads/`,记 URL;只接受常见图片扩展名。
- **分层**:后台视图(`server/admin/`)= 接口层,只收参 / 校验 / 调写侧 service / 渲染;写库在 service 用单事务。

## Risks / Trade-offs

- [内存限速重启清零] → demo 可接受;真要持久化再落库。
- [弱口令 + 公网] → 已知(共识 §3),**演示完 `launchctl unload` 下线**兜底。
- [一刀偏大] → 建议**分 3 批实现**:批1 骨架+登录+限速+仪表盘;批2 分类+商品+传图;批3 推荐位+订单。tasks 已按此分组,可分批 apply。

## Open Questions

- 真实商品图素材待用户提供(不阻塞;先占位)。

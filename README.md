# RIDEMALL 车载商店(商店4)

车载横屏购物 **demo**:车机 Android 商城 + 网页后台 + Flask/SQLite 后端。内部演示,不接真实支付。

## 文档导航
- 做什么 / 不做什么 → [需求共识.md](需求共识.md)
- 后端存啥 / 给啥接口 → [后端需求.md](后端需求.md)
- 系统架构 → [docs/architecture.md](docs/architecture.md) · 接口速查 → [docs/api.md](docs/api.md)
- 为什么这样设计 → [docs/adr/](docs/adr/)
- 视觉 / 交互还原 → [design_handoff_ridemall_carstore/README.md](design_handoff_ridemall_carstore/README.md)
- 工作纪律 → [CLAUDE.md](CLAUDE.md) · 测试方案 → `测试方案.md`(🚧 下一步)

## 运行(🚧 待代码落地后填实命令)
- 后端:`cd server` → 装依赖 → 跑 Flask → 本地 `http://localhost:<port>`
- 后台:浏览器开 `/admin`,账号见配置(默认见 [需求共识.md §3](需求共识.md))
- 车机:Android Studio 开 `android/`,跑 1920×1080 横屏模拟器
- 测试:`pytest`(后端)/ `gradle test`(车机),覆盖见 `测试方案.md`(🚧)

## 部署(Mac mini)
复用 `panqian-tunnel`,公网 **https://mall4.hearagain.space**。
演示完一键下线:`launchctl unload <plist>` → 公网立即 404。

> 本 README 是骨架;具体命令 / 端口 / 依赖随 `server/`、`android/` 落地补实。

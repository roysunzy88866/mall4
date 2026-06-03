# RIDEMALL 车载商店(商店4)

车载横屏购物 **demo**:车机 Android 商城 + 网页后台 + Flask/SQLite 后端。内部演示,不接真实支付。**本项目用 OpenSpec 管理变更**。

## 文档导航
- 做什么 / 不做什么 → [需求共识.md](需求共识.md)
- 后端存啥 / 给啥接口 → [后端需求.md](后端需求.md) · [docs/api.md](docs/api.md)
- 系统架构 → [docs/architecture.md](docs/architecture.md) · 为什么这样设计 → [docs/adr/](docs/adr/)
- 正式需求 + 场景(真相源) → `openspec/specs/` · 变更提案 → `openspec/changes/`
- 测试方案 → [测试方案.md](测试方案.md) · 技术债 → [docs/tech-debt.md](docs/tech-debt.md)
- 视觉 / 交互还原 → [design_handoff_ridemall_carstore/README.md](design_handoff_ridemall_carstore/README.md)
- 工作纪律 → [CLAUDE.md](CLAUDE.md)

## 运行后端 / 测试
```bash
python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
.venv/bin/python run.py            # 起后端(端口/路径走 env,见 server/config.py)
.venv/bin/python -m pytest         # 跑测试 + rules 100% 覆盖率闸门
.venv/bin/pre-commit install       # 装提交硬卡(测试不绿则 commit 被拦)
```
- 环境变量:`RIDEMALL_PORT` / `RIDEMALL_DB` / `RIDEMALL_UPLOADS`(均有默认值)。
- 已就位:车机只读接口 `GET /api/home|categories|categories/<id>/products|products/<id>`。
- 🚧 后续 change:下单接口、后台网页 + 登录、车机 `android/`。

## 部署(Mac mini)
复用 `panqian-tunnel`,公网 **https://mall4.hearagain.space**。演示完一键下线:`launchctl unload <plist>` → 公网立即 404。

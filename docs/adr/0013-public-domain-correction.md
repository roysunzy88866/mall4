# ADR-0013 · 公网域名校正:mall4 → mall4-admin

- **状态**:Accepted(2026-06-04)
- **范围**:共识 §3 锚点 F6(公网域名)+ Android release 构建配置 + 全部文档。
- **触发**:前后端联动测试时实测发现域名与现实不符。

## 背景

共识 2026-06-03 记录"公网域名改用品牌子域 `mall4.hearagain.space`"(F6),据此 `build.gradle.kts` release 配置、README、architecture、CLAUDE.md、openspec/project.md 全写成 `mall4`。

2026-06-04 实测(证据):
- `dig mall4.hearagain.space` → **无解析记录**;`curl https://mall4.hearagain.space/api/home` → SSL 连接失败(域名根本没架起来)。
- `dig mall4-admin.hearagain.space` → 解析到 Cloudflare(104.21.21.168 / 172.67.199.162);`/api/home` → **HTTP 200**。
- 模拟器在装 APK 的 OkHttp 日志显示它请求的是 `https://mall4-admin.hearagain.space`(实际在跑的就是它)。
- git 提交 `084d593`「真后台上线 mall4-admin.hearagain.space」也印证真部署落在 `mall4-admin`。

即:**"计划用的"(mall4)从未配置 DNS/隧道,"实际在跑的"(mall4-admin)才是真**。

## 决策

以现实为准,公网域名统一为 **`mall4-admin.hearagain.space`**(用户 2026-06-04 明文拍板)。
文档 / `build.gradle.kts` release / 共识 §3 F6 全部从 `mall4` 校正为 `mall4-admin`。
品牌子域 `mall4` 作为未来可选项登记技术债(DEBT-2026-06-010),非阻塞演示。

## 后果

- release APK 指向 `mall4-admin`(可连);本地 debug 仍走 `10.0.2.2:8000`(不变)。
- 若日后要启用品牌域 `mall4`:配 `mall4` 子域 DNS + Cloudflared 路由 → 改 release URL + 文档 → 重出 release 包(见 DEBT-2026-06-010)。
- 后台口令、部署路径、隧道(panqian-tunnel)等其余部署事实不变。

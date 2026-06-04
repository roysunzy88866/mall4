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
- 环境变量:`RIDEMALL_PORT`(默认 8000)/ `RIDEMALL_DB` / `RIDEMALL_UPLOADS`(均有默认值)。
- 已就位:车机只读 + 下单接口 + 网页后台(`/admin`,**浅色重做** + 订单状态管理/物流);车机端 `android/` 浏览主干 + 购物车下单闭环 + **订单状态机 + mock 物流 + 订单详情**。
- 状态机:`paid(待发货)→shipping→delivering→delivered`,按 `RIDEMALL_STATUS_STEP_SECONDS`(默认 30 秒)惰性自动推进,后台可快进/改状态。
- **取消 + 7 天无理由退货**:签收前可取消(自动+mock退款);签收后 7 天内(`RIDEMALL_RETURN_WINDOW_SECONDS`)可申请退货 → 后台审核(通过=已退款 mock/拒绝);后台「退货审核」页 + 订单详情就地审核 + 快进窗口。
- ✅ ADR-0012 订单全生命周期升级**已全部落地**(购物车/多商品/状态机/物流/取消/退货)。

## 运行车机端(Android)
工程在 [`android/`](android/)(Kotlin + Jetpack Compose,minSdk 26)。需 JDK 17 + Android SDK(platform android-34)。
```bash
# 1) 先起后端(车机经 10.0.2.2 访问宿主回环;debug base URL 已配 http://10.0.2.2:8000)
.venv/bin/python run.py

# 2) 新建 1920×1080 横屏模拟器 ridemall-car(本机无 avdmanager 时用克隆法:
#    复制已有 AVD 目录 + 改 config.ini 的 hw.lcd.width/height=1920/1080、skin.name=1920x1080)
#    本仓已建好 ridemall-car;若要重建见 docs/architecture.md「车机端」。
~/Library/Android/sdk/emulator/emulator -avd ridemall-car -no-snapshot -gpu auto &

# 3) 编译 + 安装 + 启动(用 JDK 17)
cd android
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :app:assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
~/Library/Android/sdk/platform-tools/adb shell am start -n space.hearagain.ridemall/.MainActivity

# 车机端纯逻辑单测(价格/图URL/映射/设备号/轮播)
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :app:testDebugUnitTest
```
- base URL 走 `BuildConfig.API_BASE_URL`:debug=`http://10.0.2.2:8000`(模拟器→宿主),release=`https://mall4.hearagain.space`(公网)。不硬编码。
- 车机本地只存一个设备号(SharedPreferences),不缓存任何业务数据。

## 部署(Mac mini)
复用 `panqian-tunnel`。**真后台(Flask 全应用)已上线**:
- 公网:**https://mall4.hearagain.space**(`/admin` 后台 + `/api` 车机接口);账号 `admin / ‹REDACTED›`。
- Mac mini 上:代码 `~/ridemall4/`、Python 3.11 venv、launchd `com.user.ridemall4`(端口 18776,cloudflared 已路由)、启动脚本 `~/ridemall4/run_prod.sh`、日志 `~/ridemall4/.logs/`。
- 更新部署:`rsync server run.py macmini:~/ridemall4/ && ssh macmini 'launchctl kickstart -k gui/$(id -u)/com.user.ridemall4'`。

> 🔴 **安全债(共识 §3 已接受)**:弱口令 + 公网。**演示完必须一键下线**:
> ```
> ssh macmini 'launchctl unload ~/Library/LaunchAgents/com.user.ridemall4.plist'
> ```
> 下线后公网 502/404。车机 release 包指向公网;debug 包指向 `10.0.2.2:8000` 本地。

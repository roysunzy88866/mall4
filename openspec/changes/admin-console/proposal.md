## Why

车机两侧(浏览 `catalog-browsing` + 下单 `ordering`)已通,但商品/分类/推荐位现在只能靠 seed,改不了;订单也没法在后台看。这一刀做**网页后台**,让管理员能维护数据、看订单——闭合 B1(管理员维护)+ B3(后台看订单)。

## What Changes

- 网页后台:**Flask + Jinja2 服务端渲染 + Bootstrap 5**,单管理员登录 + 登录失败限速。
- **分类管理**:增 / 删 / 改名 / 改排序 / 停用(删有商品的分类拒绝,D9)。
- **商品管理**:增 / 删 / 改 / 传图 / 选分类 / 价格(两位小数)/ 描述 / 库存(仅展示)。
- **推荐位管理**:从所有商品挑入 Banner、上移 / 下移;保存强制 **3~5**(F4a)。
- **订单查看**:倒序列出 时间 / 商品 / 价格 / 设备号(手动 F5,A7)。
- **仪表盘**:商品数 / 订单数 / 营业额 / 最近 5 笔订单。
- 顺手把**价格元↔分换算抽进 `rules`(用 Decimal)**,闭合 [DEBT-003](../../../docs/tech-debt.md)。

不在本变更:车机 `android/`、`deploy/`、操作日志 / 导出(共识 §7 暂不做)。无 **BREAKING**。

## Capabilities

### New Capabilities
- `admin-console`: 管理员网页后台——登录与限速、分类 / 商品 / 推荐位维护、订单查看、仪表盘。

### Modified Capabilities
（无——`catalog-browsing` / `ordering` 行为不变;后台写入复用其数据表。）

## Impact

- 新增 `server/admin/`(Jinja2 模板 + 视图)、写侧 repositories / services、图片上传到 `server/uploads/`。
- session 密钥、管理员账号口令走 `config`/env(不硬编码)。新依赖:无(session、Jinja2 均 Flask 自带)。
- 登录限速可内存实现(后端需求 §1 注;不一定落库)。

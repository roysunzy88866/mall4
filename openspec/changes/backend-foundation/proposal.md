## Why

车机端和后台背后什么都还没有,任何功能都得先有后端地基(应用骨架 + 数据库 + seed)。这个变更立起地基,并交付**第一个可演示能力**:车机能浏览一个已预置数据的商店(推荐页 / 分类 / 详情)。下单与后台留给后续变更。

## What Changes

- 新建 Flask 后端骨架,按务实分层(接口/业务/数据/表 + rules 纯函数,见 [ADR-0001](../../../docs/adr/0001-backend-architecture.md))。
- 配置走 `config.py` / env(端口、DB 路径、uploads 路径、后台口令),**不硬编码**。
- 建 SQLite 数据模型:`categories` / `products` / `product_images` / `banners` / `orders` / `order_items`(本变更只建表与只读用途;下单写入、后台维护在后续变更)。
- 首次启动 seed:5 分类 + ~18 商品(用设计稿 `data.js` 数值)+ 占位图 + 4 推荐位 + 3~5 笔演示订单(挂在演示设备号)。
- 车机**只读接口**:`GET /api/home`、`/api/categories`、`/api/categories/{id}/products`、`/api/products/{id}`。
- 业务规则首批(rules,纯函数,100% 单测):为你推荐排序(上架倒序)、停用分类过滤、Banner 文案自动套。
- 单元 + 集成测试随接口落(见 [测试方案.md](../../../测试方案.md))。

不在本变更:下单 `POST /api/orders`、后台网页、图片上传 UI(均为后续变更)。无 **BREAKING**(全新)。

## Capabilities

### New Capabilities
- `catalog-browsing`: 车机端从后端浏览商品目录——推荐页(banner + 为你推荐)、分类商品、商品详情、导航分类。

### Modified Capabilities
（无——首个变更,`openspec/specs/` 尚为空。）

## Impact

- 新增 `server/`(务实分层目录)、SQLite DB 文件、`server/uploads/`。
- 依赖:Flask + SQLite ——**已在 [需求共识.md §3](../../../需求共识.md) 决策**,不需新 ADR。
- 定义 `X-Device-Id` 请求头约定(本变更读接口未用,为后续下单/订单铺路)。

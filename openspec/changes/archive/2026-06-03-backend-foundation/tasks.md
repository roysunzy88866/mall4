## 1. 骨架与配置

- [x] 1.1 建 `server/` 务实分层目录(`api/` `services/` `repositories/` `models/` `rules/` + `app.py` `config.py` `seed.py`)
- [x] 1.2 `config.py`:端口 / DB 路径 / uploads 路径 / 后台口令 走 env,**不硬编码**
- [x] 1.3 加依赖(Flask 等);记录「Flask+SQLite 已在需求共识 §3 决策,无需新 ADR」
- [x] 1.4 搭测试闸门:`pytest` + 覆盖率门槛(`rules` 目标 100%,见 [测试方案.md](../../../测试方案.md));先放一条占位失败测试确认闸门生效

## 2. 数据模型与 seed

- [x] 2.1 定义 6 张 SQLite 表:`categories` `products` `product_images` `banners` `orders` `order_items`(字段以 后端需求.md 数据模型为准)
- [x] 2.2 `seed.py`:库空时预置 5 分类 + ~18 商品(data.js 数值)+ 占位图 + 4 推荐位 + 3~5 笔演示订单;幂等

## 3. 业务规则(rules · 纯函数 · 先写失败测试再实现)

- [x] 3.1 为你推荐排序(上架时间倒序)+ 单测
- [x] 3.2 停用分类过滤(停用类商品不进 home/为你推荐)+ 单测
- [x] 3.3 Banner 文案自动套(标题=商品名 / 价=商品价 / 描述=节选)+ 单测

## 4. 只读接口(每写一个立刻集成测试)

- [x] 4.1 `GET /api/categories`(启用、按排序)+ 集成测试
- [x] 4.2 `GET /api/categories/{id}/products`(含空态)+ 集成测试
- [x] 4.3 `GET /api/products/{id}`(含不存在 → 404「已下架」)+ 集成测试
- [x] 4.4 `GET /api/home`(banner + 为你推荐、排除停用)+ 集成测试

## 5. 校验

- [x] 5.1 全部单元 + 集成测试绿,覆盖率达标(`rules` 100%;三层见 [测试方案.md](../../../测试方案.md))
- [x] 5.2 `openspec validate backend-foundation --strict` 通过

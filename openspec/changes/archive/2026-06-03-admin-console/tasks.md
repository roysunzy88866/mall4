> 建议分 3 批 apply(见 design):批1 = 组 1~3,批2 = 组 4~5,批3 = 组 6~7;组 8 收尾。

## 1. 纯规则(rules · 先写失败测试 · 100% 覆盖)

- [x] 1.1 `rules/money`:`yuan_to_cents`(Decimal,防浮点)/ `cents_to_yuan` + 单测(含 `19.95`、`X.X5`);**闭合 DEBT-003**,seed 改用它
- [x] 1.2 `rules/admin`:`login_throttle`(IP 失败时间戳 + now → 是否限速,5 次/5 分钟)+ 单测
- [x] 1.3 `rules/admin`:`banner_count_ok`(数量 3~5)+ 单测

## 2. 后台骨架 + 登录 + 限速

- [x] 2.1 `server/admin/` 蓝图 + `base.html`(Bootstrap5 CDN)+ `login_required` 装饰器;session 密钥走 config
- [x] 2.2 `/admin/login`(GET/POST):校验账号口令 + 调 `login_throttle`;成功建 session,失败/限速提示 + 集成测试(成功/错误/连错5次/未登录重定向)

## 3. 仪表盘

- [x] 3.1 `/admin`:商品数 / 订单数 / 营业额(已支付合计)/ 最近 5 单 + 集成测试

## 4. 分类管理

- [x] 4.1 写侧 repo/service:增 / 改名 / 改排序 / 停用 / 删(删有商品→拒)
- [x] 4.2 `/admin/categories` 页面 + 集成测试(增、改名、停用、删空、删有商品被拒)

## 5. 商品管理 + 传图

- [x] 5.1 写侧 repo/service:增 / 改 / 删;价格经 `yuan_to_cents`
- [x] 5.2 传图:`secure_filename` + uuid 存 uploads,记 URL,限图片扩展名
- [x] 5.3 `/admin/products` 页面 + 集成测试(创建含传图、改价 19.95 无误差、删商品)

## 6. 推荐位管理

- [x] 6.1 写侧 service:挑入 / 上移下移 / 保存校验 `banner_count_ok`
- [x] 6.2 `/admin/banners` 页面 + 集成测试(调序、<3 被拒、>5 被拒)

## 7. 订单查看

- [x] 7.1 `/admin/orders`:全部订单倒序(时间/商品/价/设备号)+ 集成测试

## 8. 校验

- [x] 8.1 全部单元 + 集成测试绿,`rules` 100% 覆盖
- [x] 8.2 `scripts/archive admin-console` 前置门禁通过(pytest + `openspec validate --strict`)

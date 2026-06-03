## 1. 数据层(详情 + 订单 + 纯逻辑红绿)

- [x] 1.1 先写失败单测:购物车 add/merge、20 种上限、clampQty(1~99)、selectedTotalCents、badgeCount、toOrderItems、倒计时 `secondsToClock`(179→"02:59")、订单 DTO→Model 映射、status→中文映射
- [x] 1.2 详情 DTO(`ProductDetailDto` 含 images)+ `ApiService.productDetail(id)`;订单 DTO(`OrderDto`/`OrderItemDto`)+ `ApiService.createOrder(body)` / `orders()`
- [x] 1.3 Repository:`productDetail`、`createOrder(items)`、`orders()`;DTO↔Model 映射(价格 ¥、图 URL、status 中文)跑绿 1.1

## 2. 购物车状态与纯逻辑

- [x] 2.1 `CartItem`(product 快照 + qty + selected)、`OrderDraft`(下单草稿,锁价快照)
- [x] 2.2 ViewModel 加 `cart` 内存态 + 操作(加/改数量/删/勾选/全选/清空);角标数;纯逻辑用 1.x 函数

## 3. 商品详情页

- [x] 3.1 详情屏:主图 + 缩略图(点切)+ 名称/价格/3 固定标签/描述 +「加入购物车」「立即购买」
- [x] 3.2 商品卡/Banner 点击进详情(接回 car-browsing 里预留的 onProductClick/onBannerClick)
- [x] 3.3 商品已下架(404)优雅提示

## 4. 购物车页 + 入口

- [x] 4.1 右上角购物车图标 + 数量角标(全局,叠在内容区右上)
- [x] 4.2 购物车页:列表(图/名/价/数量±/删/勾选)、默认全选、合计=勾选项、上限提示、「结算」勾中项;空态

## 5. 确认 / 支付 / 成功

- [x] 5.1 确认订单页:固定收货 + 多商品清单 + 合计 +「去支付 ¥X」;进页锁定 OrderDraft(快照)
- [x] 5.2 扫码支付覆盖层:二维码(伪)+ 179s 倒计时(02:59,≤30s 变红)+ 提示;点屏 → POST /api/orders → 成功才进成功态、失败弹网络异常;归零仅提示超时、点屏仍可成功
- [x] 5.3 支付成功覆盖层:对勾 + 1.5s → 订单历史页(导航高亮回订单)

## 6. 订单历史页

- [x] 6.1 订单历史:`GET /api/orders` 倒序,每条 时间/状态(中文)/多商品/合计/固定收货;空态「暂无订单」
- [x] 6.2 立即购买路径:详情「立即购买」→ 直接确认页(单件草稿,跳过购物车)

## 7. 验证与收尾

- [x] 7.1 编译 + 跑绿全部 JVM 单测(`testDebugUnitTest`)
- [x] 7.2 装到 `ridemall-car` 手测闭环:详情→加购物车→购物车勾选→结算→确认→支付(点屏)→成功→订单历史可见;立即购买;断网下单失败弹网络异常。截图存证
- [x] 7.3 提交前后端 `pytest` 仍绿;`openspec validate cart-checkout --strict` 过;README 车机端段落补一句闭环已通

## 1. 纯函数(红绿)

- [x] 1.1 先写失败单测:`can_cancel(status)`、`can_request_return(status, delivered_elapsed, window)`(窗口边界 0/=window/>window)、BRANCH_LABELS
- [x] 1.2 实现 rules/lifecycle 扩展,跑绿(rules 仍 100%)

## 2. schema + 服务

- [x] 2.1 orders 加 `return_reason/return_note/cancelled_at`(schema + 幂等迁移)
- [x] 2.2 lifecycle_service:cancel_order / request_return / approve_return / reject_return / fast_forward_return_window(校验设备 + 状态 + 事务)
- [x] 2.3 order_detail/list 输出 `can_cancel`/`can_return` 标志(后端算窗口)+ 分支态物流补行

## 3. 接口

- [x] 3.1 车机 `POST /api/orders/{id}/cancel`、`POST /api/orders/{id}/return`(reason/note,设备校验,失败 409/403)
- [x] 3.2 后台 `GET /admin/returns`、`POST /admin/returns/{id}/approve|reject`、订单详情快进退货窗口
- [x] 3.3 集成测试:cancel/return/approve/reject/窗口/设备校验/营业额扣减

## 4. 后台退货审核页

- [x] 4.1 退货审核页(申请列表:订单/商品/原因/说明/状态 + 通过/拒绝)+ 侧栏入口 + 仪表盘退货待审计数
- [x] 4.2 订单详情:return_review 时就地通过/拒绝;delivered 时「快进退货窗口到过期」

## 5. 车机取消/退货

- [x] 5.1 车机订单详情:can_cancel→「取消订单」;can_return→「申请退货」(原因下拉+说明)→ POST
- [x] 5.2 分支态(已取消/退货审核中/已退款/退货被拒)徽章 + 物流分支展示

## 6. 验证收尾

- [x] 6.1 pytest 全绿 + rules 100%;车机单测绿;`openspec validate cancel-and-return --strict`
- [x] 6.2 后台手测(退货审核通过/拒绝、快进窗口)+ 车机手测(取消、申请退货)截图
- [x] 6.3 docs 同步;后台浅色快照(含退货审核页)deploy 到 mall4-admin.hearagain.space

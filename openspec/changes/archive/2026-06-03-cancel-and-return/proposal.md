## Why

订单状态机 + 物流已就绪,补上**取消 + 7 天无理由退货**——这是 ADR-0012 第四刀,补齐订单全生命周期。对应共识 F14(取消)/F15(退货)/B6/B7。

## What Changes

- **取消订单**(签收前):订单状态 `paid/shipping/delivering`(未签收)时,用户可取消 → 自动生效:状态 `cancelled`、mock 退款、物流转「已拦截/退回」。
- **7 天无理由退货**(签收后):订单 `delivered` 且在退货窗口内(`RIDEMALL_RETURN_WINDOW_SECONDS`,默认 7 天)→ 用户填原因 + 选填说明 → 状态 `return_review`(退货审核中)。
- **后台审核**:`return_review` 订单,管理员通过 → `returning`(退货中)→ `refunded`(已退款,mock);拒绝 → `return_rejected`(退货被拒)。
- **接口**:车机 `POST /api/orders/{id}/cancel`、`POST /api/orders/{id}/return`(带原因/说明);后台 `POST /admin/returns/{id}/approve|reject`、退货审核页 `GET /admin/returns`、订单详情就地审核 + 「快进退货窗口到过期」演示按钮。
- **车机**:订单详情按状态显示「取消订单」(签收前)/「申请退货」(签收后 7 天内)按钮 + 退货表单;状态/物流相应展示分支态。
- **后台**:退货审核页(申请列表 + 通过/拒绝)+ 订单详情就地审核 + 仪表盘退货待审计数。

## Capabilities

### New Capabilities
- `returns`: 取消订单(签收前自动)+ 7 天无理由退货(签收后,后台审核)+ 退款 mock + 后台退货审核 + 车机取消/退货入口。建立在 `order-lifecycle` 状态机上。

### Modified Capabilities
<!-- 以新能力承载;消费 order-lifecycle 的状态机与 delivered_at。 -->

## Impact

- **orders 表 +3 列**:`return_reason`、`return_note`、`cancelled_at`(幂等迁移)。
- **rules/lifecycle 加纯函数**:`can_cancel`、`can_request_return`(窗口判定)、退货状态流转;仍 100% 覆盖。
- **不破**:已建状态机;`换货`不做(共识 A12)。
- **配置**:`RIDEMALL_RETURN_WINDOW_SECONDS`(默认 7 天)。

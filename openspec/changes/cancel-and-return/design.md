## Context

`order-lifecycle` 已建状态机(paid→…→delivered)+ `delivered_at`。本刀加分支态:`cancelled`(签收前)、`return_review/returning/refunded/return_rejected`(签收后)。

## Goals / Non-Goals

**Goals:** 取消(签收前自动)+ 7 天退货(签收后,后台审核)+ 退款 mock + 后台退货审核页/就地审核 + 车机取消/退货入口 + 可配窗口/快进。

**Non-Goals:** 换货(A12 不做);真实退款。

## Decisions

### D1 · 分支态复用 status + 退货字段
orders 加 `return_reason`、`return_note`、`cancelled_at`。退货状态走 `status`(return_review→returning→refunded / return_rejected);无需新表(退货审核页 = 查 status 在退货态的订单 + 其 reason/note)。`manual=1`(分支态由动作驱动,停自动推进)。

### D2 · 纯函数(rules/lifecycle 扩展,100% 测)
`can_cancel(status)`(主阶段且未签收 = stage_index 0~2)、`can_request_return(status, delivered_elapsed, window)`(delivered 且 0≤elapsed≤window)、`return_after_approve()`=refunded、分支态加入 STAGE_LABELS? 否——STAGE_LABELS 仅主阶段;分支态单列 `BRANCH_LABELS`。

### D3 · service 动作(事务 + 校验)
`cancel_order(conn, id, device_id, now)`:校验属本设备 + can_cancel → status=cancelled, cancelled_at, manual=1。
`request_return(conn, id, device_id, reason, note, now, window)`:校验 + can_request_return(now-delivered_at) → status=return_review, 存 reason/note。
`approve_return / reject_return(conn, id, now)`:return_review → refunded / return_rejected。
`fast_forward_return_window(conn, id, now, window)`:delivered_at = now - window - 60(令过期),演示用。

### D4 · 接口
车机:`POST /api/orders/{id}/cancel`、`POST /api/orders/{id}/return`(body reason/note;带 X-Device-Id)。失败返 409/403。
后台:`GET /admin/returns`、`POST /admin/returns/{id}/approve|reject`、订单详情就地审核 + 退货窗口快进。

### D5 · 车机 UI
订单详情:`can_cancel` → 「取消订单」按钮(确认后 POST cancel);`can_request_return` → 「申请退货」→ 弹原因选择(下拉)+ 说明 → POST return。分支态(已取消/退货审核中/已退款/退货被拒)在步条下方以徽章+说明展示。窗口判定:车机用 `GET /api/orders/{id}` 返回的 `can_cancel`/`can_return` 标志(后端算好,车机不自己算时间)。

### D6 · 物流分支
取消 → 物流补「已拦截/退回」节点(展示文案);退款 → 「已退款」。简化:car/admin 详情在分支态时,物流时间线追加一行分支说明。

### D7 · 测试
rules:can_cancel、can_request_return(窗口边界)。集成:cancel 改 cancelled、delivered 不可 cancel;return 窗口内/外;approve→refunded、reject→rejected;营业额扣减;设备校验(他人单不可取消)。

## Risks / Trade-offs

- **R1 退货 reason 校验**:demo 接受任意非空原因,前端给下拉;后端只存。
- **R2 窗口边界**:用 `<=` 含端点;演示靠后台快进过期复演「不可退」。
- **R3 returning→refunded 一步到位**:demo 通过即退款(不模拟物流回寄),`returning` 作为瞬时/可省;实现里通过即置 refunded,保留 returning 文案在审核记录。

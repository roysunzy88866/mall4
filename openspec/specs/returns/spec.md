# returns Specification

## Purpose
TBD - created by archiving change cancel-and-return. Update Purpose after archive.
## Requirements
### Requirement: 取消订单(签收前)

订单在**未签收**(状态 `paid`/`shipping`/`delivering`)时,用户 SHALL 可取消 → 自动生效(不审核):状态转 `cancelled`、记取消时刻、物流转「已拦截/退回」语义。已签收(`delivered`)及之后 MUST NOT 可取消(改走退货)。取消 MUST 校验订单属于该设备号。

#### Scenario: 在途可取消
- **WHEN** 用户对一笔 `shipping` 订单请求取消
- **THEN** 状态变为 `cancelled`

#### Scenario: 已签收不可取消
- **WHEN** 用户对一笔 `delivered` 订单请求取消
- **THEN** 拒绝(不改状态),提示改走退货

### Requirement: 7 天无理由退货(签收后)

订单 `delivered` 且距签收时刻在退货窗口(`RIDEMALL_RETURN_WINDOW_SECONDS`,默认 7 天)内时,用户 SHALL 可申请退货:提交退货原因(+ 选填说明)→ 状态转 `return_review`(退货审核中)。超窗口 MUST NOT 可申请。

#### Scenario: 窗口内可申请退货
- **WHEN** 一笔刚签收的订单,用户提交退货原因
- **THEN** 状态变为 `return_review`,记录原因

#### Scenario: 超窗口不可退货
- **WHEN** 距签收已超过退货窗口
- **THEN** 拒绝申请(不改状态)

### Requirement: 后台退货审核

`return_review` 订单,后台 SHALL 可**通过**(状态 → `returning` → `refunded`,退款 mock)或**拒绝**(状态 → `return_rejected`)。后台退货审核页 SHALL 列出待审/已处理的退货申请。后台 SHALL 提供「快进退货窗口到过期」演示操作。

#### Scenario: 通过退货
- **WHEN** 管理员通过一笔 `return_review` 退货
- **THEN** 状态变为 `refunded`(已退款)

#### Scenario: 拒绝退货
- **WHEN** 管理员拒绝一笔 `return_review` 退货
- **THEN** 状态变为 `return_rejected`

### Requirement: 营业额与展示

营业额 SHALL 扣除 `cancelled` 与 `refunded` 订单(共识 A13)。车机订单详情 SHALL 按状态显示「取消订单」(签收前)/「申请退货」(签收后窗口内)入口及分支态展示。

#### Scenario: 退款后不计营业额
- **WHEN** 一笔订单变为 `refunded`
- **THEN** 仪表盘营业额不含该单金额


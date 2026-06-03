# admin-console Specification

## Purpose
TBD - created by archiving change admin-console. Update Purpose after archive.
## Requirements
### Requirement: 管理员登录与限速
系统 SHALL 提供 `/admin/login` 校验固定管理员账号口令(取自配置);成功后建立登录 session,失败则拒绝。同一 IP 在 5 分钟内连续失败达 5 次,系统 MUST 限速拒绝后续尝试。未登录访问任何后台页 MUST 重定向到登录页。

#### Scenario: 登录成功
- **WHEN** 管理员用正确账号口令提交登录
- **THEN** 建立 session 并跳转到后台仪表盘

#### Scenario: 口令错误被拒
- **WHEN** 管理员提交错误口令
- **THEN** 停留在登录页并提示失败,不建立 session

#### Scenario: 连错 5 次被限速
- **WHEN** 同一 IP 在 5 分钟内登录失败累计 5 次后再次尝试
- **THEN** 系统拒绝并提示稍后再试(即使口令正确)

#### Scenario: 未登录访问后台被重定向
- **WHEN** 未登录访问任一 `/admin/...` 页面
- **THEN** 重定向到 `/admin/login`

### Requirement: 分类管理
系统 SHALL 允许管理员 增 / 改名 / 改排序 / 停用 / 删 分类。删除一个**仍含商品**的分类 MUST 被拒绝并提示先清空。

#### Scenario: 创建分类
- **WHEN** 管理员提交新分类名
- **THEN** 分类被创建并出现在列表

#### Scenario: 停用分类
- **WHEN** 管理员停用某分类
- **THEN** 该分类 `is_active` 置否(车机端据此整类隐藏,见 catalog-browsing)

#### Scenario: 删有商品的分类被拒
- **WHEN** 管理员删除一个仍含商品的分类
- **THEN** 系统拒绝并提示「请先清空该分类」

### Requirement: 商品管理
系统 SHALL 允许管理员 增 / 改 / 删 商品,可选所属分类、设价格(两位小数)、描述、库存(仅展示),并上传商品图。价格 MUST 以两位小数录入、后端以「分」存储且换算无误差。

#### Scenario: 创建商品含传图
- **WHEN** 管理员提交新商品(名 / 分类 / 价 / 描述)并上传一张图
- **THEN** 商品被创建,图存入 uploads 并以 URL 关联

#### Scenario: 改价两位小数无误差
- **WHEN** 管理员把某商品价格改为如 `19.95`
- **THEN** 后端存为 `1995` 分,车机展示回 `¥19.95`(不偏分)

### Requirement: 推荐位管理
系统 SHALL 允许管理员从所有商品挑选放入首页推荐位并上移 / 下移排序。保存时推荐位数量 MUST 在 **3~5** 之间,否则拒绝并提示。

#### Scenario: 调整推荐位顺序
- **WHEN** 管理员对某推荐位上移 / 下移
- **THEN** 其 `sort_order` 相应变化,车机首页 Banner 顺序随之变

#### Scenario: 少于 3 个被拒
- **WHEN** 管理员保存的推荐位少于 3 个
- **THEN** 系统拒绝并提示数量需在 3~5

#### Scenario: 多于 5 个被拒
- **WHEN** 管理员保存的推荐位多于 5 个
- **THEN** 系统拒绝并提示数量需在 3~5

### Requirement: 订单查看
系统 SHALL 在后台倒序列出全部订单,每条显示 下单时间 / 商品 / 价格 / 设备号;手动刷新(不自动)。

#### Scenario: 列出全部订单
- **WHEN** 管理员打开订单页
- **THEN** 按时间倒序看到所有设备的订单(含设备号),手动 F5 可见最新

### Requirement: 仪表盘
系统 SHALL 在后台首页显示 商品数 / 订单数 / 营业额(已支付订单合计)/ 最近 5 笔订单。

#### Scenario: 仪表盘统计正确
- **WHEN** 管理员打开仪表盘
- **THEN** 商品数、订单数、营业额(= 已支付订单合计)、最近 5 笔订单 与库中数据一致


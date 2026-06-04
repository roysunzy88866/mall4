## MODIFIED Requirements

### Requirement: 左侧常驻导航

车机 APP SHALL 在左侧常驻一条导航栏,自上而下为:`推荐`(固定首项)、后端启用分类(动态、按 `sort_order` 顺序,来自 `GET /api/categories`)、`订单`(固定末项)。当前所在项 MUST 呈选中态(高亮)。后台增删/停用分类、改名或改排序后,车机在切换到某分类 tab 时 MUST 重新拉取 `GET /api/categories` 并据此更新左侧导航,使名称与顺序反映后台最新状态。

#### Scenario: 导航项构成
- **WHEN** 后端返回 N 个启用分类
- **THEN** 导航显示「推荐」+ 这 N 个分类(按 `sort_order`)+「订单」,共 N+2 项

#### Scenario: 选中态跟随当前页
- **WHEN** 用户点击某个分类导航项
- **THEN** 右侧切到该分类页,且该导航项呈选中态、其余项为普通态

#### Scenario: 切分类刷新导航
- **WHEN** 后台改了某分类的名称或排序后,用户在车机点击某个分类 tab
- **THEN** 车机重新拉取分类列表,左侧导航的名称与顺序更新为后台最新状态

#### Scenario: 订单项进入订单页
- **WHEN** 用户点击「订单」导航项
- **THEN** 进入订单历史页(见 car-checkout),不报错

### Requirement: 为你推荐网格

推荐页 SHALL 在轮播下方以两列网格展示「为你推荐」商品,数据取自 `GET /api/home` 的 `recommended`(后端已保证为全部上架、排除停用分类、按上架时间倒序)。每个商品卡 MUST 展示商品图、名称与价格:商品主图 URL 非空时 MUST 加载真实图片,主图为空(null)时 MUST 回退到占位图块。当 `recommended` 为空时,推荐页 MUST 显示空态而非报错。

#### Scenario: 渲染推荐网格(有图)
- **WHEN** `GET /api/home` 返回若干带主图 URL 的推荐商品
- **THEN** 推荐页以两列网格渲染商品卡,卡片加载并显示真实商品主图、名称与价格,可纵向滚动

#### Scenario: 无主图回退占位
- **WHEN** 某推荐商品主图字段为 null
- **THEN** 该商品卡显示占位图块(不空白、不报错),名称与价格正常显示

#### Scenario: 推荐为空
- **WHEN** `recommended` 返回空列表
- **THEN** 推荐页显示空态,不崩溃

### Requirement: 分类页

点击某分类导航项 SHALL 在右侧展示该分类商品网格,数据取自 `GET /api/categories/{id}/products`;页面顶部 MUST 显示分类名与商品数量(「N 件商品」)。每个商品卡 MUST 按「主图非空加载真实图、为空回退占位」展示商品图、名称与价格。该分类无商品时,MUST 显示「该分类暂无商品」空态。

#### Scenario: 渲染分类网格
- **WHEN** 用户点击一个含商品的分类
- **THEN** 右侧以两列网格展示该类商品(主图/名/价,主图缺失则占位),顶部显示分类名与「N 件商品」

#### Scenario: 空分类
- **WHEN** 用户点击一个没有商品的分类
- **THEN** 右侧显示「该分类暂无商品」空态,不报错

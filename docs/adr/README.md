# ADR 索引 · RIDEMALL

架构决策记录(ADR)= **跨切片 / 架构 / 共识范围裁决**的编号档案。单个 change 的技术决策写在该 change 的 `openspec/changes/*/design.md`(归档留痕),不在此重复。

| ADR | 决策 | 来源 |
|---|---|---|
| [0001](0001-backend-architecture.md) | 后端务实分层 | 本轮(商店4) |
| [0002](0002-consensus-section1-tighten.md) | 共识 §1「车机端不存数据」措辞收紧 | 本轮 |
| [0004](0004-car-ui-ia-redesign.md) | 车机 UI/IA 改版(5 大分类、收货只展示、超时态 D-001 不实现) | 迁自 车载商店02版本 |
| [0005](0005-banner-recommendation.md) | 推荐位 3~5 张 + 上移/下移 | 迁自 02版本 |
| [0011](0011-post-payment-navigate-to-orders.md) | 支付成功后进订单页 | 迁自 02版本 |
| [0012](0012-order-lifecycle-upgrade.md) | 订单全生命周期升级(购物车/物流/退换货)—— 反转共识 §2 三项排除 | 本轮(商店4) |
| [0013](0013-public-domain-correction.md) | 公网域名校正 mall4 → mall4-admin(mall4 子域从未启用,以实际部署为准) | 本轮(商店4) |

> ⚠️ **0004 / 0005 / 0011 迁自上一轮项目(车载商店02版本)**。文内对 `docs/story-map.md`、`docs/user-stories.md`、`docs/architecture/backend.md` 等的「see also」链接指向旧目录结构,**本轮已用 OpenSpec `openspec/specs/` + `docs/architecture.md` 替代,那些链接可忽略**。决策结论本身仍有效,且已并入 [需求共识.md](../../需求共识.md) 正文。

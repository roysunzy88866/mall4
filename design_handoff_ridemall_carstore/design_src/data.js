/* RIDEMALL data — window.RM */
(function () {
  // category keys map to nav. Each product: id, name, price, cat, desc, tone(placeholder accent label)
  const P = (id, name, price, cat, desc) => ({ id, name, price, cat, desc });

  const products = [
    P('p1', '车载充电头 65W 快充', 39.00, 'car', '65W 大功率，双口 PD+QC 快充，铝合金外壳散热快，适配大部分车机与手机，盲插不伤接口。'),
    P('p2', '出风口手机支架', 35.00, 'car', '夹臂硅胶护垫，单手即可取放；卡扣稳固贴合空调出风口，过减速带不晃动。'),
    P('p3', '车载香氛 沉香木质', 49.00, 'car', '天然精油缓释，单次注油可持续约 30 天；出风口免打孔安装，木质沉静气息。'),
    P('p4', '行车记录仪 2K 星光夜视', 299.00, 'car', '2K 超清广角 140°，星光夜视降噪；24h 停车监控，紧急碰撞自动锁存。'),
    P('p5', '遮阳挡 隔热前挡', 35.00, 'car', '五层铝膜隔热反射，夏季车内降温更快；折叠收纳，适配多数车型前挡尺寸。'),
    P('p6', '真皮方向盘套 免缝', 89.00, 'car', '头层牛皮免手缝，按车型定制贴合；防滑透气，四季可用握感扎实。'),

    P('e1', '便携充电枪 7kW 家用', 459.00, 'energy', '7kW 家用便携充电，国标接口适配主流电车；过流过温多重保护，随车收纳包。'),
    P('e2', '车载逆变器 1200W', 199.00, 'energy', '点烟口/电瓶取电，纯正弦波输出 1200W；带双 USB，户外车内供电稳定。'),
    P('e3', '应急启动电源 12V', 269.00, 'energy', '大电流瞬时启动，亏电搭电一键唤醒；兼具充电宝与照明，自驾常备。'),

    P('o1', '车载充气泵 数显', 129.00, 'outdoor', '数显预设胎压自停，强光照明应急；金属缸体散热，3 分钟充满一胎。'),
    P('o2', '车顶行李箱 420L', 1299.00, 'outdoor', '420L 大容量，ABS 硬壳防水；双开锁设计，快拆横杆适配主流车型。'),
    P('o3', '便携折叠露营桌', 159.00, 'outdoor', '铝合金一秒展开，承重 30kg；收纳如手提箱，自驾露营轻量首选。'),
    P('o4', '车载保温箱 18L 冷暖', 339.00, 'outdoor', '点烟口供电制冷/保温，18L 容量；长途自驾饮品冰镇、热食保温两用。'),

    P('d1', 'HUD 抬头显示 OBD', 219.00, 'digital', 'OBD 即插即用，时速/转速/水温投射前挡；自动感光调亮，超速提醒。'),
    P('d2', '车载蓝牙 FM 发射器', 45.00, 'digital', '无损蓝牙 5.3，FM 调频播放手机音乐；双 USB 快充，老车焕新利器。'),
    P('d3', '胎压监测 太阳能 TPMS', 159.00, 'digital', '太阳能免布线，四轮实时胎压温度；漏气高温声光预警，安装免拆胎。'),

    P('q1', '车载迷你冰箱 4L', 219.00, 'life', '半导体制冷静音，4L 桌面/车载两用；可放 6 罐饮料，化妆品母婴皆宜。'),
    P('q2', '头枕颈椎记忆棉枕', 69.00, 'life', '慢回弹记忆棉，贴合颈椎曲线；可拆洗布套，长途驾驶缓解疲劳。'),
    P('q3', '车载垃圾桶 带盖', 29.00, 'life', '磁吸开合不洒漏，悬挂免占空间；内置替换袋槽，车内整洁清爽。'),
  ];

  // nav: key, label, icon (lucide-like svg path id)
  const nav = [
    { key: 'rec',     label: '推荐',     icon: 'sparkle' },
    { key: 'car',     label: '车载用品', icon: 'car' },
    { key: 'energy',  label: '加油充电', icon: 'bolt' },
    { key: 'outdoor', label: '户外旅行', icon: 'tent' },
    { key: 'digital', label: '3C数码',   icon: 'chip' },
    { key: 'life',    label: '品质生活', icon: 'cup' },
    { key: 'orders',  label: '订单',     icon: 'receipt', bottom: true },
  ];

  // hero slides for home
  const slides = [
    { kicker: 'RIDEMALL · 出行优选', title: '把补能装进\n后备箱', desc: '7kW 便携充电 · 1200W 逆变 · 应急启动，一套搞定旅途用电。', price: 459.00, pid: 'e1', label: 'HERO · 补能' },
    { kicker: '车机焕新', title: '65W 快充\n上车即满电', desc: '双口 PD+QC，铝合金散热，车机与手机同时高速回血。', price: 39.00, pid: 'p1', label: 'HERO · 快充' },
    { kicker: '安全在路上', title: '2K 星光夜视\n看得更清', desc: '140° 广角 · 24 小时停车监控 · 碰撞自动锁存。', price: 299.00, pid: 'p4', label: 'HERO · 记录仪' },
    { kicker: '说走就走', title: '420L 车顶箱\n装下整个假期', desc: 'ABS 硬壳防水，快拆横杆，露营装备从此有处安放。', price: 1299.00, pid: 'o2', label: 'HERO · 户外' },
  ];

  // recommended order for home grid (~10)
  const recIds = ['p1','p2','e3','p4','d1','o1','p3','q1','d3','p5','e2','q2'];

  // fixed demo address
  const address = { name: '张三', phone: '138****8888', loc: '上海市浦东新区 ×× 路 ×× 号' };

  // seed orders (reverse chrono); each: time, items:[{pid,qty}]
  const seedOrders = [
    { id: 'o-2026052914', time: '2026-05-29 14:32', items: [{ pid: 'p1', qty: 1 }] },
    { id: 'o-2026052911', time: '2026-05-29 11:08', items: [{ pid: 'p2', qty: 1 }] },
    { id: 'o-2026052819', time: '2026-05-28 19:45', items: [{ pid: 'd1', qty: 1 }] },
  ];

  const byId = {};
  products.forEach(p => { byId[p.id] = p; });

  window.RM = {
    products, byId, nav, slides, recIds, address, seedOrders,
    get: (id) => byId[id],
    inCat: (cat) => products.filter(p => p.cat === cat),
    fmt: (n) => n.toFixed(2),
    // tone hue per category for placeholder tint variety
    catLabel: { car:'车载用品', energy:'加油充电', outdoor:'户外旅行', digital:'3C数码', life:'品质生活' },
  };
})();

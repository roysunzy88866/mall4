/* RIDEMALL app root */
(function () {
const { useState, useEffect, useRef } = React;
const RM = window.RM;
const {
  NavRail, HomeScreen, CategoryScreen, DetailScreen, ConfirmScreen,
  PayScreen, SuccessScreen, OrdersScreen, NetworkErrorScreen,
  useTweaks, TweaksPanel, TweakSection, TweakRadio, TweakColor,
} = window;

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "accent": "#2fd6c4",
  "density": "特大",
  "homeLayout": "A",
  "slideSpeed": 3,
  "netError": false,
  "emptyData": false
}/*EDITMODE-END*/;

const ACCENTS = {
  "#2fd6c4": { ink: "#04201d" },
  "#4a8cff": { ink: "#04132e" },
  "#ff8a3c": { ink: "#2a1402" },
  "#b9f24a": { ink: "#16240a" },
};
const DENSITY = { "标准": 1, "大": 1.12, "特大": 1.24 };

function hexToRgb(h) {
  const m = h.replace('#', '');
  return [parseInt(m.slice(0, 2), 16), parseInt(m.slice(2, 4), 16), parseInt(m.slice(4, 6), 16)];
}
function applyAccent(el, hex) {
  if (!el) return;
  const [r, g, b] = hexToRgb(hex);
  const ink = (ACCENTS[hex] || { ink: '#06231f' }).ink;
  el.style.setProperty('--acc', hex);
  el.style.setProperty('--acc-ink', ink);
  el.style.setProperty('--acc-soft', `rgba(${r},${g},${b},.14)`);
  el.style.setProperty('--acc-line', `rgba(${r},${g},${b},.42)`);
  el.style.setProperty('--acc-glow', `rgba(${r},${g},${b},.30)`);
}

// ---- stage scaling: 1920x1080 letterbox ----
function fitStage() {
  const stage = document.getElementById('stage');
  if (!stage) return;
  const s = Math.min(window.innerWidth / 1920, window.innerHeight / 1080);
  stage.style.transform = `scale(${s})`;
}

function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const stageRef = useRef(null);

  // keep the 1920x1080 canvas letterboxed to fit any viewport
  useEffect(() => {
    fitStage();
    window.addEventListener('resize', fitStage);
    const id = setTimeout(fitStage, 80);
    return () => { window.removeEventListener('resize', fitStage); clearTimeout(id); };
  }, []);

  // routing
  const [view, setView] = useState({ screen: 'home' });
  const [activeNav, setActiveNav] = useState('rec');
  const [orders, setOrders] = useState(RM.seedOrders.slice());

  // apply accent + density to stage element
  useEffect(() => { applyAccent(stageRef.current, t.accent); }, [t.accent]);
  useEffect(() => {
    if (stageRef.current) stageRef.current.style.setProperty('--fs', DENSITY[t.density] || 1);
  }, [t.density]);

  // ---- navigation handlers ----
  const goNav = (key) => {
    setActiveNav(key);
    if (key === 'rec') setView({ screen: 'home' });
    else if (key === 'orders') setView({ screen: 'orders' });
    else setView({ screen: 'category', cat: key });
  };
  const openDetail = (pid) => setView({ screen: 'detail', pid });
  const goOrder = (pid) => setView({ screen: 'confirm', pid });
  const goPay = (pid) => setView({ screen: 'pay', pid });
  const paySuccess = () => {
    const pid = view.pid;
    const now = new Date();
    const z = (n) => String(n).padStart(2, '0');
    const time = `${now.getFullYear()}-${z(now.getMonth() + 1)}-${z(now.getDate())} ${z(now.getHours())}:${z(now.getMinutes())}`;
    setOrders(o => [{ id: 'o-' + now.getTime(), time, items: [{ pid, qty: 1 }] }, ...o]);
    setView({ screen: 'success' });
  };
  const successDone = () => { setActiveNav('rec'); setView({ screen: 'home' }); };
  const back = () => {
    if (view.screen === 'detail') goNav(activeNav);
    else if (view.screen === 'confirm') setView({ screen: 'detail', pid: view.pid });
    else goNav(activeNav);
  };

  // ---- main content router ----
  let main = null;
  switch (view.screen) {
    case 'home':
      main = <HomeScreen layout={t.homeLayout} slideSpeed={(t.slideSpeed || 3) * 1000} onOpen={openDetail} />;
      break;
    case 'category':
      main = <CategoryScreen cat={view.cat} empty={t.emptyData} onOpen={openDetail} />;
      break;
    case 'detail':
      main = <DetailScreen pid={view.pid} onBack={back} onOrder={goOrder} />;
      break;
    case 'confirm':
      main = <ConfirmScreen pid={view.pid} onBack={back} onPay={goPay} />;
      break;
    case 'orders':
      main = <OrdersScreen orders={t.emptyData ? [] : orders} onOpen={openDetail} />;
      break;
    default:
      main = <HomeScreen layout={t.homeLayout} slideSpeed={(t.slideSpeed || 3) * 1000} onOpen={openDetail} />;
  }

  // fullscreen overlays cover nav
  const overlay =
    view.screen === 'pay'
      ? <PayScreen pid={view.pid} onSuccess={paySuccess} onCancel={() => setView({ screen: 'confirm', pid: view.pid })} />
      : view.screen === 'success'
        ? <SuccessScreen onDone={successDone} />
        : t.netError
          ? <NetworkErrorScreen onRetry={() => setTweak('netError', false)} />
          : null;

  return (
    <div id="stage" ref={stageRef}>
      <NavRail active={activeNav} onNav={goNav} />
      <div className="content">{main}</div>
      {overlay}

      <TweaksPanel>
        <TweakSection label="排版 · 车机可读性" />
        <TweakRadio label="字号密度" value={t.density} options={['标准', '大', '特大']}
          onChange={(v) => setTweak('density', v)} />
        <TweakSection label="主题色" />
        <TweakColor label="强调色" value={t.accent}
          options={['#2fd6c4', '#4a8cff', '#ff8a3c', '#b9f24a']}
          onChange={(v) => setTweak('accent', v)} />
        <TweakSection label="推荐页版式（A/B/C 对比）" />
        <TweakRadio label="首页布局" value={t.homeLayout} options={['A', 'B', 'C']}
          onChange={(v) => { setTweak('homeLayout', v); setActiveNav('rec'); setView({ screen: 'home' }); }} />
        <TweakRadio label="幻灯切换" value={String(t.slideSpeed)} options={['2', '3', '5']}
          onChange={(v) => setTweak('slideSpeed', Number(v))} />
        <TweakSection label="状态演示" />
        <TweakRadio label="网络异常页" value={t.netError ? '显示' : '关闭'} options={['关闭', '显示']}
          onChange={(v) => setTweak('netError', v === '显示')} />
        <TweakRadio label="空数据态" value={t.emptyData ? '空' : '正常'} options={['正常', '空']}
          onChange={(v) => setTweak('emptyData', v === '空')} />
      </TweaksPanel>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('stage-wrap')).render(<App />);
})();

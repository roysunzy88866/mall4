/* RIDEMALL screens → window */
(function () {
const { useState, useEffect, useRef } = React;
const RM = window.RM;
const { ProductCard, Placeholder, Price, RMIcon, QRCode } = window;

// ============ HERO CAROUSEL (3 layout variants) ============
function HomeHero({ layout, onOpen, slideSpeed }) {
  const [i, setI] = useState(0);
  const slides = RM.slides;
  useEffect(() => {
    const t = setInterval(() => setI(p => (p + 1) % slides.length), slideSpeed);
    return () => clearInterval(t);
  }, [slideSpeed, slides.length]);

  const Dots = ({ center = true }) => (
    <div className="dots" style={center ? null : { justifyContent: 'flex-start', marginLeft: 64 }}>
      {slides.map((_, k) => (
        <span key={k} className={'dot' + (k === i ? ' on' : '')} onClick={() => setI(k)} />
      ))}
    </div>
  );

  const grad = (dir = '105deg') => ({
    background: `linear-gradient(${dir}, rgba(10,12,15,.95) 12%, rgba(10,12,15,.55) 46%, rgba(10,12,15,.05) 78%)`,
  });

  // ---- Variant A: classic wide banner ----
  if (layout === 'A') {
    return (
      <div>
        <div className="hero" style={{ height: 384 }}>
          {slides.map((s, k) => (
            <div key={k} className={'hero-slide' + (k === i ? ' on' : '')} onClick={() => onOpen(s.pid)}>
              <Placeholder className="" label={s.label} style={{ position: 'absolute', inset: 0 }} />
              <div className="hero-grad" style={grad()} />
              <div className="hero-copy">
                <div className="hero-kicker">{s.kicker}</div>
                <div className="hero-title" style={{ whiteSpace: 'pre-line' }}>{s.title}</div>
                <div className="hero-desc">{s.desc}</div>
                <div className="hero-price"><span className="cur">¥</span>{RM.fmt(s.price)}</div>
              </div>
            </div>
          ))}
        </div>
        <Dots />
      </div>
    );
  }

  // ---- Variant C: immersive full-bleed taller ----
  if (layout === 'C') {
    return (
      <div className="hero" style={{ height: 470 }}>
        {slides.map((s, k) => (
          <div key={k} className={'hero-slide' + (k === i ? ' on' : '')} onClick={() => onOpen(s.pid)}>
            <Placeholder className="" label={s.label} style={{ position: 'absolute', inset: 0 }} />
            <div className="hero-grad" style={{ background: 'linear-gradient(0deg, rgba(8,10,13,.96) 4%, rgba(8,10,13,.35) 46%, rgba(8,10,13,.65) 100%)' }} />
            <div className="hero-copy" style={{ maxWidth: '70%', alignSelf: 'flex-end', paddingBottom: 64 }}>
              <div className="hero-kicker">{s.kicker}</div>
              <div className="hero-title" style={{ whiteSpace: 'pre-line', fontSize: 'calc(58px * var(--fs))' }}>{s.title}</div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 30, marginTop: 22 }}>
                <div className="hero-desc" style={{ marginTop: 0, maxWidth: 560 }}>{s.desc}</div>
                <div className="hero-price" style={{ marginTop: 0 }}><span className="cur">¥</span>{RM.fmt(s.price)}</div>
              </div>
            </div>
            <div className="dots" style={{ position: 'absolute', bottom: 26, left: 0, right: 0, zIndex: 3 }}>
              {slides.map((_, kk) => <span key={kk} className={'dot' + (kk === i ? ' on' : '')} onClick={(e) => { e.stopPropagation(); setI(kk); }} />)}
            </div>
          </div>
        ))}
      </div>
    );
  }

  // ---- Variant B: focus + side promos ----
  const others = [0, 1, 2, 3].map(o => slides[(i + 1 + o) % slides.length]).slice(0, 3);
  return (
    <div>
      <div style={{ display: 'grid', gridTemplateColumns: '1.62fr 1fr', gap: 22, height: 430 }}>
        <div className="hero" style={{ height: '100%' }}>
          {slides.map((s, k) => (
            <div key={k} className={'hero-slide' + (k === i ? ' on' : '')} onClick={() => onOpen(s.pid)}>
              <Placeholder className="" label={s.label} style={{ position: 'absolute', inset: 0 }} />
              <div className="hero-grad" style={grad('120deg')} />
              <div className="hero-copy" style={{ maxWidth: '78%' }}>
                <div className="hero-kicker">{s.kicker}</div>
                <div className="hero-title" style={{ whiteSpace: 'pre-line' }}>{s.title}</div>
                <div className="hero-price"><span className="cur">¥</span>{RM.fmt(s.price)}</div>
              </div>
              <div className="dots" style={{ position: 'absolute', bottom: 22, left: 0, right: 0, justifyContent: 'flex-start', marginLeft: 64, zIndex: 3 }}>
                {slides.map((_, kk) => <span key={kk} className={'dot' + (kk === i ? ' on' : '')} onClick={(e) => { e.stopPropagation(); setI(kk); }} />)}
              </div>
            </div>
          ))}
        </div>
        <div style={{ display: 'grid', gridTemplateRows: '1fr 1fr', gap: 22 }}>
          {others.slice(0, 2).map((s, k) => (
            <div key={k} className="hero" style={{ cursor: 'pointer' }} onClick={() => onOpen(s.pid)}>
              <Placeholder className="" label={s.label} style={{ position: 'absolute', inset: 0 }} />
              <div className="hero-grad" style={grad('110deg')} />
              <div className="hero-copy" style={{ maxWidth: '92%', padding: '0 34px' }}>
                <div className="hero-title" style={{ fontSize: 'calc(30px * var(--fs))', whiteSpace: 'pre-line' }}>{s.title}</div>
                <div className="hero-price" style={{ fontSize: 'calc(28px*var(--fs))', marginTop: 12 }}><span className="cur">¥</span>{RM.fmt(s.price)}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ============ HOME ============
function HomeScreen({ layout, slideSpeed, onOpen }) {
  const rec = RM.recIds.map(id => RM.get(id)).filter(Boolean);
  return (
    <div className="scroll home-scroll">
      <HomeHero layout={layout} slideSpeed={slideSpeed} onOpen={onOpen} />
      <div className="sec-title">为你推荐</div>
      <div className="grid2">
        {rec.map(p => <ProductCard key={p.id} p={p} onOpen={onOpen} />)}
      </div>
    </div>
  );
}

// ============ CATEGORY ============
function CategoryScreen({ cat, empty, onOpen }) {
  const items = empty ? [] : RM.inCat(cat);
  return (
    <div className="scroll">
      <div className="page-head">
        <div className="page-title">{RM.catLabel[cat]}</div>
        {items.length > 0 && <div className="page-sub">{items.length} 件商品</div>}
      </div>
      {items.length === 0 ? (
        <div className="empty">
          <div className="box"><RMIcon name="car" size={48} className="" /></div>
          <div className="t1">该分类暂无商品</div>
        </div>
      ) : (
        <div className="grid2">{items.map(p => <ProductCard key={p.id} p={p} onOpen={onOpen} />)}</div>
      )}
    </div>
  );
}

// ============ DETAIL ============
function DetailScreen({ pid, onBack, onOrder }) {
  const p = RM.get(pid);
  const [t, setT] = useState(0);
  const thumbs = ['主图', '细节', '场景'];
  const tags = [['7天无理由', 'check'], ['包邮', 'check'], ['自营', 'check']];
  return (
    <div className="scroll">
      <button className="back-btn" onClick={onBack}><RMIcon name="chevL" size={20} className="" />返回</button>
      <div className="detail">
        <div>
          <Placeholder className="main-img" label={`${p.name} · ${thumbs[t]}`} />
          <div className="thumbs">
            {thumbs.map((tn, k) => (
              <div key={k} className={'thumb-pick' + (k === t ? ' on' : '')} onClick={() => setT(k)}>
                <Placeholder className="" label={tn} style={{ width: '100%', height: '100%' }} />
              </div>
            ))}
          </div>
        </div>
        <div>
          <div className="d-name">{p.name}</div>
          <div className="d-price"><span className="cur">¥</span>{RM.fmt(p.price)}</div>
          <div className="tags">
            {tags.map(([label, ic]) => (
              <span key={label} className="tag"><RMIcon name={ic} size={16} className="ck" />{label}</span>
            ))}
          </div>
          <div className="d-desc-label">商品描述</div>
          <div className="d-desc">{p.desc}</div>
          <div style={{ marginTop: 44 }}>
            <button className="btn-primary block" onClick={() => onOrder(p.id)}>下&nbsp;&nbsp;单</button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ============ CONFIRM ============
function ConfirmScreen({ pid, onBack, onPay }) {
  const p = RM.get(pid);
  const a = RM.address;
  return (
    <div className="scroll">
      <div className="page-head" style={{ alignItems: 'center' }}>
        <button className="back-btn" onClick={onBack}><RMIcon name="chevL" size={20} className="" />返回</button>
        <div className="page-title">确认订单</div>
      </div>
      <div style={{ maxWidth: 1080 }}>
        <div className="panel-card">
          <div className="pc-head">收货信息<span className="badge">演示 · 固定地址</span></div>
          <div className="addr">
            <span className="nm">{a.name}</span>
            <span className="ph-num">{a.phone}</span>
            <span className="loc">{a.loc}</span>
          </div>
        </div>
        <div className="panel-card">
          <div className="pc-head">订单详情</div>
          <div className="line-item">
            <Placeholder className="li-thumb" label={p.name.slice(0, 4)} />
            <div className="li-name">{p.name}</div>
            <div className="li-price"><span style={{ color: 'var(--acc)', fontSize: '.7em' }}>¥</span>{RM.fmt(p.price)}</div>
            <div className="li-qty">×1</div>
          </div>
          <div className="total-row">合计<span className="tt"><span className="cur" style={{ fontSize: '.6em' }}>¥</span>{RM.fmt(p.price)}</span></div>
        </div>
        <div className="pay-bar">
          <button className="btn-primary" onClick={() => onPay(p.id)}>去支付&nbsp;<span className="amt">¥{RM.fmt(p.price)}</span></button>
        </div>
      </div>
    </div>
  );
}

// ============ QR PAY (fullscreen) ============
function PayScreen({ pid, onSuccess, onCancel, startSec = 179 }) {
  const p = RM.get(pid);
  const [left, setLeft] = useState(startSec); // 02:59
  useEffect(() => {
    if (left <= 0) return;
    const t = setTimeout(() => setLeft(l => l - 1), 1000);
    return () => clearTimeout(t);
  }, [left]);

  if (left <= 0) {
    return (
      <div className="overlay">
        <div className="warn-ring"><RMIcon name="warn" size={62} className="" /></div>
        <div className="err-t">支付超时</div>
        <div className="err-s">二维码已失效，请重新发起支付</div>
        <div style={{ display: 'flex', gap: 18, marginTop: 40 }}>
          <button className="btn-ghost" style={{ marginTop: 0 }} onClick={() => setLeft(startSec)}>重新支付</button>
          <button className="back-btn" style={{ height: 70, fontSize: 22 }} onClick={onCancel}>返回订单</button>
        </div>
      </div>
    );
  }
  const mm = String(Math.floor(left / 60)).padStart(2, '0');
  const ss = String(left % 60).padStart(2, '0');
  const warn = left <= 30;
  return (
    <div className="overlay" style={{ cursor: 'pointer' }} onClick={onSuccess}>
      <div style={{ fontSize: 36, fontWeight: 700, whiteSpace: 'nowrap', marginBottom: 6 }}>
        请扫码支付 <span className="pay-amt-big" style={{ color: 'var(--acc)', fontSize: 44 }}>¥{RM.fmt(p.price)}</span>
      </div>
      <div className="qr-shell"><QRCode size={250} /></div>
      <div className={'countdown' + (warn ? ' warn' : '')}>
        <span className="lab">支付剩余时间</span>
        <span className="clock">{mm}:{ss}</span>
      </div>
      <div className="hint">点击屏幕任意位置 = <b>支付成功</b>（演示用）</div>
    </div>
  );
}

// ============ SUCCESS (fullscreen) ============
function SuccessScreen({ onDone }) {
  useEffect(() => { const t = setTimeout(onDone, 1500); return () => clearTimeout(t); }, []);
  return (
    <div className="overlay">
      <div className="check-ring">
        <div className="core"><RMIcon name="check" size={64} className="" style={{ color: 'var(--acc-ink)' }} /></div>
      </div>
      <div className="success-t">支付成功</div>
      <div className="success-s">1.5 秒后自动返回推荐页</div>
    </div>
  );
}

// ============ ORDERS ============
function OrdersScreen({ orders, onOpen }) {
  const a = RM.address;
  if (!orders.length) {
    return (
      <div className="scroll">
        <div className="page-head"><div className="page-title">我的订单</div></div>
        <div className="empty">
          <div className="box"><RMIcon name="receipt" size={48} className="" /></div>
          <div className="t1">暂无订单</div>
        </div>
      </div>
    );
  }
  return (
    <div className="scroll">
      <div className="page-head"><div className="page-title">我的订单</div><div className="page-sub">{orders.length} 笔</div></div>
      <div style={{ maxWidth: 1140 }}>
        {orders.map(o => {
          const it = o.items[0];
          const p = RM.get(it.pid);
          return (
            <div key={o.id} className="order-card" onClick={() => onOpen(p.id)} style={{ cursor: 'pointer' }}>
              <div className="oc-top">
                <span className="oc-time">{o.time}</span>
                <span className="status-paid"><RMIcon name="check" size={15} className="" />已支付</span>
              </div>
              <div className="line-item">
                <Placeholder className="li-thumb" label={p.name.slice(0, 4)} />
                <div className="li-name">{p.name}</div>
                <div className="li-price"><span style={{ color: 'var(--acc)', fontSize: '.7em' }}>¥</span>{RM.fmt(p.price)}</div>
                <div className="li-qty">×{it.qty}</div>
              </div>
              <div className="oc-addr"><RMIcon name="receipt" size={16} className="" />收货 {a.name} {a.phone} {a.loc}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ============ NETWORK ERROR (fullscreen) ============
function NetworkErrorScreen({ onRetry }) {
  return (
    <div className="overlay">
      <div className="warn-ring"><RMIcon name="warn" size={62} className="" /></div>
      <div className="err-t">网络异常</div>
      <div className="err-s">请检查网络后重试</div>
      <button className="btn-ghost" onClick={onRetry}>点击重试</button>
    </div>
  );
}

Object.assign(window, {
  HomeScreen, CategoryScreen, DetailScreen, ConfirmScreen,
  PayScreen, SuccessScreen, OrdersScreen, NetworkErrorScreen,
});
})();

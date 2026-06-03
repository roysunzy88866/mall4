/* RIDEMALL shared components → window */

// ---- line icons (stroke, inherit currentColor) ----
const RMIcon = ({ name, size = 26, className = 'ic' }) => {
  const paths = {
    sparkle: <path d="M12 3l2 5 5 2-5 2-2 5-2-5-5-2 5-2 2-5z" />,
    car: <g><path d="M5 16l1.5-5.5A2 2 0 018.4 9h7.2a2 2 0 011.9 1.5L19 16" /><rect x="3" y="16" width="18" height="4" rx="1.5" /><circle cx="7.5" cy="20" r="1.3" /><circle cx="16.5" cy="20" r="1.3" /></g>,
    bolt: <path d="M13 3L5 13h5l-1 8 8-11h-5l1-7z" />,
    tent: <g><path d="M12 4L4 20M12 4l8 16M3 20h18M12 9l-4 11M12 9l4 11" /></g>,
    chip: <g><rect x="6" y="6" width="12" height="12" rx="2" /><path d="M9 3v3M15 3v3M9 18v3M15 18v3M3 9h3M3 15h3M18 9h3M18 15h3" /></g>,
    cup: <g><path d="M6 9h12v5a5 5 0 01-5 5h-2a5 5 0 01-5-5V9z" /><path d="M18 10h2a2 2 0 010 4h-2" /><path d="M9 3v2M13 3v2" /></g>,
    receipt: <g><path d="M6 3h12v18l-3-2-3 2-3-2-3 2V3z" /><path d="M9 8h6M9 12h6" /></g>,
    check: <path d="M4 12l5 5L20 6" />,
    chevL: <path d="M15 5l-7 7 7 7" />,
    warn: <g><path d="M12 4l9 16H3l9-16z" /><path d="M12 10v4M12 17v.5" /></g>,
    wifi: <g><path d="M2 8.5a16 16 0 0120 0M5 12a11 11 0 0114 0M8.5 15.5a6 6 0 017 0" /><circle cx="12" cy="19" r="1" /></g>,
  };
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round">
      {paths[name] || null}
    </svg>
  );
};

// ---- striped placeholder ----
const Placeholder = ({ label, className = '', style }) =>
  <div className={'ph ' + className} data-label={label} style={style} />;

// ---- price ----
const Price = ({ value, className = 'price' }) => (
  <span className={className}><span className="cur">¥</span>{window.RM.fmt(value)}</span>
);

// ---- nav rail ----
const NavRail = ({ active, onNav }) => {
  const RM = window.RM;
  const top = RM.nav.filter(n => !n.bottom);
  const bottom = RM.nav.filter(n => n.bottom);
  const Item = (n) => (
    <div key={n.key}
      className={'nav-item' + (active === n.key ? ' active' : '')}
      onClick={() => onNav(n.key)}>
      <RMIcon name={n.icon} />
      <span>{n.label}</span>
    </div>
  );
  return (
    <nav className="nav">
      <div className="brand">
        <div className="mark" />
        <div className="word">RIDE<b>MALL</b></div>
      </div>
      <div className="nav-list">{top.map(Item)}</div>
      <div className="nav-divider" />
      {bottom.map(Item)}
    </nav>
  );
};

// ---- product card ----
const ProductCard = ({ p, onOpen }) => (
  <div className="card" onClick={() => onOpen(p.id)}>
    <Placeholder className="thumb" label={p.name.slice(0, 6)} />
    <div className="body">
      <div className="name">{p.name}</div>
      <div className="row">
        <Price value={p.price} />
        <span className="buy-hint">查看 ›</span>
      </div>
    </div>
  </div>
);

// ---- fake QR (deterministic, with finder squares) ----
const QRCode = ({ size = 244, n = 25 }) => {
  const cell = size / n;
  // deterministic pseudo-random
  let seed = 7;
  const rnd = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return (seed >> 8) % 100 / 100; };
  const isFinder = (r, c) => {
    const inBox = (R, C) => r >= R && r < R + 7 && c >= C && c < C + 7;
    return inBox(0, 0) || inBox(0, n - 7) || inBox(n - 7, 0);
  };
  const finderFill = (r, c) => {
    const f = (R, C) => {
      const rr = r - R, cc = c - C;
      if (rr < 0 || cc < 0 || rr > 6 || cc > 6) return null;
      const edge = rr === 0 || rr === 6 || cc === 0 || cc === 6;
      const core = rr >= 2 && rr <= 4 && cc >= 2 && cc <= 4;
      return edge || core;
    };
    const a = f(0, 0); if (a !== null) return a;
    const b = f(0, n - 7); if (b !== null) return b;
    const d = f(n - 7, 0); if (d !== null) return d;
    return null;
  };
  const cells = [];
  for (let r = 0; r < n; r++) {
    for (let c = 0; c < n; c++) {
      let on;
      if (isFinder(r, c)) on = finderFill(r, c);
      else on = rnd() > 0.52;
      if (on) cells.push(
        <i key={r + '-' + c} style={{ gridRow: r + 1, gridColumn: c + 1, background: '#0a0c0f', borderRadius: cell * 0.18 }} />
      );
    }
  }
  return (
    <div className="qr" style={{
      width: size, height: size,
      gridTemplateColumns: `repeat(${n}, 1fr)`, gridTemplateRows: `repeat(${n}, 1fr)`,
    }}>{cells}</div>
  );
};

Object.assign(window, { RMIcon, Placeholder, Price, NavRail, ProductCard, QRCode });

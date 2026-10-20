// デザイン案（NET-09）：URL に ?design=A ／ B ／ C を付けたときだけ効く。付けなければ何もしない（ふだんのページはそのまま）。
// 選ばれた案は docs/DESIGN_SYSTEM.md にルールとして書き、network.html・node.html に直接入れる（そのときこのファイルは消す）。
// 共通のルール：文字は BIZ UDPゴシック（Google Fonts から読む。フォントのファイルはリポジトリに入れない）。
// 色は意味のある所だけ（✅/❓・モノ・発見）。幹の違いは帯の名前とアイコンの形。絵文字・アイコンは単色のドット絵にそろえる
(function () {
  const id = (new URLSearchParams(location.search).get('design') || '').toUpperCase();
  const OPTIONS = {
    // A：シンプル・白基調。いちばん読みやすい。教科書・授業向け
    A: { scheme: 'light', font: '"BIZ UDPGothic", sans-serif', labelFont: '"BIZ UDPGothic", sans-serif', pixel: false,
         vars: { bg: '#ffffff', panel: '#ffffff', ink: '#1a1a1a', sub: '#5f6368', line: '#e3e3e3', 'band-a': '#f6f7f8', 'band-b': '#ffffff',
                 accent: '#1f5fbf', hl: '#e8710a', prod: '#ffffff' },
         tech: '#5f6368', done: '#3c4043', prodColor: '#1f5fbf', idea: '#7b3fbf', edge: '#9aa0a6', icon: '#3c4043',
         radius: 8, cardBorder: '1px solid var(--line)', shadow: 'none' },
    // B：少しゲーム寄り。暗い背景、見出しとモノの名前だけドット風の書体（DotGothic16）、角ばった枠
    B: { scheme: 'dark', font: '"BIZ UDPGothic", sans-serif', labelFont: '"DotGothic16", "BIZ UDPGothic", sans-serif', pixel: true,
         vars: { bg: '#10131a', panel: '#171c26', ink: '#eef1f5', sub: '#9aa3b2', line: '#2a3240', 'band-a': '#141923', 'band-b': '#10131a',
                 accent: '#7cc4ff', hl: '#ffd23f', prod: '#1d2430' },
         tech: '#8b95a5', done: '#c9d1dc', prodColor: '#ffd23f', idea: '#b388ff', edge: '#5b6576', icon: '#e6ebf2',
         radius: 2, cardBorder: '2px solid var(--line)', shadow: '0 0 0 2px #000' },
    // C：その中間。生成り色の紙、本文は BIZ UDPゴシック、大きな見出しだけドット風
    C: { scheme: 'light', font: '"BIZ UDPGothic", sans-serif', labelFont: '"BIZ UDPGothic", sans-serif', pixel: true,
         vars: { bg: '#faf7f0', panel: '#fffdf8', ink: '#2b2622', sub: '#6b6259', line: '#e4dccd', 'band-a': '#f3eee3', 'band-b': '#faf7f0',
                 accent: '#2f6f8f', hl: '#d9480f', prod: '#fffdf8' },
         tech: '#7a6f64', done: '#4a423b', prodColor: '#2f6f8f', idea: '#7c4dab', edge: '#b6aa98', icon: '#4a423b',
         radius: 6, cardBorder: '1px solid var(--line)', shadow: '0 1px 0 var(--line)' },
  };
  const o = OPTIONS[id];
  if (!o) return;
  // フォント（Google Fonts。SIL OFL なのでファイルはリポジトリに入れず、ここから読む）
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = 'https://fonts.googleapis.com/css2?family=BIZ+UDPGothic:wght@400;700&family=DotGothic16&display=swap';
  document.head.appendChild(link);
  document.documentElement.setAttribute('data-theme', o.scheme);
  const v = Object.entries(o.vars).map(([k, x]) => `--${k}: ${x};`).join(' ');
  const pixelHead = o.pixel ? `h1, #card h2 span, header.top h1, section h2 { font-family: "DotGothic16", "BIZ UDPGothic", sans-serif; letter-spacing: .02em; }` : '';
  const css = `
  :root, :root[data-theme="${o.scheme}"] { ${v} --radius: ${o.radius}px; --card-border: ${o.cardBorder}; --shadow: ${o.shadow}; }
  @media (prefers-color-scheme: dark) { :root:not([data-theme="light"]) { ${v} } }
  body { font-family: ${o.font}; font-size: 15px; line-height: 1.7; background: var(--bg); color: var(--ink); }
  ${pixelHead}
  /* 部品：見出し・ボタン・カード・チップ・余白（4・8・12・16・24px の段） */
  header h1 { font-size: 18px; } header .stat { font-size: 12px; }
  header input[type=search], header select { height: 36px; border-radius: var(--radius); font-size: 14px; }
  .btn, #card .more { min-height: 36px; display: inline-flex; align-items: center; padding: 0 14px; border-radius: var(--radius); font-size: 14px; font-weight: 700; }
  section, header.top { border: var(--card-border); border-radius: calc(var(--radius) * 1.5); box-shadow: var(--shadow); padding: 16px; margin-top: 12px; }
  section h2 { font-size: 16px; margin: 0 0 12px; }
  #card { padding: 16px; } #card h2 { font-size: 18px; } #card .sec h3 { font-size: 13px; letter-spacing: .02em; }
  #card .chip, .chip { border-radius: var(--radius); padding: 2px 10px; font-size: 13px; }
  .legend, .hint { border-radius: var(--radius); border: var(--card-border); }
  .legend .sw { background: ${o.edge} !important; }   /* 線は1色（年の差の色分けはやめる） */
  /* 絵文字・テクスチャは単色のドット絵に（仮）：画像は形だけ、色は文字色 */
  #card h2 .em, header.top .ic, .chip { font-variant-emoji: text; }
  #card h2 img, header.top .ic img { image-rendering: pixelated; filter: grayscale(1) contrast(1.4) ${o.scheme === 'dark' ? 'invert(1)' : ''}; }
  #card h2 .em, header.top .ic { filter: grayscale(1); }
  `;
  const st = document.createElement('style'); st.textContent = css; document.head.appendChild(st);
  window.DESIGN = { id, font: o.font, labelFont: o.labelFont, tech: o.tech, done: o.done, prod: o.prodColor, idea: o.idea, edge: o.edge, icon: o.icon, mono: true };
})();

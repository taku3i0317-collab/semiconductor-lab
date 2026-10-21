// 「〇〇を作るには？」のミニムービー（S-017、見本）。モノの番号を渡すだけで動く1つの部品：Film.play(id, { onEnd })
// 1 真ん中にモノ →「〇〇を作るには？」 2 前提の技術が世代ごとに（近い→遠い）外から集まって線でつながる（数を数える）
// 3 いちばん奥の打製石器 →「全部、ここから始まった」 4 まわりに人の肖像 →「〇人＋名前の残っていない、たくさんの人たち」
// 5 石器 → モノへ光が走る →「次は、あなたが選んで」。数はすべてデータ（tech_network_graph.js・tech_network_detail.js）から数える。
// 10〜15 秒。右上の「とばす」でいつでも終わり、画面のどこを押しても次の場面へ。音は無し。
// 「動きを減らす」の設定（prefers-reduced-motion）では動かさず、同じ内容を静止画の5枚で（押すと次）
(function () {
  const G = window.TECH_GRAPH;
  const byId = new Map(G.nodes.map(n => [n.id, n]));
  const parents = new Map();
  for (const [f, t, k] of G.edges) if (k === 'r' || k === 'e' || k === 'p') (parents.get(t) || parents.set(t, []).get(t)).push(f);
  const EN = () => document.documentElement.lang === 'en';
  const nm = n => (EN() && n.name_en) || n.name;
  const T = {
    ask: n => EN() ? `What does it take to make ${nm(n)}?` : `${nm(n)}を作るには？`,
    count: k => EN() ? `${k} technologies` : `${k} 個の技術`,
    origin: EN() ? 'It all started here.' : '全部、ここから始まった',
    people: (p, a) => EN() ? `${p} people + many whose names are lost (${a} technologies)` : `${p} 人＋名前の残っていない、たくさんの人たち（${a} の技術）`,
    you: EN() ? 'Now it is your turn to choose.' : '次は、あなたが選んで',
    skip: EN() ? 'Skip ▶▶' : 'とばす ▶▶',
    gen: (d, n) => EN() ? ` · ${d} step${d > 1 ? 's' : ''} back: +${n}` : `・${d} 世代前 +${n}`,
    next: EN() ? 'Next ▶' : '次へ ▶',
  };
  const imgs = new Map();
  const img = src => { if (!src) return null; let i = imgs.get(src); if (!i) { i = new Image(); i.src = src; imgs.set(src, i); } return i; };
  const reduce = () => !!window.FILM_STILL || matchMedia('(prefers-reduced-motion: reduce)').matches;   // FILM_STILL：見本で静止画を確かめる用
  const css = v => getComputedStyle(document.documentElement).getPropertyValue(v).trim();
  // 重いデータ（人物）は、ムービーが始まってから裏で読む（最初の表示を待たせない）
  let detailP = null;
  const loadDetail = () => detailP || (detailP = window.TECH_DETAIL ? Promise.resolve() : new Promise(res => {
    const s = document.createElement('script'); s.src = (window.FILM_BASE || '') + 'tech_network_detail.js'; s.onload = res; s.onerror = res; document.head.appendChild(s);
  }));

  // 祖先を世代（何段さかのぼるか、いちばん近い道）で分け、円の上の角度を決める
  function build(id) {
    const dist = new Map([[id, 0]]), via = new Map();
    let q = [id];
    while (q.length) { const nq = []; for (const u of q) for (const p of parents.get(u) || []) if (!dist.has(p) && byId.has(p)) { dist.set(p, dist.get(u) + 1); via.set(p, u); nq.push(p); } q = nq; }
    const layers = [];
    for (const [k, d] of dist) (layers[d] || (layers[d] = [])).push(k);
    const ang = new Map([[id, -Math.PI / 2]]);
    for (let d = 1; d < layers.length; d++) {
      const L = layers[d].sort((a, b) => ang.get(via.get(a)) - ang.get(via.get(b)) || (byId.get(a).year ?? 0) - (byId.get(b).year ?? 0));
      const off = d % 2 ? 0 : Math.PI / L.length;
      L.forEach((k, i) => ang.set(k, -Math.PI / 2 + off + (i / L.length) * Math.PI * 2));
    }
    return { dist, via, layers, ang };
  }

  function play(id, opt = {}) {
    const target = byId.get(id); if (!target) return;
    const { dist, via, layers, ang } = build(id);
    const total = dist.size - 1, nL = layers.length - 1;
    const stone = dist.has('0-01') ? '0-01' : layers[nL][0];
    // 時間割（ms）：前提の数・世代の数で長さを変える（少ない物は短く）
    const P2 = Math.max(1800, Math.min(6500, nL * 750));
    const t1 = 1400, t2 = t1 + P2 + 700, t3 = t2 + 1800, t4 = t3 + 2400, t5 = t4 + 2300;
    const STARTS = [0, t1, t2, t3, t4, t5];
    loadDetail();

    const root = document.createElement('div');
    root.className = 'film';
    root.innerHTML = `<canvas></canvas><div class="cap" aria-live="polite"></div><div class="cnt"></div>
      <button type="button" class="skip">${T.skip}</button>${reduce() ? `<button type="button" class="nextb">${T.next}</button>` : ''}`;
    document.body.appendChild(root);
    const cv = root.querySelector('canvas'), ctx = cv.getContext('2d'), cap = root.querySelector('.cap'), cnt = root.querySelector('.cnt');
    let W = 0, H = 0, dpr = 1;
    const resize = () => { dpr = devicePixelRatio || 1; W = innerWidth; H = innerHeight; cv.width = W * dpr; cv.height = H * dpr; cv.style.width = W + 'px'; cv.style.height = H + 'px'; };
    resize(); addEventListener('resize', resize);
    const C = { bg: css('--bg') || '#f8f5ee', ink: css('--ink') || '#222', sub: css('--sub') || '#777', line: css('--line') || '#ddd', hl: css('--hl') || '#d2691e', prod: css('--node-prod') || '#2f6f8f', tech: css('--node-tech') || '#8a6a4a' };

    // 人物（肖像）：裏で読めたら作る
    let people = null;
    loadDetail().then(() => {
      const D = window.TECH_DETAIL; if (!D) return;
      const seen = new Map(); let anon = 0;
      for (const k of dist.keys()) {
        const ps = (D.nodes[k] && D.nodes[k].people) || [];
        if (!ps.length && k !== id) anon++;
        for (const p of ps) if (!seen.has(p.name)) seen.set(p.name, p.pi != null ? D.portraits[p.pi] : null);
      }
      people = { n: seen.size, anon, pics: [...seen.values()].filter(Boolean).slice(0, 28) };
    });

    // 世代の円の半径：数の多い世代ほど間を広く、少ない世代（遠い昔）は詰める（画面を広く使う）
    const RAD = [0]; for (let d = 1; d <= nL; d++) RAD[d] = RAD[d - 1] + Math.max(d === 1 ? 64 : 34, Math.min(90, layers[d].length * 1.15));   // 1つ目の円は真ん中の大きな丸に重ならないように
    const pos = k => { const d = dist.get(k), a = ang.get(k); return [Math.cos(a) * RAD[d], Math.sin(a) * RAD[d]]; };
    const nodeR = d => d === 0 ? 34 : Math.max(3, Math.min(15, 0.45 * (2 * Math.PI * RAD[d]) / layers[d].length));
    const ease = x => 1 - Math.pow(1 - Math.max(0, Math.min(1, x)), 3);
    const layerStart = d => t1 + (d - 1) * (P2 / Math.max(1, nL));
    // 石器 → モノ の光の道
    const path = []; for (let u = stone; u; u = via.get(u)) path.push(u);

    let t0 = performance.now(), jump = 0, done = false, still = reduce() ? 0 : -1;
    const now = () => still >= 0 ? STARTS[still + 1] - 1 : performance.now() - t0 + jump;
    function next() {
      if (still >= 0) { still++; if (still > 4) return end(); return; }
      const t = now(), n = STARTS.find(s => s > t);
      if (n == null || n >= t5) return end();
      jump += n - t;
    }
    function end() {
      if (done) return; done = true;
      root.classList.add('out');
      setTimeout(() => { root.remove(); removeEventListener('resize', resize); opt.onEnd && opt.onEnd(id); }, reduce() ? 0 : 400);
    }
    root.querySelector('.skip').onclick = e => { e.stopPropagation(); end(); };
    root.addEventListener('click', next);
    root.addEventListener('keydown', e => { if (e.key === 'Escape') end(); });

    function frame() {
      if (done) return;
      const t = now();
      if (t >= t5 && still < 0) { end(); return; }
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      ctx.fillStyle = C.bg; ctx.fillRect(0, 0, W, H);
      // カメラ：集まった世代が画面に入るように、だんだん引く
      let shown = 0; for (let d = 1; d <= nL; d++) if (t >= layerStart(d)) shown = d;
      const want = Math.min(1.6, (Math.min(W, H * 0.72) / 2 - 24) / (RAD[Math.max(1, shown)] + 18));
      const s = frame.s = frame.s ? frame.s + (want - frame.s) * (still >= 0 ? 1 : 0.06) : want;
      const cx = W / 2, cy = H * 0.46;
      ctx.translate(cx, cy); ctx.scale(s, s);
      // 線（子 ← 前提）
      let arrived = 0;
      ctx.lineWidth = 1 / s;
      for (const [k, d] of dist) {
        if (!d) continue;
        const p = ease((t - layerStart(d)) / 700);
        if (p <= 0) continue;
        if (p >= 1) arrived++;
        const [x, y] = pos(k), [vx, vy] = pos(via.get(k));
        ctx.globalAlpha = 0.25 * p; ctx.strokeStyle = C.sub;
        ctx.beginPath(); ctx.moveTo(vx, vy); ctx.lineTo(vx + (x - vx) * p, vy + (y - vy) * p); ctx.stroke();
      }
      // 点：外から飛んできて、円の上の場所へ
      for (const [k, d] of dist) {
        const n = byId.get(k);
        let [x, y] = pos(k), a = 1;
        if (d) {
          const p = ease((t - layerStart(d)) / 700); if (p <= 0) continue;
          const far = (Math.max(W, H) / s) * 0.9, aa = ang.get(k);
          x = Math.cos(aa) * (far + (RAD[d] - far) * p); y = Math.sin(aa) * (far + (RAD[d] - far) * p); a = p;
        } else a = ease(t / 900);
        const r = nodeR(d) * (d ? 1 : 0.6 + 0.4 * a);
        ctx.globalAlpha = a;
        ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2); ctx.fillStyle = C.bg; ctx.fill();
        const glow = (t >= t2 && k === stone) || (t >= t4 && path.includes(k) && pathLit(k, t));
        ctx.lineWidth = (glow ? 4 : d ? 1.5 : 3) / Math.max(1, s * 0.8); ctx.strokeStyle = glow ? C.hl : n.kind === 'product' ? C.prod : C.tech; ctx.stroke();
        const im = img(n.icon);
        if (im && im.complete && r >= 5) { ctx.imageSmoothingEnabled = false; ctx.drawImage(im, x - r * 0.7, y - r * 0.7, r * 1.4, r * 1.4); }
      }
      // 打製石器の輪（3 の場面）
      if (t >= t2 && t < t4 + 2000) {
        const [x, y] = pos(stone), q = ((t - t2) % 1200) / 1200;
        ctx.globalAlpha = still >= 0 ? 0.6 : 1 - q; ctx.strokeStyle = C.hl; ctx.lineWidth = 3 / s;
        ctx.beginPath(); ctx.arc(x, y, nodeR(dist.get(stone)) + 6 / s + q * 26 / s, 0, Math.PI * 2); ctx.stroke();
      }
      // 光の道（5 の場面）：石器 → モノ
      if (t >= t4) {
        const p = Math.min(1, (t - t4) / 1500), m = p * (path.length - 1), i = Math.floor(m);
        ctx.globalAlpha = 1; ctx.strokeStyle = C.hl; ctx.lineWidth = 4 / s; ctx.beginPath();
        for (let j = 0; j <= Math.min(i, path.length - 2); j++) {
          const [ax, ay] = pos(path[j]), [bx, by] = pos(path[j + 1]), f = j < i ? 1 : m - i;
          ctx.moveTo(ax, ay); ctx.lineTo(ax + (bx - ax) * f, ay + (by - ay) * f);
        }
        ctx.stroke();
      }
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      // 人の肖像（4 の場面）：画面のふちに並ぶ
      if (t >= t3 && people) {
        const k = people.pics.length, p = ease((t - t3) / 900);
        const sz0 = Math.min(48, Math.max(30, W / 12)), rx = W / 2 - sz0 / 2 - 6, ry = Math.min(H * 0.46 - sz0 / 2 - 8, H * 0.36 - sz0 / 2);   // 下の文と重ならないように
        people.pics.forEach((src, i) => {
          const im = img(src), a = -Math.PI / 2 + (i / k) * Math.PI * 2;
          if (!im || !im.complete) return;
          ctx.globalAlpha = p * (t >= t4 ? Math.max(0.25, 1 - (t - t4) / 1200) : 1);
          const sz = sz0;
          ctx.imageSmoothingEnabled = false;
          ctx.drawImage(im, cx + Math.cos(a) * rx - sz / 2, cy + Math.sin(a) * ry - sz / 2, sz, sz);
        });
      }
      ctx.globalAlpha = 1;
      // 文（HTML の上に。ふりがな・折り返しが効く）
      const sc = t < t1 ? 0 : t < t2 ? 1 : t < t3 ? 2 : t < t4 ? 3 : 4;
      const capText = [T.ask(target), T.ask(target), `${T.origin}（${nm(byId.get(stone))}）`, people ? T.people(people.n, people.anon) : '…', T.you][sc];
      if (cap.dataset.t !== capText) { cap.dataset.t = capText; cap.textContent = capText; cap.classList.remove('in'); void cap.offsetWidth; cap.classList.add('in'); }
      // 数：ぜんぶの数と、いま集まっている世代（何段さかのぼったか）の数。とても多い物でも「何世代前に何個」と分かる
      cnt.textContent = sc >= 1 ? T.count(Math.min(total, sc >= 2 ? total : arrived)) + (sc === 1 && shown ? T.gen(shown, layers[shown].length) : '') : '';
      requestAnimationFrame(frame);
    }
    function pathLit(k, t) { const i = path.indexOf(k); return (t - t4) / 1500 >= i / Math.max(1, path.length - 1); }
    root.tabIndex = -1; root.focus();
    requestAnimationFrame(frame);
  }
  window.Film = { play };
})();

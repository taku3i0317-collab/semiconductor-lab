// 日本語の折り返し（S-016）：言葉の途中で改行しないように、文節（「たどり着いた」のようなまとまり）の切れ目でだけ折り返す。
// 切れ目は BudouX（Google の文節の道具、docs/vendor/budoux-ja.min.js、Apache License 2.0）が見つけ、見えない空白（ゼロ幅スペース）を入れる。
// Safari でも効く。CSS の word-break: auto-phrase（Chrome）も design.css で一緒に使う。英語のときは何もしない（英語は hyphens: auto）。
// 画面が書き換わったら（カード・ページの中身）、新しい文にも入れる
(function () {
  let parser = null, obs = null;
  const isJa = () => document.documentElement.lang !== 'en';
  function getParser() {
    if (!parser && window.customElements && customElements.get('budoux-ja')) parser = document.createElement('budoux-ja').parser;
    return parser;
  }
  const SKIP = 'svg, canvas, script, style, input, textarea, select, button, code, pre, .results';
  function apply(root) {
    const p = getParser();
    if (!p || !isJa() || !root || root.nodeType !== 1 || root.closest(SKIP)) return;
    if (obs) obs.disconnect();
    try { p.applyToElement(root); } catch (e) { /* 折り返しは見た目だけなので、失敗しても先へ */ }
    watch();
  }
  function watch() {
    if (!obs) obs = new MutationObserver(ms => {
      const roots = new Set();
      for (const m of ms) for (const n of m.addedNodes) {
        const el = n.nodeType === 1 ? n : n.parentElement;
        if (el && el.isConnected && !(el.tagName === 'RT')) roots.add(el);
      }
      for (const r of roots) apply(r);
    });
    obs.observe(document.body, { childList: true, subtree: true });
  }
  function init() { apply(document.body); }
  window.LineBreak = { init, apply };
})();

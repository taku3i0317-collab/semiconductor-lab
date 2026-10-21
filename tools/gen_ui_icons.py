"""画面の印のドット絵（S-016：絵文字をやめる）。図のアイコンと同じ 16×16・icons_e の 22 色（tools/gen_icons.py の PALETTE と from_glyph）。

見本 docs/mockups/S016_emoji.html と、ページで使う docs/ui_icons.js（S-020：ユーザーの OK で全部に）を作る。ui_icons.js は文の中の ✅ ❓ ⚠️ などを、このドット絵の <img> に置き換える。
形は docs/ui_glyphs.json（'#'＝1つ目の素材の薄い色・ふちは濃い色、'+'＝2つ目の素材の濃い色）。
実行：python tools/gen_ui_icons.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_icons import from_glyph, data_uri  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
GLYPHS = ROOT / "docs" / "ui_glyphs.json"
OUT = ROOT / "docs" / "mockups" / "S016_emoji.html"
JS = ROOT / "docs" / "ui_icons.js"
ALT = {"ok": "ゲームにある", "todo": "まだ", "blocked": "作れない", "wip": "作っている途中", "none": "入れない", "warn": "確かでない", "no": "ちがう", "person": "人物", "idea": "発見"}


def main():
    g = json.loads(GLYPHS.read_text(encoding="utf-8"))
    rows = []
    for key, v in g.items():
        if key.startswith("_"):
            continue
        uri = data_uri(from_glyph(v["glyph"], v["mat"]))
        img = lambda px: f'<img src="{uri}" width="{px}" height="{px}" alt="">'
        rows.append(f"""<tr><td class="emo">{v['emoji']}</td><td>{img(32)}</td><td>{img(16)}</td><td>{v['meaning']}<br><small>{v['where']}</small></td>
<td><span class="chip">9-23 MOSFET <span class="e">{v['emoji']}</span></span><br><span class="chip">9-23 MOSFET {img(16)}</span></td><td><small>{'・'.join(v['mat'])}</small></td></tr>""")
    OUT.write_text(f"""<!doctype html>
<html lang="ja"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>S-016 絵文字の置き換え見本</title>
<!-- 自動生成：tools/gen_ui_icons.py（形は docs/ui_glyphs.json）。見本だけで、まだページには入れていない -->
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=BIZ+UDPGothic:wght@400;700&family=DotGothic16&display=swap">
<link rel="stylesheet" href="../design.css">
<style>
body {{ margin: 0; padding: 12px 16px 32px; }}
table {{ border-collapse: collapse; width: 100%; max-width: 760px; }}
th, td {{ border-bottom: 1px solid var(--line); padding: 6px 8px; text-align: left; vertical-align: middle; font-size: 14px; }}
th {{ font-size: 12px; color: var(--sub); font-weight: 400; }}
td.emo {{ font-size: 28px; }}
img {{ image-rendering: pixelated; vertical-align: middle; }}
.chip {{ display: inline-flex; align-items: center; gap: 4px; border: 1px solid var(--line); border-radius: var(--radius); padding: 1px 8px; margin: 2px 0; background: var(--panel); font-size: 13px; }}
small {{ color: var(--sub); }}
</style></head><body>
<h1>絵文字 → 自作のドット絵（見本）</h1>
<p>左が今の絵文字（機器ごとに絵が違う）、右が置き換え後（図のアイコンと同じ 16×16・22 色）。右の2つの欄は、ふだんの大きさ（16px）とチップの中での見え方。<br>
<small>22 色に緑と赤が無いので、「ゲームにある」は青緑（ガラス）、「止まっている」は赤茶（銅）にした。</small></p>
<table><tr><th>今</th><th>新（32px）</th><th>新（16px）</th><th>意味・使う所</th><th>チップの中（上：今／下：新）</th><th>色（素材）</th></tr>
{''.join(rows)}
</table></body></html>
""", encoding="utf-8")
    print(f"wrote {OUT} ({len(rows)} icons)")
    icons = {}
    for key, v in g.items():
        if key.startswith("_"):
            continue
        uri = data_uri(from_glyph(v["glyph"], v["mat"]))
        for e in {v["emoji"], v["emoji"].replace("️", "")}:
            icons[e] = [uri, ALT[key]]
    JS.write_text("// 自動生成：tools/gen_ui_icons.py（形は docs/ui_glyphs.json）。手で直さない\n"
        "// 画面の印（✅ ❓ ⚠️ など）の絵文字を、図のアイコンと同じ 16×16 のドット絵に置き換える（S-020。機器ごとに絵が違う絵文字をやめる）\n"
        "window.UI_ICONS = " + json.dumps(icons, ensure_ascii=False) + ";\n" + r"""(function () {
  const I = window.UI_ICONS, keys = Object.keys(I).sort((a, b) => b.length - a.length);
  const RE = new RegExp(keys.map(k => k.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|'), 'g');
  const SKIP = /^(SCRIPT|STYLE|TEXTAREA|INPUT|SELECT|OPTION|TITLE|CANVAS|SVG|CODE|PRE)$/i;
  let obs = null;
  function apply(root) {
    if (!root) return;
    const list = [], w = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    while (w.nextNode()) { const t = w.currentNode; RE.lastIndex = 0; if (RE.test(t.nodeValue)) list.push(t); }
    if (!list.length) return;
    if (obs) obs.disconnect();
    for (const t of list) {
      let p = t.parentNode, skip = false;
      for (let e = p; e && e !== document.body; e = e.parentNode) if (e.nodeType === 1 && (SKIP.test(e.nodeName) || e.closest && e.closest('svg'))) { skip = true; break; }
      if (skip || !p) continue;
      const f = document.createDocumentFragment(), s = t.nodeValue; let last = 0, m; RE.lastIndex = 0;
      while ((m = RE.exec(s))) {
        if (m.index > last) f.appendChild(document.createTextNode(s.slice(last, m.index)));
        const im = document.createElement('img'); im.className = 'uic'; im.src = I[m[0]][0]; im.alt = I[m[0]][1]; im.title = I[m[0]][1];
        f.appendChild(im); last = m.index + m[0].length;
      }
      if (last < s.length) f.appendChild(document.createTextNode(s.slice(last)));
      p.replaceChild(f, t);
    }
    watch();
  }
  function watch() {
    if (!obs) obs = new MutationObserver(ms => { for (const m of ms) for (const n of m.addedNodes) apply(n.nodeType === 1 ? n : n.parentNode); });
    obs.observe(document.body, { childList: true, subtree: true });
  }
  window.UiIcons = { apply, init: () => apply(document.body) };
})();
""", encoding="utf-8")
    print(f"wrote {JS} ({len(icons)} marks)")



if __name__ == "__main__":
    main()

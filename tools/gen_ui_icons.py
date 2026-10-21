"""画面の印のドット絵（S-016：絵文字をやめる）。図のアイコンと同じ 16×16・icons_e の 22 色（tools/gen_icons.py の PALETTE と from_glyph）。

今は見本だけ：docs/mockups/S016_emoji.html（今の絵文字と置き換え後を並べる）を作る。ユーザーの OK のあと、ページの ✅ ❓ などをこの絵に置き換える。
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


if __name__ == "__main__":
    main()

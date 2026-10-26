"""前のウィキの URL を切らずに、新しいページへ案内する（S-061、設計図 docs/mockups/S060_spec.md「URL が切れないように」）。

tools/gen_wiki.py の後に動かす：
- wiki/items/<名前>.html・wiki/machines/<名前>.html：その物（ノード）がある物は、短い案内のページにする（すぐ nodes/<番号>.html#it-<名前> へ移る、canonical も新しい方、noindex）
  物の無いアイテム（ノードが決まっていない物）とマルチブロックのページは、そのまま残す
- wiki/index.html → 入口 index.html、wiki/guides/* → 新しい道案内 guides/*
- wiki/index.json：残ったページだけ（sitemap.xml はこれを見る）
使い方：python tools/wiki_redirects.py
"""
import html
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WIKI = ROOT / "docs" / "wiki"
SITE = "https://taku3i0317-collab.github.io/tech-network/"


def stub(target, title):
    t = html.escape(target, quote=True)
    return f"""<!doctype html>
<html lang="ja"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)}（ページが移りました）</title>
<meta name="robots" content="noindex"><link rel="canonical" href="{SITE}{t}">
<meta http-equiv="refresh" content="0; url=../../{t}">
<script>location.replace('../../{t}' + (location.search || ''));</script>
</head><body><p>このページは <a href="../../{t}">{html.escape(title)}</a> に移りました。</p></body></html>
"""


def main():
    W = json.loads((ROOT / "docs" / "wiki_export.json").read_text(encoding="utf-8"))
    s = (ROOT / "docs" / "tech_network_graph.js").read_text(encoding="utf-8")
    nodes = {n["id"] for n in json.loads(s[s.index("{"):s.rindex("}") + 1])["nodes"]}
    moved, kept = 0, 0
    keep_index = []
    old = json.loads((WIKI / "index.json").read_text(encoding="utf-8")) if (WIKI / "index.json").exists() else []
    by_u = {x["u"]: x for x in old}
    for it in W["items"]:
        slug = it["id"].split(":", 1)[1]
        nd = it.get("node")
        name = it["name"].get("ja") or it["name"].get("en")
        for sub in ("items", "machines"):
            p = WIKI / sub / f"{slug}.html"
            if not p.exists():
                continue
            if nd in nodes:
                p.write_text(stub(f"nodes/{nd}.html#it-{slug}", name), encoding="utf-8")
                moved += 1
            else:
                kept += 1
                if f"{sub}/{slug}.html" in by_u:
                    keep_index.append(by_u[f"{sub}/{slug}.html"])
    for x in old:   # マルチブロックなど、アイテムでないページは残す
        if x["u"].startswith("multiblocks/"):
            keep_index.append(x)
    (WIKI / "index.html").write_text(stub("index.html", "技術の地図"), encoding="utf-8")
    for g, to in (("first-hour", "guides/first-hour.html"), ("ai-chip", "guides/ai-chip.html"), ("radio", "index.html"), ("calculator", "index.html")):
        p = WIKI / "guides" / f"{g}.html"
        if p.exists():
            p.write_text(stub(to, "道案内"), encoding="utf-8")
    (WIKI / "index.json").write_text(json.dumps(keep_index, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"wiki redirects: moved {moved}, kept {kept} (+ multiblocks), index {len(keep_index)}")


if __name__ == "__main__":
    main()

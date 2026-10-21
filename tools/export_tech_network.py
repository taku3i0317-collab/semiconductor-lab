#!/usr/bin/env python3
"""tech-network の公開用フォルダを作る（S-010。ライセンス CC0）。

semiconductor-lab の技術ネットワークを、別リポジトリ tech-network として世界に公開し、監査してもらうための書き出し。
出力：build/tech-network/（git に入らないフォルダ。ここを新しいリポジトリの中身にする）
  data/nodes/<番号>.json … ノードごとに1ファイル。"history"（本当の技術の歴史）と "game"（MOD での作り方）を分ける
  data/edges.json・data/glossary.json・data/meta.json
  tools/check.py        … 自動チェック（存在しない番号・前提の輪・年の逆転・出典の印）。依存なしの Python
  docs/                 … ネットワーク図（GitHub Pages）。network.html・node.html・people.html・glossary.html
  .github/              … 誤りの報告の Issue のひな形、check.py を動かす GitHub Actions
  README.md（日・英）・LICENSE（CC0）
使い方:  python tools/gen_tree_views.py → python tools/export_tech_network.py [--repo owner/name]
        → cd build/tech-network → git add -A → git commit → git push（公開リポジトリ taku3i0317-collab/tech-network、S-011）
入れない物（DESIGN「公開」と今の決まり）：Wikipedia の文の丸写し（O-002 で点検ずみ）・Mojang の素材・存命の人の顔
"""
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "build" / "tech-network"
REPO = "taku3i0317-collab/tech-network"
if "--repo" in sys.argv:
    REPO = sys.argv[sys.argv.index("--repo") + 1]

HISTORY = ["name", "name_en", "kind", "trunk", "trunk_name", "year", "year_range", "year_uncertain", "real_full", "hee1", "hee1_en",
           "hee_tree", "sources", "refs", "people", "media", "requires", "understanding", "enables", "explains", "parts", "materials",
           "enabled_by", "explained_by", "frontier"]
GAME = ["status", "buildable_now", "play", "missing", "item", "recipes", "machine_recipes", "placeholder", "game_text", "hee_game"]


def main():
    data = json.loads((ROOT / "docs" / "tech_network.json").read_text(encoding="utf-8"))
    portraits = data.get("portraits", [])
    # 作り直すときは中身だけ消す（.git は残す＝公開リポジトリの履歴。S-011）
    OUT.mkdir(parents=True, exist_ok=True)
    for c in OUT.iterdir():
        if c.name == ".git":
            continue
        shutil.rmtree(c) if c.is_dir() else c.unlink()
    (OUT / "data" / "nodes").mkdir(parents=True)
    for n in data["nodes"]:
        h = {k: n.get(k) for k in HISTORY if k in n}
        h["real"] = h.pop("real_full", None)
        # 人物：肖像は自作のドット絵（存命の人は載せていない）。番号で持っている肖像を中に戻す
        h["people"] = [{k: v for k, v in p.items() if k != "pi"} | ({"portrait": portraits[p["pi"]]} if p.get("pi") is not None else {})
                       for p in n.get("people", [])]
        g = {k: n.get(k) for k in GAME if k in n}
        rec = {"id": n["id"], "history": h, "game": g, "icon": n.get("icon")}
        (OUT / "data" / "nodes" / f"{n['id']}.json").write_text(json.dumps(rec, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    (OUT / "data" / "edges.json").write_text(json.dumps(data["edges"], ensure_ascii=False, indent=0) + "\n", encoding="utf-8")
    shutil.copy(ROOT / "docs" / "glossary.json", OUT / "data" / "glossary.json")
    meta = {k: data[k] for k in ("generated_by", "start", "goal", "real_note", "year_note", "requires_note", "edge_kinds", "kinds") if k in data}
    meta["license"] = "CC0-1.0"
    meta["source_repository"] = "https://github.com/taku3i0317-collab/semiconductor-lab"
    meta["explained_inversions"] = [x for x in data.get("year_inversions", []) if x.get("explained")]
    (OUT / "data" / "meta.json").write_text(json.dumps(meta, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")

    # 図のページ（GitHub Pages は docs/ から）
    (OUT / "docs").mkdir()
    for f in ("network.html", "node.html", "people.html", "glossary.html", "design.css", "autolink.js", "glossary.js",
              "tech_network_graph.js", "tech_network_detail.js", "furigana.js", "furigana_dict.js", "linebreak.js", "ui_icons.js", "film.js", "credits.html"):
        shutil.copy(ROOT / "docs" / f, OUT / "docs" / f)
    shutil.copytree(ROOT / "docs" / "vendor", OUT / "docs" / "vendor")   # BudouX（Apache-2.0。CC0 ではない。LICENSE-budoux.txt を一緒に）
    for f in ("node.html",):   # 誤りの報告はこのリポジトリの Issue へ
        p = OUT / "docs" / f
        p.write_text(p.read_text(encoding="utf-8").replace("https://github.com/taku3i0317-collab/semiconductor-lab", f"https://github.com/{REPO}"), encoding="utf-8")
    (OUT / "docs" / "index.html").write_text('<!doctype html><meta charset="utf-8"><meta http-equiv="refresh" content="0; url=network.html"><a href="network.html">Technology network</a>\n', encoding="utf-8")

    # Issue のひな形と自動チェック
    shutil.copytree(ROOT / ".github" / "ISSUE_TEMPLATE", OUT / ".github" / "ISSUE_TEMPLATE")
    owner, name = REPO.split("/")
    cfg = OUT / ".github" / "ISSUE_TEMPLATE" / "config.yml"   # 図へのリンクはこのリポジトリの GitHub Pages へ
    cfg.write_text(cfg.read_text(encoding="utf-8").replace("https://taku3i0317-collab.github.io/semiconductor-lab/", f"https://{owner}.github.io/{name}/"), encoding="utf-8")
    (OUT / ".github" / "workflows").mkdir(parents=True)
    (OUT / ".github" / "workflows" / "check.yml").write_text(CHECK_YML, encoding="utf-8")
    (OUT / "tools").mkdir()
    (OUT / "tools" / "check.py").write_text(CHECK_PY, encoding="utf-8")
    (OUT / "README.md").write_text(README.replace("{REPO}", REPO).replace("{N}", str(len(data["nodes"]))), encoding="utf-8")
    (OUT / "LICENSE").write_text(LICENSE, encoding="utf-8")
    size = sum(f.stat().st_size for f in OUT.rglob("*") if f.is_file())
    print(f"wrote {OUT} ({len(data['nodes'])} nodes, {size / 1e6:.1f} MB)")


CHECK_YML = """name: check
on: [push, pull_request]
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: "3.12"
      - run: python tools/check.py
"""

CHECK_PY = '''#!/usr/bin/env python3
"""tech-network の自動チェック（依存なし）。失敗があれば終了コード 1
1. 存在しない番号：前提・理解・前提になる・説明する・部品・線が、無いノードを指していないか
2. 前提の輪：前提をたどって自分に戻る輪が無いか
3. 年の逆転：ノードの年が、必ず要る前提の年より古くないか（data/meta.json の explained_inversions に説明ずみの物は除く。報告だけで失敗にはしない）
4. 出典の印：本当の話（history.real）があるのに出典（sources・refs）が1つも無く、⚠️ も無いノード
Automatic checks: unknown IDs, prerequisite cycles, year inversions (report only), and nodes with neither a source nor a ⚠️ mark.
"""
import json, sys
from pathlib import Path
D = Path(__file__).resolve().parent.parent / "data"
nodes = {p.stem: json.loads(p.read_text(encoding="utf-8")) for p in (D / "nodes").glob("*.json")}
edges = json.loads((D / "edges.json").read_text(encoding="utf-8"))
meta = json.loads((D / "meta.json").read_text(encoding="utf-8"))
bad = []
for e in edges:
    for k in ("from", "to"):
        if e[k] not in nodes:
            bad.append(f"unknown id [{e[k]}] in edge {e['from']} -> {e['to']} ({e['kind']})")
pre = {i: [e["from"] for e in edges if e["to"] == i and e["kind"] == "requires"] for i in nodes}
state = {}
def dfs(u, stack):
    state[u] = 1; stack.append(u)
    for p in pre.get(u, []):
        if state.get(p) == 1:
            bad.append("cycle: " + " -> ".join(stack[stack.index(p):] + [p]))
        elif not state.get(p):
            dfs(p, stack)
    stack.pop(); state[u] = 2
sys.setrecursionlimit(10000)
for u in nodes:
    if not state.get(u):
        dfs(u, [])
explained = {(x["node"], x["requires"]) for x in meta.get("explained_inversions", [])}
inv = []
for i, n in nodes.items():
    y = n["history"].get("year")
    for alts in n["history"].get("requires") or []:
        ys = [max([nodes[a]["history"]["year"] for a in alt if a in nodes and nodes[a]["history"].get("year") is not None] or [None], key=lambda v: -10**9 if v is None else v) for alt in alts]
        ys = [v for v in ys if v is not None]
        if y is not None and ys and min(ys) > y:
            late = [a for alt in alts for a in alt if a in nodes and nodes[a]["history"].get("year") == min(ys)]
            if not any((i, a) in explained for a in late):
                inv.append(f"[{i}] {y} < prerequisite {late} {min(ys)}")
nosrc = []
for i, n in nodes.items():
    h = n["history"]
    if h.get("real") and not h.get("sources") and not h.get("refs") and "⚠️" not in json.dumps(h, ensure_ascii=False) and "出典" not in (h.get("hee1") or "") + (h.get("real") or ""):
        nosrc.append(i)
print(f"nodes={len(nodes)} edges={len(edges)} errors={len(bad)} year_inversions={len(inv)} (report only) no_source_mark={len(nosrc)} (report only)")
for b in bad: print("ERROR", b)
for b in inv[:300]: print("inversion", b)
if nosrc: print("no source or warning mark:", " ".join(sorted(nosrc)))
sys.exit(1 if bad else 0)
'''

README = """# tech-network — 技術の地図 / A map of technology

石器から AI まで、{N} の技術・モノ・発見が「何が無いと何が作れないか」でつながる地図です。
教育 MOD「Semiconductor Lab」（Minecraft）のために作り、世界中の人に確かめてもらうために公開しています。
From stone tools to AI: {N} technologies, products and discoveries, connected by "what cannot be made without what".
Made for the educational Minecraft mod "Semiconductor Lab", and published so that people everywhere can check it.

- 図 / Map: https://{OWNER}.github.io/{NAME}/ （GitHub Pages、docs/）。「English」ボタンで英語に / English button available
- データ / Data: `data/nodes/<id>.json`（1ノード1ファイル）。`history` が本当の技術の歴史、`game` が MOD での作り方 / `history` is the real history, `game` is how it is made in the mod
- つながり / Edges: `data/edges.json`（kind：requires＝前提・understanding＝なぜ動くかの説明・enables＝考え方から生まれた・part＝部品・tool＝つくるのに使った道具・same＝同じしくみ）
- 自動チェック / Checks: `python tools/check.py`（存在しない番号・前提の輪・年の逆転・出典の印 / unknown IDs, cycles, year inversions, source marks）

## 誤りを見つけたら / Found a mistake?
Issue のひな形（年・人物／つながり／出典／画像）から知らせてください。ノードのページの「誤りを報告」ボタンで、番号が入った状態で開きます。
Please use the issue templates (year/person, link, source, image). The "report" buttons on each node page open them with the node ID filled in.
https://github.com/{REPO}/issues

## 決まり / Rules
- 確かさ：◎ 信頼できる出典2つ以上 ／ ○ 1つ ／ △ 未確認（⚠️）。Wikipedia だけの物は ○ まで / ◎ two reliable sources, ○ one, △ unverified (⚠️); Wikipedia alone is at most ○
- 存命の人の顔（肖像）は載せません。肖像は自作のドット絵です / No portraits of living people; portraits are original pixel art
- 文は自分の言葉で書いています（Wikipedia などの丸写しはしない）/ Texts are written in our own words (no copying)

## 影響を受けた作品 / Inspirations
科学の出典とは別に、影響を受けた人・作品 / Separate from the scientific sources, the people and works that inspired this project:
- 亜留間次郎さん（薬理凶室）— 配信・動画、『アリエナイ医学事典』（三才ブックス）ほか。いちばん影響を受けました。/ Jiro Aruma (Yakuri Kyoshitsu) — streams and videos, "Arienai Igaku Jiten" (Sansai Books) and more. Our biggest influence.

## ライセンス / License
CC0 1.0（パブリックドメイン）。名前を書かなくても、何にでも自由に使えます。/ CC0 1.0 (public domain dedication). Use it for anything, no attribution required.
例外：docs/vendor/budoux-ja.min.js（日本語の折り返しの道具 BudouX、Google）は Apache License 2.0（docs/vendor/LICENSE-budoux.txt）。/ Exception: docs/vendor/budoux-ja.min.js (BudouX by Google, for Japanese line breaks) is under the Apache License 2.0 (docs/vendor/LICENSE-budoux.txt).
元のリポジトリ / Source: https://github.com/taku3i0317-collab/semiconductor-lab
"""

LICENSE = """CC0 1.0 Universal

The authors of tech-network have dedicated this work to the public domain by waiving all of their rights to the work
worldwide under copyright law, including all related and neighboring rights, to the extent allowed by law.
You can copy, modify, distribute and perform the work, even for commercial purposes, all without asking permission.

Full legal text: https://creativecommons.org/publicdomain/zero/1.0/legalcode
Summary: https://creativecommons.org/publicdomain/zero/1.0/

Links to third-party images (Wikimedia Commons, etc.) in data/nodes/*.json "media" are references only; those images keep
their own licenses as listed with each link.

Exception: docs/vendor/budoux-ja.min.js is BudouX (Copyright 2021 Google LLC), licensed under the Apache License,
Version 2.0, not CC0. Its license text is in docs/vendor/LICENSE-budoux.txt.
"""

if __name__ == "__main__":
    owner, name = REPO.split("/")
    README = README.replace("{OWNER}", owner).replace("{NAME}", name)
    main()

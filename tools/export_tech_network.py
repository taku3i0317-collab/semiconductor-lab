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
import subprocess
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
    for f in ("network.html", "node.html", "node.css", "node.js", "thing.css", "thing.js", "people.html", "glossary.html", "design.css", "autolink.js", "glossary.js",
              "tech_network_graph.js", "tech_network_detail.js", "furigana.js", "furigana_dict.js", "linebreak.js", "ui_icons.js", "film.js", "credits.html", "ranking.html", "og_image.png", "share.js", "pwa.js", "manifest.webmanifest", "app_icon_192.png", "app_icon_512.png", "app_icon_maskable_512.png", "apple-touch-icon.png"):
        shutil.copy(ROOT / "docs" / f, OUT / "docs" / f)
    shutil.copytree(ROOT / "docs" / "vendor", OUT / "docs" / "vendor")
    subprocess.run([sys.executable, "-I", str(ROOT / "tools" / "gen_node_pages.py")], check=True)   # いつも今のデータから作り直す（このリポジトリの docs/nodes も＝semiconductor-lab の Pages）
    shutil.copytree(ROOT / "docs" / "nodes", OUT / "docs" / "nodes")
    if (ROOT / "docs" / "wiki" / "index.html").exists():   # S-056：MOD のウィキ（tools/gen_wiki.py・tools/gen_wiki_iso.mjs が作る）
        shutil.copytree(ROOT / "docs" / "wiki", OUT / "docs" / "wiki")   # S-054：ノードごとのページ（tools/gen_node_pages.py が作る）   # BudouX（Apache-2.0。CC0 ではない。LICENSE-budoux.txt を一緒に）
    for f in ("node.html", "node.js", "network.html"):   # 誤りの報告はこのリポジトリの Issue へ。リンクの見た目（OGP）の URL はこのリポジトリの GitHub Pages へ（S-023）
        p = OUT / "docs" / f
        p.write_text(p.read_text(encoding="utf-8").replace("https://github.com/taku3i0317-collab/semiconductor-lab", f"https://github.com/{REPO}").replace("https://taku3i0317-collab.github.io/semiconductor-lab/", "https://{}.github.io/{}/".format(*REPO.split("/"))), encoding="utf-8")
    subprocess.run([sys.executable, "-I", str(ROOT / "tools" / "gen_sw.py"), str(OUT / "docs")], check=True)   # オフライン用の service worker（S-026）。版の番号は書き出した中身から
    # いちばん上のページ（/tech-network/ を開くと出る）＝入口（S-061、tools/gen_home.py。Search Console の確認の1行〈S-052〉も入っている）。章の一覧・道案内・さがすの索引も
    subprocess.run([sys.executable, "-I", str(ROOT / "tools" / "gen_home.py"), str(OUT / "docs")], check=True)

    # Issue のひな形と自動チェック
    shutil.copytree(ROOT / ".github" / "ISSUE_TEMPLATE", OUT / ".github" / "ISSUE_TEMPLATE")
    owner, name = REPO.split("/")
    cfg = OUT / ".github" / "ISSUE_TEMPLATE" / "config.yml"   # 図へのリンクはこのリポジトリの GitHub Pages へ
    cfg.write_text(cfg.read_text(encoding="utf-8").replace("https://taku3i0317-collab.github.io/semiconductor-lab/", f"https://{owner}.github.io/{name}/"), encoding="utf-8")
    (OUT / ".github" / "workflows").mkdir(parents=True)
    (OUT / ".github" / "workflows" / "check.yml").write_text(CHECK_YML, encoding="utf-8")
    (OUT / "tools").mkdir()
    (OUT / "tools" / "check.py").write_text(CHECK_PY, encoding="utf-8")
    fill = lambda t: t.replace("{REPO}", REPO).replace("{N}", str(len(data["nodes"]))).replace("{E}", str(len(data["edges"])))
    # S-051：応援（寄付）の入口。network_meta.json の support が空のあいだは何も出さない（FUNDING.yml も置かない）
    sup = json.loads((ROOT / "docs" / "network_meta.json").read_text(encoding="utf-8")).get("support") or {}
    readme = fill(README)
    if sup.get("url"):
        u = sup["url"]
        readme = readme.replace("## Found a mistake?", "## Support\n\nFor grown-ups: if this map is useful to you, you can support its upkeep here: " + u + " (no ads, ever; everything stays CC0).\n\n## Found a mistake?", 1)
        readme = readme.replace("## 誤りを見つけたら", "## 応援\n大人の方へ：この地図を続けるための応援を受け付けています：" + u + "（広告は付けません。全部 CC0 のまま）\n\n## 誤りを見つけたら", 1)
        fy = "".join(k + ": " + v + "\n" for k, v in (("github", sup.get("github")), ("ko_fi", sup.get("ko_fi"))) if v)
        if fy:
            (OUT / ".github" / "FUNDING.yml").write_text(fy, encoding="utf-8")
    (OUT / "README.md").write_text(readme, encoding="utf-8")
    # S-050：引用のしかた（CITATION.cff）と、検索のための sitemap.xml（日本語と英語 ?lang=en）
    import datetime
    (OUT / "CITATION.cff").write_text(fill(CITATION).replace("{DATE}", datetime.date.today().isoformat()), encoding="utf-8")
    site = "https://{}.github.io/{}/".format(*REPO.split("/"))
    urls = [f'<url><loc>{site}{pg}</loc><xhtml:link rel="alternate" hreflang="ja" href="{site}{pg}"/><xhtml:link rel="alternate" hreflang="en" href="{site}{pg}?lang=en"/></url>'
            for pg in ["index.html"] + sorted(f"chapters/{p.name}" for p in (OUT / "docs" / "chapters").glob("*.html")) + sorted(f"guides/{p.name}" for p in (OUT / "docs" / "guides").glob("*.html")) + ["network.html", "ranking.html", "people.html", "glossary.html", "credits.html"]
            + [f"nodes/{i}.html" for i in json.loads((ROOT / "docs" / "nodes" / "index.json").read_text(encoding="utf-8"))["indexed"]]
            + (["wiki/index.html"] + [f"wiki/{x['u']}" for x in json.loads((ROOT / "docs" / "wiki" / "index.json").read_text(encoding="utf-8"))] if (ROOT / "docs" / "wiki" / "index.json").exists() else [])]   # S-056：ウィキ   # S-054：中身のあるノードのページ（noindex の物は入れない）
    (OUT / "docs" / "sitemap.xml").write_text('<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">\n' + "\n".join(urls) + "\n</urlset>\n", encoding="utf-8")
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

README = """# tech-network — A map of technology / 技術の地図

**What:** a CC0 knowledge graph of {N} technologies, products and discoveries — from stone tools to EUV lithography and AI — connected by "what cannot be made without what" ({E} links), each with years, people, sources and a certainty mark.
**For whom:** anyone who wants to know how our technical civilization was built, or what it would take to make something (a smartphone, a transistor, an EUV machine) from scratch — students, teachers, curious people, and AI systems that need checked prerequisites.
**License:** CC0 1.0 (public domain). Use it for anything, no attribution required.

- **Interactive map:** https://{OWNER}.github.io/{NAME}/ (works on phones; "English" button, or add `?lang=en`)
- **Ranking of the technologies that support the most things:** https://{OWNER}.github.io/{NAME}/ranking.html
- **Source repository:** https://github.com/taku3i0317-collab/semiconductor-lab (the educational Minecraft mod "Semiconductor Lab" this was made for)
- **Cite:** `CITATION.cff` (GitHub's "Cite this repository" button)

## Data

| file | content |
|---|---|
| `data/nodes/<id>.json` | one node per file. `history` = the real history (name, name_en, kind, year, people, sources, refs with certainty, requires …). `game` = how it is made in the mod (kept separate) |
| `data/edges.json` | `[{"from": "0-01", "to": "0-02", "kind": "requires"}, …]`. kinds: `requires` (cannot be made without), `understanding` (explains why it works, found later), `enables` (an idea that made it possible), `part` (a component), `tool` (used to design or make it), `same` (same mechanism) |
| `data/glossary.json` | terms in Japanese and English |

```python
import json, glob, collections
nodes = {}
for f in glob.glob("data/nodes/*.json"):
    d = json.load(open(f, encoding="utf-8")); nodes[d["id"]] = d
edges = json.load(open("data/edges.json", encoding="utf-8"))
parents = collections.defaultdict(list)
for e in edges:
    if e["kind"] in ("requires", "enables", "part"):
        parents[e["to"]].append(e["from"])
need, stack = set(), ["B-10"]          # everything needed to make a smartphone
while stack:
    for p in parents[stack.pop()]:
        if p not in need:
            need.add(p); stack.append(p)
print(len(need), "prerequisites, e.g.", sorted(nodes[k]["history"]["name_en"] for k in list(need)[:5]))
```

Checks: `python tools/check.py` (unknown IDs, prerequisite cycles, year inversions, source marks). It runs on every push.

## Honest limits

- **Most texts were written by AI** (Claude), in a project run by one person with several AI sessions. Facts were checked against sources, and about 100 nodes were also audited by a second AI model. **No human expert has reviewed the data yet.**
- Certainty: ◎ two reliable sources agree, ○ one source (Wikipedia alone is at most ○), △/⚠️ unverified or disputed.
- "requires" is a teaching simplification chosen with sources ("historically needed to make it"), not a complete bill of materials.
- Coverage is centred on what the game needs (semiconductors, electricity, chemistry); other fields are thin. Long texts are in Japanese; every node has an English name and a short English fun fact.
- No portraits of living people; portraits are original pixel art. Dangerous technologies are described for understanding, not as step-by-step instructions.

## Found a mistake?

Please open an issue (templates: year/person, link, source, image): https://github.com/{REPO}/issues — the "report" button on each node page fills in the node ID.

## License

CC0 1.0. Exception: `docs/vendor/budoux-ja.min.js` (BudouX by Google, for Japanese line breaks) is Apache License 2.0 (`docs/vendor/LICENSE-budoux.txt`). Links to third-party images keep their own licenses.

Inspiration (separate from the scientific sources): Jiro Aruma (Yakuri Kyoshitsu) — streams, videos and books. Our biggest influence.

---

# 日本語

**何か：** 石器から EUV 露光装置・AI まで、{N} の技術・モノ・発見を「何が無いと何が作れないか」（{E} のつながり）で結んだ地図。1つ1つに年・人物・出典・確かさの印。
**だれのため：** 技術の文明がどうやってできたかを知りたい人、何か（スマホ・トランジスタ・EUV）をゼロから作るには何が要るかを知りたい人。学生・先生・好奇心のある人、確かめた前提が要る AI。
**ライセンス：** CC0 1.0（パブリックドメイン）。名前を書かなくても、何にでも自由に使えます。

- 図：https://{OWNER}.github.io/{NAME}/ （スマホでも見られる。「English」ボタンか `?lang=en`）・ランキング：https://{OWNER}.github.io/{NAME}/ranking.html
- 元のリポジトリ：https://github.com/taku3i0317-collab/semiconductor-lab （教育 MOD「Semiconductor Lab」）
- 引用のしかた：`CITATION.cff`（GitHub の「Cite this repository」）
- データ：`data/nodes/<id>.json`（1ノード1ファイル。`history`＝本当の歴史、`game`＝MOD での作り方）、`data/edges.json`（つながり。kind：requires＝前提・understanding＝なぜ動くか・enables＝考え方から生まれた・part＝部品・tool＝つくるのに使った道具・same＝同じしくみ）、`data/glossary.json`（用語）。使い方の例は上の Python
- 自動チェック：`python tools/check.py`（push のたびに動く）

## 正直に書くこと（限界）
- **文の大部分は AI（Claude）が書いた。** 1人の人と複数の AI のセッションで作っている。出典で確かめ、約 100 のノードは別の AI のモデルでも監査した。**人の専門家の確認はまだ**
- 確かさ：◎ 信頼できる出典2つ以上 ／ ○ 1つ（Wikipedia だけは ○ まで）／ △・⚠️ 未確認・諸説
- 「前提」は教えるための単純化（出典をもとに「歴史の上で作るのに要った物」）で、部品表のすべてではない
- 範囲はゲームに要る物（半導体・電気・化学）が中心。長い文は日本語（名前と短いへえは全ノード英語あり）
- 存命の人の顔は載せない（肖像は自作のドット絵）。危ない技術は理解のために書き、作り方の手順は書かない

## 誤りを見つけたら
Issue のひな形（年・人物／つながり／出典／画像）から：https://github.com/{REPO}/issues （ノードのページの「誤りを報告」で番号が入った状態で開く）

## ライセンス
CC0 1.0。例外：docs/vendor/budoux-ja.min.js（BudouX、Google）は Apache License 2.0。ほかのサイトの画像へのリンクは、それぞれのライセンス。
影響を受けた作品（科学の出典とは別に）：亜留間次郎さん（薬理凶室）— 配信・動画・本。いちばん影響を受けました。
"""

CITATION = """cff-version: 1.2.0
message: "If you use this dataset, please cite it as below (CC0: citation is welcome but not required)."
title: "tech-network: a map of technology (what cannot be made without what)"
type: dataset
authors:
  - alias: taku3i0317-collab
license: CC0-1.0
repository-code: "https://github.com/{REPO}"
url: "https://{OWNER}.github.io/{NAME}/"
abstract: "A CC0 knowledge graph of {N} technologies, products and discoveries from stone tools to EUV lithography and AI, connected by prerequisites ({E} links), with years, people, sources and certainty marks. Most texts were written by AI and checked against sources; not yet reviewed by human experts."
keywords:
  - history of technology
  - knowledge graph
  - education
  - open data
  - semiconductor
date-released: "{DATE}"
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
    CITATION = CITATION.replace("{OWNER}", owner).replace("{NAME}", name)
    main()

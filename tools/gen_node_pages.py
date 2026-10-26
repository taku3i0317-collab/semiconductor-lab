"""ノードごとの本物のページ nodes/<番号>.html を書き出す（S-054。S-061 で「1つの物に1つのページ」の形に作り直した、設計図 docs/mockups/S060_spec.md）。

上から いつも同じ順：これは何か（絵・名前・一言・▶ できるまで／共有）→ どうやって手に入れるか → 作り方（アイコンのレシピ、
A の docs/wiki_export.json から）→ 本当の歴史（もっと読むでたたむ）→ つながり（中心図はここから1つ）。本文は HTML に最初から書く（JS が無くても読める）。
- ページごとに title・description（へえ①）・canonical（tech-network のページ）・hreflang（日本語＝そのまま、英語＝?lang=en）・og・JSON-LD
- へえも本当の話も無いノードは noindex（中身ができたら自動で外れる）。sitemap.xml には入れない
- og:image：中身のあるノードだけ、アイコンと名前の絵（nodes/og/<番号>.png、1200×630）。無いノードは共通の og_image.png
- アイテムごとに id="it-<名前>"（前のウィキのアイテムのページ wiki/items/<名前>.html から、ここへ案内する）
- バニラの材料は日本語の名前（docs/vanilla_ja.json、自分たちで書いた）、絵は使わない
使い方：python tools/gen_node_pages.py [出力の docs フォルダ]（省略すると docs/）。tools/export_tech_network.py からも呼ぶ
"""
import base64
import html
import io
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SITE = "https://taku3i0317-collab.github.io/tech-network/"   # canonical は tech-network のページ（semiconductor-lab の Pages も同じ所を指す）
KIND = {"minecraft:crafting_shaped": "作業台", "minecraft:crafting_shapeless": "作業台", "minecraft:crafting_transmute": "作業台", "minecraft:smelting": "かまど",
        "minecraft:blasting": "溶鉱炉", "minecraft:campfire_cooking": "たき火", "minecraft:smoking": "燻製器"}
XLABEL = {"tool": ("つくるのに使った道具", "道具として使われた先"), "same": ("同じしくみ", "同じしくみ"), "improves": ("これが改良した元", "これを改良した物"),
          "byproduct": ("これが副産物としてできた元", "これを作る時に一緒にできた物"), "demand": ("これの需要を生んだ物", "これが需要を生んだ物"), "replaces": ("これが置き換えた物", "これを置き換えた物")}


def js_obj(path):
    s = path.read_text(encoding="utf-8")
    return json.loads(s[s.index("{"):s.rindex("}") + 1])


def year_text(n):
    y = n.get("year")
    if y is None:
        return "年不明"
    t = f"紀元前{-y}年" if y < 0 else f"{y}年"
    return t + ("頃" if n.get("year_uncertain") or (y is not None and y < 1500) else "")


def clean(t):
    """表示用：出典の書き込み（出典：URL）や簡単な記号を外す"""
    t = re.sub(r"（[^（）]*出典[:：][^）]*）", "", t or "")
    t = re.sub(r"\*\*|__|`", "", t)
    return t.strip()


def og_image(n, out):
    """1200×630：左にアイコン（ドット絵を大きく）、右に名前と年。中身のあるノードだけ"""
    from PIL import Image, ImageDraw, ImageFont
    W, H = 1200, 630
    im = Image.new("RGB", (W, H), "#faf7f0")
    d = ImageDraw.Draw(im)
    d.rectangle([24, 24, W - 24, H - 24], outline="#2f6f8f", width=6)
    if (n.get("icon") or "").startswith("data:image"):   # 新しいノードはアイコンがまだ無いことがある
        ic = Image.open(io.BytesIO(base64.b64decode(n["icon"].split(",", 1)[1]))).convert("RGBA").resize((384, 384), Image.NEAREST)
        im.paste(ic, (80, 123), ic)
    font = lambda s: ImageFont.truetype("C:/Windows/Fonts/meiryob.ttc", s) if Path("C:/Windows/Fonts/meiryob.ttc").exists() else ImageFont.load_default()
    name, x = n["name"], 520
    size = 76 if len(name) <= 7 else 60 if len(name) <= 10 else 46
    lines = [name[i:i + 12] for i in range(0, len(name), 12)][:2]
    y = 170
    for ln in lines:
        d.text((x, y), ln, fill="#2b2622", font=font(size)); y += size + 14
    d.text((x, y + 10), f"{year_text(n)}・{n.get('trunk_name', '')}", fill="#6b6259", font=font(36))
    if n.get("name_en"):
        d.text((x, y + 64), n["name_en"][:34], fill="#6b6259", font=font(30))
    d.text((x, H - 110), "技術ネットワーク / tech-network", fill="#2f6f8f", font=font(32))
    im.convert("P", palette=Image.ADAPTIVE, colors=48).save(out, optimize=True)   # 色を減らして軽く（ドット絵と字なので見た目はほぼ同じ）


def main():
    out_docs = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "docs"
    G = js_obj(ROOT / "docs" / "tech_network_graph.js")
    D = js_obj(ROOT / "docs" / "tech_network_detail.js")["nodes"]
    W = json.loads((ROOT / "docs" / "wiki_export.json").read_text(encoding="utf-8"))
    VAN = json.loads((ROOT / "docs" / "vanilla_ja.json").read_text(encoding="utf-8"))["names"]
    meta = json.loads((ROOT / "docs" / "network_meta.json").read_text(encoding="utf-8"))
    ITEMS = {i["id"]: i for i in W["items"]}
    MACH = {m["id"]: m for m in W["machines"]}
    ISO = ROOT / "docs" / "wiki" / "iso"
    ICONS = ROOT / "docs" / "wiki" / "icons"
    qf = ROOT / "build" / "wikidata" / "qids.json"
    QID = {k: v.get("qid") for k, v in (json.loads(qf.read_text(encoding="utf-8")).get("nodes", {}) if qf.exists() else {}).items() if v.get("qid")}
    byId = {n["id"]: n for n in G["nodes"]}
    kids, pars = {}, {}
    for f, t, k in G["edges"]:
        if k in ("r", "p", "e"):
            kids.setdefault(f, []).append(t)
            pars.setdefault(t, []).append(f)
    by_node = {}
    for i, it in ITEMS.items():
        if it.get("node"):
            by_node.setdefault(it["node"], []).append(i)
    made, mach_recs = {}, {}
    for r in W["recipes"]:
        if (r.get("output") or {}).get("item"):
            made.setdefault(r["output"]["item"], []).append(r)
    for r in W["machine_recipes"]:
        mach_recs.setdefault(r.get("machine"), []).append(r)
        for x in r.get("outputs", []):
            if x.get("item"):
                made.setdefault(x["item"], []).append(r)
    nd = out_docs / "nodes"
    (nd / "og").mkdir(parents=True, exist_ok=True)
    for old in nd.glob("*.html"):
        old.unlink()
    esc = lambda s: html.escape(str(s if s is not None else ""), quote=True)
    sl = lambda i: i.split(":", 1)[-1]

    def iname(i):
        if i in ITEMS:
            return ITEMS[i]["name"].get("ja") or ITEMS[i]["name"]["en"]
        if i in MACH:
            return MACH[i]["name"].get("ja")
        return VAN.get(sl(i), sl(i).replace("_", " "))

    def iicon(i):
        if i.startswith("semiconductor-lab:") and (ICONS / f"{sl(i)}.png").exists():
            return f'<img src="wiki/icons/{sl(i)}.png" alt="">'
        return f'<span class="vi">{esc(iname(i)[:1])}</span>'   # バニラ：絵は使わない

    def ilink(i):
        it = ITEMS.get(i)
        if it and it.get("node") in byId:
            return f'nodes/{it["node"]}.html#it-{sl(i)}'
        return f"wiki/items/{sl(i)}.html" if it else None

    def cell(x):
        i = x.get("item") or ""
        if not i and x.get("tag"):
            return f'<div class="cell"><span class="vi">#</span><b>{esc(x["tag"].split(":")[-1].replace("_", " "))} の仲間</b></div>'
        n = x.get("count", 1)
        alt = "".join(f"<br>または {esc(iname(a))}" for a in x.get("alt", []))
        inner = f'{iicon(i)}<b>{esc(iname(i))}{alt}</b>' + (f"<i>×{n}</i>" if n and n > 1 else "")
        u = ilink(i)
        return f'<a class="cell" href="{u}">{inner}</a>' if u else f'<div class="cell">{inner}</div>'

    def recipe(r):
        ins = "".join(cell(x) for x in r.get("inputs", []))
        if "machine" in r:
            m = r["machine"]
            u = ilink(m)
            how = f'<a href="{u}">{esc(iname(m))}</a>' if u else esc(iname(m))
            outs = "".join(cell(x) for x in r.get("outputs", []))
            sub = "・".join(x for x in [esc(r.get("process") or ""), f'{r["watts"]:,.0f} W' if r.get("watts") else "", f'{r["seconds"]:g} 秒' if r.get("seconds") else ""] if x)
            note = f'<div class="note">{esc(r["note"])}</div>' if r.get("note") else ""
        else:
            how = KIND.get(r.get("type"), "作業台")
            outs = cell(r["output"]) if r.get("output") else ""
            sub = f'{r["seconds"]:g} 秒' if r.get("seconds") else ""
            note = ""
        return f'<div class="rec"><div class="ins">{ins}</div><div class="how"><span>{how}</span><small>{sub}</small><em>→</em></div><div class="outs">{outs}</div>{note}</div>'

    def chips(ids):
        out = []
        for x in ids:
            if x not in byId:
                continue
            b = byId[x]
            ic = f'<img src="{b["icon"]}" alt="">' if b.get("icon") else '<span class="noi"></span>'
            out.append(f'<a href="nodes/{x}.html">{ic}<span data-en="{esc(b.get("name_en") or b["name"])}">{esc(b["name"])}</span></a>')
        return "".join(out)

    XL = [x for x in meta.get("extra_links", []) if "from" in x and x.get("kind") in XLABEL]
    indexed, noindex = [], []
    for n in G["nodes"]:
        i, d = n["id"], D.get(n["id"], {})
        hee1 = clean(d.get("hee1"))
        real = clean(d.get("real_full"))
        thin = not hee1 and not real
        heads = {"1": hee1}
        ref = (d.get("refs") or [{}])[0] if d.get("refs") else {}
        for k in ("2", "3", "4"):
            heads[k] = clean((d.get("hee_game") or {}).get("note" + k) or ((ref.get("hee") or {}).get(k) if ref else "") or (d.get("hee_tree") or {}).get(k))
        kind = "発見と歴史" if n["kind"] == "idea" else "仕組みと歴史"
        title = f'{n["name"]} とは｜{year_text(n)}・{kind}｜技術ネットワーク'
        desc = (hee1 or real or f'{n["name"]}（{year_text(n)}）：前提と使い道、出典。')[:150]
        if n.get("name_en"):
            desc = f'{desc} — {n["name_en"]}'[:200]
        canon = f"{SITE}nodes/{i}.html"
        img = f"{SITE}nodes/og/{i}.png" if not thin else f"{SITE}og_image.png"
        about = {"@type": "Thing", "name": n["name"]}
        if n.get("name_en"):
            about["alternateName"] = n["name_en"]
        if i in QID:
            about["sameAs"] = f"https://www.wikidata.org/wiki/{QID[i]}"
        ld = {"@context": "https://schema.org", "@type": "WebPage", "name": title, "description": desc, "url": canon, "inLanguage": "ja",
              "license": "https://creativecommons.org/publicdomain/zero/1.0/", "about": about,
              "isPartOf": {"@type": "Dataset", "name": "tech-network: a map of technology", "url": SITE}}
        reqs = list(dict.fromkeys(p for p in pars.get(i, []) if p in byId))
        uses = list(dict.fromkeys(k for k in kids.get(i, []) if k in byId))
        items = by_node.get(i, [])
        # 2 どうやって手に入れるか
        get = []
        for it in items:
            if not made.get(it):
                get.append(f'<div>{iicon(it)} <b>{esc(iname(it))}</b>：<span data-en="no recipe: found, mined or gathered">レシピは無い（拾う・掘る・ほかの方法）</span></div>')
        if items and len(get) < len(items):
            get.insert(0, f'<div><span data-en="In the game you make it as shown below.">ゲームでは、下の「作り方」で作る</span>（{len(items) - len(get)}）</div>')
        if not items:
            if n.get("status") == "✅":
                get = ['<div><span data-en="In the game (details not yet written).">ゲームで作れる（くわしい作り方はまだ）</span></div>']
            else:
                get = ['<div><span data-en="Not in the game yet: real history only.">ゲームにはまだ無い（本当の歴史だけ）</span></div>']
        # 3 作り方
        how, has3d = [], False
        for it in items:
            rs = made.get(it, [])
            mr = mach_recs.get(it, [])
            iso = (ISO / f"{sl(it)}.png").exists()
            if not rs and not mr and not iso:
                continue
            blk = [f'<div class="item" id="it-{sl(it)}"><div class="iname">{iicon(it)}<span>{esc(iname(it))}</span></div>']
            blk += [recipe(r) for r in rs[:3]]
            if len(rs) > 3:
                blk.append(f'<p class="meta">ほか {len(rs) - 3} の作り方</p>')
            if iso:
                if not has3d:
                    blk.append(f'<div class="iso"><img src="wiki/iso/{sl(it)}.png" alt=""><button type="button" class="btn" data-3d="{it}" data-en="View in 3D">3D で見る</button></div><div id="v3d"></div>')
                    has3d = True
                else:
                    blk.append(f'<div class="iso"><img src="wiki/iso/{sl(it)}.png" alt=""></div>')
            if mr:
                blk.append(f'<p class="meta"><span data-en="What this machine makes">この機械でできること</span>（{len(mr)}）</p>' + "".join(recipe(r) for r in mr[:4]) + (f'<p class="meta">ほか {len(mr) - 4}</p>' if len(mr) > 4 else ""))
            blk.append("</div>")
            how.append("".join(blk))
        # 4 本当の歴史（最初の段落だけ。のこりは「もっと読む」）
        paras = [p for p in real.split("\n") if p.strip()]
        more = []
        for k, lv in (("2", "②学生"), ("3", "③大人"), ("4", "④ガチ")):
            if heads[k]:
                more.append(f"<p><b>{lv}</b> {esc(heads[k])}</p>")
        more += [f"<p>{esc(p)}</p>" for p in paras[1:]]
        if d.get("people"):
            more.append("<p><b>人物</b>　" + "・".join(f'{esc(p.get("name"))}（{esc(p.get("year"))}）' for p in d["people"]) + "</p>")
        if d.get("reading"):
            more.append("<p><b>もっと知りたい人へ</b>（Long Now 財団の Manual for Civilization の推薦の本）　" + "・".join(f'<i>{esc(b["title"])}</i>（{esc(b["authors"])}）' + (f'『{esc(b["ja"])}』' if b.get("ja") else "") for b in d["reading"]) + "</p>")
        srcs = [u for u in dict.fromkeys((d.get("sources") or []) + [r.get("url") for r in d.get("refs", []) if r.get("url")]) if str(u).startswith("http")]
        if srcs:
            more.append("<p><b>出典</b></p><ul>" + "".join(f'<li><a href="{esc(u)}" rel="noopener">{esc(u)}</a></li>' for u in srcs) + "</ul>")
        # 5 つながり（前提・使い道・ほかの種類）
        xgroups = {}
        for x in XL:
            if x["to"] == i and x["from"] in byId:
                xgroups.setdefault(XLABEL[x["kind"]][0], []).append(x["from"])
            elif x["from"] == i and x["to"] in byId:
                xgroups.setdefault(XLABEL[x["kind"]][1], []).append(x["to"])
        icon_big = f'<img src="{n["icon"]}" alt="">' if n.get("icon") else ""
        warn = '　<span class="warn">⚠️ 年は諸説</span>' if n.get("year_uncertain") else ""
        one_en = f' data-en="{esc(clean(d.get("hee1_en")))}"' if d.get("hee1_en") else ""
        film_btn = f'<button type="button" class="btn main" data-film="{i}" data-en="▶ How it was made">▶ できるまで</button>' if reqs else ""
        how_html = ('<h2 data-en="How to make it">作り方</h2>' + "".join(how)) if how else ""
        hist = f"<p>{esc(paras[0])}</p>" if paras else '<p class="meta">（まだ書いていない）</p>'
        more_html = f'<details class="more"><summary data-en="Read more (students, adults, deep dive, people, sources)">もっと読む（学生・大人・ガチ・人物・出典）</summary>{"".join(more)}</details>' if more else ""
        uses_more = f'<span class="meta">ほか {len(uses) - 24}</span>' if len(uses) > 24 else ""
        xg_html = "".join(f'<h3>{esc(lab)}</h3><div class="th">{chips(ids)}</div>' for lab, ids in xgroups.items())
        body = f"""<div class="top"><a class="home" href="index.html" data-en="Tech map">技術の地図</a><form action="index.html"><input type="search" name="q" placeholder="さがす（物・技術・人の名前）" data-en-ph="Search (things, techniques, people)" aria-label="さがす"></form></div>
<div class="wrap cols2"><div class="cols"><div>
<section class="what"><div class="big">{icon_big}</div><div><h1 data-en="{esc(n.get("name_en") or n["name"])}">{esc(n["name"])}</h1><div class="meta">{esc(year_text(n))}・{esc(n.get("trunk_name", ""))}{warn}</div></div></section>
<p class="one"{one_en}>{esc(hee1)}</p>
<div class="acts">{film_btn}<button type="button" class="btn" data-share="{i}" data-share-view="node" data-en="Share">共有</button></div>
<h2 data-en="How to get it">どうやって手に入れるか</h2><div class="get">{"".join(get)}</div>
{how_html}
<h2 data-en="Real history">本当の歴史</h2>{hist}
{more_html}
</div><div class="side">
<h2 data-en="Connections">つながり</h2><div class="links">
<h3 data-en="Needed to make this">これを作るのに要る</h3><div class="th">{chips(reqs) or '<span class="meta">（始まり）</span>'}</div>
<h3 data-en="Made possible by this">これで作れるようになった</h3><div class="th">{chips(uses[:24]) or '<span class="meta">—</span>'}{uses_more}</div>
{xg_html}
</div>
<p><a class="btn" href="network.html#center/{i}" data-en="See the ring map">中心図で見る</a></p>
<div class="quiet"><button type="button" id="langBtn" lang="en">English</button><button type="button" id="furiBtn" aria-pressed="false">ふりがな</button><a href="https://github.com/taku3i0317-collab/tech-network/issues/new/choose" rel="noopener" data-en="Report a mistake">誤りを知らせる</a><a href="network.html" data-en="Year map">年の図</a></div>
</div></div></div>"""
        head = "\n".join([
            '<base href="../">',
            f"<title>{esc(title)}</title>",
            f'<meta name="description" content="{esc(desc)}">',
            '<meta name="robots" content="noindex">' if thin else "",
            f'<link rel="canonical" href="{canon}">',
            f'<link rel="alternate" hreflang="ja" href="{canon}">', f'<link rel="alternate" hreflang="en" href="{canon}?lang=en">', f'<link rel="alternate" hreflang="x-default" href="{canon}">',
            f'<meta property="og:type" content="article"><meta property="og:title" content="{esc(title)}"><meta property="og:description" content="{esc(desc)}"><meta property="og:url" content="{canon}"><meta property="og:image" content="{img}"><meta name="twitter:card" content="summary_large_image">',
            '<script type="application/ld+json">' + json.dumps(ld, ensure_ascii=False) + "</script>",
        ])
        viewer = '<script src="wiki/viewer.js" defer></script>' if has3d else ""
        page = f"""<!doctype html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
{head}
<meta name="theme-color" content="#faf7f0"><link rel="manifest" href="manifest.webmanifest"><link rel="icon" type="image/png" href="app_icon_192.png">
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=BIZ+UDPGothic:wght@400;700&family=DotGothic16&display=swap">
<link rel="stylesheet" href="thing.css">
</head>
<body>
{body}
<script src="thing.js"></script>
<script src="tech_network_graph.js" defer></script><script src="film.js" defer></script><script src="share.js" defer></script>
{viewer}
<script src="furigana.js"></script><script>Furigana.init(document.getElementById('furiBtn'));</script>
<script src="pwa.js" defer></script>
</body>
</html>
"""
        (nd / f"{i}.html").write_text(page, encoding="utf-8")
        if thin:
            noindex.append(i)
        else:
            indexed.append(i)
            og = nd / "og" / f"{i}.png"
            if not og.exists():
                og_image(n, og)
    (nd / "index.json").write_text(json.dumps({"indexed": indexed, "noindex": noindex}, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"node pages: {len(indexed) + len(noindex)} (indexed {len(indexed)}, noindex {len(noindex)}) -> {nd}")


if __name__ == "__main__":
    main()

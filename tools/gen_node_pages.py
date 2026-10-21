"""ノードごとの本物のページ nodes/<番号>.html を書き出す（S-054）。

中身は node.html と同じ見た目・動き（JS はそのまま）。そのうえで、本文（名前・年・へえ・本当の話・前提と使い道のリンク・人物・出典）を
HTML に最初から書いておく（JS が無くても読める・検索に見つけてもらえる）。JS が動くと、いつもの画面に入れかわる。
- ページごとに title・description（へえ①）・canonical（tech-network のページ）・hreflang（日本語＝そのまま、英語＝?lang=en）・og・JSON-LD
- へえも本当の話も無いノードは noindex（中身ができたら自動で外れる）。sitemap.xml には入れない
- og:image：中身のあるノードだけ、アイコンと名前の絵（nodes/og/<番号>.png、1200×630）。無いノードは共通の og_image.png
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
    if n.get("icon", "").startswith("data:image"):
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
    qf = ROOT / "build" / "wikidata" / "qids.json"
    QID = {k: v.get("qid") for k, v in (json.loads(qf.read_text(encoding="utf-8")).get("nodes", {}) if qf.exists() else {}).items() if v.get("qid")}
    tpl = (ROOT / "docs" / "node.html").read_text(encoding="utf-8")
    # 見出しまわりの S-050 の行（node.html 1枚用の説明・URL）は、ページごとの物に入れかえる
    tpl = re.sub(r'<meta name="description"[^>]*>\n', "", tpl)
    tpl = re.sub(r'<link rel="(canonical|alternate)"[^>]*>\n', "", tpl)
    tpl = re.sub(r"<title>[^<]*</title>", "", tpl)
    byId = {n["id"]: n for n in G["nodes"]}
    kids = {}
    for f, t, k in G["edges"]:
        if k in ("r", "p", "e"):
            kids.setdefault(f, []).append(t)
    nd = out_docs / "nodes"
    (nd / "og").mkdir(parents=True, exist_ok=True)
    for old in nd.glob("*.html"):
        old.unlink()
    esc = lambda s: html.escape(str(s or ""), quote=True)
    link = lambda i: f'<a href="nodes/{i}.html">{esc(byId[i]["name"])}</a>' if i in byId else ""
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
        reqs = [p for alts in d.get("requires", []) for a in alts for p in a]
        reqs = list(dict.fromkeys([p for p in reqs if p in byId] + [p for p in d.get("parts", []) if p in byId]))
        uses = [k for k in dict.fromkeys(kids.get(i, [])) if k in byId]
        about = {"@type": "Thing", "name": n["name"]}
        if n.get("name_en"):
            about["alternateName"] = n["name_en"]
        if i in QID:
            about["sameAs"] = f"https://www.wikidata.org/wiki/{QID[i]}"
        ld = {"@context": "https://schema.org", "@type": "WebPage", "name": title, "description": desc, "url": canon, "inLanguage": "ja",
              "license": "https://creativecommons.org/publicdomain/zero/1.0/", "about": about,
              "isPartOf": {"@type": "Dataset", "name": "tech-network: a map of technology", "url": SITE}}
        head = "\n".join([
            '<base href="../">',
            f"<title>{esc(title)}</title>",
            f'<meta name="description" content="{esc(desc)}">',
            '<meta name="robots" content="noindex">' if thin else "",
            f'<link rel="canonical" href="{canon}">',
            f'<link rel="alternate" hreflang="ja" href="{canon}">', f'<link rel="alternate" hreflang="en" href="{canon}?lang=en">', f'<link rel="alternate" hreflang="x-default" href="{canon}">',
            f'<meta property="og:type" content="article"><meta property="og:title" content="{esc(title)}"><meta property="og:description" content="{esc(desc)}"><meta property="og:url" content="{canon}"><meta property="og:image" content="{img}"><meta name="twitter:card" content="summary_large_image">',
            '<script type="application/ld+json">' + json.dumps(ld, ensure_ascii=False) + "</script>",
            f'<script>window.NODE_ID = "{i}";</script>',
        ])
        body = [f'<article class="static"><h1>[{i}] {esc(n["name"])}</h1>',
                f'<p class="muted">{esc(year_text(n))}・{esc(n.get("trunk_name", ""))}{("・" + esc(n["name_en"])) if n.get("name_en") else ""}</p>']
        hv = [(k, lv) for k, lv in (("1", "①子ども"), ("2", "②学生"), ("3", "③大人"), ("4", "④ガチ")) if heads[k]]
        if hv:
            body.append("<section><h2>へえ</h2>" + "".join(f"<p><b>{lv}</b> {esc(heads[k])}</p>" for k, lv in hv) + "</section>")
        if real:
            body.append("<section><h2>本当の話</h2>" + "".join(f"<p>{esc(p)}</p>" for p in real.split("\n") if p.strip()) + "</section>")
        if reqs:
            body.append("<section><h2>前提（これが無いと作れない）</h2><p>" + "・".join(link(p) for p in reqs) + "</p></section>")
        if uses:
            body.append("<section><h2>これを使って生まれた物</h2><p>" + "・".join(link(k) for k in uses[:60]) + (f" ほか {len(uses) - 60}" if len(uses) > 60 else "") + "</p></section>")
        if d.get("people"):
            body.append("<section><h2>人物</h2><p>" + "・".join(f'{esc(p.get("name"))}（{esc(p.get("year"))}）' for p in d["people"]) + "</p></section>")
        srcs = list(dict.fromkeys((d.get("sources") or []) + [r.get("url") for r in d.get("refs", []) if r.get("url")]))
        if srcs:
            body.append("<section><h2>出典</h2><ul>" + "".join(f'<li><a href="{esc(u)}" rel="noopener">{esc(u)}</a></li>' for u in srcs if str(u).startswith("http")) + "</ul></section>")
        body.append('<p class="muted">技術ネットワーク（CC0）— <a href="network.html">図</a>・<a href="ranking.html">ランキング</a></p></article>')
        page = tpl.replace('<meta name="viewport" content="width=device-width, initial-scale=1">\n',
                           '<meta name="viewport" content="width=device-width, initial-scale=1">\n' + head + "\n", 1)
        page = page.replace('<main id="page"><p class="muted">読み込み中…</p></main>', '<main id="page">' + "\n".join(body) + "</main>", 1)
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

"""入口・章の一覧・道案内・検索の索引を書き出す（S-061、設計図 docs/mockups/S060_spec.md）。

  index.html            … 入口：大きな「さがす」→ 道案内（はじめての1時間・AI チップまで）→ 章から（分野 16＋モノ・発見）→ 地図で見る
  chapters/<分野>.html  … 章の一覧：作れる順（前提が先。全部のつながりで並べ、同じ順なら年の順）
  guides/<名前>.html    … 道案内：その物までに要る物を作れる順に（ゲームにある物に印）
  search.json           … さがすの索引（物 644 と、ゲームのアイテム 860〈その物のページの目印へ〉）
本文は HTML に最初から書く。さがすは index.html?q=… で動く（物のページの上の さがす からも）。
使い方：python tools/gen_home.py [出力の docs フォルダ]（省略すると docs/）。tools/export_tech_network.py からも呼ぶ
"""
import html
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SITE = "https://taku3i0317-collab.github.io/tech-network/"
VERIFY = '<meta name="google-site-verification" content="lP8qobgUCL3mnbf0cKd4l2dH_m3H-Vr99uwRI9Eea1A" />'   # S-052・S-053
esc = lambda s: html.escape(str(s if s is not None else ""), quote=True)
CHAP = [("0", "生きる道具", "石器・火・土器・木炭"), ("1", "金属", "銅・鉄・鋼・いろいろな金属"), ("2", "化学", "ソーダ・酸・ガラス・プラスチック"), ("3", "熱", "炉・耐火物"),
        ("4", "電気と動力", "電池・発電機・モーター"), ("5", "真空", "ポンプ・真空管"), ("6", "ガス", "水素・酸素・塩素"), ("7", "光", "レンズ・光学ガラス・レーザー"),
        ("8", "精密と計測", "ねじ・旋盤・時計・はかり"), ("9", "半導体", "ゲルマニウム・シリコン・IC"), ("10", "制御と計算", "リレー・計算機・AI"), ("11", "農業と食", "畑・肥料・食べ物を保つ"),
        ("12", "建築と土木", "コンクリート・水道・橋"), ("13", "医薬と衛生", "消毒・ワクチン・薬"), ("14", "輸送", "車輪・船・鉄道・自動車"), ("15", "通信と情報", "紙・印刷・電信・ラジオ・ネット")]
EXTRA = [("P", "モノ（作品）", "スマホ・電卓・腕時計…（いくつもの技術を組み合わせた物）"), ("B", "枝", "太陽電池・液晶・LED…"), ("I", "発見と考え方", "周期表・電磁誘導・バンド理論…")]


def js_obj(p):
    s = p.read_text(encoding="utf-8")
    return json.loads(s[s.index("{"):s.rindex("}") + 1])


_SRC = ROOT / "docs" / "i18n" / "source.json"
UIK = [(k[3:], v["en"]) for k, v in (json.loads(_SRC.read_text(encoding="utf-8"))["strings"].items() if _SRC.exists() else []) if k.startswith("ui.") and v.get("en")]


def shell(title, desc, path, body, depth=0, extra_head=""):
    for _k, _en in UIK:   # 画面の文にキー（S-064）
        body = body.replace(f'data-en="{_en}"', f'data-k="ui.{_k}" data-en="{_en}"')
    up = "../" * depth
    canon = SITE + path
    return f"""<!doctype html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
{extra_head}<title>{esc(title)}</title>
<meta name="description" content="{esc(desc)}">
<link rel="canonical" href="{canon}"><link rel="alternate" hreflang="ja" href="{canon}"><link rel="alternate" hreflang="en" href="{canon}?lang=en">
<meta property="og:title" content="{esc(title)}"><meta property="og:description" content="{esc(desc)}"><meta property="og:url" content="{canon}"><meta property="og:image" content="{SITE}og_image.png">
<meta name="theme-color" content="#faf7f0"><link rel="manifest" href="{up}manifest.webmanifest"><link rel="icon" type="image/png" href="{up}app_icon_192.png">
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=BIZ+UDPGothic:wght@400;700&family=DotGothic16&display=swap">
<link rel="stylesheet" href="{up}thing.css">
</head>
<body>
<div class="top"><a class="home" href="{up}index.html" data-en="Tech map">技術の地図</a><form action="{up}index.html"><input type="search" name="q" placeholder="さがす（物・技術・人の名前）" data-en-ph="Search (things, techniques, people)" aria-label="さがす"></form></div>
<div class="wrap">
{body}
<div class="quiet"><button type="button" id="langBtn" lang="en">English</button><button type="button" id="furiBtn" aria-pressed="false">ふりがな</button><a href="{up}network.html" data-en="Year map">年の図</a><a href="{up}ranking.html" data-en="Ranking">ランキング</a><a href="{up}people.html" data-en="People">人物</a><a href="{up}glossary.html" data-en="Glossary">用語集</a><a href="{up}credits.html" data-en="Credits">影響を受けた作品</a></div>
</div>
<script src="{up}thing.js"></script>
<script src="{up}furigana.js"></script><script>Furigana.init(document.getElementById('furiBtn'));</script>
<script src="{up}pwa.js" defer></script>
</body>
</html>
"""


def main():
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "docs"
    G = js_obj(ROOT / "docs" / "tech_network_graph.js")
    D = js_obj(ROOT / "docs" / "tech_network_detail.js")["nodes"]
    W = json.loads((ROOT / "docs" / "wiki_export.json").read_text(encoding="utf-8"))
    byId = {n["id"]: n for n in G["nodes"]}
    pars = {}
    for f, t, k in G["edges"]:
        if k in ("r", "p", "e") and f in byId and t in byId:
            pars.setdefault(t, set()).add(f)
    # 作れる順：前提を全部先に（深さ＝前提をたどるいちばん長い道）。同じ深さは年の順
    depth = {}
    def dep(u, stack=()):
        if u in depth:
            return depth[u]
        if u in stack:
            return 0
        v = 1 + max((dep(p, stack + (u,)) for p in pars.get(u, ())), default=-1)
        depth[u] = v
        return v
    import sys as _s
    _s.setrecursionlimit(10000)
    for k in byId:
        dep(k)
    order = sorted(byId, key=lambda k: (depth[k], byId[k].get("year") or 0, k))
    year = lambda n: "" if n.get("year") is None else (f"紀元前{-n['year']}年" if n["year"] < 0 else f"{n['year']}年")
    def row(k, depth_):
        n = byId[k]
        up = "../" * depth_
        ic = f'<img src="{n["icon"]}" alt="">' if n.get("icon") else '<span style="width:32px"></span>'
        game = ' <span class="g" data-en="in the game">ゲームにある</span>' if n.get("status") == "✅" else ""
        return f'<li>{ic}<a href="{up}nodes/{k}.html" data-k="{k}.name" data-en="{esc(n.get("name_en") or n["name"])}">{esc(n["name"])}</a>{game}<span class="y">{esc(year(n))}</span></li>'
    (out / "chapters").mkdir(parents=True, exist_ok=True)
    (out / "guides").mkdir(parents=True, exist_ok=True)
    cards = []
    for code, name, sub in CHAP + EXTRA:
        ks = [k for k in order if byId[k]["trunk"] == code]
        if not ks:
            continue
        body = f'<h1>{esc(name)}</h1><p class="meta">{esc(sub)}。{len(ks)} の物を、作れる順（前提が先）に。押すとその物のページへ。</p><ol class="order">{"".join(row(k, 1) for k in ks)}</ol>'
        (out / "chapters" / f"{code}.html").write_text(shell(f"{name}｜技術の地図", f"{name}（{sub}）の {len(ks)} の物を、作れる順に。", f"chapters/{code}.html", body, depth=1), encoding="utf-8")
        cards.append((code, name, sub, len(ks)))
    # 道案内：その物までに要る物（前提をたどった全部）を作れる順に
    def need(goal):
        goals = [goal] if isinstance(goal, str) else list(goal)
        s, st = set(goals), list(goals)
        while st:
            u = st.pop()
            for p in pars.get(u, ()):
                if p not in s:
                    s.add(p)
                    st.append(p)
        return [k for k in order if k in s]
    GUIDES = [("first-hour", "はじめての1時間", "石器・火・土器・木炭・塩から、はじめての金属（銅）まで", [k for k in byId if byId[k]["trunk"] == "0"] + ["1-03"]),
              ("ai-chip", "AI チップまで", "石器から、自分で作ったチップの上の AI まで。作る順に全部", "B-11")]
    for slug, name, sub, goal in GUIDES:
        if isinstance(goal, str) and goal not in byId:
            continue
        ks = need(goal)
        body = f'<h1>{esc(name)}</h1><p class="meta">{esc(sub)}。{len(ks)} の物を、作れる順（前提が先）に。「ゲームにある」の印は、ゲームで作れる物。</p><ol class="order">{"".join(row(k, 1) for k in ks)}</ol>'
        (out / "guides" / f"{slug}.html").write_text(shell(f"{name}｜技術の地図", f"{sub}（{len(ks)} の物）", f"guides/{slug}.html", body, depth=1), encoding="utf-8")
    # さがすの索引：物（名前・英語の名前・人物）とゲームのアイテム（その物のページの目印へ）
    idx = []
    for k, n in byId.items():
        people = " ".join(p.get("name", "") for p in D.get(k, {}).get("people", []))
        idx.append({"u": f"nodes/{k}.html", "ja": n["name"], "en": n.get("name_en") or "", "p": people, "t": n.get("trunk_name", ""), "k": "物"})
    for it in W["items"]:
        nd = it.get("node")
        if nd in byId:
            s = it["id"].split(":", 1)[1]
            idx.append({"u": f"nodes/{nd}.html#it-{s}", "ja": it["name"].get("ja") or "", "en": it["name"].get("en") or "", "p": "", "t": byId[nd]["name"], "k": "アイテム", "i": f"wiki/icons/{s}.png"})
    (out / "search.json").write_text(json.dumps(idx, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    # 入口
    chap_cards = "".join(f'<a class="card" href="chapters/{c}.html"><b>{esc(nm)}</b><span>{esc(sub)}（{n}）</span></a>' for c, nm, sub, n in cards if c in [x[0] for x in CHAP])
    extra_cards = "".join(f'<a class="card" href="chapters/{c}.html"><b>{esc(nm)}</b><span>{esc(sub)}（{n}）</span></a>' for c, nm, sub, n in cards if c in [x[0] for x in EXTRA])
    nT = len(byId)
    body = f"""<h1 data-en="Tech map">技術の地図</h1>
<p class="meta" data-en="From stone tools to AI chips: {nT} things and what each one needs. Search by name, or follow a guide.">石器から AI チップまで、{nT} の物と、それを作るのに要る物。名前でさがすか、道案内でたどる。</p>
<form action="index.html"><input class="search" type="search" name="q" id="q" placeholder="さがす：スマホ・鉄・トランジスタ・ファラデー…" data-en-ph="Search: smartphone, iron, transistor, Faraday…" aria-label="さがす" autocomplete="off"></form>
<ul class="res" id="res"></ul>
<h2 data-en="Guides">道案内</h2><div class="cards"><a class="card" href="guides/first-hour.html"><b>はじめての1時間</b><span>石器・火・土器から、はじめての金属まで</span></a><a class="card" href="guides/ai-chip.html"><b>AI チップまで</b><span>作る順に、ぜんぶたどる</span></a></div>
<h2 data-en="Chapters">章から</h2><div class="cards">{chap_cards}</div>
<h2 data-en="More">ほかに</h2><div class="cards">{extra_cards}</div>
<h2 data-en="See the map">地図で見る</h2><p><a class="btn" href="network.html" data-en="Year map (all things by year)">年の図（全部を年の順に）</a> <a class="btn" href="ranking.html" data-en="Unsung heroes ranking">縁の下の力持ちランキング</a></p>
<script>
(async function () {{
  const q = document.getElementById('q'), res = document.getElementById('res');
  let L = null;
  const esc = s => String(s).replace(/[&<>"]/g, c => ({{ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }}[c]));
  const run = async () => {{
    const t = q.value.trim().toLowerCase(); if (!t) {{ res.innerHTML = ''; return; }}
    if (!L) L = await (await fetch('search.json')).json();
    const hit = L.filter(x => x.ja.toLowerCase().includes(t) || x.en.toLowerCase().includes(t) || x.p.toLowerCase().includes(t));
    hit.sort((a, b) => (a.k === '物' ? 0 : 1) - (b.k === '物' ? 0 : 1) || a.ja.length - b.ja.length);
    res.innerHTML = hit.slice(0, 40).map(x => `<li><a href="${{x.u}}">${{x.i ? `<img src="${{x.i}}" alt="" onerror="this.remove()">` : ''}}<span><b>${{esc(x.ja)}}</b> <span class="meta">${{esc(x.en)}}${{x.k === 'アイテム' ? '・' + esc(x.t) + ' のアイテム' : '・' + esc(x.t)}}</span></span></a></li>`).join('') || '<li class="meta">見つからない</li>';
  }};
  q.addEventListener('input', run);
  const p = new URLSearchParams(location.search).get('q'); if (p) {{ q.value = p; run(); }}
}})();
</script>"""
    (out / "index.html").write_text(shell("技術の地図 — 石器から AI チップまで", f"石器から AI チップまで、{nT} の物と、それを作るのに要る物の地図（CC0）。A CC0 map of technologies: what cannot be made without what.", "index.html", body, depth=0, extra_head=VERIFY + "\n"), encoding="utf-8")
    print(f"home: chapters {len(cards)}, guides {len(GUIDES)}, search entries {len(idx)}")


if __name__ == "__main__":
    main()

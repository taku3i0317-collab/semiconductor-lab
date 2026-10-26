"""S-060 の見本（設計図の絵）を作る。本番は変えない。docs/mockups/S060/ に、本物のデータから4枚：
  index.html（入口：探す・道案内・章から）、list.html（章の一覧：作れる順）、
  thing-0-03.html（土器）・thing-9-08.html（CZ 法）（1つの物に1つのページ：これは何か → 手に入れ方 → 作り方 → 本当の歴史 → つながり）
使い方：python tools/mock_s060.py
"""
import html
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
OUT = DOCS / "mockups" / "S060"
esc = lambda s: html.escape(str(s if s is not None else ""), quote=True)
js = lambda p: (lambda s: json.loads(s[s.index("{"):s.rindex("}") + 1]))((DOCS / p).read_text(encoding="utf-8"))
G, D, W = js("tech_network_graph.js"), js("tech_network_detail.js")["nodes"], json.loads((DOCS / "wiki_export.json").read_text(encoding="utf-8"))
NODE = {n["id"]: n for n in G["nodes"]}
ITEMS = {i["id"]: i for i in W["items"]}
MACH = {m["id"]: m for m in W["machines"]}
# バニラの材料の日本語の名前（自分たちで書いた、ふつうのことば。ゲームの言葉のファイルは写さない）
VAN = {"iron_ingot": "鉄インゴット", "water_bucket": "水入りバケツ", "copper_ingot": "銅インゴット", "sand": "砂", "charcoal": "木炭", "clay_ball": "粘土玉",
       "stick": "棒", "coal": "石炭", "calcite": "方解石", "sugar": "砂糖", "furnace": "かまど", "flint": "火打ち石", "gravel": "砂利", "stone": "石",
       "bone_meal": "骨粉", "diamond": "ダイヤモンド", "paper": "紙", "bucket": "バケツ", "beetroot": "ビートルート", "leather": "革", "gold_ingot": "金インゴット",
       "iron_nugget": "鉄の粒", "brick": "れんが", "sugar_cane": "サトウキビ", "gold_nugget": "金の粒", "bricks": "れんがブロック", "ice": "氷", "wheat": "小麦",
       "honeycomb": "ハチの巣のかけら", "tuff": "凝灰岩", "raw_iron": "鉄の原石", "bone": "骨", "cinnabar": "辰砂", "glass_pane": "板ガラス",
       "gray_concrete_powder": "灰色のコンクリートの粉", "dried_kelp": "乾いた昆布", "copper_nugget": "銅の粒", "piston": "ピストン", "smooth_stone": "なめらかな石",
       "gray_concrete": "灰色のコンクリート", "white_wool": "白い羊毛", "cauldron": "大釜", "sulfur": "硫黄", "potent_sulfur": "強い硫黄", "sulfur_spike": "硫黄の柱",
       "apple": "リンゴ", "sweet_berries": "スイートベリー", "lever": "レバー", "oak_log": "オークの原木"}
KIND = {"minecraft:crafting_shaped": "作業台", "minecraft:crafting_shapeless": "作業台", "minecraft:crafting_transmute": "作業台", "minecraft:smelting": "かまど",
        "minecraft:blasting": "溶鉱炉", "minecraft:campfire_cooking": "たき火", "minecraft:smoking": "燻製器"}


def name(i):
    if i in ITEMS:
        return ITEMS[i]["name"].get("ja") or ITEMS[i]["name"]["en"]
    if i in MACH:
        return MACH[i]["name"].get("ja")
    return VAN.get(i.split(":")[-1], i.split(":")[-1])


def icon(i, size=40):
    s = i.split(":")[-1]
    if i.startswith("semiconductor-lab:") and (DOCS / "wiki" / "icons" / f"{s}.png").exists():
        return f'<img src="../../wiki/icons/{s}.png" width="{size}" height="{size}" alt="">'
    return f'<span class="vi" style="width:{size}px;height:{size}px">{esc(name(i)[:1])}</span>'   # バニラ：絵は使わない（字の頭文字の四角）


def cell(x):
    i = x.get("item") or ""
    n = x.get("count", 1)
    return f'<div class="cell">{icon(i)}<b>{esc(name(i))}</b>{f"<i>×{n}</i>" if n and n > 1 else ""}</div>'


def recipe(r):
    ins = "".join(cell(x) for x in r.get("inputs", []))
    if "machine" in r:
        how, outs = name(r["machine"]), "".join(cell(x) for x in r.get("outputs", []))
        sub = "・".join(x for x in [f'{r["watts"]:,.0f} W' if r.get("watts") else "", f'{r["seconds"]:g} 秒' if r.get("seconds") else ""] if x)
    else:
        how, outs = KIND.get(r.get("type"), "作業台"), cell(r["output"]) if r.get("output") else ""
        sub = f'{r["seconds"]:g} 秒' if r.get("seconds") else ""
    return f'<div class="rec"><div class="ins">{ins}</div><div class="how"><span>{esc(how)}</span><small>{esc(sub)}</small><em>→</em></div><div class="outs">{outs}</div></div>'


def made_by(i):
    rs = [r for r in W["recipes"] if (r.get("output") or {}).get("item") == i]
    rs += [r for r in W["machine_recipes"] if any(x.get("item") == i for x in r.get("outputs", []))]
    return rs


CSS = """
:root { --bg:#faf7f0; --panel:#fffdf8; --ink:#2b2622; --sub:#6b6259; --line:#e4dccd; --accent:#2f6f8f; --warm:#e8a33d; }
* { box-sizing: border-box; } body { margin:0; background:var(--bg); color:var(--ink); font:16px/1.75 "BIZ UDPGothic", sans-serif; }
.top { position:sticky; top:0; z-index:5; display:flex; gap:8px; align-items:center; padding:10px 16px; background:var(--bg); border-bottom:1px solid var(--line); }
.top a.home { font-family:"DotGothic16",sans-serif; font-size:18px; color:var(--ink); text-decoration:none; white-space:nowrap; }
.top input { flex:1; min-width:0; font:inherit; padding:8px 12px; border:1px solid var(--line); border-radius:10px; background:var(--panel); }
.wrap { max-width:760px; margin:0 auto; padding:16px 16px 64px; }
h1 { font-family:"DotGothic16",sans-serif; font-weight:400; font-size:clamp(26px,7vw,36px); margin:0; line-height:1.3; }
h2 { font-size:15px; letter-spacing:.08em; color:var(--sub); margin:28px 0 8px; border-top:1px solid var(--line); padding-top:14px; }
.what { display:flex; gap:16px; align-items:center; }
.what .big { width:88px; height:88px; flex:none; display:flex; align-items:center; justify-content:center; background:var(--panel); border:1px solid var(--line); border-radius:16px; }
.what .big img { width:64px; height:64px; image-rendering:pixelated; }
.one { font-size:19px; margin:10px 0 2px; }
.meta { color:var(--sub); font-size:14px; }
.chip { display:inline-block; font-size:13px; border:1px solid var(--line); border-radius:999px; padding:0 10px; margin:2px 4px 2px 0; background:var(--panel); }
.acts { display:flex; gap:8px; margin:14px 0 0; }
.btn { font:inherit; font-weight:700; padding:8px 14px; border:1px solid var(--accent); color:var(--accent); background:var(--panel); border-radius:10px; text-decoration:none; }
.btn.main { background:var(--accent); color:#fff; }
.get { display:grid; gap:8px; }
.get div { background:var(--panel); border:1px solid var(--line); border-radius:12px; padding:10px 12px; }
.rec { display:grid; grid-template-columns: 1fr 92px 1fr; align-items:center; gap:8px; background:var(--panel); border:1px solid var(--line); border-radius:12px; padding:10px; margin:8px 0; }
.ins, .outs { display:flex; flex-wrap:wrap; gap:6px; } .outs { justify-content:flex-start; }
.cell { width:84px; display:flex; flex-direction:column; align-items:center; text-align:center; font-size:12px; line-height:1.3; }
.cell img, .cell .vi { image-rendering:pixelated; } .cell b { font-weight:400; margin-top:2px; } .cell i { font-style:normal; color:var(--accent); font-weight:700; }
.vi { display:inline-flex; align-items:center; justify-content:center; border:1px dashed var(--sub); border-radius:8px; color:var(--sub); font-size:18px; }
.how { display:flex; flex-direction:column; align-items:center; text-align:center; } .how span { font-weight:700; font-size:14px; } .how small { color:var(--sub); font-size:12px; } .how em { font-style:normal; font-size:22px; color:var(--warm); line-height:1; }
.more { color:var(--sub); font-size:15px; } details summary { cursor:pointer; color:var(--accent); font-weight:700; }
.links { display:grid; grid-template-columns: 1fr 1fr; gap:12px; } .links h3 { font-size:14px; margin:0 0 4px; color:var(--sub); }
.th { display:flex; flex-wrap:wrap; gap:6px; } .th a { display:inline-flex; align-items:center; gap:6px; padding:2px 10px 2px 2px; border:1px solid var(--line); border-radius:10px; background:var(--panel); color:var(--ink); text-decoration:none; font-size:14px; }
.th img { width:28px; height:28px; image-rendering:pixelated; }
.quiet { color:var(--sub); font-size:13px; margin-top:32px; } .quiet a { color:var(--sub); }
.cards { display:grid; grid-template-columns: repeat(auto-fill, minmax(220px,1fr)); gap:12px; }
.card { display:block; background:var(--panel); border:1px solid var(--line); border-radius:14px; padding:14px; color:var(--ink); text-decoration:none; }
.card b { font-size:18px; display:block; } .card span { color:var(--sub); font-size:14px; }
.search { font:inherit; font-size:18px; width:100%; padding:14px 16px; border:2px solid var(--accent); border-radius:14px; background:var(--panel); }
ol.order { list-style:none; padding:0; margin:0; counter-reset:o; } ol.order li { counter-increment:o; display:flex; align-items:center; gap:10px; padding:8px 0; border-bottom:1px solid var(--line); }
ol.order li::before { content:counter(o); width:28px; text-align:right; color:var(--sub); font-size:13px; } ol.order img { width:32px; height:32px; image-rendering:pixelated; }
ol.order .y { margin-left:auto; color:var(--sub); font-size:13px; } ol.order a { color:var(--ink); text-decoration:none; font-weight:700; }
@media (min-width: 900px) { .wrap { max-width: 1040px; } .cols { display:grid; grid-template-columns: 1fr 320px; gap:32px; } .side { position:sticky; top:72px; align-self:start; } }
@media (max-width: 480px) { .rec { grid-template-columns: 1fr; } .how { flex-direction:row; gap:8px; justify-content:center; } .how em { transform: rotate(90deg); } .links { grid-template-columns: 1fr; } }
"""
FONT = '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=BIZ+UDPGothic:wght@400;700&family=DotGothic16&display=swap">'


def shell(title, body):
    return f"""<!doctype html><html lang="ja"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>{esc(title)}</title>{FONT}<style>{CSS}</style></head>
<body><div class="top"><a class="home" href="index.html">技術の地図</a><input type="search" placeholder="さがす（物・技術・人の名前）" aria-label="さがす"></div>
<div class="wrap">{body}</div></body></html>"""


def year(n):
    y = n.get("year")
    return "" if y is None else (f"紀元前{-y}年" if y < 0 else f"{y}年")


def thing(nid):
    n, d = NODE[nid], D.get(nid, {})
    items = [i for i, it in ITEMS.items() if it.get("node") == nid]
    hee1 = re.sub(r"（[^（）]*出典[:：][^）]*）", "", d.get("hee1") or "")
    real = re.sub(r"（[^（）]*出典[:：][^）]*）", "", d.get("real_full") or "").split("\n")[0][:220]
    par = [f for f, t, k in G["edges"] if t == nid and k in "rep" and f in NODE][:8]
    kid = [t for f, t, k in G["edges"] if f == nid and k in "rep" and t in NODE][:8]
    ic = n.get("icon")
    get = []
    for i in items:
        rs = made_by(i)
        if not rs:
            get.append(f'<div>{icon(i, 28)} <b>{esc(name(i))}</b>：レシピは無い（拾う・掘る・ほかの方法）</div>')
    recs = "".join(f'<p class="meta">{esc(name(i))}</p>' + "".join(recipe(r) for r in made_by(i)[:2]) for i in items if made_by(i))
    th = lambda ids: "".join(f'<a href="#">{f"<img src={chr(34)}{NODE[x]["icon"]}{chr(34)} alt={chr(34)}{chr(34)}>" if NODE[x].get("icon") else ""}{esc(NODE[x]["name"])}</a>' for x in ids)
    body = f"""<div class="cols"><div>
<section class="what"><div class="big">{f'<img src="{ic}" alt="">' if ic else ''}</div><div><h1>{esc(n["name"])}</h1><div class="meta">{esc(year(n))}・{esc(n.get("trunk_name", ""))}</div></div></section>
<p class="one">{esc(hee1)}</p>
<div class="acts"><a class="btn main" href="#">▶ できるまで</a><a class="btn" href="#">共有</a></div>
<h2>どうやって手に入れるか</h2><div class="get">{"".join(get) or f'<div>ゲームでは、下の作り方で作る（{len(items)} 種類のアイテム）</div>' if items else '<div>ゲームにはまだ無い（本当の歴史だけ）</div>'}</div>
{f'<h2>作り方</h2>{recs}' if recs else ''}
<h2>本当の歴史</h2><p>{esc(real)}</p><details><summary>もっと読む（学生・大人・ガチ・人物・出典）</summary><p class="more">へえ②〜④、人物、出典はここにたたむ。</p></details>
</div><div class="side">
<h2>つながり</h2><div class="links"><div><h3>これを作るのに要る</h3><div class="th">{th(par) or '<span class="meta">（始まり）</span>'}</div></div><div><h3>これで作れるようになった</h3><div class="th">{th(kid) or '<span class="meta">—</span>'}</div></div></div>
<p><a class="btn" href="#">中心図で見る</a></p>
<p class="quiet">誤りを知らせる・English・ふりがな</p>
</div></div>"""
    return shell(f'{n["name"]}｜技術の地図（見本）', body)


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for nid in ("0-03", "9-08"):
        (OUT / f"thing-{nid}.html").write_text(thing(nid), encoding="utf-8")
    # 入口
    first = [x for x in ("0-01", "0-02", "0-03", "0-12", "0-15", "1-03") if x in NODE]
    body = f"""<h1>技術の地図</h1><p class="meta">石器から AI チップまで。物の名前でさがす、道案内で順にたどる。</p>
<input class="search" type="search" placeholder="さがす：スマホ・鉄・トランジスタ…" aria-label="さがす">
<h2>道案内</h2><div class="cards"><a class="card" href="list.html"><b>はじめての1時間</b><span>石器・火・土器から、はじめての金属まで</span></a><a class="card" href="list.html"><b>AI チップまで</b><span>作る順に、ぜんぶたどる</span></a></div>
<h2>章から</h2><div class="cards"><a class="card" href="list.html"><b>生きる道具</b><span>石器・火・土器・木炭</span></a><a class="card" href="list.html"><b>金属</b><span>銅・鉄・鋼</span></a><a class="card" href="list.html"><b>化学</b><span>ソーダ・硫酸・塩素</span></a><a class="card" href="list.html"><b>電気</b><span>電池・発電機・真空管</span></a><a class="card" href="list.html"><b>半導体</b><span>ゲルマニウム・シリコン・IC</span></a></div>
<h2>地図で見る</h2><p><a class="btn" href="#">技術の地図（年の順）</a> <a class="btn" href="#">ランキング</a></p>"""
    (OUT / "index.html").write_text(shell("技術の地図（見本）", body), encoding="utf-8")
    # 章の一覧（作れる順）
    lis = "".join(f'<li>{f"<img src={chr(34)}{NODE[x]["icon"]}{chr(34)} alt={chr(34)}{chr(34)}>" if NODE[x].get("icon") else ""}<a href="thing-0-03.html">{esc(NODE[x]["name"])}</a><span class="y">{esc(year(NODE[x]))}</span></li>' for x in sorted((k for k in NODE if k.startswith("0-")), key=lambda k: NODE[k].get("year") or 0))
    (OUT / "list.html").write_text(shell("生きる道具｜技術の地図（見本）", f'<h1>生きる道具</h1><p class="meta">作れる順（前提が先）。押すとその物のページへ。</p><ol class="order">{lis}</ol>'), encoding="utf-8")
    print("wrote", OUT)


if __name__ == "__main__":
    main()

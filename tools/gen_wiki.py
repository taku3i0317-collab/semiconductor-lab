"""MOD のウィキを書き出す（S-056・S-057）。ネットワーク図と一体（同じ見た目・同じノードのページへのリンク）。

元：A の docs/wiki_export.json（tools/wiki_export.py、S-022）と、MOD の自作のテクスチャ（src/main/resources/assets/semiconductor-lab/textures）。
書き出す物（docs/wiki/）：
  index.html            … 検索（アイテム・機械・道案内の名前で）
  items/<名前>.html     … アイテム・ブロック 1つずつ：絵・名前・説明・作り方（レシピの図）・使い道・本当の歴史（ノードのページ）へ
  machines/<名前>.html  … 機械 1つずつ：機械の画面（gui_preview）・ブロックの絵・工程（入る物 → 機械 → 出る物・電気・時間）・「3D で見る」
  guides/<名前>.html    … 道案内（はじめての1時間・ラジオ・電卓・AI チップまで）：データの前提の道から、作る順に
  icons/・tex/          … MOD の自作の絵だけ（Mojang の絵は使わない。バニラの材料は名前の字だけ）
  models.json           … 3D で見る時のブロックのモデル（S-058、wiki/viewer.js が読む）
決まり：お題の答え・ヒントは出さない（wiki_export.json にも入っていない）。書き換えは Issue・GitHub の提案で（だれでも書けるウィキにはしない）
使い方：python tools/gen_wiki.py
"""
import html
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
OUT = DOCS / "wiki"
TEX = ROOT / "src" / "main" / "resources" / "assets" / "semiconductor-lab" / "textures"
SITE = "https://taku3i0317-collab.github.io/tech-network/"
NS = "semiconductor-lab:"
esc = lambda s: html.escape(str(s if s is not None else ""), quote=True)
yr = lambda y: "" if y is None else (f"紀元前{-y}年" if y < 0 else f"{y}年")
slug = lambda i: i.split(":", 1)[1] if i.startswith(NS) else None

W = json.loads((DOCS / "wiki_export.json").read_text(encoding="utf-8"))
G = (lambda s: json.loads(s[s.index("{"):s.rindex("}") + 1]))((DOCS / "tech_network_graph.js").read_text(encoding="utf-8"))
NODE = {n["id"]: n for n in G["nodes"]}
ITEMS = {i["id"]: i for i in W["items"]}
MACH = {m["id"]: m for m in W["machines"]}
GUI = set(W.get("gui_preview", []))


def name(i, lang="ja"):
    if i in ITEMS:
        return ITEMS[i]["name"].get(lang) or ITEMS[i]["name"].get("en")
    if i in MACH:
        return MACH[i]["name"].get(lang) or MACH[i]["name"].get("en")
    s = i.split(":", 1)[-1].replace("_", " ")
    return s + ("（バニラ）" if i.startswith("minecraft:") and lang == "ja" else "")


def icon_src(i):
    """MOD の自作の絵だけ。アイテムの絵 → ブロックの絵（正面・横・全部の順）。無ければ None（バニラも None）"""
    s = slug(i)
    if not s:
        return None
    for p in (TEX / "item" / f"{s}.png", TEX / "block" / f"{s}.png", TEX / "block" / f"{s}_front.png", TEX / "block" / f"{s}_side.png", TEX / "block" / f"{s}_top.png"):
        if p.exists():
            return p
    return None


def page(title, desc, path, body, depth=2, noindex=False):
    up = "../" * depth
    canon = SITE + path
    return f"""<!doctype html>
<html lang="ja">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{esc(title)}</title>
<meta name="description" content="{esc(desc)}">
{'<meta name="robots" content="noindex">' if noindex else ''}
<link rel="canonical" href="{canon}">
<meta property="og:title" content="{esc(title)}"><meta property="og:description" content="{esc(desc)}"><meta property="og:url" content="{canon}"><meta property="og:image" content="{SITE}og_image.png">
<link rel="icon" type="image/png" href="{up}app_icon_192.png">
<link rel="stylesheet" href="{up}design.css">
<link rel="stylesheet" href="{up}wiki/wiki.css">
</head>
<body>
<div class="wrap">
<nav class="wnav"><a href="{up}wiki/index.html">ウィキ</a><a href="{up}network.html">技術ネットワーク図</a><a href="{up}wiki/guides/first-hour.html">はじめての1時間</a></nav>
{body}
<footer class="muted">MOD「Semiconductor Lab」のウィキ（CC0、データから自動で作った）。誤りは <a href="https://github.com/taku3i0317-collab/semiconductor-lab/issues/new/choose" rel="noopener">Issue</a> で。お題の答え・ヒントはのせていません。</footer>
</div>
</body>
</html>
"""


def chip(i, depth=2, n=None):
    up = "../" * depth
    src = icon_src(i)
    im = f'<img src="{up}wiki/icons/{slug(i)}.png" alt="">' if src else '<span class="noimg">◇</span>'
    cnt = f'<b class="n">×{n}</b>' if n and n > 1 else ""
    label = esc(name(i))
    if i in ITEMS:
        return f'<a class="it" href="{up}wiki/items/{slug(i)}.html" title="{label}">{im}<span>{label}</span>{cnt}</a>'
    return f'<span class="it vanilla" title="{label}">{im}<span>{label}</span>{cnt}</span>'


def ing(x, depth=2):
    if "item" in x:
        alts = "".join(f'<span class="or">または {esc(name(a))}</span>' for a in x.get("alt", []))
        return chip(x["item"], depth, x.get("count")) + alts
    if "tag" in x:
        return f'<span class="it vanilla"><span class="noimg">#</span><span>{esc(x["tag"].split(":")[-1].replace("_", " "))} の仲間</span></span>'
    return f'<span class="it vanilla"><span>{esc(json.dumps(x, ensure_ascii=False)[:40])}</span></span>'


KIND = {"minecraft:crafting_shaped": "作業台", "minecraft:crafting_shapeless": "作業台", "minecraft:crafting_transmute": "作業台", "minecraft:smelting": "かまど",
        "minecraft:blasting": "溶鉱炉", "minecraft:campfire_cooking": "たき火", "minecraft:smoking": "燻製器"}


def recipe_html(r, depth=2):
    ins = "".join(ing(x, depth) for x in r.get("inputs", []))
    if "machine" in r:
        m = r["machine"]
        how = f'<a class="mach" href="{"../" * depth}wiki/machines/{slug(m)}.html">{esc(name(m))}</a>' if m in MACH else f'<span class="mach">{esc(name(m))}</span>'
        meta = "・".join(x for x in [esc(r.get("process") or ""), f'{r["watts"]:,.0f} W' if r.get("watts") else "", f'{r["seconds"]:g} 秒' if r.get("seconds") else ""] if x)
        outs = "".join(ing(x, depth) for x in r.get("outputs", []))
        note = f'<div class="muted">{esc(r["note"])}</div>' if r.get("note") else ""
    else:
        how = f'<span class="mach">{KIND.get(r.get("type"), r.get("type", "").split(":")[-1])}</span>'
        meta = f'{r["seconds"]:g} 秒' if r.get("seconds") else ""
        outs = ing(r["output"], depth) if r.get("output") else ""
        note = ""
    return f'<div class="rec"><div class="ins">{ins}</div><div class="arrow">→ {how}{f"<small>{meta}</small>" if meta else ""} →</div><div class="outs">{outs}</div>{note}</div>'


def main():
    if OUT.exists():
        for sub in ("items", "machines", "guides", "icons", "tex", "gui", "multiblocks"):
            shutil.rmtree(OUT / sub, ignore_errors=True)
    for sub in ("items", "machines", "guides", "icons", "tex", "gui", "multiblocks"):
        (OUT / sub).mkdir(parents=True, exist_ok=True)
    # 絵：アイテムのアイコン（MOD の自作だけ）と、3D 用のブロックのテクスチャ
    for i in ITEMS:
        src = icon_src(i)
        if src:
            shutil.copy(src, OUT / "icons" / f"{slug(i)}.png")
    for m in MACH:
        src = icon_src(m)
        if src and not (OUT / "icons" / f"{slug(m)}.png").exists():
            shutil.copy(src, OUT / "icons" / f"{slug(m)}.png")
    for g in GUI:
        p = DOCS / "gui_preview" / g
        if p.exists() and g.endswith("_ja_jp.png"):
            shutil.copy(p, OUT / "gui" / g)
    models = {}
    for bid, v in (W.get("block_models") or {}).items():
        models[bid] = v
        for m in (v.get("models") or {}).values():
            for t in (m.get("textures") or {}).values():
                if isinstance(t, str) and t.startswith(NS + "block/"):
                    p = TEX / "block" / (t.split("/", 1)[1] + ".png")
                    if p.exists():
                        dst = OUT / "tex" / (t.split("/", 1)[1] + ".png")
                        dst.parent.mkdir(parents=True, exist_ok=True)
                        shutil.copy(p, dst)
    (OUT / "models.json").write_text(json.dumps({"_note": "S-058：3D で見る時のブロックのモデル（wiki_export.json の block_models と multiblocks）。テクスチャは MOD の自作だけ（tex/）。Mojang の絵は無いので、バニラのテクスチャの所は単色", "models": models, "multiblocks": W.get("multiblocks", [])}, ensure_ascii=False), encoding="utf-8")
    made, used = {}, {}
    for r in W["recipes"]:
        if r.get("output", {}).get("item"):
            made.setdefault(r["output"]["item"], []).append(r)
        for x in r.get("inputs", []):
            if x.get("item"):
                used.setdefault(x["item"], []).append(r)
    for r in W["machine_recipes"]:
        for x in r.get("outputs", []):
            if x.get("item"):
                made.setdefault(x["item"], []).append(r)
        for x in r.get("inputs", []):
            if x.get("item"):
                used.setdefault(x["item"], []).append(r)
    index = []
    # アイテムのページ
    for i, it in ITEMS.items():
        s = slug(i)
        if not s:
            continue
        nm, en = it["name"].get("ja") or it["name"]["en"], it["name"].get("en", "")
        notes = it.get("notes") or {}
        lv = [("note1", "①子ども"), ("note2", "②学生"), ("note3", "③大人"), ("note4", "④ガチ")]
        heeh = "".join(f'<p><b>{l}</b> {esc((notes.get(k) or {}).get("ja"))}</p>' for k, l in lv if (notes.get(k) or {}).get("ja"))
        nd = it.get("node")
        hist = f'<p class="hist">本当の歴史：<a href="../../nodes/{nd}.html">[{nd}] {esc(NODE[nd]["name"])}</a>（{yr(NODE[nd].get("year"))}）</p>' if nd in NODE else ""
        ph = '<p class="badge warn">仮（まだ本物どおりでない所がある）</p>' if it.get("placeholder") else ""
        mk = "".join(recipe_html(r) for r in made.get(i, [])) or '<p class="muted">（作り方のレシピは無い：拾う・掘る・ほかの方法）</p>'
        us = "".join(recipe_html(r) for r in used.get(i, [])[:40]) or '<p class="muted">（このアイテムを使うレシピは無い）</p>'
        src = icon_src(i)
        big = f'<img class="big" src="../icons/{s}.png" alt="">' if src else ""
        mach = f'<p><a class="btn" href="../machines/{s}.html">機械のページ（画面・3D）</a></p>' if i in MACH else ""
        MB = any(mb.get("controller_block") == i for mb in W.get("multiblocks", []))
        if it.get("kind") == "block" and i not in MACH:   # ブロック：斜め上から見た絵（S-057）と「3D で見る」（S-058）。マルチブロックは組み立て
            mach += f'<figure class="iso"><img src="../iso/{s}.png" alt="{esc(nm)}（斜め上から）" onerror="this.parentNode.remove()"><figcaption><button type="button" class="btn" data-3d="{i}">{"3D で組み立てを見る（1段ずつ）" if MB else "3D で見る"}</button></figcaption></figure><div id="v3d"></div><script src="../viewer.js"></script>' 
        body = f"""<header class="ihead">{big}<div><h1>{esc(nm)}</h1><p class="muted">{esc(en)}・{"ブロック" if it.get("kind") == "block" else "アイテム"}・{esc(it.get("trunk") or "")}</p>{ph}{hist}</div></header>
{mach}<section><h2>説明</h2>{heeh or '<p class="muted">（説明はまだ無い）</p>'}</section>
<section><h2>作り方</h2>{mk}</section>
<section><h2>使い道</h2>{us}</section>"""
        desc = ((notes.get("note1") or {}).get("ja") or f"{nm}（Semiconductor Lab）") [:140]
        (OUT / "items" / f"{s}.html").write_text(page(f"{nm}（{en}）の作り方と使い道｜Semiconductor Lab ウィキ", desc, f"wiki/items/{s}.html", body), encoding="utf-8")
        index.append({"k": "item", "id": s, "ja": nm, "en": en, "u": f"items/{s}.html", "i": bool(src)})
    # 機械のページ
    for m, mm in MACH.items():
        s = slug(m)
        nm, en = mm["name"].get("ja") or mm["name"]["en"], mm["name"].get("en", "")
        recs = [r for r in W["machine_recipes"] if r.get("machine") == m]
        gui = f'<figure><img class="gui" src="../gui/{s}_ja_jp.png" alt="{esc(nm)} の画面"><figcaption class="muted">機械の画面（MOD のデータから描いた絵）</figcaption></figure>' if f"{s}_ja_jp.png" in GUI else ""
        iso = f'<figure class="iso"><img src="../iso/{s}.png" alt="{esc(nm)}（斜め上から）" onerror="this.parentNode.remove()"><figcaption><button type="button" class="btn" data-3d="{m}">3D で見る</button></figcaption></figure>'
        it = ITEMS.get(m)
        nd = it.get("node") if it else None
        hist = f'<p class="hist">本当の歴史：<a href="../../nodes/{nd}.html">[{nd}] {esc(NODE[nd]["name"])}</a></p>' if nd in NODE else ""
        mbl = "".join(f'<p><a class="btn" href="../multiblocks/{slug(mb["controller_block"])}.html">組み立て（マルチブロック {mb["width"]}×{mb["depth"]}×{mb["height"]}）を 3D で見る</a></p>' for mb in W.get("multiblocks", []) if s.startswith(slug(mb["controller_block"]) or "~"))
        body = f"""<header class="ihead"><div><h1>{esc(nm)}</h1><p class="muted">{esc(en)}・機械</p>{hist}{mbl}<p><a href="../items/{s}.html">アイテムのページ（作り方）</a></p></div></header>
{iso}
<div id="v3d"></div>
<section><h2>工程（{len(recs)}）</h2>{"".join(recipe_html(r) for r in recs) or '<p class="muted">（工程のデータは無い）</p>'}</section>
{gui}
<script src="../viewer.js"></script>"""
        (OUT / "machines" / f"{s}.html").write_text(page(f"{nm}（{en}）｜機械｜Semiconductor Lab ウィキ", f"{nm}：{len(recs)} の工程。入る物・出る物・電気・時間。", f"wiki/machines/{s}.html", body), encoding="utf-8")
        index.append({"k": "machine", "id": s, "ja": nm, "en": en, "u": f"machines/{s}.html", "i": bool(icon_src(m))})
    # マルチブロック：組み立ての 3D（1段ずつ）。置くブロックの数はデータから
    for mb in W.get("multiblocks", []):
        cs = slug(mb["controller_block"])
        cnt = {}
        for layer in mb["layers"]:
            for row in layer:
                for ch in row:
                    if ch not in " .":
                        cnt[ch] = cnt.get(ch, 0) + 1
        lab = lambda ch: "制御のブロック" if ch == mb.get("controller") else ((mb["legend"].get(ch) or {}).get("name_text") or {}).get("ja", ch) + ("（バニラ。絵は単色の箱）" if str((mb["legend"].get(ch) or {}).get("block", "")).startswith("minecraft:") else "")
        rel = [m for m in MACH if slug(m).startswith(cs)]
        nm = (name(rel[0]) if rel else cs) + "（組み立て）"
        body = f"""<h1>{esc(nm)}</h1><p class="muted">マルチブロック {mb["width"]}×{mb["depth"]}×{mb["height"]}（横×奥×高さ）。{"・".join(f'<a href="../machines/{slug(m)}.html">{esc(name(m))}</a>' for m in rel)}</p>
<figure class="iso"><img src="../iso/{cs}.png" alt="{esc(nm)}（斜め上から）" onerror="this.parentNode.remove()"><figcaption><button type="button" class="btn" data-3d="{mb["controller_block"]}">3D で組み立てを見る（1段ずつ）</button></figcaption></figure><div id="v3d"></div>
<section><h2>置くブロック</h2><ul>{"".join(f"<li>{esc(lab(ch))} ×{k}</li>" for ch, k in cnt.items())}</ul></section>
<script src="../viewer.js"></script>"""
        (OUT / "multiblocks" / f"{cs}.html").write_text(page(f"{nm}｜Semiconductor Lab ウィキ", f"{nm}：{mb['width']}×{mb['depth']}×{mb['height']} のマルチブロックを1段ずつ 3D で。", f"wiki/multiblocks/{cs}.html", body), encoding="utf-8")
        index.append({"k": "machine", "id": cs, "ja": nm, "en": "multiblock", "u": f"multiblocks/{cs}.html", "i": False})
    # 道案内：データの前提の道から（作る順＝前提が先）。ゲームにある（✅）物だけ、各ステップに作るアイテム
    par = {}
    for f, t, k in G["edges"]:
        if k in ("r", "p"):
            par.setdefault(t, []).append(f)
    by_node = {}
    for i, it in ITEMS.items():
        if it.get("node"):
            by_node.setdefault(it["node"], []).append(i)
    # node.html の「ここにたどり着くには」と同じ：requires（全部そろう＝AND、1項目の中の案は OR）を、いちばん少なくてすむ案でさかのぼる
    D = (lambda t: json.loads(t[t.index("{"):t.rindex("}") + 1]))((DOCS / "tech_network_detail.js").read_text(encoding="utf-8"))["nodes"]
    memo = {}
    def cost(u, stack=()):
        if u in memo:
            return memo[u][0]
        if u in stack:
            return 0
        tot, pick = 1, []
        for alts in (D.get(u, {}).get("requires") or []):
            best, bc = None, 1e9
            for a in alts:
                c = sum(cost(i, stack + (u,)) for i in a if i in NODE)
                if c < bc:
                    bc, best = c, a
            if best:
                tot += bc; pick.append(best)
        memo[u] = (tot, pick)
        return tot
    def order(goal):
        cost(goal)
        seen, out = set(), []
        def visit(u):
            if u in seen or u not in NODE:
                return
            seen.add(u)
            for i in sorted({x for alts in (D.get(u, {}).get("requires") or []) for a in alts for x in a} | set(D.get(u, {}).get("parts") or []), key=lambda x: (NODE.get(x, {}).get("year") or 0)):
                visit(i)   # 前提を全部（どの案も）。作る順＝前提が先
            out.append(u)
        visit(goal)
        return [u for u in out if NODE[u].get("status") == "✅" and by_node.get(u)]
    GUIDES = [("first-hour", "はじめての1時間", "石器・火・土器から、生きるための道具まで", "0-01", 14),
              ("radio", "ラジオを作るまで", "第1章のゴール：トランジスタラジオまで", "15-10", None),
              ("calculator", "電卓まで", "集積回路の電卓まで", "B-08", None),
              ("ai-chip", "AI チップまで", "自分で作ったチップの上の AI まで", "B-11", None)]
    glinks = []
    for gslug, gt, gd, goal, cut in GUIDES:
        if goal not in NODE:
            continue
        if gslug == "first-hour":   # はじめての1時間：生存（幹0）のゲームにある物を、作る順に
            steps = []
            for g0 in sorted((k for k in NODE if k.startswith("0-")), key=lambda x: (NODE[x].get("year") or 0)):
                steps += [u for u in order(g0) if u not in steps]
        else:
            steps = order(goal)
        if cut:
            steps = steps[:cut]
        lis = "".join(f'<li><a href="../../nodes/{u}.html"><b>{esc(NODE[u]["name"])}</b></a> <span class="muted">{yr(NODE[u].get("year"))}</span><div class="its">{"".join(chip(i) for i in by_node[u][:6])}</div></li>' for u in steps)
        body = f'<h1>{esc(gt)}</h1><p>{esc(gd)}。前提が先に来る順（データの前提の道から自動）。{len(steps)} ステップ。押すとそのアイテムの作り方へ。</p><ol class="guide">{lis}</ol>'
        (OUT / "guides" / f"{gslug}.html").write_text(page(f"{gt}｜Semiconductor Lab ウィキ", f"{gd}。{len(steps)} ステップ。", f"wiki/guides/{gslug}.html", body), encoding="utf-8")
        index.append({"k": "guide", "id": gslug, "ja": gt, "en": "", "u": f"guides/{gslug}.html", "i": False})
        glinks.append(f'<li><a href="guides/{gslug}.html">{esc(gt)}</a> <span class="muted">{esc(gd)}（{len(steps)} ステップ）</span></li>')
    (OUT / "index.json").write_text(json.dumps(index, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    nI, nM = sum(1 for x in index if x["k"] == "item"), sum(1 for x in index if x["k"] == "machine")
    body = f"""<h1>Semiconductor Lab ウィキ</h1>
<p>MOD のアイテム {nI}・機械 {nM}・道案内 {len(glinks)}。データ（docs/wiki_export.json）から自動で作ったページです。本当の歴史は<a href="../network.html">技術ネットワーク</a>のノードのページへ。</p>
<input type="search" id="q" placeholder="アイテム・機械の名前でさがす" aria-label="さがす" autocomplete="off"><ul id="res" class="res"></ul>
<h2>道案内</h2><ul>{"".join(glinks)}</ul>
<h2>機械</h2><div class="grid">{"".join(chip(m, 1) .replace('../wiki/items/', 'machines/').replace('../wiki/icons/', 'icons/') for m in MACH)}</div>
<script>
(async function () {{
  const L = await (await fetch('index.json')).json(), q = document.getElementById('q'), res = document.getElementById('res');
  const esc = s => String(s).replace(/[&<>"]/g, c => ({{ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }}[c]));
  q.addEventListener('input', () => {{
    const t = q.value.trim().toLowerCase(); if (!t) {{ res.innerHTML = ''; return; }}
    res.innerHTML = L.filter(x => x.ja.toLowerCase().includes(t) || x.en.toLowerCase().includes(t)).slice(0, 40).map(x => `<li><a href="${{x.u}}">${{x.i ? `<img src="icons/${{x.id}}.png" alt="">` : ''}}${{esc(x.ja)}} <span class="muted">${{esc(x.en)}}・${{{{ item: 'アイテム', machine: '機械', guide: '道案内' }}[x.k]}}</span></a></li>`).join('') || '<li class="muted">見つからない</li>';
  }});
}})();
</script>"""
    (OUT / "index.html").write_text(page("Semiconductor Lab ウィキ（アイテム・機械・道案内）", f"MOD のアイテム {nI}・機械 {nM} の作り方と使い道、道案内。CC0。", "wiki/index.html", body, depth=1), encoding="utf-8")
    print(f"wiki: items {nI}, machines {nM}, guides {len(glinks)}, icons {len(list((OUT / 'icons').glob('*.png')))}, gui {len(list((OUT / 'gui').glob('*.png')))}")


if __name__ == "__main__":
    main()

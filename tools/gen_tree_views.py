#!/usr/bin/env python3
"""docs/TECH_TREE.md を読んで docs/TECH_TREE_VIEW.md を生成する。

使い方:  python tools/gen_tree_views.py          （ビューを書き出す）
         python tools/gen_tree_views.py --check  （検査結果だけを表示）

読み取りルール（TECH_TREE.md の書き方に合わせる）
- ノード = 行頭が "### [幹-番号]" の見出し。[B-xx] は枝
- 状態 = 見出しの ✅ / 🔧 / ⛔ / ❓（"⛔→❓" は ❓ 扱い＝新鉱石で解決予定）
- 前提 = 本文の "前提:" から次の "／" まで。"," で区切った1項目に " or " があれば OR
- "移動" "を参照（ここにはノードを作らない）" の見出しは別名として無視
- 理解 = "理解: [2-39]..." の項目。作るのに要るのではなく「なぜそうなるか」を後で分かった科学（例：パン・酒と発酵の科学）。
  前提には数えない（作れるか・年の逆転の判定に使わない）。JSON では kind="understanding" のつながり
- 年 = "年: 1866（1866〜67） ⚠️" の項目（西暦、紀元前はマイナス、括弧は範囲、⚠️ は諸説・未確認）。無ければ本物欄から推定
書き出し：docs/TECH_TREE_VIEW.md ／ docs/tech_network.json（全部入り）／ docs/tech_network_graph.js（図の軽いデータ・位置つき）／ docs/tech_network_detail.js（カード用の重いデータ）
追加の読み込み（技術ネットワーク図だけに合流。TECH_TREE_VIEW.md の表・今すぐ作れる には入れない）
- docs/products.md の [P-番号]（種類: モノ、遊び方: 作品）
- docs/ideas.md の [I-番号]（種類: 発見、遊び方: 実験）。「前提になる: [..]」は 発見→技術 の線（kind="enables"）、
  「説明する: [..]」は 理解の線（kind="understanding"）。技術ノードの前提（作れるかの判定）には数えない
"""
import base64
import json
import re
import sys
from collections import defaultdict, deque
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "docs" / "TECH_TREE.md"
EXTRA_SRC = [ROOT / "docs" / "products.md", ROOT / "docs" / "ideas.md"]
# 英語（O-021）：ノードの名前とへえ①の英訳。docs/network_en.json（{番号: {name, hee1}}）。図とページで日本語／英語を切り替える
EN_SRC = ROOT / "docs" / "network_en.json"
# 本番のアイコン（S-008、案 E「色付き」）：tools/gen_icons.py が作る docs/icons_e.json（番号 → data URI）
ICON_E_SRC = ROOT / "docs" / "icons_e.json"
ICON_E = json.loads(ICON_E_SRC.read_text(encoding="utf-8")) if ICON_E_SRC.exists() else {}
EN = {k: v for k, v in json.loads(EN_SRC.read_text(encoding="utf-8")).items() if not k.startswith("_")} if EN_SRC.exists() else {}
OUT = ROOT / "docs" / "TECH_TREE_VIEW.md"
JSON_OUT = ROOT / "docs" / "tech_network.json"
JS_OUT = ROOT / "docs" / "tech_network.js"          # 以前の全部入り（NET-05 で分けたので消す）
GRAPH_JS = ROOT / "docs" / "tech_network_graph.js"   # network.html が最初に読む軽いデータ（位置つき）
DETAIL_JS = ROOT / "docs" / "tech_network_detail.js"  # カード・node.html 用の重いデータ
META = ROOT / "docs" / "network_meta.json"
TEX = ROOT / "src" / "main" / "resources" / "assets" / "semiconductor-lab" / "textures"
YEAR_RE = re.compile(r"(?:^|／|\s)年:\s*(-?\d+)(?:（([^）]*)）)?(\s*⚠️)?")

ID_RE = re.compile(r"\[(\d+|B|P|I)-(\d+[a-z]?)\]")
HEAD_RE = re.compile(r"^###\s+\[(\d+|B|P|I)-(\d+[a-z]?)\]\s*(.*)$")
START, GOAL = "0-01", "10-16"   # 最終目的は AI（DESIGN「MOD の最終目的」、2026-10-20）。EUV [9-40] はその手前の最後の機械
GOALS = [GOAL]
TRUNK_NAMES = {
    "0": "生存・道具", "1": "冶金", "2": "化学", "3": "熱", "4": "動力・電気",
    "5": "真空", "6": "ガス", "7": "光学", "8": "精密・計測", "9": "半導体プロセス",
    "10": "制御・計算", "11": "農業・食", "12": "建築・土木", "13": "医薬・衛生",
    "14": "輸送", "15": "通信・情報", "B": "枝",
}
EXTRA_TRUNKS = {"P": "モノ（作品）", "I": "発見・考え方"}   # products.md・ideas.md
ERAS = [("〜BC", -10**9, 0), ("1〜1500", 1, 1500), ("1500〜1800", 1501, 1800),
        ("1800〜1900", 1801, 1900), ("1900〜1950", 1901, 1950), ("1950〜", 1951, 10**9)]


def nid(m):
    return f"{m[0]}-{m[1]}"


def trunk_of(i):
    return i.split("-")[0]


def sort_key(i):
    t, n = i.split("-")
    num = int(re.match(r"\d+", n).group())
    return ({"B": 999, "P": 1000, "I": 1001}[t] if t in ("B", "P", "I") else int(t), num, n)


def parse(src=SRC):
    lines = src.read_text(encoding="utf-8").splitlines() if src.exists() else []
    nodes, dups, cur = {}, [], None
    for line in lines:
        m = HEAD_RE.match(line)
        if m:
            i, rest = nid((m.group(1), m.group(2))), m.group(3)
            if "移動" in rest[:40] or "を参照（ここにはノードを作らない）" in rest:
                cur = None
                continue
            if i in nodes:
                dups.append(i)
            cur = nodes.setdefault(i, {"id": i, "head": rest, "body": ""})
            cur["head"], cur["body"] = rest, rest
            continue
        if line.startswith("## ") or line.startswith("### "):
            cur = None
        elif cur is not None:
            cur["body"] += "\n" + line
    for n in nodes.values():
        fill(n)
    return nodes, dups


def fill(n):
    head = n["head"]
    first = re.split(r"\s{2,}|前提:|／", head)[0]
    n["name"] = re.sub(r"\s*→\s*$", "",re.sub(r"[✅❓⛔🔧🌿⚠️【】\"]|（.*", "", first).strip())[:24] or n["id"]
    q = re.search(r"（([^）]{1,14})）", first)   # 名前のかっこ書き。同じ名前が2つあるときだけ付けて見分ける（S-015）
    n["qual"] = q.group(1) if q else ""
    status_zone = re.split(r"前提:", head)[0]
    if "✅" in status_zone:
        n["st"] = "✅"
    elif "🔧" in status_zone:
        n["st"] = "🔧"
    elif "⛔" in status_zone and "⛔→❓" not in status_zone:
        n["st"] = "⛔"
    else:
        n["st"] = "❓"
    n["branch"] = n["id"].startswith("B-") or "🌿" in status_zone
    m = re.search(r"前提[:：](.*?)(?:／|\n本物|\n|$)", n["body"], re.S)
    # groups: 項目ごとに「代替案のリスト」、代替案は ID の AND
    groups = []
    if m:
        for item in re.split(r"[,、](?![^（(]*[）)])", m.group(1)):   # 括弧の中の , と 、 では分けない
            alts = [[nid(x) for x in ID_RE.findall(a)] for a in re.split(r"\s+or\s+|\s*/\s*(?=\[)", item)]
            alts = [a for a in alts if a]
            if alts:
                groups.append(alts)
    n["groups"] = groups
    n["pre"] = sorted({i for alts in groups for a in alts for i in a}, key=sort_key)
    um = re.search(r"(?:^|／|\s)理解[:：](.*?)(?:／|\n|$)", n["body"], re.S)
    n["understand"] = sorted({nid(x) for x in ID_RE.findall(um.group(1))}, key=sort_key) if um else []
    # 発見（ideas.md）から技術への線：前提になる（無いと作れなかった）／説明する（なぜ動くか）
    def ids_of(pat):
        m = re.search(r"(?:^|／|\s)" + pat + r"[:：](.*?)(?:／|\n|$)", n["body"], re.S)
        return sorted({nid(x) for x in ID_RE.findall(m.group(1))}, key=sort_key) if m else []
    n["enables"], n["explains"] = ids_of("前提になる"), ids_of("説明する")
    # モノの部品（S-002）：「部品: [P-07], …」。部品→モノの線（kind="part"）。作れるかの判定には数えない
    n["parts"] = ids_of("部品")
    mm = re.search(r"(?:^|／|\s)素材[:：](.*?)(?:／|\n|$)", n["body"], re.S)
    n["materials"] = [x.strip() for x in re.split(r"[、,・]", mm.group(1)) if x.strip()] if mm else []
    # いま進行中（S-005）：「区分: 進行中」のノードは、図の右端の「いま進行中（現在の最前線）」の列に置く
    n["frontier"] = bool(re.search(r"(?:^|／|\s)区分[:：]\s*進行中", n["body"]))
    if "回避" in status_zone:
        n["st"] = "➖"
    ym = YEAR_RE.search(n["body"])
    if ym:
        n["year"], n["year_range"], n["year_uncertain"], n["year_src"] = int(ym.group(1)), ym.group(2), bool(ym.group(3)), "field"
    else:
        real = re.search(r"本物[:：](.*?)(?:／|$)", n["body"], re.S)
        n["year"], n["year_range"], n["year_uncertain"], n["year_src"] = year_of(real.group(1) if real else n["body"]), None, True, "guess"


def year_of(text):
    pats = [
        (r"約?(\d+(?:\.\d+)?)万年前", lambda g: -int(float(g) * 10000)),
        (r"紀元前(\d+)世紀", lambda g: -int(g) * 100 + 50),
        (r"紀元前(\d+)(?:〜\d+)?年", lambda g: -int(g)),
        (r"(?<![\d.])(\d{1,2})世紀", lambda g: (int(g) - 1) * 100 + 50),
        (r"(?<![\d.×⁻\-])(1[0-9]\d\d|20[0-2]\d)(?![\d,])", int),
        (r"(?<![\d.])(\d{2,3})年", int),
    ]
    # 温度・長さ・量などの数字（範囲 "1200〜1300℃" を含む）は年ではないので先に消す
    unit = r"(?:℃|K\b|Pa|nm|µm|mm|kWh|kW|W\b|V\b|ppm|%|倍|個|本|枚|kg|気圧|回|トン|bit|m\b|L\b|g\b)"
    text = re.sub(r"[\d.,]+(?:\s?[〜~\-]\s?[\d.,]+)?\s?" + unit, " ", text)
    best = None
    for p, f in pats:
        m = re.search(p, text)
        if m and (best is None or m.start() < best[0]):
            best = (m.start(), f(m.group(1)))
    return best[1] if best else None


GLOSSARY_SRC = ROOT / "docs" / "glossary.json"
GLOSSARY_JS = ROOT / "docs" / "glossary.js"
GLOSSARY = {k: v for k, v in json.loads(GLOSSARY_SRC.read_text(encoding="utf-8")).items() if not k.startswith("_")} if GLOSSARY_SRC.exists() else {}


def build_terms(out_nodes):
    """言葉の辞書（S-009）：文の中の言葉 → リンク先。ノードの名前（かっこを外した形・かっこの中の別名も）・人物の名前（→ その人のノード）・用語集の言葉（→ "g:言葉"）。
    同じ言葉が別のノードを指すとき（あいまい）は、ノードの正式な名前以外は使わない。2文字より短い言葉は入れない"""
    def forms(name):
        base = re.sub(r"[（(].*?[）)]", "", name).strip()
        inner = [x.strip() for m in re.findall(r"[（(](.*?)[）)]", name) for x in re.split(r"[・、，,＝/／]", m)]
        return [name, base] + [x for x in inner if not re.fullmatch(r"[\d\s.〜~ー-]*", x)]
    ja, en, amb = {}, {}, set()
    for n in out_nodes:
        ja.setdefault(n["name"], n["id"])
    for n in out_nodes:
        for f in forms(n["name"])[1:]:
            if len(f) < 2 or f == n["name"]:
                continue
            if f in ja and ja[f] != n["id"]:
                amb.add(f)
            ja.setdefault(f, n["id"])
        for p in n.get("people", []):
            nm = p.get("name", "")
            for f in {nm, re.split(r"[・＝]", nm)[-1]}:
                if len(f) >= 3 or (f == nm and len(f) == 2 and not re.fullmatch(r"[ァ-ヶー]+", f)):   # 2文字のカタカナの名前（ベルなど）は別の言葉に入りやすいので使わない
                    ja.setdefault(f, n["id"])
        if n.get("name_en"):
            for f in {n["name_en"], re.sub(r"\s*\(.*?\)", "", n["name_en"]).strip()}:
                if len(f) >= 3:
                    en.setdefault(f, n["id"])
    exact = {n["name"] for n in out_nodes}
    for f in amb:
        if f not in exact:
            ja.pop(f, None)
    meta_alias = json.loads(META.read_text(encoding="utf-8")).get("aliases", {}) if META.exists() else {}
    for nid, words in meta_alias.items():
        if not nid.startswith("_"):
            for f in words:
                ja.setdefault(f, nid)   # network_meta.json の aliases（O-023：論理ゲート → [10-05] など）
    for k, v in GLOSSARY.items():
        for f in [k] + v.get("aliases", []):
            ja.setdefault(f, "g:" + k)
        if v.get("en"):
            en.setdefault(v["en"], "g:" + k)
    return sorted(ja.items(), key=lambda kv: -len(kv[0])), sorted(en.items(), key=lambda kv: -len(kv[0]))


def clean_url(u):
    """本文から取った URL の後ろの句読点・閉じかっこを外す（URL の中の「(computer)」や「,_1911」は残す）"""
    u = u.rstrip(",.、。）")
    while u.endswith(")") and u.count(")") > u.count("("):
        u = u[:-1].rstrip(",.、。）")
    return u


def link_check(nodes):
    """つながりの検査（O-013）：products・ideas も合わせた全ノードで、
    ①前提・前提になる・説明する・部品・本文中の [番号] が存在しない番号を指していないか
    ②名前がほとんど同じノード（同じ物が2つ）の候補。名前のかっこ書きと空白を除いて同じ、または4文字以上の名前が別の名前にそのまま含まれる"""
    bad = []
    for n in nodes.values():
        refs = [("前提", p) for p in n["pre"]] + [("前提になる", p) for p in n.get("enables", [])] +                [("説明する", p) for p in n.get("explains", [])] + [("部品", p) for p in n.get("parts", [])] +                [("本文", nid(x)) for x in ID_RE.findall(n["body"])]
        for f, i in refs:
            if i not in nodes:
                bad.append((n["id"], f, i))
    norm = lambda t: re.sub(r"[（(].*?[）)]|\s|・", "", t)
    names = sorted(((norm(n["name"]), n["id"]) for n in nodes.values()), key=lambda x: sort_key(x[1]))
    same = []
    for a in range(len(names)):
        for b in range(a + 1, len(names)):
            x, y = names[a][0], names[b][0]
            if x and y and (x == y or (min(len(x), len(y)) >= 4 and (x in y or y in x))):
                same.append((names[a][1], names[b][1]))
    return sorted(set(bad)), same


def check(nodes):
    undefined = defaultdict(list)
    for n in nodes.values():
        for p in n["pre"]:
            if p not in nodes:
                undefined[p].append(n["id"])
    # 循環（DFS）
    color, cycles = {}, []

    def dfs(u, stack):
        color[u] = 1
        stack.append(u)
        for p in nodes[u]["pre"]:
            if p not in nodes:
                continue
            if color.get(p) == 1:
                cycles.append(stack[stack.index(p):] + [p])
            elif not color.get(p):
                dfs(p, stack)
        stack.pop()
        color[u] = 2

    sys.setrecursionlimit(10000)
    for u in sorted(nodes, key=sort_key):
        if not color.get(u):
            dfs(u, [])
    trunks = defaultdict(int)
    for i in nodes:
        trunks[trunk_of(i)] += 1
    empty = [t for t in TRUNK_NAMES if t != "B" and trunks.get(t, 0) == 0]
    return undefined, cycles, empty


def ancestors(nodes, targets):
    seen, q = set(), deque(t for t in targets if t in nodes)
    while q:
        u = q.popleft()
        if u in seen:
            continue
        seen.add(u)
        q.extend(p for p in nodes[u]["pre"] if p in nodes)
    return seen


def buildable(nodes):
    out = []
    for n in nodes.values():
        if n["st"] not in ("❓", "⛔"):
            continue
        ok = all(any(all(i in nodes and nodes[i]["st"] == "✅" for i in a) for a in alts)
                 for alts in n["groups"])
        if ok and n["st"] == "❓":
            out.append(n["id"])
    return sorted(out, key=sort_key)


def shortest_chain(nodes, start, goal):
    # 辺: 前提 → ノード。start から goal への最短の鎖（BFS）
    succ = defaultdict(list)
    for n in nodes.values():
        for p in n["pre"]:
            succ[p].append(n["id"])
    prev, q = {start: None}, deque([start])
    while q:
        u = q.popleft()
        if u == goal:
            break
        for v in sorted(succ[u], key=sort_key):
            if v not in prev:
                prev[v] = u
                q.append(v)
    if goal not in prev:
        return None
    path, u = [], goal
    while u:
        path.append(u)
        u = prev[u]
    return path[::-1]


def critical_chain(nodes, goal):
    # goal の前提のうち、いちばん深い鎖をたどる（実際に律速する鎖）
    # OR の項目は「いちばん浅い代替案」で数える（実際にはそちらを選べばよいので）
    memo = {}

    def alt_depth(a):
        return max((depth(i) for i in a if i in nodes), default=0)

    def best_alt(alts):
        return min(alts, key=alt_depth)

    def depth(u):
        if u not in memo:
            memo[u] = 0  # 循環よけ
            memo[u] = 1 + max((alt_depth(best_alt(g)) for g in nodes[u]["groups"]), default=0)
        return memo[u]

    path, u = [goal], goal
    while True:
        cands = [i for g in nodes[u]["groups"] for i in best_alt(g)
                 if i in nodes and i not in path]  # 循環で止まらないように
        if not cands:
            return path[::-1]
        u = max(cands, key=lambda p: (depth(p), sort_key(p)))
        path.append(u)


def topo(nodes, subset):
    indeg = {u: 0 for u in subset}
    succ = defaultdict(list)
    for u in subset:
        for p in nodes[u]["pre"]:
            if p in subset:
                indeg[u] += 1
                succ[p].append(u)
    q = sorted([u for u, d in indeg.items() if d == 0], key=sort_key)
    out = []
    while q:
        u = q.pop(0)
        out.append(u)
        for v in succ[u]:
            indeg[v] -= 1
            if indeg[v] == 0:
                q.append(v)
                q.sort(key=sort_key)
    return out + sorted(set(subset) - set(out), key=sort_key)  # 循環が残れば末尾


def explained_inversions():
    """network_proposals.md の「年の逆転」の表で、状態が「説明ずみ」の行（Y-番号）に書かれた「[a] … ＜ … [b]」の組（O-017）。
    実装担当が「前提は変えず、本物の別の道を注で書いた」などと説明した逆転は、自動チェックで区別して数える"""
    out = {}
    if not PROPOSALS.exists():
        return out
    for line in PROPOSALS.read_text(encoding="utf-8").splitlines():
        m = re.match(r"\| (Y-\d+) \|(.*)\|", line)
        if m and "説明ずみ" in line.split("|")[-2]:
            for a, b in re.findall(r"\[([^\]]+)\][^＜\[]*＜[^\[]*\[([^\]]+)\]", line.split("|")[2]):
                out[(a, b)] = m.group(1)
    return out


def year_inversions(nodes):
    """年の逆転：ノードの年が、必ず要る前提（AND の項目。OR はいちばん新しくない代替案）の年より古いもの。
    本物の歴史では「前提より先に生まれた」技術（例：後の別ルートでゲームでは作る）を見つけるための一覧。直さずに報告だけ"""
    out, seen = [], set()
    for n in nodes.values():
        y = n["year"]
        if y is None:
            continue
        for alts in n["groups"]:
            def alt_year(a):
                ys = [nodes[i]["year"] for i in a if i in nodes and nodes[i]["year"] is not None]
                return max(ys) if ys else None
            cands = [(alt_year(a), a) for a in alts]
            cands = [c for c in cands if c[0] is not None]
            if not cands:
                continue
            py, a = min(cands, key=lambda c: c[0])   # OR なら、いちばん古い代替案で足りる
            if py > y:
                late = max((i for i in a if i in nodes and nodes[i]["year"] is not None), key=lambda i: nodes[i]["year"])
                if (n["id"], late) not in seen:   # 同じ前提が2つの項目に出ても1件
                    seen.add((n["id"], late))
                    out.append((n["id"], y, late, nodes[late]["year"], py - y))
    return sorted(out, key=lambda r: -r[4])


VANILLA_JA = {"sand": "砂", "iron_ingot": "鉄インゴット", "copper_ingot": "銅インゴット", "gold_nugget": "金塊（粒）", "iron_nugget": "鉄の粒",
              "copper_nugget": "銅の粒", "coal": "石炭", "charcoal": "木炭", "clay_ball": "粘土玉", "bone_meal": "骨粉", "brick": "れんが",
              "bucket": "バケツ", "water_bucket": "水入りバケツ", "calcite": "方解石", "cauldron": "大釜", "cinnabar": "辰砂", "leather": "革",
              "planks": "木材", "potent_sulfur": "強力な硫黄", "redstone": "レッドストーン", "stick": "棒", "sulfur": "硫黄ブロック",
              "sulfur_spike": "尖った硫黄"}


def node_details(nodes, icons):
    """ノード詳細ページ（docs/node.html）用：本物の全文、へえ①〜④、出典（REFERENCES）、ゲームでの作り方（MOD のレシピ JSON と lang）"""
    res = ROOT / "src" / "main" / "resources"
    ja = json.loads((res / "assets" / "semiconductor-lab" / "lang" / "ja_jp.json").read_text(encoding="utf-8"))
    def lang(key):
        for p in ("item.", "block."):
            if p + key in ja:
                return ja[p + key]
        return None
    def ja_name(full):
        ns, _, k = full.replace("#", "").partition(":")
        if ns == "semiconductor-lab":
            return lang("semiconductor-lab." + k) or k
        return VANILLA_JA.get(k, k)
    # MOD のレシピ JSON → 出てくるアイテムごと
    recipes = defaultdict(list)
    for f in sorted((res / "data" / "semiconductor-lab" / "recipe").glob("*.json")):
        r = json.loads(f.read_text(encoding="utf-8"))
        out = r.get("result", {})
        out_id = out.get("id") if isinstance(out, dict) else out
        cnt = out.get("count", 1) if isinstance(out, dict) else 1
        typ = r.get("type", "").split(":")[-1]
        if typ in ("crafting_shaped", "crafting_shapeless"):
            ings = r.get("ingredients") or [r["key"][c] for row in r["pattern"] for c in row if c != " "]
            cnt_by = defaultdict(int)
            for x in ings:
                cnt_by[ja_name(x if isinstance(x, str) else x.get("item", str(x)))] += 1
            how = "作業台：" + " ＋ ".join(f"{k}×{v}" if v > 1 else k for k, v in cnt_by.items())
        elif typ in ("smelting", "blasting"):
            src = r.get("ingredient"); src = src if isinstance(src, str) else src.get("item", "")
            how = ("かまど" if typ == "smelting" else "溶鉱炉") + "：" + ja_name(src) + " を焼く"
        else:
            continue
        recipes[out_id].append(how + f" → {ja_name(out_id)}" + (f"×{cnt}" if cnt > 1 else ""))
    # REFERENCES.md の項目（見出しの［番号］で対応）
    refs = defaultdict(list)
    ref_text = (ROOT / "docs" / "REFERENCES.md").read_text(encoding="utf-8")
    for sec in re.split(r"\n(?=## )", ref_text):
        head = sec.split("\n", 1)[0]
        ids = re.findall(r"［(\d+|B)-(\d+[a-z]?)", head)
        if not head.startswith("## ") or not ids:
            continue
        get = lambda pat: (re.search(pat + r"[:：]\s*(.*)", sec) or [None, ""])[1].strip()
        hees = {k: get(r"- へえ" + c) for k, c in (("1", "①"), ("2", "②"), ("3", "③"), ("4", "④"))}
        item = {"title": head[3:].strip(), "answer": re.sub(r"\*\*", "", get(r"\*\*答え\*\*")), "hee": hees,
                "certainty": get(r"確かさ"), "basis": get(r"根拠")}
        for t, num in ids:
            refs[f"{t}-{num}"].append(item)
    # 機械のレシピ（GameTest exportMachineRecipes が書き出す docs/machine_recipes.json）
    mfile = ROOT / "docs" / "machine_recipes.json"
    machine = json.loads(mfile.read_text(encoding="utf-8")).get("recipes", []) if mfile.exists() else []
    def thing(x, out=False):
        """量の数字の項目から表示の文を作る（例：「黒鉛棒 8回で1本」「水素ガス 4 mol」「水入りバケツ 1/4」）。
        per_runs は材料なら「N回で1個へる」、できる物なら「N回に1個できる」"""
        name = ja_name(x["item"]) if ":" in x["item"] else x["item"]
        c = x.get("count", 1)
        frac = {0.25: "1/4", 0.5: "1/2", 0.75: "3/4"}
        if x.get("per_runs"):
            s = f"{name} {x['per_runs']}回{'に' if out else 'で'}{c:g}" + ("本" if "棒" in name else "個") + ("できる" if out else "")
        elif x.get("unit"):
            s = f"{name} {c:g} {x['unit']}"
        elif c in frac:
            s = f"{name} {frac[c]}"
        else:
            s = f"{name}×{c:g}" if c != 1 else name
        extra = []
        if x.get("max"):
            extra.append(f"最大{x['max']}")
        if x.get("alt"):
            extra.append("・".join(ja_name(a) for a in x["alt"]) + "も可")
        if x.get("consumed") is False:
            extra.append("減らない")
        if x.get("cond"):
            extra.append(x["cond"])
        return s + (f"（{'、'.join(extra)}）" if extra else "")
    def power(r):
        w, s = r["watts"], r["seconds"]
        if w < 0:
            return f"発電 {(-w) / 1000:g} kW" + (f"・石炭1個 {s:g} 秒" if s else "")
        if w == 0:   # 説明に「電気なし」があれば二重に書かない
            tag = "" if "電気なし" in (r.get("note") or "") else "電気なし"
            return "・".join(x for x in (tag, f"{s:g} 秒" if s else "") if x) or "電気なし"
        return f"{w / 1000:g} kW × {s:g} 秒＝{r.get('energy_kj', 0):,} kJ" if s else f"{w / 1000:g} kW"
    by_item = defaultdict(list)
    for r in machine:
        line = (f"{ja_name(r['machine'])}「{r['process']}」：" + " ＋ ".join(thing(x) for x in r["inputs"]) + " → "
                + " ＋ ".join(thing(x, True) for x in r["outputs"]) + f"（{power(r)}）" + (f"。{r['note']}" if r.get("note") else ""))
        keys = {r["machine"]} | {x["item"] for x in r["outputs"] if ":" in x["item"]}
        for k in keys:
            by_item[k].append(line)
    out = {}
    for i, n in nodes.items():
        body = n["body"]
        real = re.search(r"本物[:：]\s*(.*?)(?:／|$)", body, re.S)
        tree_hee = {k: (re.search(r"へえ" + c + r"[:：]\s*(.*?)(?:／|\n|$)", body) or [None, ""])[1].strip()
                    for k, c in (("2", "②"), ("3", "③"), ("4", "④"))}
        item_id = None
        if i in icons:
            item_id = re.sub(r"_(front|side|top|bottom)$", "", icons[i].split("/", 1)[1])
        game = {}
        if item_id:
            for s in ("note1", "note2", "note3", "note4"):
                v = lang(f"semiconductor-lab.{item_id}.{s}") or (lang(f"semiconductor-lab.{item_id}.note") if s == "note2" else None)
                if v:
                    game[s] = v
        out[i] = {"real_full": re.sub(r"\*\*|`", "", real.group(1)).strip() if real else "",
                  "hee_tree": tree_hee, "hee_game": game, "refs": refs.get(i, []),
                  "item": item_id, "recipes": recipes.get(f"semiconductor-lab:{item_id}", []) if item_id else [],
                  "machine_recipes": by_item.get(f"semiconductor-lab:{item_id}", []) if item_id else [],
                  "placeholder": lang(f"semiconductor-lab.{item_id}.placeholder") if item_id else None,
                  "game_text": field_all(body, r"ゲーム") or field_all(body, r"MC入手") or field_all(body, r"MC")}
    return out


def kind_of(body):
    if re.search(r"種類[:：]\s*モノ", body):
        return "product"
    if re.search(r"種類[:：]\s*発見", body):
        return "idea"
    return "tech"


def field_all(body, pat):
    m = re.search(r"(?:^|／|\s)" + pat + r"[:：]\s*(.*?)(?:／|$)", body, re.S)
    return re.sub(r"\*\*|`", "", m.group(1)).strip()[:600] if m else ""


GRAN_REPORT = {}


def export_json(nodes):
    """技術ネットワーク図（ROADMAP 技術ネットワーク ②）の元データ。ノード・つながり・子孫の数"""
    succ = defaultdict(set)
    for n in nodes.values():
        for p in n["pre"]:
            if p in nodes:
                succ[p].add(n["id"])
    def descendants(u):
        seen, q = set(), deque([u])
        while q:
            v = q.popleft()
            for w in succ[v]:
                if w not in seen:
                    seen.add(w)
                    q.append(w)
        return seen
    def field(body, pat, n=400):
        m = re.search(pat + r"[:：]\s*(.*?)(?:／|\n|$)", body, re.S)
        return re.sub(r"\*\*|`", "", m.group(1)).strip()[:n] if m else ""
    now = set(buildable(nodes))
    # 見た目の情報（アイコン・モノの絵文字・人物）。docs/network_meta.json を手で編集する
    meta = json.loads(META.read_text(encoding="utf-8")) if META.exists() else {}
    icons, people = meta.get("icons", {}), meta.get("people", {})
    emoji, symbols = meta.get("products", {}), meta.get("trunk_symbols", {})
    def icon_uri(rel):
        p = TEX / (rel + ".png")
        return "data:image/png;base64," + base64.b64encode(p.read_bytes()).decode() if p.exists() else None
    details = node_details(nodes, icons)
    out_nodes, edges = [], []
    for i in sorted(nodes, key=sort_key):
        n = nodes[i]
        out_nodes.append({**details.get(i, {}),"id": i, "name": n["name"], "trunk": trunk_of(i), "trunk_name": {**TRUNK_NAMES, **EXTRA_TRUNKS}.get(trunk_of(i), ""),
                          "status": n["st"], "branch": n["branch"], "year": n["year"], "year_range": n["year_range"],
                          "year_uncertain": n["year_uncertain"], "requires": [[a for a in alts] for alts in n["groups"]],
                          "understanding": n["understand"], "buildable_now": i in now,
                          "real": field(n["body"], r"本物"), "hee1": field(n["body"], r"へえ①?", 1500),   # S-019：へえ①の後ろの（出典：…）が長いので 400 字で切れていた
                          "name_en": EN.get(i, {}).get("name"), "hee1_en": EN.get(i, {}).get("hee1") or None,
                          "kind": kind_of(n["body"]),
                          "play": field(n["body"], r"遊び方"), "missing": field(n["body"], r"足りない"),
                          "sources": [clean_url(u) for u in re.findall(r"https?://[^\s／]+", field_all(n["body"], r"出典"))],
                          "enables": n["enables"], "explains": n["explains"], "parts": n["parts"], "materials": n["materials"], "frontier": n["frontier"],
                          "enabled_by": [k for k, o in nodes.items() if i in o["enables"]],
                          "explained_by": [k for k, o in nodes.items() if i in o["explains"]],
                          "icon": ICON_E.get(i) or (icon_uri(icons[i]) if i in icons else None), "emoji": emoji.get(i),
                          "symbol": symbols.get(trunk_of(i), "●"), "people": people.get(i, []),
                          "media": [x for x in meta.get("media", {}).get(i, []) if isinstance(x, dict)],
                          "descendants": len(descendants(i))})
        for gi, alts in enumerate(n["groups"]):
            for a in alts:
                for p in a:
                    if p in nodes:
                        edges.append({"from": p, "to": i, "kind": "requires", "group": gi, "or": len(alts) > 1})
        for p in n["understand"]:
            if p in nodes:
                edges.append({"from": p, "to": i, "kind": "understanding"})
        for t in n["enables"]:
            if t in nodes:
                edges.append({"from": i, "to": t, "kind": "enables"})
        for t in n["explains"]:
            if t in nodes:
                edges.append({"from": i, "to": t, "kind": "understanding"})
        for t in n["parts"]:
            if t in nodes:
                edges.append({"from": t, "to": i, "kind": "part"})
    # 粒度（S-035）：中心図の輪の意味。network_meta.json の granularity から（モノ＝1、発見＝3、そのほかは幹の決まり、迷う物は手で）
    gr = meta.get("granularity", {})
    unclear = []
    for n in out_nodes:
        if n["kind"] == "product":
            n["gran"] = 1
        elif n["kind"] == "idea":
            n["gran"] = 3
        elif n["id"] in gr.get("nodes", {}):
            n["gran"] = gr["nodes"][n["id"]]
        else:
            n["gran"] = gr.get("trunk", {}).get(n["trunk"], 3)
            if n["trunk"] in gr.get("unclear_trunks", []):
                unclear.append(n["id"])
    if gr:
        GRAN_REPORT.update({"unclear": unclear, "count": {g: sum(1 for n in out_nodes if n.get("gran") == g) for g in (1, 2, 3, 4)}})
    # PageRank（S-039、ブリンとペイジ 1998）：「使う物 → 前提」を参照とみなし、多くの物に（間接にも）使われる物ほど点が高い。
    # 重み：requires・enables 1、part 0.7、understanding 0.5。減衰 0.85、行き先の無い点は全体に配る。点は 0〜100（いちばん高い物＝100）
    PR_W = {"requires": 1.0, "enables": 1.0, "part": 0.7, "understanding": 0.5}
    # 外した前提（S-036）：TECH_TREE の前提の書き方がおかしい物（考え方の前提に材料など）。担当が直すまでネットワークの側で外す（network_meta.json の dropped_requires）
    drop = {(x["from"], x["to"]) for x in meta.get("dropped_requires", [])}
    if drop:
        edges[:] = [e for e in edges if not (e["kind"] == "requires" and (e["from"], e["to"]) in drop)]
    # 補った前提（S-025）：TECH_TREE に書きもれていた本当の前提。担当が TECH_TREE に書くまで、ネットワークの側で補う（network_meta.json の added_requires）。
    # TECH_TREE に同じ前提がもうあれば足さない（重複しない）
    have = {(e["from"], e["to"]) for e in edges if e["kind"] == "requires"}
    adj = {}
    for e in edges:
        if e["kind"] in ("requires", "enables", "part"):
            adj.setdefault(e["from"], set()).add(e["to"])
    def reaches(a, b):   # a から前へ進んで b に着くか（着くなら b→a を足すと輪になる）
        seen, q = {a}, [a]
        while q:
            u = q.pop()
            if u == b:
                return True
            for v in adj.get(u, ()):
                if v not in seen:
                    seen.add(v); q.append(v)
        return False
    for x in meta.get("added_requires", []):
        f, t = x.get("from"), x.get("to")
        if f in nodes and t in nodes and (f, t) not in have:
            if reaches(t, f):
                print(f"warning: added_requires {f}->{t} は前提の輪になるので足さない")
                continue
            edges.append({"from": f, "to": t, "kind": "requires", "added": True}); have.add((f, t)); adj.setdefault(f, set()).add(t)
    ids = [n["id"] for n in out_nodes]; ix = {k: i for i, k in enumerate(ids)}; NN = len(ids)
    outw = [dict() for _ in ids]
    for e in edges:
        w = PR_W.get(e["kind"])
        if w and e["from"] in ix and e["to"] in ix:
            u, v = ix[e["to"]], ix[e["from"]]   # 使う物 u が 前提 v を参照
            outw[u][v] = max(outw[u].get(v, 0), w)
    pr = [1.0 / NN] * NN
    for _ in range(100):
        nxt = [0.15 / NN] * NN; dang = 0.0
        for u in range(NN):
            tot = sum(outw[u].values())
            if not tot:
                dang += pr[u]; continue
            for v, w in outw[u].items():
                nxt[v] += 0.85 * pr[u] * w / tot
        nxt = [x + 0.85 * dang / NN for x in nxt]
        if sum(abs(a - b) for a, b in zip(nxt, pr)) < 1e-10:
            pr = nxt; break
        pr = nxt
    top = max(pr)
    for n in out_nodes:
        n["pr"] = round(pr[ix[n["id"]]] / top * 100, 2)
    # 前提でも理解でもないつながり（O-019）：つくるのに使った道具（tool）・同じしくみ（same）。network_meta.json の extra_links から
    for x in meta.get("extra_links", []):
        if "from" in x and x["from"] in nodes and x["to"] in nodes:
            edges.append({"from": x["from"], "to": x["to"], "kind": x["kind"]})
    # S-062：存命と見られる人の名前を、書き出すすべての文で置きかえる（network_meta の living_names。長い物から）
    _ln = sorted(((meta.get("living_names") or {}).get("map") or {}).items(), key=lambda kv: -len(kv[0]))
    def _scrub(v):
        if isinstance(v, str):
            for a, b in _ln:
                v = v.replace(a, b)
            return v
        if isinstance(v, list):
            return [_scrub(x) for x in v]
        if isinstance(v, dict):
            return {k: (_scrub(x) if k not in ("id", "icon", "portrait", "url", "sources") else x) for k, x in v.items()}
        return v
    if _ln:
        out_nodes[:] = [_scrub(n) for n in out_nodes]
    # 重複を消す（NET-05）：「本物」は real_full だけ（real は1行目の400字なので表示側で作る）。肖像は人物ごとに1か所（portraits）にまとめて番号で参照
    portraits, pidx = [], {}
    for n in out_nodes:
        n.pop("real", None)
        ps = []
        for p in n["people"]:
            p = dict(p)
            uri = p.pop("portrait", None)
            if uri:
                if uri not in pidx:
                    pidx[uri] = len(portraits)
                    portraits.append(uri)
                p["pi"] = pidx[uri]
            ps.append(p)
        n["people"] = ps
    data = {"generated_by": "tools/gen_tree_views.py", "source": "docs/TECH_TREE.md", "start": START, "goal": GOAL,
            "real_note": "本物の全文は real_full。図のカードに出す短い本物は real_full の1行目の先頭400字",
            "portraits": portraits, "portraits_note": "人物の肖像（data URI）。people[].pi が番号",
            "year_note": "西暦。紀元前はマイナス。year_uncertain=true は諸説・未確認（⚠️）",
            "requires_note": "requires はノードを作るのに要る項目の並び（全部そろう＝AND）。1項目の中の複数案は OR、案の中は AND",
            "edge_kinds": {"requires": "前提（無いと作れない）", "understanding": "理解（後で分かった科学。作るのには要らない）",
                           "enables": "発見→技術（その考えがないと作れなかった。作れるかの判定には数えない）",
                           "part": "部品→モノ（そのモノを形づくる部品のモノ。作れるかの判定には数えない）"},
            "kinds": {"tech": "技術", "product": "モノ（TECH_TREE の「種類: モノ」と products.md）", "idea": "発見・考え方（ideas.md）"},
            "nodes": out_nodes, "edges": edges, "year_inversions": [
                {"node": a, "year": y, "requires": p, "requires_year": py, "gap": d, "explained": explained_inversions().get((a, p))}
                for a, y, p, py, d in year_inversions(nodes)]}
    JSON_OUT.write_text(json.dumps(data, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    # 図の表示用（NET-05）：軽いデータ（位置を先に計算して入れる）と、カード・詳細用の重いデータに分ける。
    # ファイルを直接開いても読めるように JS（window.〜 = …）で書く（file:// では fetch できないため）
    # S-056：ウィキ（A の docs/wiki_export.json）のアイテムを、ノードごとに
    WIKI_ITEMS = {}
    _wf = ROOT / "docs" / "wiki_export.json"
    if _wf.exists():
        for _it in json.loads(_wf.read_text(encoding="utf-8")).get("items", []):
            if _it.get("node") and _it["id"].startswith("semiconductor-lab:"):
                WIKI_ITEMS.setdefault(_it["node"], []).append({"s": _it["id"].split(":", 1)[1], "ja": _it["name"].get("ja") or _it["name"].get("en"), "en": _it["name"].get("en")})
    # S-047：出典のある丸（「あ！」の道に使える）：REFERENCES の確かさ ◎○ か、出典の URL がある
    for n in out_nodes:
        n["wiki_items"] = WIKI_ITEMS.get(n["id"], [])[:40]   # S-056：このノードのゲームのアイテム（ウィキのページへ）
        n["reading"] = [{k: b.get(k) for k in ("title", "authors", "year", "ja")} for b in (meta.get("reading") or {}).get("books", []) if n["id"] in b.get("nodes", [])]   # S-055：もっと知りたい人へ（Manual for Civilization の推薦の本）
        n["src"] = 1 if n.get("refs") or n.get("sources") or re.search(r"出典[:：]", (n.get("real_full") or "") + (n.get("hee1") or "")) else 0   # tools/provenance.py と同じ数え方
    light_keys = ("id", "name", "name_en", "gran", "pr", "src", "trunk", "trunk_name", "status", "year", "year_range", "year_uncertain", "kind",
                  "icon", "emoji", "symbol", "descendants", "buildable_now", "frontier")
    layout = compute_layout(out_nodes, edges)
    kind_code = {"requires": "r", "understanding": "u", "enables": "e", "part": "p", "tool": "t", "same": "s",
                 "improves": "i", "byproduct": "b", "demand": "d", "replaces": "x"}   # S-059：改良・副産物・需要を生んだ・置き換えた
    graph = {"goal": GOAL, "start": START, "inversions": sum(1 for x in data["year_inversions"] if not x["explained"]),
             "nodes": [{k: n[k] for k in light_keys} for n in out_nodes],
             "edges": [[e["from"], e["to"], kind_code[e["kind"]]] for e in edges], "layout": layout,
             "aha": {k: v for k, v in meta.get("aha", {}).items() if not k.startswith("_")},
             "support": (meta.get("support") or {}).get("url") or None}   # S-051：応援のリンク（空なら出さない）
    detail = {"nodes": {n["id"]: {k: v for k, v in n.items() if k not in light_keys} for n in out_nodes},
              "portraits": portraits, "year_inversions": data["year_inversions"],
              "materials": {k: v for k, v in meta.get("materials", {}).items() if not k.startswith("_")},
              "extra_links": [x for x in meta.get("extra_links", []) if "from" in x]}
    detail["terms"], detail["terms_en"] = build_terms(out_nodes)
    GLOSSARY_JS.write_text("// 自動生成：tools/gen_tree_views.py（元は docs/glossary.json）。用語集（S-009）\nwindow.GLOSSARY = "
                           + json.dumps(GLOSSARY, ensure_ascii=False, separators=(",", ":")) + ";\n", encoding="utf-8")
    for path, var, obj, what in ((GRAPH_JS, "TECH_GRAPH", graph, "図に要る軽いデータ（番号・名前・年・幹・状態・種類・アイコン・位置・つながり・子孫の数）"),
                                 (DETAIL_JS, "TECH_DETAIL", detail, "カード・詳細ページ用の重いデータ（本物・へえ・出典・レシピ・人物）")):
        path.write_text(f"// 自動生成：tools/gen_tree_views.py。{what}\nwindow.{var} = "
                        + json.dumps(obj, ensure_ascii=False, separators=(",", ":")) + ";\n", encoding="utf-8")
    if JS_OUT.exists():   # 以前の全部入りの JS は使わなくなった
        JS_OUT.unlink()
    return data


# ---- ネットワーク図の配置（NET-05）：以前ブラウザ（network.html）で d3-force で計算していたものを、同じ設定でここで計算する ----
XS_DOMAIN = [-3400000, -100000, -10000, -3000, 0, 1000, 1500, 1700, 1800, 1850, 1900, 1930, 1950, 1970, 1990, 2030]
XS_RANGE = [0, 50, 120, 200, 260, 320, 380, 470, 580, 690, 820, 930, 1040, 1170, 1300, 1460]
LAYOUT_LEFT = 110
FRONTIER_GAP, FRONTIER_W = 30, 220   # いま進行中の列（S-005）：時間軸の右端からのすき間と幅


def _xs(y):
    """横軸：おおまかな年（古い時代は詰め、近代を広げる）。d3.scaleLinear().clamp(true) と同じ折れ線"""
    if y <= XS_DOMAIN[0]:
        return XS_RANGE[0]
    for k in range(1, len(XS_DOMAIN)):
        if y <= XS_DOMAIN[k]:
            a, b = XS_DOMAIN[k - 1], XS_DOMAIN[k]
            return XS_RANGE[k - 1] + (y - a) / (b - a) * (XS_RANGE[k] - XS_RANGE[k - 1])
    return XS_RANGE[-1]


def _collide(x, y, vx, vy, radii, iterations):
    """d3.forceCollide（strength 1）と同じ式。重なりの候補は各回のはじめに一度に求め、ノードの番号順に押し合う"""
    import numpy as np
    n = len(x)
    for _ in range(iterations):
        px, py = x + vx, y + vy
        dx = px[:, None] - px[None, :]
        dy = py[:, None] - py[None, :]
        rr = (radii[:, None] + radii[None, :]) * 1.5
        cand = np.argwhere(np.triu(dx * dx + dy * dy < rr * rr, 1))
        for i, j in cand:
            ri, rj = radii[i], radii[j]
            xi, yi = x[i] + vx[i], y[i] + vy[i]
            ddx, ddy = xi - x[j] - vx[j], yi - y[j] - vy[j]
            l2, r = ddx * ddx + ddy * ddy, ri + rj
            if l2 < r * r:
                if ddx == 0:
                    ddx, l2 = 1e-6, l2 + 1e-12
                if ddy == 0:
                    ddy, l2 = 1e-6, l2 + 1e-12
                l = l2 ** 0.5
                l = (r - l) / l
                ddx, ddy = ddx * l, ddy * l
                w = rj * rj / (ri * ri + rj * rj)
                vx[i] += ddx * w; vy[i] += ddy * w
                vx[j] -= ddx * (1 - w); vy[j] -= ddy * (1 - w)
    # i の行の押し合いは j の順番によらない（i の位置は i を処理し始めたときの値を使うため）ので、番号順で d3 と同じになる


def compute_layout(out_nodes, edges):
    """時間軸（年×幹の帯）とネットワーク（つながりだけの力学レイアウト）の位置。d3-force の設定・手順を同じにした移植"""
    import math
    import numpy as np
    land = lambda n: n["kind"] in ("product", "idea")
    R = lambda n: 15 if n["kind"] == "product" else 12 if n["kind"] == "idea" else 3 + 1.3 * math.sqrt(n["descendants"])
    band_of = lambda n: "P" if n["kind"] == "product" else "I" if n["kind"] == "idea" else n["trunk"]
    trunks = sorted({n["trunk"] for n in out_nodes if n["trunk"] not in ("P", "I")}, key=lambda t: (t == "B", 0 if t == "B" else int(t)))
    keys = ["P", "I"] + trunks
    N = len(out_nodes)
    x, y, tx = np.zeros(N), np.zeros(N), np.zeros(N)
    idx = {n["id"]: k for k, n in enumerate(out_nodes)}
    bands, y0 = {}, 30
    fx0 = LAYOUT_LEFT + XS_RANGE[-1] + FRONTIER_GAP
    has_frontier = any(n.get("frontier") for n in out_nodes)
    def x_of(n):
        if n.get("frontier"):   # 2000〜2030 年を列の幅に広げる
            yr = n["year"] if n["year"] is not None else 2020
            return fx0 + 24 + max(0, min(1, (yr - 2000) / 30)) * (FRONTIER_W - 48)
        return LAYOUT_LEFT + _xs(n["year"] if n["year"] is not None else 0)
    for t in keys:
        ns = sorted([n for n in out_nodes if band_of(n) == t], key=lambda n: n["year"] if n["year"] is not None else 0)
        lanes, lane_of, is_land = [], {}, t in ("P", "I")
        for n in ns:
            k = idx[n["id"]]
            x[k] = tx[k] = x_of(n)
            r = R(n)
            li = next((q for q, last in enumerate(lanes) if x[k] - x[idx[last["id"]]] >= (64 if is_land else r + R(last) + 3)), -1)
            if li < 0:
                lanes.append(n); li = len(lanes) - 1
            else:
                lanes[li] = n
            lane_of[n["id"]] = li
        lane_h = 44 if is_land else 15
        h = len(lanes) * lane_h + 40 if is_land else min(220, max(48, len(lanes) * 15 + 26))
        for n in ns:
            y[idx[n["id"]]] = y0 + 20 + (10 if is_land else 0) + (lane_of[n["id"]] % max(1, math.floor((h - 28) / lane_h))) * lane_h
        bands[t] = [y0, h]
        y0 += h
    by = np.array([bands[band_of(n)][0] for n in out_nodes], float)
    bh = np.array([bands[band_of(n)][1] for n in out_nodes], float)
    isl = np.array([land(n) for n in out_nodes])
    Rr = np.array([R(n) for n in out_nodes])
    ty = by + bh / 2 + 4
    sy = np.where(isl, 0.005, 0.04)
    rad = np.where(isl, 30, Rr + 1.5)
    rclamp = np.where(isl, Rr, np.minimum(Rr, 10))
    vx, vy = np.zeros(N), np.zeros(N)
    alpha, decay = 1.0, 1 - 0.001 ** (1 / 300)
    for _ in range(260):   # d3：alpha を減らし、力（x → y → 重なり）を足し、速さを 0.6 倍して位置を動かす
        alpha += (0 - alpha) * decay
        vx += (tx - x) * 0.6 * alpha
        vy += (ty - y) * sy * alpha
        _collide(x, y, vx, vy, rad, 3)
        vx *= 0.6; vy *= 0.6
        x += vx; y += vy
        y = np.maximum(by + 16 + rclamp, np.minimum(by + bh - np.where(isl, 16, 4) - rclamp, y))
        x = np.maximum(LAYOUT_LEFT + 4, x)
    W, H = (fx0 + FRONTIER_W + 20 if has_frontier else LAYOUT_LEFT + XS_RANGE[-1] + 40), y0 + 20
    time_pos = {n["id"]: [round(float(x[k]), 2), round(float(y[k]), 2)] for k, n in enumerate(out_nodes)}
    # ネットワーク：前提と「発見→技術」の線をばねに。子孫の多い技術（ハブ）ほど中心へ
    links = [(idx[e["from"]], idx[e["to"]]) for e in edges if e["kind"] in ("requires", "enables", "part") and e["from"] in idx and e["to"] in idx]
    count = np.zeros(N)
    for s, t in links:
        count[s] += 1; count[t] += 1
    bias = [count[s] / (count[s] + count[t]) for s, t in links]
    maxd = max((n["descendants"] for n in out_nodes), default=1) or 1
    radial = np.array([60 + 520 * (1 - math.sqrt(n["descendants"] / maxd)) for n in out_nodes])
    # S-014：モノ・発見の丸は、スマホで 1.6 倍に拡大して描いても重ならない間（15×1.6＋名前の分）。技術も少し広く
    rad2 = np.where(isl, 28, Rr + 4) + 2
    nx, ny = x - W / 2, y - H / 2
    vx, vy = np.zeros(N), np.zeros(N)
    alpha = 1.0
    for _ in range(320):   # d3：link → manyBody → radial → collide
        alpha += (0 - alpha) * decay
        for (s, t), b in zip(links, bias):
            dx = nx[t] + vx[t] - nx[s] - vx[s] or 1e-6
            dy = ny[t] + vy[t] - ny[s] - vy[s] or 1e-6
            l = math.hypot(dx, dy)
            l = (l - 28) / l * alpha * 0.25
            dx *= l; dy *= l
            vx[t] -= dx * b; vy[t] -= dy * b
            vx[s] += dx * (1 - b); vy[s] += dy * (1 - b)
        ddx = nx[None, :] - nx[:, None]
        ddy = ny[None, :] - ny[:, None]
        l2 = ddx * ddx + ddy * ddy
        l2 = np.where(l2 < 1, np.sqrt(l2), l2)
        np.fill_diagonal(l2, np.inf)
        vx += (ddx * (-38) * alpha / l2).sum(axis=1)
        vy += (ddy * (-38) * alpha / l2).sum(axis=1)
        dxr = np.where(nx == 0, 1e-6, nx)
        rr = np.sqrt(dxr * dxr + ny * ny)
        kk = (radial - rr) * 0.12 * alpha / rr
        vx += dxr * kk; vy += ny * kk
        _collide(nx, ny, vx, vy, rad2, 1)
        vx *= 0.6; vy *= 0.6
        nx += vx; ny += vy
    net_pos = {n["id"]: [round(float(nx[k] + W / 2), 2), round(float(ny[k] + H / 2), 2)] for k, n in enumerate(out_nodes)}
    return {"left": LAYOUT_LEFT, "W": W, "H": H, "xs": [XS_DOMAIN, XS_RANGE], "bands": bands, "time": time_pos, "net": net_pos,
            "frontier": [fx0, fx0 + FRONTIER_W] if has_frontier else None}


AUDIT_OUT = ROOT / "docs" / "AUDIT_QUEUE.md"
PROPOSALS = ROOT / "docs" / "network_proposals.md"
AUDIT_HEAD = """# AUDIT_QUEUE.md — 監査の待ち行列（自動生成：tools/gen_tree_views.py。手で編集しない）

元データは docs/network_proposals.md の「⚠️ の裏取り」の表（V-番号）。技術ネットワーク担当（NET-03）が出典で確かめた結果を、あとで別の監査者にまとめて見てもらうための一覧。

## 監査する人への説明
- **このプロジェクト**：Minecraft の教育 MOD「Semiconductor Lab」。石器から EUV 露光装置・AI まで、本物の技術の歴史を遊びながら学ぶ。技術どうしのつながり（前提・別ルート・後で分かった科学）を「技術ネットワーク」として公開し、世界中の人に監査してもらう予定（docs/network.html・docs/node.html）
- **確かさの基準**：◎＝信頼できる出典2つ以上で一致／○＝1つで確認／△＝未確認・出典どうしが食い違う。**Wikipedia だけの場合は ○ 止まり**（この一覧では自動で ○ に下げ、「Wikipedia のみ」と書く）
- **見てほしいこと**：①年・場所・人物が正しいか ②因果関係（「A があったから B ができた」）が言いすぎていないか ③出典が主張を本当に支えているか（URL を開いて、その文が本当に書いてあるか）④「正しくは…」の訂正そのものが正しいか
- **結果の書き方**：項目ごとに「V-番号｜同意 / 修正 / 不明｜理由（1〜2文）｜出典 URL」。修正のときは正しい文も書く
- **優先順**：✖（訂正）→ △（未確認）→ ○（出典1つ・Wikipedia のみ）→ ◎（本線の年・人物、念のため）。どれも [10-16] AI までの道（本線）のノード

"""


def write_audit_queue():
    """network_proposals.md の V- 行から AUDIT_QUEUE.md を作る（判定の厳しい順）"""
    if not PROPOSALS.exists():
        return 0
    rows = []
    for line in PROPOSALS.read_text(encoding="utf-8").splitlines():
        m = re.match(r"\|\s*(V-\d+)\s*\|(.*)\|\s*$", line)
        if not m:
            continue
        cells = [c.strip() for c in m.group(2).split("|")]
        if len(cells) < 5:
            continue
        node, claim, verdict, result, src = cells[:5]
        urls = re.findall(r"https?://[^\s,|]+", src)
        wiki_only = bool(urls) and all("wikipedia.org" in u or "wikipedia.com" in u for u in urls)
        if not urls and src.strip() == "同上":
            urls, wiki_only = rows[-1]["urls"], rows[-1]["wiki_only"]
        v = verdict
        if v.startswith("◎") and wiki_only:
            v = "○（Wikipedia のみ。元は ◎）"
        rank = 0 if "✖" in v else 1 if "△" in v else 2 if v.startswith("○") else 3
        if "✖" in v:
            ask = "訂正（「正しくは…」）が出典で本当に支えられているか"
        elif "△" in v:
            ask = "食い違い・未確認の点を、より確かな出典で決められるか"
        elif wiki_only:
            ask = "Wikipedia 以外の独立した出典で確かめられるか"
        elif v.startswith("○"):
            ask = "2つ目の独立した出典があるか"
        else:
            ask = "出典が主張（年・人物・場所）をそのまま支えているか"
        rows.append({"id": m.group(1), "node": node, "claim": claim, "v": v, "result": result, "urls": urls,
                     "wiki_only": wiki_only, "rank": rank, "ask": ask})
    rows.sort(key=lambda r: (r["rank"], int(r["id"][2:])))
    names = ["✖ 訂正した項目", "△ 未確認のまま残った項目", "○ 出典1つ・Wikipedia のみの項目", "◎ 本線の年・人物（念のため）"]
    L = [AUDIT_HEAD.rstrip(), "", f"全 {len(rows)} 件（" + "・".join(f"{n.split()[0]} {sum(1 for r in rows if r['rank'] == k)}" for k, n in enumerate(names)) + "）"]
    for k, n in enumerate(names):
        rs = [r for r in rows if r["rank"] == k]
        if not rs:
            continue
        L += ["", f"## {n}（{len(rs)}）", "", "| 番号 | ノード | 主張 | 今の判定 | NET-03 の結果 | 出典 | 特に確かめてほしい点 |", "|---|---|---|---|---|---|---|"]
        for r in rs:
            L.append(f"| {r['id']} | {r['node']} | {r['claim']} | {r['v']} | {r['result']} | {' , '.join(r['urls']) or '—'} | {r['ask']} |")
    AUDIT_OUT.write_text("\n".join(L) + "\n", encoding="utf-8")
    return len(rows)


def mid(i):
    return "n" + i.replace("-", "_")


def label(n, year=True):
    y = n["year"]
    ys = "" if not year or y is None else (f" ({-y}BC)" if y < 0 else f" ({y})")
    return f'{mid(n["id"])}["{n["id"]} {n["name"]}{ys} {n["st"]}"]'


def mermaid(nodes, ids, edges_within=True):
    out = ["```mermaid", "graph LR"]
    s = set(ids)
    for i in ids:
        out.append("  " + label(nodes[i]))
    for i in ids:
        for p in nodes[i]["pre"]:
            if p in s or not edges_within:
                if p in nodes:
                    out.append(f"  {mid(p)}-->{mid(i)}")
    out.append("```")
    return out


def render(nodes, dups):
    undefined, cycles, empty = check(nodes)
    L = ["# TECH_TREE_VIEW.md（自動生成：tools/gen_tree_views.py。手で編集しない）", ""]
    # 1) ダッシュボード
    L += ["## 1. 状態ダッシュボード", "", "| 幹 | 名前 | ✅ | 🔧 | ❓ | ⛔ | 計 | 進捗 |", "|---|---|---|---|---|---|---|---|"]
    by = defaultdict(list)
    for n in nodes.values():
        by[trunk_of(n["id"])].append(n)
    tot = defaultdict(int)
    for t in TRUNK_NAMES:
        ns = by.get(t, [])
        c = {s: sum(1 for n in ns if n["st"] == s) for s in "✅🔧❓⛔"}
        for k, v in c.items():
            tot[k] += v
        pct = f"{100 * c['✅'] // len(ns)}%" if ns else "—"
        L.append(f"| {t} | {TRUNK_NAMES[t]} | {c['✅']} | {c['🔧']} | {c['❓']} | {c['⛔']} | {len(ns)} | {pct} |")
    allc = len(nodes)
    L.append(f"| 計 | | {tot['✅']} | {tot['🔧']} | {tot['❓']} | {tot['⛔']} | {allc} | {100 * tot['✅'] // max(allc, 1)}% |")
    bl = buildable(nodes)
    L += ["", f"**今すぐ作れる（前提が全部✅の❓）: {len(bl)} 個**", ""]
    for t in TRUNK_NAMES:
        xs = [i for i in bl if trunk_of(i) == t]
        if xs:
            L.append(f"- 幹{t} {TRUNK_NAMES[t]}: " + "、".join(f"[{i}] {nodes[i]['name']}" for i in xs))
    # 2) 背骨
    L += ["", "## 2. 背骨（石器→EUV→AI）", ""]
    sc = shortest_chain(nodes, START, GOAL)
    if sc:
        L.append(f"最短の鎖（[{START}] から [{GOAL}] AI まで {len(sc)} ノード）")
        L += mermaid_chain(nodes, sc)
    else:
        L.append(f"⚠️ [{START}] から [{GOAL}] へ前提をたどる鎖が見つからない（背骨が切れている）")
    cc = critical_chain(nodes, GOAL)
    L += ["", f"律速の鎖（[{GOAL}] から最も深い前提をたどる、{len(cc)} ノード）"]
    L += mermaid_chain(nodes, cc)
    # 3) 幹ごとの依存図
    L += ["", "## 3. 幹ごとの依存図（幹内の辺のみ。31ノード以上は次数の上位30）", ""]
    for t in TRUNK_NAMES:
        ns = sorted((n["id"] for n in by.get(t, [])), key=sort_key)
        if not ns:
            continue
        if len(ns) > 30:
            deg = defaultdict(int)
            for i in ns:
                for p in nodes[i]["pre"]:
                    if p in ns:
                        deg[i] += 1
                        deg[p] += 1
            ns = sorted(sorted(ns, key=lambda i: -deg[i])[:30], key=sort_key)
        L += [f"### 幹{t} {TRUNK_NAMES[t]}", ""] + mermaid(nodes, ns) + [""]
    # 4) 年表マトリクス
    L += ["## 4. 年表マトリクス（本物の欄の最初の年。年なしは除外）", "",
          "| 幹 | " + " | ".join(e[0] for e in ERAS) + " |", "|---|" + "---|" * len(ERAS)]
    for t in TRUNK_NAMES:
        cells = []
        for _, lo, hi in ERAS:
            xs = [n["id"] for n in by.get(t, []) if n["year"] is not None and lo <= n["year"] <= hi]
            cells.append(" ".join(sorted(xs, key=sort_key)) or "")
        L.append(f"| {t} {TRUNK_NAMES[t]} | " + " | ".join(cells) + " |")
    # 5) AI（EUV の先）までの残り
    anc = ancestors(nodes, GOALS)
    todo = [u for u in topo(nodes, anc) if nodes[u]["st"] in ("❓", "⛔", "🔧")]
    L += ["", f"## 5. AI までの残り ToDo（{' '.join('['+g+']' for g in GOALS)} の祖先のうち未完成、トポロジカル順）", "",
          f"{len(todo)} / {len(anc)} ノードが未完成", ""]
    for k, u in enumerate(todo, 1):
        n = nodes[u]
        L.append(f"{k}. [{u}] {n['name']} {n['st']}")
    # 6) 自動検査
    L += ["", "## 6. 自動検査", ""]
    L.append(f"- 未定義の前提ID: {len(undefined)} " + " ".join(f"[{k}]←{','.join(v)}" for k, v in sorted(undefined.items(), key=lambda kv: sort_key(kv[0]))))
    L.append(f"- 循環: {len(cycles)} " + " / ".join("→".join(c) for c in cycles[:10]))
    L.append(f"- ノード0個の幹: {', '.join(empty) or 'なし'}")
    L.append(f"- 重複定義: {', '.join(dups) or 'なし'}")
    L.append(f"- 背骨 [{START}]→[{GOAL}]: {'つながっている' if sc else '切れている'}")
    guessed = sorted((n["id"] for n in nodes.values() if n["year_src"] != "field"), key=sort_key)
    L.append(f"- 「年:」の項目: {len(nodes) - len(guessed)} / {len(nodes)}（⚠️ {sum(1 for n in nodes.values() if n['year_uncertain'] and n['year_src'] == 'field')}）" + (f"。項目なし: {' '.join(guessed)}" if guessed else ""))
    inv = year_inversions(nodes)
    L += ["", f"### 年の逆転（ノードの年が、必ず要る前提の年より古い：{len(inv)} 件。直さずに報告だけ）", "",
          "| ノード | 年 | 前提（いちばん新しい） | 前提の年 | 差 |", "|---|---|---|---|---|"]
    ex = explained_inversions()
    for a, y, p, py, d in inv:
        L.append(f"| [{a}] {nodes[a]['name']} | {y} | [{p}] {nodes[p]['name']} | {py} | {d}" + (f"（説明ずみ {ex[(a, p)]}）" if (a, p) in ex else "") + " |")
    return L, (undefined, cycles, empty, dups, sc, bl, todo)


def mermaid_chain(nodes, chain):
    out = ["```mermaid", "graph LR"]
    for i in chain:
        out.append("  " + label(nodes[i]))
    for a, b in zip(chain, chain[1:]):
        out.append(f"  {mid(a)}-->{mid(b)}")
    out.append("```")
    return out


def dedupe_names(nodes):
    """S-015：図に同じ名前が2回出ないように。同じ名前のノードには、見出しのかっこ書き（例「高炉（銑鉄）」）を付けて見分ける。
    かっこ書きの無い物どうしが同じ名前なら warning を出す（products.md などの名前を直す）"""
    by = {}
    for i, n in nodes.items():
        by.setdefault(n["name"], []).append(i)
    for name, ids in by.items():
        if len(ids) < 2:
            continue
        for i in ids:
            if nodes[i].get("qual"):
                nodes[i]["name"] = f"{name}（{nodes[i]['qual']}）"
    left = {}
    for i, n in nodes.items():
        left.setdefault(n["name"], []).append(i)
    for name, ids in left.items():
        if len(ids) > 1:
            print(f"warning: 同じ名前のノード {name} {ids}（かっこ書きで見分けられない。名前を直す）")


def main():
    nodes, dups = parse()
    L, (undefined, cycles, empty, dups, sc, bl, todo) = render(nodes, dups)
    if "--check" in sys.argv:
        print(f"nodes={len(nodes)} undefined={len(undefined)} cycles={len(cycles)} empty={empty} dups={dups}")
        for k, v in sorted(undefined.items(), key=lambda kv: sort_key(kv[0])):
            print(f"  undefined [{k}] <- {v}")
        for c in cycles:
            print("  cycle", "->".join(c))
        print("spine:", " > ".join(sc) if sc else "CUT")
        print("buildable:", bl)
        print("todo:", len(todo))
        inv = year_inversions(nodes)
        ex = explained_inversions()
        print(f"year inversions: {len(inv)}（うち説明ずみ {sum(1 for r in inv if (r[0], r[2]) in ex)}）")
        for a, y, p, py, d in inv:
            print(f"  [{a}] {y} < [{p}] {py} (gap {d})" + (f"  説明ずみ {ex[(a, p)]}" if (a, p) in ex else ""))
        allnodes = dict(nodes)
        for src in EXTRA_SRC:
            allnodes.update({k: v for k, v in parse(src)[0].items() if k not in allnodes})
        bad, same = link_check(allnodes)
        print(f"bad links: {len(bad)}")
        for a, f, i in bad:
            print(f"  [{a}] {f} -> [{i}] がない")
        badurl = [(i, u) for i, n in allnodes.items() for u in re.findall(r"https?://[^\s／]+", n["body"])
                  if re.search(r"[（）、。「」]|[^\x00-\x7f%]", u.rstrip(",.）)")) or u.endswith(",")]
        noicon = [i for i in sorted(allnodes, key=sort_key) if i not in ICON_E]
        print(f"no production icon: {len(noicon)} {' '.join(noicon)}（tools/gen_icons.py：docs/icon_glyphs.json に形を足す）")
        print(f"odd urls: {len(badurl)}")
        for i, u in badurl:
            print(f"  [{i}] {u}")
        print(f"same-name candidates: {len(same)}")
        for a, b in same:
            print(f"  [{a}] {allnodes[a]['name']} ／ [{b}] {allnodes[b]['name']}")
        return
    OUT.write_text("\n".join(L) + "\n", encoding="utf-8")
    # products.md・ideas.md のノードはネットワーク図にだけ合流させる（TECH_TREE_VIEW.md には入れない）
    for src in EXTRA_SRC:
        extra, edup = parse(src)
        clash = [i for i in extra if i in nodes] + edup
        if clash:
            print(f"warning: {src.name} の重複 {clash}")
        nodes.update({k: v for k, v in extra.items() if k not in nodes})
    dedupe_names(nodes)
    data = export_json(nodes)
    print(f"wrote {AUDIT_OUT.name} ({write_audit_queue()} items)")
    print(f"wrote {OUT} ({len(nodes)} nodes), {JSON_OUT.name} ({len(data['nodes'])} nodes, {len(data['edges'])} edges, {len(data['year_inversions'])} year inversions)")


if __name__ == "__main__":
    main()

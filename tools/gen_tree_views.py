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
書き出し：docs/TECH_TREE_VIEW.md ／ docs/tech_network.json（技術ネットワーク図の元データ：ノードとつながり）
"""
import base64
import json
import re
import sys
from collections import defaultdict, deque
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "docs" / "TECH_TREE.md"
OUT = ROOT / "docs" / "TECH_TREE_VIEW.md"
JSON_OUT = ROOT / "docs" / "tech_network.json"
JS_OUT = ROOT / "docs" / "tech_network.js"
META = ROOT / "docs" / "network_meta.json"
TEX = ROOT / "src" / "main" / "resources" / "assets" / "semiconductor-lab" / "textures"
YEAR_RE = re.compile(r"(?:^|／|\s)年:\s*(-?\d+)(?:（([^）]*)）)?(\s*⚠️)?")

ID_RE = re.compile(r"\[(\d+|B)-(\d+[a-z]?)\]")
HEAD_RE = re.compile(r"^###\s+\[(\d+|B)-(\d+[a-z]?)\]\s*(.*)$")
START, GOAL = "0-01", "10-16"   # 最終目的は AI（DESIGN「MOD の最終目的」、2026-10-20）。EUV [9-40] はその手前の最後の機械
GOALS = [GOAL]
TRUNK_NAMES = {
    "0": "生存・道具", "1": "冶金", "2": "化学", "3": "熱", "4": "動力・電気",
    "5": "真空", "6": "ガス", "7": "光学", "8": "精密・計測", "9": "半導体プロセス",
    "10": "制御・計算", "11": "農業・食", "12": "建築・土木", "13": "医薬・衛生",
    "14": "輸送", "15": "通信・情報", "B": "枝",
}
ERAS = [("〜BC", -10**9, 0), ("1〜1500", 1, 1500), ("1500〜1800", 1501, 1800),
        ("1800〜1900", 1801, 1900), ("1900〜1950", 1901, 1950), ("1950〜", 1951, 10**9)]


def nid(m):
    return f"{m[0]}-{m[1]}"


def trunk_of(i):
    return i.split("-")[0]


def sort_key(i):
    t, n = i.split("-")
    num = int(re.match(r"\d+", n).group())
    return (999 if t == "B" else int(t), num, n)


def parse():
    lines = SRC.read_text(encoding="utf-8").splitlines()
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
    def field(body, pat):
        m = re.search(pat + r"[:：]\s*(.*?)(?:／|\n|$)", body, re.S)
        return re.sub(r"\*\*|`", "", m.group(1)).strip()[:400] if m else ""
    now = set(buildable(nodes))
    # 見た目の情報（アイコン・モノの絵文字・人物）。docs/network_meta.json を手で編集する
    meta = json.loads(META.read_text(encoding="utf-8")) if META.exists() else {}
    icons, people = meta.get("icons", {}), meta.get("people", {})
    emoji, symbols = meta.get("products", {}), meta.get("trunk_symbols", {})
    def icon_uri(rel):
        p = TEX / (rel + ".png")
        return "data:image/png;base64," + base64.b64encode(p.read_bytes()).decode() if p.exists() else None
    out_nodes, edges = [], []
    for i in sorted(nodes, key=sort_key):
        n = nodes[i]
        out_nodes.append({"id": i, "name": n["name"], "trunk": trunk_of(i), "trunk_name": TRUNK_NAMES.get(trunk_of(i), ""),
                          "status": n["st"], "branch": n["branch"], "year": n["year"], "year_range": n["year_range"],
                          "year_uncertain": n["year_uncertain"], "requires": [[a for a in alts] for alts in n["groups"]],
                          "understanding": n["understand"], "buildable_now": i in now,
                          "real": field(n["body"], r"本物"), "hee1": field(n["body"], r"へえ①?"),
                          "kind": "product" if re.search(r"種類[:：]\s*モノ", n["body"]) else "tech",
                          "icon": icon_uri(icons[i]) if i in icons else None, "emoji": emoji.get(i),
                          "symbol": symbols.get(trunk_of(i), "●"), "people": people.get(i, []),
                          "descendants": len(descendants(i))})
        for gi, alts in enumerate(n["groups"]):
            for a in alts:
                for p in a:
                    if p in nodes:
                        edges.append({"from": p, "to": i, "kind": "requires", "group": gi, "or": len(alts) > 1})
        for p in n["understand"]:
            if p in nodes:
                edges.append({"from": p, "to": i, "kind": "understanding"})
    data = {"generated_by": "tools/gen_tree_views.py", "source": "docs/TECH_TREE.md", "start": START, "goal": GOAL,
            "year_note": "西暦。紀元前はマイナス。year_uncertain=true は諸説・未確認（⚠️）",
            "requires_note": "requires はノードを作るのに要る項目の並び（全部そろう＝AND）。1項目の中の複数案は OR、案の中は AND",
            "edge_kinds": {"requires": "前提（無いと作れない）", "understanding": "理解（後で分かった科学。作るのには要らない）"},
            "nodes": out_nodes, "edges": edges, "year_inversions": [
                {"node": a, "year": y, "requires": p, "requires_year": py, "gap": d} for a, y, p, py, d in year_inversions(nodes)]}
    JSON_OUT.write_text(json.dumps(data, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    # docs/network.html はファイルを直接開いても読めるように、同じ中身を JS としても書く（file:// では fetch できないため）
    JS_OUT.write_text("// 自動生成：tools/gen_tree_views.py（tech_network.json と同じ中身）\nwindow.TECH_NETWORK = "
                      + json.dumps(data, ensure_ascii=False) + ";\n", encoding="utf-8")
    return data


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
    for a, y, p, py, d in inv:
        L.append(f"| [{a}] {nodes[a]['name']} | {y} | [{p}] {nodes[p]['name']} | {py} | {d} |")
    return L, (undefined, cycles, empty, dups, sc, bl, todo)


def mermaid_chain(nodes, chain):
    out = ["```mermaid", "graph LR"]
    for i in chain:
        out.append("  " + label(nodes[i]))
    for a, b in zip(chain, chain[1:]):
        out.append(f"  {mid(a)}-->{mid(b)}")
    out.append("```")
    return out


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
        print(f"year inversions: {len(inv)}")
        for a, y, p, py, d in inv:
            print(f"  [{a}] {y} < [{p}] {py} (gap {d})")
        return
    OUT.write_text("\n".join(L) + "\n", encoding="utf-8")
    data = export_json(nodes)
    print(f"wrote {OUT} ({len(nodes)} nodes), {JSON_OUT.name} ({len(data['nodes'])} nodes, {len(data['edges'])} edges, {len(data['year_inversions'])} year inversions)")


if __name__ == "__main__":
    main()

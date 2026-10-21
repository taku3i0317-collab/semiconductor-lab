"""進捗（アドバンスメント）の全章の見直し（O-038、v1.0 #7）。ゲームなしで動く → docs/tasks/ADVANCEMENT_REVIEW.md（手で直さない）
調べること：
① つながりの抜け：parent の進捗が無い・木の根が2つ以上（ヒントの hint/ は隠しの根なので除く）
② 重なり：同じアイテムで開く進捗が、ちがう木に2つ以上（図鑑 zukan/・レシピ recipes/ は全部のアイテムにあるので除く）
③ 開く順：ガイドの巻の順（第1巻 → 第8巻）に、開く物の年（creative_tab_order.txt の年）の中央値が並んでいるか。ROADMAP の版の順と同じ
④ 開く物の入手：ガイド・章の進捗を開くアイテムに、入手の道（assets/howto_get.json、tools/check_howto.py）があるか
使い方：python tools/check_advancements.py（--strict で ① ④ が 0 でなければ終了コード 1）
"""
import json, pathlib, re, statistics, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
NS = "semiconductor-lab"
ADV = RES / f"data/{NS}/advancement"
SKIP_OVERLAP = ("zukan", "recipes")


def load(p):
    return json.loads(p.read_text(encoding="utf-8"))


def main():
    advs = {}
    for p in ADV.rglob("*.json"):
        rel = p.relative_to(ADV).as_posix()[:-5]
        advs[f"{NS}:{rel}"] = load(p)
    years = {}
    for line in (RES / f"assets/{NS}/creative_tab_order.txt").read_text(encoding="utf-8").splitlines():
        m = re.match(r"([a-z0-9_]+)\s+#\s+\S+\s+\[\S*\]\s+(-?\d+)", line)
        if m:
            years[m.group(1)] = int(m.group(2))
    ways = load(RES / f"assets/{NS}/howto_get.json")["ways"] if (RES / f"assets/{NS}/howto_get.json").exists() else {}

    broken, roots = [], {}
    for k, a in advs.items():
        tree = k.split(":")[1].split("/")[0]
        par = a.get("parent")
        if par is None:
            if tree not in ("hint",) and "display" in a:
                roots.setdefault(tree, []).append(k)
        elif par not in advs and not par.startswith("minecraft:"):
            broken.append(f"{k} → {par}")
    multi_root = {t: r for t, r in roots.items() if len(r) > 1 and t not in ("guide",)}   # guide は root と welcome（入った時）の2つで正しい

    def items_of(a):
        return set(re.findall(rf'"items":\s*"{NS}:([a-z0-9_]+)"', json.dumps(a)))
    by_item = {}
    for k, a in advs.items():
        tree = k.split(":")[1].split("/")[0]
        if tree in SKIP_OVERLAP:
            continue
        for i in items_of(a):
            by_item.setdefault(i, set()).add(k)
    overlap = {i: sorted(ks) for i, ks in by_item.items() if len({x.split(":")[1].split("/")[0] for x in ks}) > 1}

    vols = sorted([t for t in roots if re.fullmatch(r"guide\d*", t)], key=lambda t: int(t[5:] or 1))
    order = []
    for t in vols:
        ys = [years[i] for k, a in advs.items() if k.split(":")[1].startswith(t + "/") for i in items_of(a) if i in years]
        order.append((t, statistics.median(ys) if ys else None, len(ys)))
    out_of_order = [f"{order[i - 1][0]}（{order[i - 1][1]}）→ {order[i][0]}（{order[i][1]}）" for i in range(1, len(order))
                    if order[i][1] is not None and order[i - 1][1] is not None and order[i][1] < order[i - 1][1]]

    no_way = sorted({i for k, a in advs.items() if k.split(":")[1].split("/")[0] not in SKIP_OVERLAP for i in items_of(a) if i not in ways})

    L = ["# 進捗の見直し（自動生成：python tools/check_advancements.py、O-038。手で直さない）", "",
         f"- 進捗の数：{len(advs)}（図鑑 {sum(1 for k in advs if '/zukan/' in k or ':zukan/' in k)}・レシピの解放 {sum(1 for k in advs if ':recipes/' in k)}）",
         f"- ① つながりの抜け：**{len(broken)}**、根が2つ以上の木：**{len(multi_root)}**",
         f"- ② ちがう木で同じアイテムで開く進捗：**{len(overlap)}**（同じ物でガイドとお題が開くのは、わざと＝読む所と遊ぶ所。下の一覧で確かめる）",
         f"- ③ ガイドの巻の順と年の順のずれ：**{len(out_of_order)}**",
         f"- ④ 開く物に入手の道が無い：**{len(no_way)}**", "",
         "## ③ ガイドの巻（開く物の年の中央値）", ""]
    L += [f"- {t}：{y}（{n} 個）" for t, y, n in order]
    L += ["", "## ① つながりの抜け", ""] + ([f"- {b}" for b in broken] or ["- なし"])
    L += [f"- 根が2つ以上：{t} {r}" for t, r in multi_root.items()]
    L += ["", "## ② 重なり", ""] + ([f"- {i}：{', '.join(k.split(':')[1] for k in ks)}" for i, ks in sorted(overlap.items())] or ["- なし"])
    L += ["", "## ③ 順のずれ", ""] + ([f"- {x}" for x in out_of_order] or ["- なし"])
    L += ["", "## ④ 入手の道が無い開く物", ""] + ([f"- {i}" for i in no_way] or ["- なし"])
    (ROOT / "docs/tasks/ADVANCEMENT_REVIEW.md").write_text("\n".join(L) + "\n", encoding="utf-8")
    print(f"advancements {len(advs)}: broken {len(broken)}, multi-root {len(multi_root)}, overlap {len(overlap)}, out of order {len(out_of_order)}, no way {len(no_way)}")
    if "--strict" in sys.argv and (broken or no_way):
        sys.exit(1)


if __name__ == "__main__":
    main()

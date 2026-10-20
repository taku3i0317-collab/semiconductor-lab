#!/usr/bin/env python3
"""専用のクリエイティブタブ「Semiconductor Lab」の並び順を作る（IMP-31）。

並び：技術ネットワークの順＝幹の順（content/Content.java の順：幹0 → … → 幹9 → 幹15 → 枝🌿）、
同じ幹の中はノードの年の順（年が同じなら content/<幹>.java に書いた順）。
アイテムのノードは ①docs/tech_network.json の "item" ②TECH_TREE.md の ✅ ノードの本文にその ID がある ③下の NODE（手で決めたもの）の順に探す。
年は docs/tech_network.json（ネットワーク担当が TECH_TREE から作る）の "year"。

書き出す先：src/main/resources/assets/semiconductor-lab/creative_tab_order.txt（1行1つ。# の後ろは説明）。
アイテムを足したら実行する（GameTest creative_tab が、表に無いアイテムを見つけると失敗する）。
使い方:  python tools/gen_tab_order.py
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTENT = ROOT / "src/main/java/com/taku3/semiconductorlab/content"
OUT = ROOT / "src/main/resources/assets/semiconductor-lab/creative_tab_order.txt"

# ①② で見つからない・見つかったノードが違うものの、手で決めたノード
NODE = {
    # 幹1 冶金
    "magnetite": "1-47", "magnetite_ore": "1-47", "iron_sand": "1-47",
    "galena": "1-27", "galena_ore": "1-27",
    "sphalerite": "1-13", "sphalerite_ore": "1-13", "graphite_ore": "1-07",
    "germanium_dioxide": "1-01", "raw_germanium": "1-01",
    "crude_germanium_tetrachloride": "1-34", "high_purity_germanium_dioxide": "1-34", "quartz_still": "1-34",
    "wolframite": "1-28", "wolframite_ore": "1-28", "wolframite_soda_charge": "1-28", "sodium_tungstate": "1-28",
    "tungstic_acid": "1-28", "tungsten_trioxide": "1-28", "tungsten_powder": "1-28", "tungsten_bar": "1-28",
    "borax": "1-33", "indium": "9-02", "bessemer_converter": "1-26",
    # 幹2 化学
    "sulfur_powder": "2-01", "sulfur_dioxide": "2-01", "wood_vinegar": "2-23", "wood_tar": "2-23", "pitch": "2-23",
    # 幹3 熱
    "zinc_oxide": "3-06",
    # 幹4 電気
    "induction_coil": "4-09",
    # 幹5 真空
    "vacuum_tube_assembly": "5-02", "sealing_station": "5-02",
    # 幹6 ガス
    "empty_gas_cylinder": "6-03", "hydrogen_cylinder": "6-03", "oxygen_cylinder": "6-03", "chlorine_cylinder": "6-05",
    "nitrogen_gas": "6-07", "argon_gas": "6-07", "nitrogen_cylinder": "6-07", "argon_cylinder": "6-07", "sodium_hydroxide": "6-05",
    # 幹7 光学
    "quartz_tube": "7-02", "flint_glass_batch": "7-01",
    # 幹9 半導体
    "zone_refining_furnace": "9-01", "graphite_boat_blank": "9-01", "graphite_boat": "9-01", "synthetic_graphite_boat": "9-01",
    "p_type_germanium": "9-02", "germanium_seed_crystal": "9-07", "silicon_seed_crystal": "9-08", "junction_transistor_blank": "9-04",
    # 容器（IMP-33）：中身のノードの年に並べる
    "glass_bottle": "2-14", "iron_flask": "1-45", "cinnabar_retort_charge": "1-45", "lead_bottle": "1-27",
}


def trunk_order():
    s = (CONTENT / "Content.java").read_text(encoding="utf-8")
    return re.findall(r"^\s+(\w+)\.init\(\);", s, re.M)


def main():
    net = json.loads((ROOT / "docs/tech_network.json").read_text(encoding="utf-8"))
    nodes = {n["id"]: n for n in net["nodes"]}
    by_item = {n["item"]: n["id"] for n in net["nodes"] if n["item"]}
    tt = (ROOT / "docs/TECH_TREE.md").read_text(encoding="utf-8")
    bodies, cur = {}, None
    for line in tt.splitlines():
        m = re.match(r"^#{2,4} \[([0-9A-Z]+-\d+)\]", line)
        if m:
            cur = m.group(1)
            bodies[cur] = ""
        if cur:
            bodies[cur] += line + "\n"
    rows, missing = [], []
    for ti, trunk in enumerate(trunk_order()):
        src = (CONTENT / f"{trunk}.java").read_text(encoding="utf-8")
        for k, m in enumerate(re.finditer(r'public static final [\w<>]+ \w+\s*=\s*[\w.]+\("([a-z0-9_]+)"', src)):
            iid = m.group(1)
            node = NODE.get(iid) or by_item.get(iid)
            if not node:
                hits = [n for n, b in bodies.items() if "✅" in b.splitlines()[0] and re.search(rf"\b{iid}\b", b)]
                node = hits[0] if hits else None
            year = nodes[node]["year"] if node in nodes and nodes[node].get("year") is not None else None
            if year is None:
                missing.append(iid)
            rows.append((ti, year if year is not None else 10 ** 9, k, iid, trunk, node, year))
    rows.sort()
    lines = ["# 自動生成：python tools/gen_tab_order.py（IMP-31）。クリエイティブタブ「Semiconductor Lab」の並び（幹の順 → 年の順）。手で直さない",
             "# 形：ID  # 幹 ノード 年"]
    lines += [f"{iid}  # {trunk} [{node}] {year}" for _, _, _, iid, trunk, node, year in rows]
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{len(rows)} entries -> {OUT.relative_to(ROOT)}")
    if missing:
        print("年がわからない（幹の最後に並ぶ）:", ", ".join(missing))


if __name__ == "__main__":
    main()

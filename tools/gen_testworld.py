#!/usr/bin/env python3
"""試験場（S-001、IMP3）：ゲーム内でまとめて確かめるための建物を、データパックの関数として自動で作る。

使い方:  python tools/gen_testworld.py        （機械やアイテムを足したら実行し直す。CLAUDE.md の手順）
ゲームでは: 新しいワールド（クリエイティブ・コマンド有効）→ 平らな所で /function semiconductor-lab:testworld

読む物:
  src/main/resources/assets/semiconductor-lab/creative_tab_order.txt … どのアイテムがどの幹・どのノードか（区画の並び）
  docs/machine_recipes.json … 機械と材料・電力（電力が正の機械は、真下に「試験用の無限電源」を置く）
  docs/test_queue/<幹>.md   … ノードごとの「人間：」の最初の項目（看板に短く、全文は docs/TESTWORLD.md）
  docs/TECH_TREE.md         … ノードの題
  content/*.java            … アイテムの最大の重ね数（stacksTo・斧・剣）。blockstates があればブロック（作物は除く）
作る物:
  src/main/resources/data/semiconductor-lab/function/testworld.mcfunction
  src/gametest/resources/testworld_layout.json（GameTest が、置いた物が正しいかを確かめる）
  docs/TESTWORLD.md の「## 区画の一覧（自動生成）」から下
並べ方: 幹ごとに1列（南へ ROW_STEP ずつ）、ノードごとに1区画（東へ）。区画＝ブロック（機械）＋チェスト＋看板（区画の北側、北向き）。
最後の列は本物の発電の区画（蒸気機関 → 銅ケーブル → 電気炉）。
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NS = "semiconductor-lab"
RES = ROOT / "src/main/resources"
TAB = RES / f"assets/{NS}/creative_tab_order.txt"
BLOCKSTATES = RES / f"assets/{NS}/blockstates"
CONTENT = ROOT / "src/main/java/com/taku3/semiconductorlab/content"
FUNC = RES / f"data/{NS}/function/testworld.mcfunction"
LAYOUT = ROOT / "src/gametest/resources/testworld_layout.json"
DOC = ROOT / "docs/TESTWORLD.md"
DOC_MARK = "## 区画の一覧（自動生成）"

TRUNK_NAME = {"Survival": "幹0 生存（石器時代）", "Metallurgy": "幹1 冶金", "Chemistry": "幹2 化学", "Heat": "幹3 熱",
              "Power": "幹4 電気・動力", "Vacuum": "幹5 真空", "Gas": "幹6 ガス・電解", "Optics": "幹7 光学",
              "Precision": "幹8 精密", "Semiconductor": "幹9 半導体（Ge・Si）", "Agriculture": "幹11 農業・食品", "Construction": "幹12 建設・測量", "Communication": "幹15 通信",
              "Branches": "枝 製品"}
ROW_STEP = 8          # 列と列の間（南へ）
GAP = 2               # 区画と区画の間
SIGN_CHARS = 11       # 看板1行の文字数（日本語）
POWER = f"{NS}:test_power_source"
NODE_RE = r"[0-9A-Z]+-[0-9]+"


def snbt(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def max_stack():
    """MOD アイテムの最大の重ね数（わからなければ 64）"""
    m = {}
    for f in CONTENT.glob("*.java"):
        for line in f.read_text(encoding="utf-8").splitlines():
            mm = re.search(r'Reg\.item\("([a-z0-9_]+)"(.*)', line)
            if not mm:
                continue
            rest = mm.group(2)
            n = 64
            if any(k in rest for k in (".axe(", ".sword(", ".pickaxe(", "durability(")):
                n = 1
            st = re.search(r"stacksTo\((\d+)\)", rest)
            if st:
                n = int(st.group(1))
            m[mm.group(1)] = n
    return m


def count_for(item, stacks, want):
    ns, path = item.split(":", 1)
    if ns == "minecraft":
        return 1 if path.endswith("_bucket") else min(want, 64)
    return min(want, stacks.get(path, 64))


def node_titles():
    t = {}
    for line in (ROOT / "docs/TECH_TREE.md").read_text(encoding="utf-8").splitlines():
        mm = re.match(rf"### \[({NODE_RE})\] (.+)", line)
        if mm:
            t.setdefault(mm.group(1), re.split(r"\s+[✅❓⛔🔧]|\s{2}|（", mm.group(2))[0].strip())
    return t


def human_checks():
    """ノード → 「人間：」の最初の項目（節の題にノードが2つあれば両方に）"""
    h = {}
    for f in sorted((ROOT / "docs/test_queue").glob("*.md")):
        node_ids, done = [], False
        for line in f.read_text(encoding="utf-8").splitlines():
            if line.startswith("## "):
                node_ids, done = re.findall(rf"\[({NODE_RE})\]", line), False
            elif not done and line.startswith("- [ ] 人間：") and node_ids:
                text = re.sub(r"\*\*|`", "", line[len("- [ ] 人間："):]).strip()
                for n in node_ids:
                    h.setdefault(n, text)
                done = True
    return h


def machines():
    d = json.loads((ROOT / "docs/machine_recipes.json").read_text(encoding="utf-8"))["recipes"]
    m = {}
    for r in d:
        mid = r["machine"].split(":", 1)[1]
        e = m.setdefault(mid, {"watts": 0.0, "inputs": []})
        e["watts"] = max(e["watts"], r.get("watts", 0))
        for i in r.get("inputs", []):
            it = i["item"]
            if ":" in it and it not in e["inputs"]:
                e["inputs"].append(it)
    return m


def is_block(bid):
    p = BLOCKSTATES / f"{bid}.json"
    return p.exists() and "age=" not in p.read_text(encoding="utf-8")   # 作物は種としてチェストへ


def tab_rows():
    """幹 → [(ノード, [ID...])]（タブの並び順）"""
    rows = {}
    for line in TAB.read_text(encoding="utf-8").splitlines():
        mm = re.match(r"(\S+)\s+#\s+(\S+)\s+(\S+)", line)
        if not mm or line.startswith("#"):
            continue
        iid, trunk, node = mm.group(1), mm.group(2), mm.group(3).strip("[]")
        if trunk not in TRUNK_NAME:
            continue
        nodes = rows.setdefault(trunk, {})        # 同じノードは離れて出てきても1区画にまとめる（最初に出た順）
        nodes.setdefault(node, []).append(iid)
    return {t: list(n.items()) for t, n in rows.items()}


def sign_lines(node, title, check):
    body = check or "（人間の確認項目なし）"
    return [f"[{node}] {title}"[:SIGN_CHARS + 3]] + [body[k * SIGN_CHARS:(k + 1) * SIGN_CHARS] for k in range(3)]


def main():
    stacks, titles, checks, mach = max_stack(), node_titles(), human_checks(), machines()
    cmds, layout, doc = [], [], []

    def put(x, y, z, block, nbt=""):
        cmds.append(f"setblock ~{x} ~{y} ~{z} {block}{nbt}")
        layout.append({"pos": [x, y, z], "block": block.split("[")[0]})

    def chest(x, z, items):
        parts = [f'{{Slot:{s}b,id:"{it}",count:{n}}}' for s, (it, n) in enumerate(items[:27])]
        put(x, 0, z, "minecraft:chest[facing=north]", "{Items:[" + ",".join(parts) + "]}")

    def sign(x, z, lines):
        put(x, 0, z, "minecraft:oak_sign[rotation=8]", "{front_text:{messages:[" + ",".join(snbt(l) for l in lines) + "]}}")

    width, z = 10, 2
    for trunk, nodes in tab_rows().items():
        name = TRUNK_NAME[trunk]
        sign(0, z - 1, [name[:SIGN_CHARS + 3], "↓ この列", "", ""])
        doc.append(f"\n### {name}（南へ {z} ブロックの列）\n")
        x = 2
        for node, ids in nodes:
            blocks = [i for i in ids if is_block(i)]
            items = [f"{NS}:{i}" for i in ids if i not in blocks]
            for b in blocks:
                for it in mach.get(b, {}).get("inputs", []):
                    if it not in items:
                        items.append(it)
            key = node if re.fullmatch(NODE_RE, node) else "-"
            title, check = titles.get(key, ids[0]), checks.get(key, "")
            sign(x, z - 1, sign_lines(key, title, check))
            for k, b in enumerate(blocks):
                if mach.get(b, {}).get("watts", 0) > 0:
                    put(x + k, -1, z, POWER)
                put(x + k, 0, z, f"{NS}:{b}")
            cx = x + len(blocks)
            if items:
                chest(cx, z, [(it, count_for(it, stacks, 16)) for it in items])
            doc.append(f"- [{key}] {title}：ブロック {', '.join(blocks) or 'なし'}／チェスト {len(items)} 種" +
                       (f"\n  - 見ること：{check}" if check else ""))
            x = cx + 1 + GAP
        width = max(width, x)
        z += ROW_STEP

    # 本物の発電：蒸気機関 → 銅ケーブル → 電気炉（試験用の電源は使わない）
    sz = z
    sign(0, sz - 1, ["本物の発電", "↓ この列", "", ""])
    sign(2, sz - 1, ["蒸気機関で発電", "石炭と水バケツ", "を入れると", "電気炉が動く"])
    put(2, 0, sz, f"{NS}:steam_engine")
    chest(2, sz + 1, [("minecraft:coal", 32)] + [("minecraft:water_bucket", 1)] * 4)
    for cx in (3, 4, 5):
        put(cx, 0, sz, f"{NS}:copper_cable")
    put(6, 0, sz, f"{NS}:electric_furnace")
    chest(7, sz, [(f"{NS}:refractory_brick_blank", 16)])
    doc.append(f"\n### 本物の発電（南へ {sz} ブロックの列）\n- 蒸気機関（石炭＋水バケツ）→ 銅ケーブル → 電気炉（耐火れんがの成形品を焼く）。試験用の電源は使わない")
    depth = sz + 3

    head = ["# 試験場（S-001、IMP3）：自動生成 python tools/gen_testworld.py。手で直さない",
            "# 使い方：新しいワールド（クリエイティブ・コマンド有効）の平らな所で /function semiconductor-lab:testworld",
            f"# 大きさ：東へ {width}・南へ {depth} ブロック（足元から）。機械の真下のピンクと黒は試験用の無限電源"]
    prep = []
    for z0 in range(-2, depth + 1, 8):   # fill は1回 32768 ブロックまでなので列ごとに
        z1 = min(z0 + 7, depth)
        prep.append(f"fill ~-1 ~-1 ~{z0} ~{width} ~-1 ~{z1} minecraft:smooth_stone")
        prep.append(f"fill ~-1 ~ ~{z0} ~{width} ~5 ~{z1} minecraft:air")
    tail = ['execute if entity @s run tellraw @s {"text":"試験場ができた：列ごとに幹、看板に「何を確かめるか」。全文は docs/TESTWORLD.md","color":"green"}']
    FUNC.parent.mkdir(parents=True, exist_ok=True)
    FUNC.write_text("\n".join(head + prep + cmds + tail) + "\n", encoding="utf-8")
    LAYOUT.parent.mkdir(parents=True, exist_ok=True)
    LAYOUT.write_text(json.dumps({"_note": "自動生成：tools/gen_testworld.py", "width": width, "depth": depth, "blocks": layout},
                                 ensure_ascii=False) + "\n", encoding="utf-8")
    text = DOC.read_text(encoding="utf-8") if DOC.exists() else "# 試験場\n"
    text = text.split(DOC_MARK)[0].rstrip("\n") + "\n\n" + DOC_MARK + "\n（tools/gen_testworld.py が作る。手で直さない）\n" + "\n".join(doc) + "\n"
    DOC.write_text(text, encoding="utf-8")
    n_power = sum(1 for b in layout if b["block"] == POWER)
    print(f"testworld: {len(cmds)} setblock, {n_power} powered machines, {width}x{depth} -> {FUNC.relative_to(ROOT)}")


if __name__ == "__main__":
    main()

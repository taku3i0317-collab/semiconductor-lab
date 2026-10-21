"""わかりやすさの自動の検査（S-026、DESIGN「入手・操作・組み立てを分かりやすく」）。ゲームなしで動く
①入手の道が無いアイテム：MOD のアイテム・ブロック（creative_tab_order.txt）のうち、どこからも出てこない物
   道＝作業台・かまどのレシピの出る物、機械・組立台のレシピの出る物（docs/machine_recipes.json＝tools/test.ps1 が最新に）、
   世界に生える物（worldgen/feature の中のブロックと、そのブロックのルートテーブル）、ルートテーブルの書き換え（content/*.java の LootTableEvents の中の名前）、
   そして例外の一覧 HOWTO_EXTRA（機械の中でコードが作る物など、レシピの JSON に出ない道。理由を書いて足す）
②「?」の説明が無い機械：Machine の登録（content/*.java）のうち lang に block.semiconductor-lab.<id>.howto が無い物
③見取り図の無いマルチブロック：docs/multiblocks.json の制御ブロックのうち、BE が Multiblock.Host を実装していない物
使い方：python tools/check_howto.py → docs/tasks/HOWTO_GAPS.md（担当＝幹ごとの一覧、手で直さない）。--strict で 0 でなければ終了コード 1
"""
import json, pathlib, re, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
NS = "semiconductor-lab"
CONTENT = ROOT / "src/main/java/com/taku3/semiconductorlab/content"
# 例外（レシピの JSON に出ない道）：ID → 道の説明
HOWTO_EXTRA = {
    "test_power_source": "テスト用（クリエイティブのみ）",
}
TRUNK_OWNER = {"Survival": "D", "Metallurgy": "D", "Chemistry": "D", "Heat": "D", "Power": "A", "Vacuum": "A", "Gas": "A", "Optics": "C",
               "Precision": "E", "Semiconductor": "A", "Control": "A", "Agriculture": "D", "Construction": "D", "Medicine": "D",
               "Transport": "E", "Communication": "A", "Branches": "A"}


def makers():
    """作った担当：アイテムの JSON（assets/.../items/<id>.json）を最初に足した commit の頭（IMP2＝C・IMP3＝D・IMP4＝E・NET＝B・ほか＝A）"""
    import subprocess
    out = subprocess.run(["git", "log", "--reverse", "--diff-filter=A", "--name-only", "--format=@%s", "--", f"src/main/resources/assets/{NS}/items/"],
                         cwd=ROOT, capture_output=True, encoding="utf-8").stdout
    who, cur = {}, "A"
    for line in out.splitlines():
        if line.startswith("@"):
            t = line[1:]
            cur = "C" if t.startswith("IMP2") else "D" if t.startswith("IMP3") else "E" if t.startswith("IMP4") else "B" if t.startswith("NET") else "A"
        elif line.endswith(".json"):
            who.setdefault(pathlib.Path(line).stem, cur)
    return who


def load(p):
    return json.loads(p.read_text(encoding="utf-8"))


def ids_in(obj):
    return set(re.findall(rf'"{NS}:([a-z0-9_]+)"', json.dumps(obj)))


def main():
    items = {}
    for line in (RES / f"assets/{NS}/creative_tab_order.txt").read_text(encoding="utf-8").splitlines():
        m = re.match(r"([a-z0-9_]+)\s+#\s+(\S+)", line)
        if m:
            items[m.group(1)] = m.group(2)
    got = set(HOWTO_EXTRA)
    for p in (RES / f"data/{NS}/recipe").glob("*.json"):
        r = load(p).get("result")
        rid = r.get("id") if isinstance(r, dict) else r
        if isinstance(rid, str) and rid.startswith(NS + ":"):
            got.add(rid.split(":")[1])
    mr = ROOT / "docs/machine_recipes.json"
    for r in load(mr).get("recipes", []) if mr.exists() else []:
        got |= ids_in(r.get("outputs", []))
        got |= ids_in(r.get("byproducts", []))
    loot = RES / f"data/{NS}/loot_table/blocks"
    for p in (RES / f"data/{NS}/worldgen/feature").glob("*.json"):
        for b in ids_in(load(p)):
            got.add(b)
            if (loot / f"{b}.json").exists():
                got |= ids_in(load(loot / f"{b}.json"))
    consts = {}
    srcs = {p.stem: p.read_text(encoding="utf-8") for p in CONTENT.glob("*.java")}
    for s in srcs.values():
        for m in re.finditer(r'public static final [\w<>.]+ (\w+)\s*=\s*[\w.]+\("([a-z0-9_]+)"', s):
            consts[m.group(1)] = m.group(2)
    for s in srcs.values():
        for m in re.finditer(r"LootTableEvents\.\w+\.register\(", s):
            body = s[m.end(): m.end() + 3000]
            for name in re.findall(r"\b([A-Z][A-Z0-9_]+)\b", body):
                if name in consts:
                    got.add(consts[name])
    no_way = [i for i in items if i not in got]

    lang = load(RES / f"assets/{NS}/lang/ja_jp.json")
    machines = []
    for trunk, s in srcs.items():
        for m in re.finditer(r'Machine\.(?:builder|electric|simple|fuel|of)\w*\("([a-z0-9_]+)"', s):
            machines.append((m.group(1), trunk))
    no_help = [(i, t) for i, t in machines if f"block.{NS}.{i}.howto" not in lang]

    mbp = ROOT / "docs/multiblocks.json"
    java = {p.stem: p.read_text(encoding="utf-8") for p in (ROOT / "src/main/java").rglob("*BlockEntity.java")}
    no_view = []
    for mb in load(mbp).get("multiblocks", []) if mbp.exists() else []:
        cid = mb.get("controller_block", "").split(":")[-1]
        reg = next((m for src in srcs.values() for m in re.finditer(rf'Machine\.\w+\("{cid}",\s*(\w+)::new', src)), None)
        cls = reg.group(1) if reg else ""
        host = "Multiblock.Host" in java.get(cls, "")
        if not host:
            no_view.append(cid)

    who = makers()
    by_owner = {}
    for i in no_way:
        by_owner.setdefault(who.get(i, TRUNK_OWNER.get(items[i], "?")), []).append(f"{i}（{items[i]}）")
    lines = ["# わかりやすさの穴（自動生成：python tools/check_howto.py、S-026。手で直さない）", "",
             f"- ①入手の道が無いアイテム：**{len(no_way)}** / {len(items)}",
             f"- ②「?」の説明（lang block.{NS}.<id>.howto）が無い機械：**{len(no_help)}** / {len(machines)}",
             f"- ③見取り図の無いマルチブロック：**{len(no_view)}**", "",
             "道の数え方：作業台・かまどのレシピ、機械・組立台のレシピ（docs/machine_recipes.json）、世界に生える鉱石とそのルート、ルートの書き換え（LootTableEvents）、"
             "例外の一覧（tools/check_howto.py の HOWTO_EXTRA）。ここに無いのに手に入る物は、HOWTO_EXTRA に理由を書いて足すか、machine_recipes の書き出しに足す", ""]
    lines.append("## ① 入手の道が無いアイテム（作った担当ごと：アイテムの JSON を最初に足した commit の頭 IMP2＝C・IMP3＝D・IMP4＝E・ほか＝A）")
    for o in sorted(by_owner):
        lines.append(f"- **{o}**（{len(by_owner[o])}）：" + "、".join(sorted(by_owner[o])))
    lines += ["", "## ② 「?」の説明が無い機械（作った担当ごと）"]
    hb = {}
    for i, t in no_help:
        hb.setdefault(who.get(i, TRUNK_OWNER.get(t, "?")), []).append(i)
    for o in sorted(hb):
        lines.append(f"- **{o}**（{len(hb[o])}）：" + "、".join(sorted(hb[o])))
    lines += ["", "## ③ 見取り図の無いマルチブロック", "- " + ("、".join(no_view) if no_view else "なし")]
    out = ROOT / "docs/tasks/HOWTO_GAPS.md"
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"no way {len(no_way)}/{len(items)}, no help {len(no_help)}/{len(machines)}, no view {len(no_view)} -> {out.relative_to(ROOT)}")
    if "--strict" in sys.argv and (no_way or no_help or no_view):
        sys.exit(1)


if __name__ == "__main__":
    main()

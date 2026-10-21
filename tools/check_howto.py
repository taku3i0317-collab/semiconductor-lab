"""わかりやすさの自動の検査（S-026、DESIGN「入手・操作・組み立てを分かりやすく」）。ゲームなしで動く
①入手の道が無いアイテム：MOD のアイテム・ブロック（creative_tab_order.txt）のうち、どこからも出てこない物
   道＝作業台・かまどのレシピの出る物、機械・組立台のレシピの出る物（docs/machine_recipes.json＝tools/test.ps1 が最新に）、
   世界に生える物（worldgen/feature の中のブロックと、そのブロックのルートテーブル）、ルートテーブルの書き換え（content/*.java の LootTableEvents の中の名前）、
   そして例外の一覧 HOWTO_EXTRA（機械の中でコードが作る物など、レシピの JSON に出ない道。理由を書いて足す）
②「?」の説明が無い機械：Machine の登録（content/*.java）のうち lang に block.semiconductor-lab.<id>.howto が無い物
③見取り図の無いマルチブロック：docs/multiblocks.json の制御ブロックのうち、BE が Multiblock.Host を実装していない物
使い方：python tools/check_howto.py → docs/tasks/HOWTO_GAPS.md と assets/semiconductor-lab/howto_get.json（ツールチップの「作り方」1行の元、tools/test.ps1 が最後に作り直す）（担当＝幹ごとの一覧、手で直さない）。--strict で 0 でなければ終了コード 1
"""
import json, pathlib, re, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
NS = "semiconductor-lab"
CONTENT = ROOT / "src/main/java/com/taku3/semiconductorlab/content"
# 例外（レシピの JSON に出ない道）：ID → 道の説明
HOWTO_EXTRA = {
    "test_power_source": "テスト用（クリエイティブのみ）",
    "carotene": "クロマトグラフィーの塔（[2-54]）で葉の汁を分けると出る（ブロックの右クリック、ChromatographyColumnBlock）",   # IMP3 O-028
    "chlorophyll": "クロマトグラフィーの塔（[2-54]）で葉の汁を分けると出る（ChromatographyColumnBlock）",   # IMP3 O-028
    "cured_photopolymer": "光で固まる樹脂を日なたで持つと固まる（[2-45]、PhotopolymerResinItem）",   # IMP3 O-028
    "deionized_water": "イオン交換塔（[2-17]）に蒸留水を持って右クリック（IonExchangeColumnBlock）",   # IMP3 O-028
    "ethylene_gas": "アルミナの触媒管（[2-36]）をたき火の上に置き、エタノールを持って右クリック（AluminaCatalystTubeBlock）",   # IMP3 O-028
    "fermented_wash": "発酵のかめ（[2-39]）に酵母入りの砂糖水を入れて待つ（FermentationCrockBlock）",   # IMP3 O-028
    "vinegar": "発酵のかめ（[11-10]）に発酵した液を入れて空気にさらす（FermentationCrockBlock）",   # IMP3 O-028
    "formalin": "熱した銅の網（[2-27]）にメタノールを持って右クリック（CopperGauzeBlock）",   # IMP3 O-028
    "green_vitriol": "黄鉄鉱を水のそばに置いておくと風化して緑ばんになる（[2-43]、Retting の仕組み）",   # IMP3 O-028
    "retted_flax": "亜麻の茎を水に落として待つ（レッティング [0-06]、Retting）",   # IMP3 O-028
    "flax_stalks": "亜麻の作物（[0-06]）を収穫すると出る（作物のルートテーブル）",   # IMP3 O-028
    "einkorn": "野生のヒトツブコムギを刈ると、ときどき穂の落ちない種が出る（[0-04]、作物のルートテーブル）",   # IMP3 O-028
    "heliograph": "ヘリオグラフの板を日なたで露光して、テレビン油で洗う（[2-31]、HeliographPlateBlock）",   # IMP3 O-028
    "latex": "剥片でゴムの木を右クリック（[2-29]、FlakeItem）",   # IMP3 O-028
    "pine_resin": "剥片でトウヒの丸太を右クリック（[2-31]、FlakeItem）",   # IMP3 O-028
    "obsidian_flake": "たたき石で黒曜石を右クリック（[0-01]、HammerstoneItem）",   # IMP3 O-028
    "woolen_yarn": "紡錘で羊毛を紡ぐ（[0-06]、SpindleItem）",   # IMP3 O-028
    "spent_aqua_regia": "王水は時間がたつと弱ってこれになる（[2-22]、AquaRegiaItem）",   # IMP3 O-028
    "roman_concrete": "ローマンコンクリートの生コンが水にふれると固まる（[12-01]、ConcretePowderBlock）",   # IMP3 O-028
    "nitrogen_farmland": "耕地のブロック（アイテムにはならない）：エンドウが熟す・肥料・堆肥でできる（[0-05][11-05][11-06][11-07]）",   # IMP3 O-028
    "tired_farmland": "耕地のブロック（アイテムにはならない）：同じ作物を続けて収穫するとできる（[11-03]）",   # IMP3 O-028
    "salty_farmland": "耕地のブロック（アイテムにはならない）：乾いた土地の灌漑で収穫するとできる（[11-01]）",   # IMP3 O-028
    "alkali_waste": "黒灰を作業台で使うと残る（craftRemainder、[2-05] ルブラン法）",   # IMP3 O-028
    "spent_lye": "けん化した獣脂を作業台で使うと残る（craftRemainder、[2-09] 石けん）",   # IMP3 O-028
    "light_rare_earth_liquor": "焼いたバストネス石を作業台で使うと残る（craftRemainder、[1-35]）",   # IMP3 O-028
    "potassium_fluorozirconate": "粗いフッ化ジルコニウムカリウムを作業台で使うと残る（craftRemainder、[1-70] 分別結晶）",   # IMP3 O-028
    "centered_lens_barrel": "利き手に鏡筒、反対の手に顕微鏡を持って右クリックで心出しをくり返すと仕上がる（[8-14]、LensBarrelItem）",   # IMP2 O-036
    "surface_plate": "鋳鉄の板を3枚、2枚ずつ順にすり合わせると定盤になる（ホイットワースの3枚合わせ、[8-03]、CastPlateItem）",   # IMP2 O-036
    "duralumin": "焼き入れしたジュラルミンを部屋の温度で数日置くと固くなる（時効硬化 [14-13]、QuenchedDuraluminItem）",   # IMP2 O-036
    "exposed_plate": "カメラを持ち、反対の手に感光した銀板を持って、日なたで右クリックを押している間に写る（[B-02a]、CameraItem）",   # IMP2 O-036
    "typeset_form": "書いた本を持ち、反対の手に活字を持って活版印刷機を右クリックで組む（[15-02]、PrintingPressBlock）",   # IMP2 S-010
    "exposed_dry_plate": "カメラを持ち、反対の手に感光乾板を持って、日なたで右クリックを押している間に写る（[B-02b]、CameraItem）",   # IMP2 O-036
}
# 機械のコードが作る物（machine_recipes に書き出していない道）：ID → 道の印（ツールチップにも出る）
EXTRA_WAYS = {
    **{f"{g}_cylinder": ["machine:compressor"] for g in ("argon", "chlorine", "hydrogen", "nitrogen", "oxygen")},   # 圧縮機：ガスホルダーのガスを空ボンベへ
    "calcium_carbide": ["machine:arc_furnace"],   # アーク炉：生石灰＋コークス
    "chip": ["machine:dicing_saw"],               # ダイシングソー：処理したウェハーを割る
    "layout_sheet": ["machine:drafting_table"], "rubylith": ["machine:drafting_table"],   # 製図台（⚠️ 材料なし、S-001）
    "stamp": ["machine:probe_station"],           # 検査台：調べた回路をハンコに
    "aluminized_mirror": ["machine:evaporator"],   # IMP2 O-036
    "chrome_blank": ["machine:evaporator"],   # IMP2 O-036
    "coated_crown_lens": ["machine:evaporator"],   # IMP2 O-036
    "coated_flint_lens": ["machine:evaporator"],   # IMP2 O-036
    "i_line_filter": ["machine:evaporator"],   # IMP2 O-036
    "ir_cut_filter": ["machine:evaporator"],   # IMP2 O-036
    "carbon_dioxide_gas": ["machine:reverberatory_furnace"],   # IMP2 O-036
    "molybdenum_trioxide": ["machine:reverberatory_furnace"],   # IMP2 O-036
    "molybdenum": ["machine:hydrogen_furnace"],   # IMP2 O-036
    "chrome_photomask": ["machine:reduction_camera"],   # IMP2 O-036
    "cultured_quartz": ["machine:autoclave"],   # IMP2 O-036
    "fluorine_cylinder": ["machine:fluorine_cell"],   # IMP2 O-036
    "krypton_gas": ["machine:rare_gas_column"],   # IMP2 O-036
    "neon_gas": ["machine:rare_gas_column"],   # IMP2 O-036
    "xenon_gas": ["machine:rare_gas_column"],   # IMP2 O-036
    "mercury_lamp_tube": ["machine:sealing_station"],   # IMP2 O-036
    "multilayer_mirror": ["machine:sputter"],   # IMP2 O-036
    "optical_fiber": ["machine:oxyhydrogen_burner"],   # IMP2 O-036
    "ruled_grating": ["machine:ruling_engine"],   # IMP2 O-036
    "silicon_tetrachloride": ["machine:trichlorosilane_reactor"],   # IMP2 O-036
    "steel_balls_ground": ["machine:grinder"],   # IMP2 O-036
    "steel_balls_precise": ["machine:grinder"],   # IMP2 O-036
    **{f"{g}_cylinder": ["machine:compressor"] for g in ("krypton", "neon", "xenon")},   # IMP2 O-036 圧縮機：希ガスの塔のガスを空ボンベへ
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
    ways = {}   # ID → 道の印の並び（ツールチップの「作り方」1行、assets/.../howto_get.json）

    def way(i, w):
        got.add(i)
        lst = ways.setdefault(i, [])
        if w not in lst:
            lst.append(w)
    for i in HOWTO_EXTRA:
        if i != "test_power_source":
            way(i, "special")   # 手書きの道（ブロックを右クリック・待つ など）。ツールチップは「特別（図鑑）」
    for p in (RES / f"data/{NS}/loot_table/blocks").glob("*.json"):   # ブロックを壊すと出る物（作物の収穫など。ブロック自身は除く）
        for i in ids_in(load(p)) - {p.stem}:
            way(i, "loot")
    for i, ws in EXTRA_WAYS.items():
        for w in ws:
            way(i, w)
    for p in sorted((RES / f"data/{NS}/recipe").glob("*.json")):
        rr = load(p)
        r = rr.get("result")
        rid = r.get("id") if isinstance(r, dict) else r
        if isinstance(rid, str) and rid.startswith(NS + ":"):
            t = rr.get("type", "")
            way(rid.split(":")[1], "crafting" if "crafting" in t else "campfire" if "campfire" in t else "smelting")
    mr = ROOT / "docs/machine_recipes.json"
    for r in load(mr).get("recipes", []) if mr.exists() else []:
        for i in sorted((ids_in(r.get("outputs", [])) | ids_in(r.get("byproducts", []))) - {x["item"].split(":")[-1] for x in r.get("inputs", []) if "item" in x}):   # 通り抜けるだけの物は道にしない（代わりに使える alt は見ない）
            way(i, "machine:" + r["machine"].split(":")[-1])
    loot = RES / f"data/{NS}/loot_table/blocks"
    for p in (RES / f"data/{NS}/worldgen/feature").glob("*.json"):
        for b in ids_in(load(p)):
            way(b, "ore")
            if (loot / f"{b}.json").exists():
                for i in ids_in(load(loot / f"{b}.json")):
                    way(i, "ore")
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
                    way(consts[name], "loot")
    for src in srcs.values():   # 作業台で使うと残る物（craftRemainder）
        for name in re.findall(r"craftRemainder\((?:[\w.]+\.)?([A-Z][A-Z0-9_]+)\)", src):
            if name in consts:
                way(consts[name], "crafting")
    no_way = [i for i in items if i not in got]

    lang = load(RES / f"assets/{NS}/lang/ja_jp.json")
    machines = []
    for trunk, s in srcs.items():
        for m in re.finditer(r'Machine\.(\w+)\("([a-z0-9_]+)"', s):
            stmt = s[m.start(): s.index(";", m.start())]
            if m.group(1) == "electric" or ".layout(" in stmt:   # 画面のある機械だけ「?」が要る
                machines.append((m.group(2), trunk))
    recipes_by = {}
    for r in load(mr).get("recipes", []) if mr.exists() else []:
        mid = r["machine"].split(":")[-1]
        lst = recipes_by.setdefault(mid, [])
        if len(lst) < 6:
            lst.append({"in": [[x["item"], x.get("count", 1)] for x in r.get("inputs", []) if "item" in x],
                        "out": [[x["item"], x.get("count", 1)] for x in r.get("outputs", []) + r.get("byproducts", []) if "item" in x],
                        "watts": r.get("watts"), "seconds": r.get("seconds")})
    mh = RES / f"assets/{NS}/machine_howto.json"
    mh.write_text(json.dumps({"_note": "自動生成：python tools/check_howto.py（O-035）。機械の画面の「?」に出すレシピの一覧（MachineScreen）。手で直さない",
                              "recipes": {k: recipes_by[k] for k in sorted(recipes_by)}}, ensure_ascii=False, indent=0) + chr(10), encoding="utf-8")
    no_help = [(i, t) for i, t in machines if f"block.{NS}.{i}.howto" not in lang and i not in recipes_by]

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
             f"- ②「?」の説明が無い機械（画面のある機械のうち、手書きの lang block.{NS}.<id>.howto も、自動のレシピの一覧＝machine_recipes も無い物）：**{len(no_help)}** / {len(machines)}",
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
    for k in ways:   # 手書きの道は最後に（レシピ・機械・掘る・壊す が先）
        ways[k].sort(key=lambda w: w == "special")
    hg = RES / f"assets/{NS}/howto_get.json"
    hg.write_text(json.dumps({"_note": "自動生成：python tools/check_howto.py（O-035）。ツールチップの「作り方」1行（machine/HowToGet）。手で直さない",
                              "ways": {k: ways[k] for k in sorted(ways)}}, ensure_ascii=False, indent=0) + chr(10), encoding="utf-8")
    out = ROOT / "docs/tasks/HOWTO_GAPS.md"
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"no way {len(no_way)}/{len(items)}, no help {len(no_help)}/{len(machines)}, no view {len(no_view)} -> {out.relative_to(ROOT)}")
    if "--strict" in sys.argv and (no_way or no_help or no_view):
        sys.exit(1)


if __name__ == "__main__":
    main()

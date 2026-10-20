#!/usr/bin/env python3
"""今日足された「人間の確認」を、試験場の区画の順に並べた手順書 docs/HUMAN_CHECK_TODAY.md を作る（O-008、A）。

使い方:  python tools/gen_human_check.py [YYYY-MM-DD]   （日付を省くと今日）
読む物:
  docs/test_queue/<幹>.md … 「- [ ] 人間：」の行。いつ足されたかは git blame（-M -C -C で、IMP-32 で部品に分けた行も元の commit の日付）
  src/main/resources/data/semiconductor-lab/function/testworld.mcfunction … 区画の場所（看板の [ノード]・機械のブロック）。
      先に python tools/gen_testworld.py（D の試験場のスクリプト）を実行しておく
作る物: docs/HUMAN_CHECK_TODAY.md（手で直さない）
"""
import datetime
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NS = "semiconductor-lab"
PARTS = ROOT / "docs/test_queue"
FUNC = ROOT / f"src/main/resources/data/{NS}/function/testworld.mcfunction"
OUT = ROOT / "docs/HUMAN_CHECK_TODAY.md"
NODE_RE = r"[0-9A-Z]+-[0-9]+"

# ノード番号の無い節（general.md など）を、どの機械の区画で確かめるか
KEYWORDS = [("塗布", "spin_coater"), ("マスクアライナー", "mask_aligner"), ("現像", "developer_bench"), ("はがし", "resist_stripper"), ("組立台", "assembly_bench"), ("製図台", "drafting_table"), ("検査台", "probe_station"), ("高炉", "blast_furnace_hearth"),
            ("塊鉄炉", "bloomery"), ("ふいご", "bellows"), ("塩酸合成炉", "hcl_burner"), ("拡散炉", "diffusion_furnace"),
            ("汚れ", "probe_station"), ("大きな方眼", "drafting_table"), ("露光とマスク", "drafting_table"), ("洗浄", "cleaning_bench")]
# v0.3 の本線（製図台 → … → 検査台）に入れる節：見出しにこの言葉か、このノード
MAIN_WORDS = ("露光とマスク", "製図台", "検査台", "汚れ", "大きな方眼", "拡散炉の窓", "v0.3", "レジスト", "現像", "マスクアライナー", "はがし", "集積回路")
MAIN_NODES = {"7-11", "9-06", "9-13", "9-14", "9-15", "9-16", "9-25", "9-12", "9-18", "9-19", "9-20", "9-21", "9-22", "2-30", "2-32"}
# 本線の手順（機械 ID・何をする）。機械がまだ無い工程は None
MAIN_STEPS = [
    ("drafting_table", "製図台で ＋（新しい方眼紙）→ 層を選んで方眼に描く → ✂ で層ごとに原図を切り出す"),
    ("reduction_camera", "縮小カメラ（C）で原図 → フォトマスク"),
    ("oxidation_furnace", "酸化炉で、切ったシリコンの板（p型）＋酸素 → 酸化膜つきの板"),
    ("spin_coater", "レジスト塗布機＋ポジ型レジスト → レジストを塗った板（日なたに持ち出さない）"),
    ("mask_aligner", "マスクアライナー＋フォトマスク（すき間は密着）→ 露光した板"),
    ("developer_bench", "現像槽＋現像液 → 現像した板"),
    ("etch_tank", "エッチング槽＋フッ酸 → 酸化膜に窓（窓の開いた加工中のウェハー）"),
    ("resist_stripper", "レジストはがし槽＋硫酸＋過酸化水素 → レジストが取れる（ここから 塗布 → … をマスクの枚数＝5回）"),
    ("spin_coater", "ネガ型でも通す：塗布機にネガ型レジスト（KPR、[2-30]）→ 同じマスクで露光 → 現像槽の現像液はキシレン（アルカリでは現像しない）→ 同じ NOT（最初の IC の時代はネガ型）"),
    ("probe_station", "ダイヤモンドのけがき針（または ダイシングソー）でウェハー → チップ8個 → チップを検査台で調べても同じ発見（[9-32]）"),
    ("diffusion_furnace", "拡散炉＋リン／B2O3 → 窓からだけしみ込む（3回）"),
    ("evaporator", "真空蒸着装置（隣に真空容器）＋アルミ → Al の膜"),
    ("etch_tank", "エッチング槽＋リン酸 → Al の配線"),
    ("cleaning_bench", "洗浄台 → 汚れが 0 に"),
    ("probe_station", "検査台でパッドを 0 V・5 V にして「調べる」→ 発見（抵抗・ダイオード・トランジスタ・集積回路・NOT の5つ）。ガイド第4巻（give_guidebook4）のページも開くか"),
]


def zones():
    """看板の [ノード] → (x, z)、機械の ID → (x, z)（/function を実行した所から）"""
    signs, blocks = {}, {}
    for line in FUNC.read_text(encoding="utf-8").splitlines():
        m = re.match(r"setblock ~(-?\d+) ~(-?\d+) ~(-?\d+) (\S+?)(\[|\{|$)", line)
        if not m:
            continue
        x, z, bid = int(m.group(1)), int(m.group(3)), m.group(4)
        if bid == "minecraft:oak_sign":
            for n in re.findall(rf'"\[({NODE_RE})\]', line):
                signs.setdefault(n, (x, z))
        elif bid.startswith(NS + ":"):
            blocks.setdefault(bid.split(":")[1], (x, z))
    return signs, blocks


def blamed_items(day):
    """部品ファイルの「- [ ] 人間：」で、その日に足された物：(部品, 見出し, ノード, 本文)"""
    out = []
    for f in sorted(PARTS.glob("*.md")):
        if f.name.startswith("_"):
            continue
        r = subprocess.run(["git", "blame", "-w", "-M", "-C", "-C", "--date=short", "--", str(f.relative_to(ROOT))],
                           cwd=ROOT, capture_output=True, text=True, encoding="utf-8")
        head = ""
        for line in r.stdout.splitlines():
            m = re.match(r"^\S+(?:\s+\S+)?\s+\(.*?(\d{4}-\d{2}-\d{2})\s+\d+\)\s?(.*)$", line)
            if not m:
                continue
            date, text = m.group(1), m.group(2)
            if text.startswith("## "):
                head = text[3:].strip()
            elif text.startswith("- [ ] 人間：") and date == day:
                out.append((f.stem, head, re.findall(rf"\[({NODE_RE})\]", head), text[len("- [ ] 人間："):].strip()))
    return out


def place_of(item, signs, blocks):
    _, head, nodes, _ = item
    for n in nodes:
        if n in signs:
            return signs[n], f"[{n}] の看板"
    for word, bid in KEYWORDS:
        if word in head and bid in blocks:
            return blocks[bid], bid
    return None, "区画なし（クリエイティブの持ち物で）"


def is_main(item):
    _, head, nodes, _ = item
    return any(w in head for w in MAIN_WORDS) or any(n in MAIN_NODES for n in nodes)


def fmt(pos):
    return f"東 {pos[0]}・南 {pos[1]}" if pos else "—"


def main():
    day = sys.argv[1] if len(sys.argv) > 1 else datetime.date.today().isoformat()
    signs, blocks = zones()
    items = blamed_items(day)
    main_items = [i for i in items if is_main(i)]
    rest = [i for i in items if not is_main(i)]
    lines = [f"# 今日の人間の確認（{day}）",
             "",
             "自動生成：`python tools/gen_human_check.py`（O-008、A）。手で直さない。元は docs/test_queue/ の「- [ ] 人間：」のうち、この日に足された行（git blame）。",
             "",
             "## 始め方",
             "1. 新しいワールド（クリエイティブ・コマンド有効、平らな所）で `/function semiconductor-lab:testworld`（D の試験場。機械の真下のピンクと黒は試験用の無限電源）",
             "2. 場所は、コマンドを実行した所から **東へ x・南へ z** ブロック（各区画の北側に看板、チェストに材料）",
             f"3. 終わった項目は docs/test_queue/ の部品で `[ ]` を `[x]` に（この手順書は作り直せば消える）。全部で {len(items)} 項目",
             "",
             "## 1. v0.3a の確認表：本線（製図台 → … → 検査台 → チップ）を通しで（全部実機、GameTest v03_chain・v03_chain_negative と同じ順）",
             ""]
    for k, (bid, what) in enumerate(MAIN_STEPS, 1):
        pos = blocks.get(bid) if bid else None
        where = f"（{fmt(pos)}）" if pos else ("（区画なし）" if bid else "")
        lines.append(f"{k}. {what}{where}")
    lines += ["", "この本線に関わる確認（ついでに見る）：", ""]
    for i in main_items:
        pos, label = place_of(i, signs, blocks)
        lines.append(f"- **{i[1]}**（{fmt(pos)}）：{i[3]}")
    lines += ["", "## 2. そのほかの区画（北から南、西から東の順）", ""]
    keyed = []
    for i in rest:
        pos, label = place_of(i, signs, blocks)
        keyed.append(((pos[1], pos[0]) if pos else (10 ** 6, 0), i, pos, label))
    keyed.sort(key=lambda t: t[0])
    for _, i, pos, label in keyed:
        lines.append(f"- **{i[1]}**（{fmt(pos)}、{label}）")
        lines.append(f"  - する・見えれば OK：{i[3]}")
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"human check {day}: {len(items)} items ({len(main_items)} on the v0.3 main line) -> {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()

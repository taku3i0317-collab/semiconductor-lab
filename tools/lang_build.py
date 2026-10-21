#!/usr/bin/env python3
"""lang の部品（src/main/lang/<部品>/en_us.json・ja_jp.json）を1つにまとめて、
ゲームが読む src/main/resources/assets/semiconductor-lab/lang/en_us.json・ja_jp.json を作る（IMP-28）。

部品は幹ごと（survival・metallurgy・…・semiconductor・communication・branches）と、
common（幹に属さない共通の文：純度の表示・機械のふつうの画面など）・guide（ガイド本・進捗・図鑑）・vanilla（バニラのアイテムの解説）。
どのキーをどの部品に入れるかは classify()（アイテム・ブロックの ID → それを登録している content/<幹>.java）。

使い方:
  python tools/lang_build.py           # 部品 → まとめたファイルを書く
  python tools/lang_build.py --check   # まとめたファイルが部品と同じか調べるだけ（違えば終了コード 1。tools/test.ps1 が使う）
まとめたファイル（assets/.../lang）は手で書き換えない。足すときは tools/lang_add.py（部品へ自動で振り分ける）。
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PARTS = ROOT / "src/main/lang"
OUT = ROOT / "src/main/resources/assets/semiconductor-lab/lang"
CONTENT = ROOT / "src/main/java/com/taku3/semiconductorlab/content"
NS = "semiconductor-lab"
CODES = ("en_us", "ja_jp")
# まとめる順（＝ファイルの中の順）
ORDER = ["common", "guide", "survival", "metallurgy", "chemistry", "heat", "power", "vacuum", "gas", "optics", "precision",
         "semiconductor", "control", "agriculture", "construction", "transport", "communication", "branches", "vanilla"]
# ID ではない名前の message.*・gui.* の行き先
SPECIAL = {"generator": "power", "cable": "power", "refiner": "semiconductor"}
# 決まった頭のキーの行き先（ツールチップなど）
PREFIX = {f"tooltip.{NS}.battery_energy": "power", f"tooltip.{NS}.route.": "metallurgy", f"tooltip.{NS}.dopant.": "semiconductor"}


def id_map():
    """アイテム・ブロック・機械の ID → 部品（content/<幹>.java のファイル名の小文字）"""
    m = {}
    for f in CONTENT.glob("*.java"):
        part = f.stem.lower()
        if part not in ORDER:
            continue
        for mm in re.finditer(r'public static final [\w<>]+ \w+\s*=\s*[\w.]+\("([a-z0-9_]+)"', f.read_text(encoding="utf-8")):
            m[mm.group(1)] = part
    return m


def classify(key, ids):
    """キー → 部品の名前"""
    if key.startswith("item.minecraft."):
        return "vanilla"
    head = key.split(".")[0]
    if head in ("advancements", "book", "zukan") or head.startswith("guide"):
        return "guide"
    for pre, part in PREFIX.items():
        if key.startswith(pre):
            return part
    m = re.match(rf"(item|block|gui|message)\.{re.escape(NS)}\.([a-z0-9_]+)", key)
    if m:
        name = m.group(2)
        if name in ids:
            return ids[name]
        if m.group(1) in ("message", "gui") and name in SPECIAL:
            return SPECIAL[name]
    return "common"


def load_parts():
    merged = {c: {} for c in CODES}
    for part in ORDER:
        d = PARTS / part
        if not d.exists():
            continue
        got = {}
        for c in CODES:
            p = d / f"{c}.json"
            got[c] = json.loads(p.read_text(encoding="utf-8")) if p.exists() else {}
        if set(got["en_us"]) != set(got["ja_jp"]):
            sys.exit(f"{part}: en_us と ja_jp のキーが違う: {sorted(set(got['en_us']) ^ set(got['ja_jp']))[:10]}")
        for c in CODES:
            dup = set(merged[c]) & set(got[c])
            if dup:
                sys.exit(f"{part}: ほかの部品と同じキー: {sorted(dup)[:10]}")
            merged[c].update(got[c])
    unknown = [d.name for d in PARTS.iterdir() if d.is_dir() and d.name not in ORDER]
    if unknown:
        sys.exit(f"ORDER にない部品: {unknown}（tools/lang_build.py の ORDER に足す）")
    return merged


def dump(d):
    return json.dumps(d, ensure_ascii=False, indent=2) + "\n"


def main(check):
    merged = load_parts()
    bad = []
    for c in CODES:
        p = OUT / f"{c}.json"
        text = dump(merged[c])
        if check:
            if not p.exists() or p.read_text(encoding="utf-8") != text:
                bad.append(c)
        else:
            p.write_text(text, encoding="utf-8")
            print(f"{c}: {len(merged[c])} keys")
    if check and bad:
        sys.exit(f"lang が部品と違う: {bad}。python tools/lang_build.py を実行する（assets/.../lang は手で直さない）")
    if check:
        print(f"lang OK（{len(merged['en_us'])} keys）")


if __name__ == "__main__":
    main("--check" in sys.argv[1:])

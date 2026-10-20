#!/usr/bin/env python3
"""追記の多い docs を幹ごとの部品ファイルから組み立てる（IMP-32）。lang と同じ考え方：
実装担当（IMP・IMP2・IMP3）がみんな末尾に書き足してぶつかっていたので、書くのは部品ファイル、
docs/CHEMISTRY.md・docs/TEST_QUEUE.md はこのスクリプトが作る（手で直さない）。

部品：docs/chemistry/<部品>.md・docs/test_queue/<部品>.md
  _head.md   … ファイルの頭（題名と説明、最初の ## より前）
  general.md … どの幹のノードでもない節（決定・レビュー・テストの方法など）
  0_survival.md 〜 15_communication.md・B_branches.md … 見出し（## …）の最初の [幹-番号] の幹の節
1つの部品の中は書いた順。組み立てる順は ORDER（頭 → 全体 → 幹0 → … → 幹15 → 枝🌿）。

使い方:
  python tools/docs_build.py            # 部品 → CHEMISTRY.md・TEST_QUEUE.md を作る
  python tools/docs_build.py --check    # 作ったファイルが部品と同じか調べるだけ（tools/test.ps1 が使う）
  python tools/docs_build.py --adopt    # CHEMISTRY.md・TEST_QUEUE.md に直接書いてしまった節を部品へ移してから組み立てる（マージの後にも）
  python tools/docs_build.py --split    # （最初の1回だけ）今の CHEMISTRY.md・TEST_QUEUE.md を部品に分け、中身が同じか確かめる
新しい節は、そのノードの幹の部品ファイルの末尾に「## [幹-番号] …」で書き足す。
"""
import re
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = {"CHEMISTRY.md": "chemistry", "TEST_QUEUE.md": "test_queue"}
TRUNK = {"0": "0_survival", "1": "1_metallurgy", "2": "2_chemistry", "3": "3_heat", "4": "4_power", "5": "5_vacuum",
         "6": "6_gas", "7": "7_optics", "8": "8_precision", "9": "9_semiconductor", "10": "10_control", "11": "11_agriculture",
         "12": "12_construction", "13": "13_medicine", "14": "14_transport", "15": "15_communication", "B": "B_branches"}
ORDER = ["_head", "general"] + list(TRUNK.values())
MARK = "<!-- 自動生成：python tools/docs_build.py（IMP-32）。このファイルは手で直さない。書くのは docs/{dir}/<幹>.md -->\n"


def sections(text):
    """頭（最初の ## より前）と、## で始まる節の並びに分ける。節の終わりの空行はそろえる"""
    parts = re.split(r"(?m)^(?=## )", text)
    head, secs = parts[0], parts[1:]
    return head, [s.rstrip("\n") + "\n\n" for s in secs]


def part_of(section):
    m = re.search(r"\[([0-9]+|B)-\d+\]", section.split("\n", 1)[0])
    return TRUNK.get(m.group(1), "general") if m else "general"


def build(name, d):
    folder = ROOT / "docs" / d
    out = MARK.format(dir=d)
    for p in ORDER:
        f = folder / f"{p}.md"
        if f.exists():
            t = f.read_text(encoding="utf-8")
            out += t if p == "_head" else "".join(sections("\n" + t)[1]) if t.strip() else ""
    unknown = sorted(f.stem for f in folder.glob("*.md") if f.stem not in ORDER)
    if unknown:
        sys.exit(f"docs/{d}/ に ORDER にない部品がある: {unknown}（tools/docs_build.py の TRUNK に足す）")
    return out.rstrip("\n") + "\n"


def split(name, d):
    src = ROOT / "docs" / name
    text = src.read_text(encoding="utf-8")
    if text.startswith("<!-- 自動生成"):
        sys.exit(f"{name} はもう部品から作られている（--split は最初の1回だけ）")
    head, secs = sections(text)
    folder = ROOT / "docs" / d
    folder.mkdir(parents=True, exist_ok=True)
    by = {}
    for s in secs:
        by.setdefault(part_of(s), []).append(s)
    (folder / "_head.md").write_text(head, encoding="utf-8")
    for p, ss in by.items():
        (folder / f"{p}.md").write_text("".join(ss).rstrip("\n") + "\n", encoding="utf-8")
    built = build(name, d)
    # 組み立て前後で中身が同じか：頭が同じ・節の数と1つ1つの文が同じ（並びは幹ごとにまとまる）
    h2, s2 = sections(built[len(MARK.format(dir=d)):])
    same = h2 == head and Counter(s2) == Counter(secs)
    lines_before = sum(1 for l in text.splitlines() if l.strip())
    lines_after = sum(1 for l in built.splitlines() if l.strip()) - 1
    print(f"{name}: {len(secs)} 節 → 部品 {len(by) + 1} 個 {sorted(by)}；中身が同じ={same}（空でない行 {lines_before} → {lines_after}）")
    if not same:
        sys.exit("中身が変わった。split をやり直す")
    src.write_text(built, encoding="utf-8")


def adopt(name, d):
    """組み立て結果（CHEMISTRY.md など）に直接書かれた節を、部品へ移す。新しい節はその幹の部品の末尾へ、
    見出しが同じで中身が変わった節はその部品の中で置き換える（移行期・マージの後に使う）"""
    folder = ROOT / "docs" / d
    text = (ROOT / "docs" / name).read_text(encoding="utf-8")
    mark = MARK.format(dir=d)
    if text.startswith(mark):
        text = text[len(mark):]
    _, secs = sections(text)
    moved = 0
    for s in secs:
        title = s.split("\n", 1)[0]
        f = folder / f"{part_of(s)}.md"
        t = f.read_text(encoding="utf-8") if f.exists() else ""
        _, have = sections("\n" + t)
        if s in have:
            continue
        same_title = [h for h in have if h.split("\n", 1)[0] == title]
        if same_title:
            have[have.index(same_title[0])] = s
        else:
            have.append(s)
        f.write_text("".join(have).rstrip("\n") + "\n", encoding="utf-8")
        moved += 1
        print(f"{name}: {title[:60]} → docs/{d}/{f.name}")
    print(f"{name}: {moved} 節を部品へ")


def main():
    args = sys.argv[1:]
    bad = []
    for name, d in DOCS.items():
        if "--split" in args:
            split(name, d)
            continue
        if "--adopt" in args:
            adopt(name, d)
        built = build(name, d)
        p = ROOT / "docs" / name
        if "--check" in args:
            if p.read_text(encoding="utf-8") != built:
                bad.append(name)
        else:
            p.write_text(built, encoding="utf-8")
            print(f"{name}: built from docs/{d}/")
    if bad:
        sys.exit(f"部品と違う: {bad}。CHEMISTRY.md・TEST_QUEUE.md に直接書いた節があれば python tools/docs_build.py --adopt（部品へ移して組み立て直す）、なければ python tools/docs_build.py")
    if "--check" in args:
        print("docs OK（CHEMISTRY・TEST_QUEUE）")


if __name__ == "__main__":
    main()

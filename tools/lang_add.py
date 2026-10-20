#!/usr/bin/env python3
"""lang にキーを追加・上書きする（IMP-28 から：幹ごとの部品 src/main/lang/<部品>/ に書き、まとめたファイルを作り直す）。

使い方:  python tools/lang_add.py additions.json
additions.json の形: {"en": {"key": "text", ...}, "ja": {"key": "テキスト", ...}}
en と ja のキーの集合が違うとエラー（両方を必ず更新するルールのため）。
どの部品に入るかは自動（item./block.semiconductor-lab.<id> は、その ID を登録している content/<幹>.java の幹。
先に Java でアイテムを登録しておくこと。わからないキーは common）。新しいキーは部品の末尾に足す。
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import lang_build as lb  # noqa: E402


def main(path):
    add = json.loads(Path(path).read_text(encoding="utf-8"))
    if set(add["en"]) != set(add["ja"]):
        sys.exit(f"en/ja key mismatch: {set(add['en']) ^ set(add['ja'])}")
    ids = lb.id_map()
    # すでにある部品のキーは、その部品のまま上書きする
    where = {}
    for d in lb.PARTS.iterdir():
        p = d / "en_us.json"
        if p.exists():
            for k in json.loads(p.read_text(encoding="utf-8")):
                where[k] = d.name
    touched = {}
    for code, part_key in (("en_us", "en"), ("ja_jp", "ja")):
        for k, v in add[part_key].items():
            part = where.get(k) or lb.classify(k, ids)
            f = lb.PARTS / part / f"{code}.json"
            f.parent.mkdir(parents=True, exist_ok=True)
            d = json.loads(f.read_text(encoding="utf-8")) if f.exists() else {}
            d[k] = v
            f.write_text(lb.dump(d), encoding="utf-8")
            touched.setdefault(part, 0)
            touched[part] += 1
    print("parts:", {p: n // 2 for p, n in touched.items()})
    lb.main(False)


if __name__ == "__main__":
    main(sys.argv[1])

#!/usr/bin/env python3
"""lang の en_us.json / ja_jp.json にキーを追加・上書きする（順番は保つ、新しいキーは末尾）。

使い方:  python tools/lang_add.py additions.json
additions.json の形: {"en": {"key": "text", ...}, "ja": {"key": "テキスト", ...}}
en と ja のキーの集合が違うとエラー（両方を必ず更新するルールのため）。
"""
import json
import sys
from pathlib import Path

LANG = Path(__file__).resolve().parent.parent / "src/main/resources/assets/semiconductor-lab/lang"


def main(path):
    add = json.loads(Path(path).read_text(encoding="utf-8"))
    if set(add["en"]) != set(add["ja"]):
        sys.exit(f"en/ja key mismatch: {set(add['en']) ^ set(add['ja'])}")
    for code, part in (("en_us", "en"), ("ja_jp", "ja")):
        p = LANG / f"{code}.json"
        d = json.loads(p.read_text(encoding="utf-8"))
        d.update(add[part])
        p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"{code}: {len(d)} keys")


if __name__ == "__main__":
    main(sys.argv[1])

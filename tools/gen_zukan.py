#!/usr/bin/env python3
"""ガイドタブ「図鑑」（advancement zukan/<id>）を lang から自動生成する。

lang に item./block.semiconductor-lab.<id> の名前と .note3 / .note4 がある物ごとに、
「そのアイテムを手に入れたら解放され、説明に へえ③＋へえ④ の全文が出る」進捗を作る。
アイテムを足して note3/note4 を書いたら、このスクリプトを再実行する。
使い方:  python tools/gen_zukan.py
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NS = "semiconductor-lab"
LANG = ROOT / "src/main/resources/assets" / NS / "lang/en_us.json"
OUT = ROOT / "src/main/resources/data" / NS / "advancement/zukan"


def main():
    en = json.loads(LANG.read_text(encoding="utf-8"))
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.json"):
        old.unlink()
    root = {"display": {"icon": {"id": "minecraft:writable_book"}, "title": {"translate": f"zukan.{NS}.root.title"},
                        "description": {"translate": f"zukan.{NS}.root.description"},
                        "background": "minecraft:block/birch_planks", "frame": "task",
                        "show_toast": False, "announce_to_chat": False},
            "criteria": {"joined": {"trigger": "minecraft:tick"}}}
    (OUT / "root.json").write_text(json.dumps(root, indent=2) + "\n", encoding="utf-8")
    n = 0
    for k in en:
        m = re.fullmatch(rf"(item|block)\.{re.escape(NS)}\.([a-z0-9_]+)", k)
        if not m or f"{k}.note3" not in en or f"{k}.note4" not in en:
            continue
        iid = f"{NS}:{m.group(2)}"
        adv = {"parent": f"{NS}:zukan/root",
               "display": {"icon": {"id": iid}, "title": {"translate": k},
                           "description": {"translate": f"{k}.note3", "extra": ["\n\n", {"translate": f"{k}.note4"}]},
                           "frame": "task", "show_toast": False, "announce_to_chat": False, "hidden": True},
               "criteria": {"got": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": iid}]}}}}
        (OUT / f"{m.group(2)}.json").write_text(json.dumps(adv, indent=2) + "\n", encoding="utf-8")
        n += 1
    print(f"zukan: {n} entries")


if __name__ == "__main__":
    main()

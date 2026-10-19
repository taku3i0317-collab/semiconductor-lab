#!/usr/bin/env python3
"""向き＋LIT の機械ブロック（ConsumerMachineBlock）の JSON 一式と仮テクスチャを作る。

使い方:  python tools/new_machine.py <id> <本体色 #rrggbb> <窓の色 #rrggbb> <窓の色・点灯 #rrggbb> <レシピ解放のきっかけアイテムID>
作るもの: blockstates / models/block（通常・_on）/ items / loot_table / mineable/pickaxe タグ追加 /
          recipes の解放進捗（advancement/recipes/chapter1/<id>.json）/ textures/block の front・front_on・side・top
レシピ本体（recipe/<id>.json）と lang は手で書く。
"""
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
R = ROOT / "src/main/resources"
A = R / "assets/semiconductor-lab"
D = R / "data/semiconductor-lab"
NS = "semiconductor-lab"


def write(p, obj):
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main(mid, body, win, win_on, trigger):
    variants = {}
    for face, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for lit in ("false", "true"):
            v = {"model": f"{NS}:block/{mid}" + ("_on" if lit == "true" else "")}
            if y:
                v["y"] = y
            variants[f"facing={face},lit={lit}"] = v
    write(A / f"blockstates/{mid}.json", {"variants": variants})
    for suf in ("", "_on"):
        write(A / f"models/block/{mid}{suf}.json", {"parent": "minecraft:block/orientable", "textures": {
            "front": f"{NS}:block/{mid}_front{suf}", "side": f"{NS}:block/{mid}_side", "top": f"{NS}:block/{mid}_top"}})
    write(A / f"items/{mid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{mid}"}})
    write(D / f"loot_table/blocks/{mid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{NS}:{mid}"}], "condition": {"type": "minecraft:survives_explosion"}}]})
    tag = R / "data/minecraft/tags/block/mineable/pickaxe.json"
    t = json.loads(tag.read_text(encoding="utf-8"))
    if f"{NS}:{mid}" not in t["values"]:
        t["values"].append(f"{NS}:{mid}")
        tag.write_text(json.dumps(t, ensure_ascii=False), encoding="utf-8")
    write(D / f"advancement/recipes/chapter1/{mid}.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_item": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": trigger}]}},
            "has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipes": [f"{NS}:{mid}"]}}},
        "requirements": [["has_item", "has_the_recipe"]],
        "rewards": {"recipes": [f"{NS}:{mid}"]}})
    tex = A / "textures/block"
    gen = [sys.executable, str(ROOT / "tools/gen_png.py")]
    subprocess.run(gen + ["machine_side", body, str(tex / f"{mid}_side.png")], check=True)
    subprocess.run(gen + ["machine_top", body, str(tex / f"{mid}_top.png")], check=True)
    subprocess.run(gen + ["machine_front", body, str(tex / f"{mid}_front.png"), win], check=True)
    subprocess.run(gen + ["machine_front", body, str(tex / f"{mid}_front_on.png"), win_on], check=True)
    print("machine", mid, "done")


if __name__ == "__main__":
    main(*sys.argv[1:])

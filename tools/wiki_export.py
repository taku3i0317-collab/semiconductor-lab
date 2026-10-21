"""MOD のウィキ（B がページを作る）のためのデータを書き出す（S-022）。手で書かない。ネタばれ（お題の答え・ヒント・真理値表）は出さない。

元：
- アイテム・ブロック：assets/semiconductor-lab/creative_tab_order.txt（ID・幹・ノード・年、tools/gen_tab_order.py が作る）＋ lang（名前・説明 note1〜4・仮 placeholder）
- GHS の印：説明の文に出てくる絵表示の名前（どくろ・炎・腐食性 など）
- レシピ：data/semiconductor-lab/recipe/*.json（作業台・かまど など）
- 機械・組立台のレシピ：docs/machine_recipes.json（GameTest exportMachineRecipes が作る＝ tools/test.ps1 で最新に）
- お題：advancement/challenge/*.json（名前と、開く条件＝前のお題だけ）
- ガイド本：advancement/guide*/（巻・ページ・題・中身。技術の手帳 S-021 と同じ元）
- ブロックのモデル（blockstates・models/block の JSON、テクスチャは名前だけ）、マルチブロックの形（docs/multiblocks.json＝GameTest exportMultiblocks）、
  画面の絵の一覧（docs/gui_preview/*.png＝GameTest gui_layout が全部の機械の分を描く。tools/test.ps1 が1つのコマンド）（S-023）
使い方：powershell tools/test.ps1（機械のレシピ・マルチブロック・画面の絵を最新に）→ python tools/wiki_export.py  →  docs/wiki_export.json（B は docs/ からこれを読む。machine_recipes.json と同じ置き場所）
"""
import json, pathlib, re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
NS = "semiconductor-lab"
GHS_WORDS = {"どくろ": "skull", "炎": "flame", "腐食": "corrosion", "感嘆符": "exclamation", "健康有害": "health_hazard", "環境": "environment",
             "爆発": "explosive", "円上の炎": "oxidizer", "酸化性": "oxidizer", "ガスボンベ": "gas_cylinder", "高圧ガス": "gas_cylinder"}


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def main():
    en = load(RES / f"assets/{NS}/lang/en_us.json")
    ja = load(RES / f"assets/{NS}/lang/ja_jp.json")

    def name(key):
        return {"en": en.get(key), "ja": ja.get(key)}

    items = []
    order = (RES / f"assets/{NS}/creative_tab_order.txt").read_text(encoding="utf-8").splitlines()
    for line in order:
        if not line.strip() or line.startswith("#"):
            continue
        m = re.match(r"([a-z0-9_]+)\s+#\s+(\S+)\s+\[(\S*)\]\s+(\S+)", line)
        if not m:
            continue
        iid, trunk, node, year = m.groups()
        key = next((k for k in (f"item.{NS}.{iid}", f"block.{NS}.{iid}") if k in en or k in ja), f"item.{NS}.{iid}")
        notes = {}
        for n in ("note1", "note2", "note3", "note4", "note", "placeholder"):
            if key + "." + n in en or key + "." + n in ja:
                notes[n] = name(key + "." + n)
        text = " ".join(v.get("ja") or "" for v in notes.values())
        ghs = sorted({v for w, v in GHS_WORDS.items() if w in text}) if "GHS" in text or "GHS" in " ".join(v.get("en") or "" for v in notes.values()) else []
        items.append({"id": f"{NS}:{iid}", "kind": "block" if key.startswith("block.") else "item", "name": name(key), "trunk": trunk,
                      "node": node if node and node != "None" else None, "year": None if year == "None" else int(year) if re.fullmatch(r"-?\d+", year) else year,
                      "notes": notes, "ghs": ghs, "placeholder": "placeholder" in notes})

    recipes = []
    for p in sorted((RES / f"data/{NS}/recipe").glob("*.json")):
        r = load(p)
        inputs = []
        if "pattern" in r:
            counts = {}
            for row in r["pattern"]:
                for ch in row:
                    if ch != " ":
                        counts[ch] = counts.get(ch, 0) + 1
            for ch, c in counts.items():
                inputs.append({"item": r["key"][ch] if isinstance(r["key"][ch], str) else r["key"][ch], "count": c})
        for ing in r.get("ingredients", []):
            inputs.append({"item": ing, "count": 1})
        if "ingredient" in r:
            inputs.append({"item": r["ingredient"], "count": 1})
        res = r.get("result")
        out = {"item": res.get("id"), "count": res.get("count", 1)} if isinstance(res, dict) else {"item": res, "count": 1}
        e = {"id": f"{NS}:{p.stem}", "type": r.get("type"), "inputs": inputs, "output": out}
        if "cookingtime" in r:
            e["seconds"] = r["cookingtime"] / 20
        recipes.append(e)

    mr = load(ROOT / "docs/machine_recipes.json").get("recipes", [])
    machines = {}
    for r in mr:
        machines.setdefault(r["machine"], []).append(r.get("process"))
    machine_list = [{"id": m, "name": name(f"block.{NS}.{m.split(':')[1]}") if ":" in m else None, "processes": ps} for m, ps in sorted(machines.items())]

    challenges = []
    for p in sorted((RES / f"data/{NS}/advancement/challenge").glob("*.json")):
        if p.stem == "root":
            continue
        a = load(p)
        parent = a.get("parent", "")
        challenges.append({"id": p.stem, "name": name(f"litho.{NS}.challenge.{p.stem}") if p.stem != "first_stamp" else name(f"advancement.{NS}.challenge.first_stamp.title"),
                           "unlocked_by": parent.split("/")[-1] if "challenge/" in parent else None})   # 中身（動き・ヒント・表）は出さない

    guide = []
    for d in sorted((RES / f"data/{NS}/advancement").glob("guide*")):
        pages = []
        for p in sorted(d.glob("p*.json"), key=lambda x: int(re.sub(r"\D", "", x.stem) or 0)):
            a = load(p)
            disp = a.get("display", {})
            tkey = disp.get("title", {}).get("translate")
            bkey = disp.get("description", {}).get("translate")
            crit = list(a.get("criteria", {}).keys())
            pages.append({"page": p.stem, "title": name(tkey), "text": name(bkey), "opens_with": [f"{NS}:{c}" for c in crit]})
        root = load(d / "root.json") if (d / "root.json").exists() else {}
        guide.append({"volume": d.name, "title": name(root.get("display", {}).get("title", {}).get("translate", "")), "pages": pages})

    # S-023：ブロックのモデル（JSON。テクスチャは参照の名前だけ、Mojang の絵は入れない）とマルチブロックの形
    models = {}
    for it in items:
        if it["kind"] != "block":
            continue
        bid = it["id"].split(":")[1]
        bs = RES / f"assets/{NS}/blockstates/{bid}.json"
        if not bs.exists():
            continue
        state = load(bs)
        refs = set(re.findall(r'"model":\s*"([^"]+)"', json.dumps(state)))
        ms = {}
        for ref in sorted(refs):
            ns, path = ref.split(":") if ":" in ref else ("minecraft", ref)
            mp = RES / f"assets/{ns}/models/{path}.json"
            if ns == NS and mp.exists():
                ms[ref] = load(mp)
        models[it["id"]] = {"blockstate": state, "models": ms}
    mb_path = ROOT / "docs/multiblocks.json"
    multiblocks = load(mb_path).get("multiblocks", []) if mb_path.exists() else []
    for m in multiblocks:
        for c in m.get("legend", {}).values():
            c["name_text"] = name(c.get("lang", ""))
    gui = sorted(p.name for p in (ROOT / "docs/gui_preview").glob("*.png"))

    out = {"_note": "自動生成：python tools/wiki_export.py（S-022）。手で直さない。機械のレシピは docs/machine_recipes.json（tools/test.ps1 で最新）から。お題の答え・ヒントは入れていない",
           "license": "CC0-1.0", "items": items, "recipes": recipes, "machine_recipes": mr, "machines": machine_list, "challenges": challenges, "guide": guide,
           "block_models": models, "multiblocks": multiblocks, "gui_preview": gui}
    dst = ROOT / "docs/wiki_export.json"
    dst.write_text(json.dumps(out, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"{dst.relative_to(ROOT)}: items {len(items)}, recipes {len(recipes)}, machine recipes {len(mr)}, machines {len(machine_list)}, challenges {len(challenges)}, guide volumes {len(guide)}, block models {len(models)}, multiblocks {len(multiblocks)}, gui {len(gui)}")


if __name__ == "__main__":
    main()

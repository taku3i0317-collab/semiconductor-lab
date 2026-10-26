"""鉱石をバニラの石になじませる（S-032 ユーザー「鉱石が浮いて見える」）。手で書かない部分を作る
- 鉱石のモデルを「2枚重ね」に：下＝バニラの下地の絵を名前で参照（minecraft:block/stone・deepslate・granite・sand…、ファイルは MOD に入れない）、
  上＝MOD の自作の「粒だけ」の透明な PNG（textures/block/<id>_grains.png。透明な所があるので cutout で描かれる）
- 粒の描き方はバニラの鉄・銅の鉱石くらい：小さな塊 5〜6 個、1つの塊は 2〜5 マス、色は 4 段（明るい点・地の色・影・暗い縁）。色は今の絵の鉱物の色（icons_e の材料の色の決まりのまま）
- 石と深層岩の両方にできる鉱石（DUAL）は深層岩の版 deepslate_<id> も：モデル・blockstates・アイテム・ルート（同じ物が出る）・タグ（つるはし・道具の段）・生成（deepslate_ore_replaceables は深層岩の版に）・lang
使い方：python tools/gen_ore_overlay.py（Java は content の Reg.deepslateOre の1行。lang は出力の JSON を tools/lang_add.py で）
"""
import json, pathlib, random
from collections import Counter
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
R = ROOT / "src/main/resources"
A = R / "assets/semiconductor-lab"
D = R / "data/semiconductor-lab"
NS = "semiconductor-lab"
# 鉱石 → 下地（生成の場所に合わせる。いちばん多い下地）
BASE = {
    "fluorite_ore": "stone", "galena_ore": "stone", "pentlandite_ore": "stone", "pyrite_ore": "stone", "quartz_vein": "stone", "sphalerite_ore": "stone", "stibnite_ore": "stone",
    "bastnaesite_ore": "stone", "malachite_ore": "stone", "pyrolusite_ore": "stone", "rock_salt_ore": "stone",
    "chromite_ore": "deepslate", "graphite_ore": "deepslate", "magnetite_ore": "deepslate",
    "cassiterite_ore": "granite", "spodumene_ore": "granite", "tantalite_ore": "granite", "wolframite_ore": "granite",
    "bauxite_ore": "coarse_dirt", "ion_adsorption_clay": "dirt", "borax_deposit": "sand", "trona_deposit": "sand", "iron_sand": "sand",
    "natural_asphalt": "sand", "nitre_earth_block": "dripstone_block",
}
DUAL = ["fluorite_ore", "galena_ore", "pentlandite_ore", "pyrite_ore", "quartz_vein", "sphalerite_ore", "stibnite_ore"]
# 鉱物の色（今の絵から取ると下地の灰色に負ける物、本物の鉱物の色）
COLOR = {"galena_ore": (190, 196, 210), "stibnite_ore": (150, 156, 170), "fluorite_ore": (170, 120, 220), "cassiterite_ore": (70, 45, 35),
         "rock_salt_ore": (236, 214, 214), "iron_sand": (40, 40, 44), "magnetite_ore": (30, 30, 34), "graphite_ore": (125, 128, 136),
         "chromite_ore": (45, 35, 30), "tantalite_ore": (35, 30, 28), "wolframite_ore": (55, 40, 30), "pyrolusite_ore": (35, 35, 38),
         "quartz_vein": (240, 238, 232), "spodumene_ore": (220, 190, 215), "bastnaesite_ore": (200, 150, 70), "pentlandite_ore": (200, 175, 95),
         "malachite_ore": (40, 160, 90), "borax_deposit": (245, 245, 240), "trona_deposit": (230, 228, 215), "natural_asphalt": (25, 22, 20),
         "nitre_earth_block": (245, 245, 245), "bauxite_ore": (170, 80, 50), "ion_adsorption_clay": (225, 205, 190)}
# 細かい粒（砂・土の中に散る物）
FINE = {"iron_sand", "trona_deposit", "borax_deposit", "natural_asphalt", "nitre_earth_block", "ion_adsorption_clay", "bauxite_ore", "rock_salt_ore"}
# 粒の多さ（塩・砂鉄など「全体に散る」物は多め）
DENSE = {"rock_salt_ore": 14, "iron_sand": 14, "trona_deposit": 16, "borax_deposit": 16, "natural_asphalt": 16, "nitre_earth_block": 16, "ion_adsorption_clay": 14, "bauxite_ore": 16}


def mineral_color(png):
    """今の絵から鉱物の色（下地の色から遠い、いちばん多い色）"""
    src = R / "assets" / NS / "textures/block" / png
    im = Image.open(src).convert("RGB")
    cnt = Counter(im.get_flattened_data() if hasattr(im, "get_flattened_data") else im.getdata())
    bg = cnt.most_common(1)[0][0]
    for c, _ in cnt.most_common():
        if sum(abs(a - b) for a, b in zip(c, bg)) > 45:
            return c
    return (200, 200, 200)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c) + (255,)


def grains(oid, color):
    rnd = random.Random(oid)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    hi, mid, lo, edge = shade(color, 1.35), shade(color, 1.0), shade(color, 0.72), shade(color, 0.45)
    taken = set()
    n = DENSE.get(oid, 6)
    tries = 0
    while n > 0 and tries < 400:
        tries += 1
        cx, cy = rnd.randint(1, 13), rnd.randint(1, 13)
        shape = rnd.choice([[(0, 0)], [(0, 0), (1, 0)], [(0, 0), (0, 1)]]) if oid in FINE else rnd.choice([[(0, 0), (1, 0), (0, 1)], [(0, 0), (1, 0), (0, 1), (1, 1)], [(0, 0), (1, 0), (2, 0), (1, 1)], [(0, 0), (0, 1), (1, 1)], [(0, 0), (1, 0)], [(0, 0), (1, 0), (1, 1), (2, 1), (1, 2)]])
        cells = [(cx + dx, cy + dy) for dx, dy in shape]
        ring = {(x + ex, y + ey) for x, y in cells for ex in (-1, 0, 1) for ey in (-1, 0, 1)}
        if any(not (0 <= x < 16 and 0 <= y < 16) for x, y in cells) or ring & taken:
            continue
        for x, y in cells:
            taken.add((x, y))
            right_bottom = (x + 1, y) not in cells and (x, y + 1) not in cells
            px[x, y] = lo if right_bottom else mid
        x0, y0 = min(cells)
        px[x0, y0] = hi   # 左上に光
        if oid in FINE:   # 細かい粒は縁を付けない（砂の中の黒い粒が1つの塊にならないように）
            n -= 1
            continue
        for x, y in cells:   # 暗い縁（右下の外側に1マス）
            for ex, ey in ((1, 1), (0, 1), (1, 0)):
                q = (x + ex, y + ey)
                if 0 <= q[0] < 16 and 0 <= q[1] < 16 and q not in cells and px[q][3] == 0 and rnd.random() < 0.5:
                    px[q] = edge
        n -= 1
    return img


def model(base, grain):
    face = lambda tex: {d: {"texture": tex, "cullface": d} for d in ("down", "up", "north", "south", "west", "east")}
    return {"parent": "minecraft:block/block", "textures": {"particle": f"minecraft:block/{base}", "base": f"minecraft:block/{base}", "grains": f"{NS}:block/{grain}"},
            "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": face("#base")}, {"from": [0, 0, 0], "to": [16, 16, 16], "faces": face("#grains")}]}


def write(p, obj):
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    lang = {"ja": {}, "en": {}}
    for oid, base in BASE.items():
        gpng = A / "textures/block" / f"{oid}_grains.png"
        if not gpng.exists():   # 粒の絵は1回だけ作る（手で直したら上書きしない）
            grains(oid, COLOR.get(oid) or mineral_color(f"{oid}.png")).save(gpng)
        write(A / "models/block" / f"{oid}.json", model(base, f"{oid}_grains"))
        if oid not in DUAL:
            continue
        did = f"deepslate_{oid}"
        write(A / "models/block" / f"{did}.json", model("deepslate", f"{oid}_grains"))
        write(A / "blockstates" / f"{did}.json", {"variants": {"": {"model": f"{NS}:block/{did}"}}})
        write(A / "items" / f"{did}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{did}"}})
        loot = json.loads((D / "loot_table/blocks" / f"{oid}.json").read_text(encoding="utf-8"))
        write(D / "loot_table/blocks" / f"{did}.json", json.loads(json.dumps(loot).replace(f'"{NS}:{oid}"', f'"{NS}:{did}"')))
        for tag in (R / "data/minecraft/tags/block").rglob("*.json"):
            t = json.loads(tag.read_text(encoding="utf-8"))
            vals = t.get("values", [])
            if f"{NS}:{oid}" in vals and f"{NS}:{did}" not in vals:
                vals.insert(vals.index(f"{NS}:{oid}") + 1, f"{NS}:{did}")
                write(tag, t)
        feat = D / "worldgen/feature" / f"{oid}.json"
        f = json.loads(feat.read_text(encoding="utf-8"))
        for t in f.get("targets", []):
            if t["target"].get("tag") == "minecraft:deepslate_ore_replaceables":
                t["state"] = f"{NS}:{did}"
        write(feat, f)
        en = json.loads((A / "lang/en_us.json").read_text(encoding="utf-8")).get(f"block.{NS}.{oid}", oid)
        ja = json.loads((A / "lang/ja_jp.json").read_text(encoding="utf-8")).get(f"block.{NS}.{oid}", oid)
        lang["en"][f"block.{NS}.{did}"] = f"Deepslate {en}"
        lang["ja"][f"block.{NS}.{did}"] = f"深層岩の{ja}"
        lang["en"][f"block.{NS}.{did}.note1"] = f"The same ore deep underground (below Y 0), inside deepslate. Slightly harder to mine; drops the same."
        lang["ja"][f"block.{NS}.{did}.note1"] = f"地下深く（Y 0 より下）の深層岩の中の同じ鉱石。少し硬いが、掘ると同じ物が出る。"
    out = ROOT / "build/ore_overlay_lang.json"
    out.parent.mkdir(exist_ok=True)
    out.write_text(json.dumps(lang, ensure_ascii=False), encoding="utf-8")
    print(f"ores {len(BASE)}, deepslate variants {len(DUAL)}; lang -> {out.relative_to(ROOT)}")


if __name__ == "__main__":
    main()

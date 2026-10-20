#!/usr/bin/env python3
"""ゲームのフォント画像から、1文字ずつの「送り幅」（次の文字までの px）の表を作る（IMP-30）。
ゲームを起動せずに、画面の文字の幅を計算するため（GameTest gui_layout と docs/gui_preview/ が使う）。

読むもの（Minecraft 26.3、Loom のキャッシュから）：
- クライアントの jar の assets/minecraft/font/include/default.json と textures/font/*.png（ASCII・アクセント・ラテン以外の欧文）
- assets/minecraft/font/include/space.json（空白）
- ゲームの素材（assets/indexes・objects）の font/include/unifont.json と font/unifont.zip（日本語など。.hex のビット絵）

幅の決め方（バニラと同じ）：
- 絵の文字：マスの中で一番右の色のある列＋1 を「幅」、送り幅＝floor(0.5 + 幅 × height/マスの高さ) + 1
- unifont：ビット絵の左右の端から 送り幅＝(右−左)/2 + 1。size_overrides の範囲（漢字・かな・全角）は (right−left+1)/2 + 1＝9
  （どちらもゲームの実測 105 本と一致することを確かめた。GameTest font_widths_match_game）
同じ文字は先に書いてあるものが勝つ（space → default の絵 → unifont）。

書き出す先：src/gametest/resources/font_widths.json（[開始コードポイント, 個数, 送り幅] の並び）
使い方:  python tools/gen_font_widths.py
"""
import io
import json
import re
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/gametest/resources/font_widths.json"
LOOM = Path.home() / ".gradle/caches/fabric-loom/assets"


def client_jar():
    jars = sorted((ROOT / ".gradle/loom-cache").rglob("minecraft-clientOnly*.jar"))
    if not jars:
        raise SystemExit("クライアントの jar が見つからない（先に ./gradlew.bat compileClientJava）")
    return zipfile.ZipFile(jars[-1])


def asset(name):
    idx = sorted((LOOM / "indexes").glob("26.3*.json"))[-1]
    h = json.loads(idx.read_text(encoding="utf-8"))["objects"][name]["hash"]
    return (LOOM / "objects" / h[:2] / h).read_bytes()


def bitmap(jar, prov, adv):
    img = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/" + prov["file"].split(":")[1]))).convert("RGBA")
    rows = prov["chars"]
    cw, ch = img.width // len(rows[0]), img.height // len(rows)
    scale = prov.get("height", 8) / ch
    px = img.load()
    for r, row in enumerate(rows):
        for c, chr_ in enumerate(row):
            cp = ord(chr_)
            if cp == 0 or cp in adv:
                continue
            w = 0
            for x in range(cw - 1, -1, -1):
                if any(px[c * cw + x, r * ch + y][3] != 0 for y in range(ch)):
                    w = x + 1
                    break
            adv[cp] = int(0.5 + w * scale) + 1


def unihex(prov, adv):
    over = [(ord(o["from"]), ord(o["to"]), o["left"], o["right"]) for o in prov.get("size_overrides", [])]
    z = zipfile.ZipFile(io.BytesIO(asset("minecraft/" + prov["hex_file"].replace("minecraft:", "").replace("font/", "font/", 1))))
    name = [n for n in z.namelist() if n.endswith(".hex")][0]
    for line in z.read(name).decode("ascii").splitlines():
        m = re.match(r"([0-9A-Fa-f]+):([0-9A-Fa-f]+)", line)
        if not m:
            continue
        cp = int(m.group(1), 16)
        if cp in adv:
            continue
        bits = m.group(2)
        width = len(bits) * 4 // 16
        o = next((o for o in over if o[0] <= cp <= o[1]), None)
        if o:
            left, right = o[2], o[3]
        else:
            cols = set()
            for y in range(16):
                row = int(bits[y * width // 4:(y + 1) * width // 4], 16)
                for x in range(width):
                    if row >> (width - 1 - x) & 1:
                        cols.add(x)
            if not cols:
                adv[cp] = 1   # 空の絵（右−左+1 = 0 として 0/2+1）
                continue
            left, right = min(cols), max(cols)
            adv[cp] = (right - left) / 2 + 1   # 絵から測った幅（ゲームの実測と合わせた：℃ は 4）
            continue
        adv[cp] = (right - left + 1) / 2 + 1   # size_overrides の範囲（漢字・かな・全角は 9）


def main():
    jar = client_jar()
    adv = {}
    for k, v in json.loads(jar.read("assets/minecraft/font/include/space.json"))["providers"][0]["advances"].items():
        adv[ord(k)] = v
    for prov in json.loads(jar.read("assets/minecraft/font/include/default.json"))["providers"]:
        if prov["type"] == "bitmap":
            bitmap(jar, prov, adv)
    for prov in json.loads(asset("minecraft/font/include/unifont.json"))["providers"]:
        if prov["type"] == "unihex" and not prov.get("filter", {}).get("jp"):
            unihex(prov, adv)
    runs = []
    for cp in sorted(adv):
        a = adv[cp]
        a = int(a) if float(a).is_integer() else a
        if runs and runs[-1][0] + runs[-1][1] == cp and runs[-1][2] == a:
            runs[-1][1] += 1
        else:
            runs.append([cp, 1, a])
    OUT.write_text(json.dumps({"_note": "自動生成：python tools/gen_font_widths.py（IMP-30）。[開始コードポイント, 個数, 送り幅 px]",
                               "runs": runs}, separators=(",", ":")) + "\n", encoding="utf-8")
    print(f"{len(adv)} glyphs, {len(runs)} runs -> {OUT.relative_to(ROOT)} ({OUT.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()

"""ホーム画面のアプリのアイコン（S-026）。図のアイコンと同じ 16×16 のドット絵を、くっきり拡大して PNG にする。

案を3つ作り、並べた見本 docs/mockups/S026_icons.png を作る（ユーザーが選ぶ）。
選んだ案（決まるまでは仮に CHOICE）で docs/app_icon_192.png・app_icon_512.png・app_icon_maskable_512.png・apple-touch-icon.png（180）を作る。
  A 中心図の輪：真ん中に1つの丸、まわりに2つの輪（つながりの図）
  B 石器からチップへ：左下に打製石器、右上にチップ、間を線でつなぐ
  C スマホ：図のスマホのアイコン（[B-10]）
使い方：python tools/gen_app_icons.py [A|B|C]
"""
import json
import sys
from pathlib import Path
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_icons import from_glyph, PALETTE  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
BG = (250, 247, 240)          # デザイン C の地の色（--bg）
ACCENT = (47, 111, 143)       # --accent
CHOICE = (sys.argv[1] if len(sys.argv) > 1 else "A").upper()


def ring16():
    """A：16×16 の中心図。真ん中の丸（青緑）と、まわりの2つの輪の点（茶）"""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); p = im.load()
    tech, prod = PALETTE["木"], PALETTE["ガラス"]
    import math
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r <= 2.2:
                p[x, y] = prod[0] + (255,) if r > 1.4 else prod[1] + (255,)
    for rr, n, c in ((4.6, 8, tech[0]), (7.0, 14, tech[1])):
        for i in range(n):
            a = -math.pi / 2 + i * 2 * math.pi / n + (0.2 if rr > 5 else 0)
            x, y = round(7.5 + rr * math.cos(a)), round(7.5 + rr * math.sin(a))
            if 0 <= x < 16 and 0 <= y < 16:
                p[x, y] = c + (255,)
    return im


def stone_chip16():
    """B：左下に打製石器（灰）、右上にチップ（紫）、間を点線（茶）"""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); p = im.load()
    st, si, wd = PALETTE["石"], PALETTE["シリコン"], PALETTE["木"]
    stone = ["..##..", ".####.", "######", "#####.", ".###..", "..#..."]
    for j, row in enumerate(stone):
        for i, c in enumerate(row):
            if c == "#":
                p[1 + i, 9 + j] = (st[0] if i in (0, 5) or j in (0, 5) else st[1]) + (255,)
    for i in range(7):
        for j in range(7):
            p[8 + i, 1 + j] = (si[0] if i in (0, 6) or j in (0, 6) else si[1]) + (255,)
    for i in (1, 3, 5):
        p[7 + i, 0] = p[7 + i, 8] = PALETTE["金"][1] + (255,)
    for k in range(0, 6, 2):
        p[5 + k // 2 + k // 2, 8 - k // 2 - k // 2] = wd[0] + (255,)
    return im


def smartphone16():
    g = json.loads((DOCS / "icon_glyphs.json").read_text(encoding="utf-8"))["B-10"]
    return from_glyph(g["glyph"], g["mat"])


ICONS = {"A": ("中心図の輪", ring16), "B": ("石器からチップへ", stone_chip16), "C": ("スマホ", smartphone16)}


def make(icon16, size, pad):
    """地の色の四角に、16×16 を整数倍で真ん中に（pad＝まわりの余白の割合。maskable は 0.2 以上）"""
    out = Image.new("RGBA", (size, size), BG + (255,))
    k = int(size * (1 - 2 * pad) // 16)
    big = icon16.resize((16 * k, 16 * k), Image.NEAREST)
    o = (size - 16 * k) // 2
    out.paste(big, (o, o), big)
    return out


def main():
    # 見本：3案を 192 と、ホーム画面の丸い切り抜き（maskable）で並べる
    W = 3 * 260 + 20
    sheet = Image.new("RGB", (W, 330), (255, 255, 255)); d = ImageDraw.Draw(sheet)
    for i, (key, (name, f)) in enumerate(ICONS.items()):
        x = 20 + i * 260
        a = make(f(), 192, 0.08)
        sheet.paste(a, (x, 20), a)
        m = make(f(), 512, 0.2).resize((96, 96), Image.LANCZOS)
        mask = Image.new("L", (96, 96), 0); ImageDraw.Draw(mask).ellipse((0, 0, 95, 95), fill=255)
        sheet.paste(m, (x + 200 - 96 - 10, 230), mask)
        d.text((x, 225), f"{key}", fill=(0, 0, 0))
    sheet.save(DOCS / "mockups" / "S026_icons.png")
    name, f = ICONS[CHOICE]
    ic = f()
    make(ic, 192, 0.08).save(DOCS / "app_icon_192.png")
    make(ic, 512, 0.08).save(DOCS / "app_icon_512.png")
    make(ic, 512, 0.2).save(DOCS / "app_icon_maskable_512.png")
    make(ic, 180, 0.1).convert("RGB").save(DOCS / "apple-touch-icon.png")
    print(f"icons: {CHOICE}（{name}）, sample docs/mockups/S026_icons.png")


if __name__ == "__main__":
    main()

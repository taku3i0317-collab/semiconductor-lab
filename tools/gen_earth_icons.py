"""ホーム画面のアイコンの見本：自作のドット絵の地球（S-030）。docs/mockups/S030_earth_icons.png
① 地球だけ ② 地球の上にノードの点と線の網 ③ 地球のまわりを小さなノードの輪
色は図のアイコンの 22 色（tools/gen_icons.py の PALETTE）。22 色に緑が無いので、陸は「土」の色の版と、緑を1色足した版の両方を作る。
実在の会社・団体のロゴに似せない（経線・緯線の格子や、輪の上の衛星など、よくある形は使わない）。
使い方：python tools/gen_earth_icons.py
"""
import math
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_icons import PALETTE  # noqa: E402
from gen_app_icons import make, BG  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
GREEN = ((46, 112, 64), (98, 168, 98))   # 足す案の緑（濃い・薄い）

# 陸の形（16×16、'#'＝陸）。どこかの地図の写しではなく、それらしい形
LAND = ["................",
        "................",
        ".....##.........",
        "....####...##...",
        "...#####..####..",
        "...####...####..",
        "....##.....###..",
        ".....#......#...",
        "......##........",
        "......###...##..",
        ".......##..###..",
        ".......#....#...",
        "................",
        "................",
        "................",
        "................"]


def globe(cx, cy, r, land_col, im=None):
    im = im or Image.new("RGBA", (16, 16), (0, 0, 0, 0)); p = im.load()
    sea = PALETTE["布"]
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d > r:
                continue
            edge = d > r - 1
            lx, ly = int(round(7.5 + (x - cx) * 7 / r)), int(round(7.5 + (y - cy) * 7 / r))
            is_land = 0 <= lx < 16 and 0 <= ly < 16 and LAND[ly][lx] == "#"
            c = (land_col[0] if edge else land_col[1]) if is_land else (sea[0] if edge else sea[1])
            p[x, y] = c + (255,)
    return im


def design(k, land):
    if k == 1:
        return globe(7.5, 7.5, 7.2, land)
    if k == 2:   # 地球の上に、光るノードの点と線
        im = globe(7.5, 7.5, 7.2, land); p = im.load(); y1 = PALETTE["電気"]
        pts = [(4, 4), (9, 3), (12, 7), (8, 8), (4, 10), (10, 12)]
        dr = ImageDraw.Draw(im)
        for a, b in [(0, 1), (1, 2), (1, 3), (3, 4), (3, 5), (2, 5)]:
            dr.line([pts[a], pts[b]], fill=y1[0] + (255,), width=1)
        for x, y in pts:
            p[x, y] = y1[1] + (255,)
        return im
    im = globe(7.5, 7.5, 4.6, land); p = im.load()   # 3：まわりを回るノードの輪
    for i in range(10):
        a = -math.pi / 2 + i * 2 * math.pi / 10
        x, y = round(7.5 + 7 * math.cos(a)), round(7.5 + 7 * math.sin(a))
        p[x, y] = (PALETTE["電気"][0] if i % 2 else PALETTE["木"][1]) + (255,)
    return im


def main():
    rows = [("土の色の陸（22 色のまま）", PALETTE["土"]), ("緑を1色足した陸", GREEN)]
    W, H = 3 * 430 + 20, len(rows) * 250 + 40
    sheet = Image.new("RGB", (W, H), (255, 255, 255)); dr = ImageDraw.Draw(sheet)
    try:
        F = ImageFont.truetype("C:/Windows/Fonts/meiryo.ttc", 16)
    except Exception:
        F = None   # 日本語の書体が無い環境
    for j, (title, land) in enumerate(rows):
        y0 = 30 + j * 250
        dr.text((10, y0 - 24), title, fill=(0, 0, 0), font=F)
        for k in (1, 2, 3):
            x0 = 10 + (k - 1) * 430
            ic = design(k, land)
            a = make(ic, 192, 0.08); sheet.paste(a, (x0, y0), a)
            m = make(ic, 512, 0.2).resize((96, 96), Image.LANCZOS)
            mask = Image.new("L", (96, 96), 0); ImageDraw.Draw(mask).ellipse((0, 0, 95, 95), fill=255)
            sheet.paste(m, (x0 + 200, y0), mask)
            for sz, ox in ((64, 200), (48, 280)):
                s = make(ic, 192, 0.08).resize((sz, sz), Image.LANCZOS); sheet.paste(s, (x0 + ox, y0 + 110), s)
            dr.text((x0, y0 + 196), f"{'①②③'[k - 1]}  192 / maskable / 64 / 48", fill=(80, 80, 80), font=F)
    out = ROOT / "docs" / "mockups" / "S030_earth_icons.png"
    sheet.save(out); print(f"wrote {out}")


if __name__ == "__main__":
    main()

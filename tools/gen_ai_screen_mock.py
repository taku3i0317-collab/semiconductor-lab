"""AI チップの書く画面の見本（絵の案、Q32、S-019 Q30-4 案A）：docs/gui_preview/ai_chip_mock.png
色は B の icons_e と同じ 22 色（11 素材×濃い・薄い）。案A：左に 8×8 の方眼（7 を描いた所）、右に答え（ニキシー管の色の数字）・0〜9 の確からしさの棒・
16 個の隠れニューロンのランプ・下に「消す」「教える」の2つのボタン（アイコン、文字なし）
"""
import pathlib
from PIL import Image, ImageDraw

P = {"樹脂": ((38, 38, 42), (112, 112, 120)), "シリコン": ((58, 58, 108), (122, 122, 182)), "電気": ((196, 150, 0), (255, 226, 84)),
     "ガラス": ((56, 118, 138), (172, 216, 226)), "銅": ((138, 68, 38), (212, 132, 82)), "石": ((88, 86, 82), (160, 156, 148))}
SEVEN = [".######.", "......#.", ".....#..", ".....#..", "....#...", "....#...", "...#....", "...#...."]
W, H = 360, 230
img = Image.new("RGB", (W, H), P["樹脂"][0])
d = ImageDraw.Draw(img)
d.rectangle([6, 6, W - 7, H - 7], fill=P["シリコン"][0])
# 8×8 の方眼
gx, gy, c = 18, 18, 22
for y in range(8):
    for x in range(8):
        on = SEVEN[y][x] == "#"
        d.rectangle([gx + x * c, gy + y * c, gx + x * c + c - 2, gy + y * c + c - 2], fill=P["ガラス"][1] if on else P["樹脂"][1])
# 答え（ニキシー管の色、7本の線で 7）
ax, ay = 220, 20
seg = [(ax, ay, 40, 6), (ax + 34, ay, 6, 34), (ax + 34, ay + 34, 6, 34)]
for x0, y0, w, h in [(ax, ay, 40, 6), (ax + 34, ay, 6, 34), (ax + 34, ay + 34, 6, 34), (ax, ay + 62, 40, 6), (ax, ay + 34, 6, 34), (ax, ay, 6, 34), (ax, ay + 31, 40, 6)]:
    d.rectangle([x0, y0, x0 + w, y0 + h], fill=(70, 40, 20))
for x0, y0, w, h in seg:
    d.rectangle([x0, y0, x0 + w, y0 + h], fill=(255, 138, 48))
# 0〜9 の確からしさの棒
bx, by = 280, 18
conf = [2, 1, 4, 6, 1, 2, 1, 92, 3, 5]
for i, v in enumerate(conf):
    d.rectangle([bx, by + i * 9, bx + 60, by + i * 9 + 6], fill=P["樹脂"][1])
    d.rectangle([bx, by + i * 9, bx + int(60 * v / 100), by + i * 9 + 6], fill=P["電気"][1] if i == 7 else P["電気"][0])
# 16 個の隠れニューロンのランプ
lx, ly = 220, 120
lit = [3, 12, 0, 9, 15, 2, 7, 1, 11, 0, 5, 14, 2, 8, 0, 10]
for i, v in enumerate(lit):
    x0, y0 = lx + (i % 8) * 16, ly + (i // 8) * 16
    col = tuple(int(P["樹脂"][1][k] + (P["電気"][1][k] - P["樹脂"][1][k]) * v / 15) for k in range(3))
    d.ellipse([x0, y0, x0 + 11, y0 + 11], fill=col)
# ボタン（消す＝石、教える＝銅）
for k, m in enumerate(["石", "銅"]):
    x0, y0 = 220 + k * 40, 170
    d.rectangle([x0, y0, x0 + 30, y0 + 30], fill=P[m][0])
    d.rectangle([x0 + 4, y0 + 4, x0 + 26, y0 + 26], fill=P[m][1])
out = pathlib.Path("docs/gui_preview/ai_chip_mock.png")
img.resize((W * 2, H * 2), Image.NEAREST).save(out)
print(out)
